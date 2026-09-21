# Rada payments backend (M-Pesa Daraja STK push)

Cloud Functions (Node 24, region `europe-west1`) that start an STK push, receive Safaricom's
callback and grant Pro by writing `users/{uid}.proUntil`. The app never sees Daraja credentials.

| Function | Trigger | What it does |
|---|---|---|
| `initiateMpesaPayment` | callable, signed-in users | Validates plan and phone, sends the STK push, records `payments/{checkoutRequestId}` as pending |
| `mpesaCallback` | HTTPS, called by Safaricom | Checks the secret token, verifies the amount, marks the payment final and extends `proUntil` once |
| `queryMpesaPayment` | callable | Returns the payment status; if the callback is late it asks Daraja (STK Query) |

Plans and prices live in `plans.js` (the client only sends a plan id).

## Secrets (never commit these)

Run these yourself in a terminal; each prompts for the value:

```
firebase functions:secrets:set MPESA_CONSUMER_KEY
firebase functions:secrets:set MPESA_CONSUMER_SECRET
firebase functions:secrets:set MPESA_PASSKEY          # Daraja sandbox passkey (Lipa Na M-Pesa Online)
firebase functions:secrets:set MPESA_CALLBACK_TOKEN   # any long random string, e.g. `openssl rand -hex 24`
```

Non-secret settings go in `functions/.env.rada-app-78472` (git-ignored):

```
MPESA_ENV=sandbox
MPESA_SHORTCODE=174379
MPESA_CALLBACK_BASE=https://europe-west1-rada-app-78472.cloudfunctions.net/mpesaCallback
```

## Deploy

```
cd functions && npm install && npm test
firebase deploy --only functions,firestore:rules
```

All three values are required by the deploy (an empty `MPESA_CALLBACK_BASE` is fine on the first
run). Deployed to `rada-app-78472` in `europe-west1`.

## Tests

- `npm test`: unit tests (phone parsing, Daraja helpers, payment logic against a fake database).
- `npm run test:e2e`: runs the real `mpesaCallback` function and your `firestore.rules` against the
  Firebase emulators. It posts the same callbacks Safaricom sends (success, duplicate, wrong amount,
  cancelled, bad token) and checks what lands in Firestore, then tests the security rules.
  Needs **JDK 21+** on the path. Uses dummy secrets from `functions/.secret.local` (git-ignored;
  create it with `MPESA_CONSUMER_KEY`, `MPESA_CONSUMER_SECRET`, `MPESA_PASSKEY` and
  `MPESA_CALLBACK_TOKEN=local-test-token`).

## Google Play subscriptions (cards, GlobalPay, M-Pesa through Google)

`verifyPlaySubscription` checks a purchase with the Google Play Developer API and grants Pro;
`playNotifications` (Pub/Sub topic `play-rtdn`) keeps it in sync on renewals, cancellations and
refunds. Both routes end in the same `users/{uid}.proUntil` (the later of M-Pesa and Play time).

One-time setup in Google Play Console:

1. **Subscriptions** (Monetize → Products → Subscriptions): create `rada_pro_monthly` (KES 200, every
   month) and `rada_pro_annual` (KES 1,800, every year), each with one base plan set to auto-renew.
2. **API access** (Setup → API access): link the Google Cloud project `rada-app-78472`, then invite
   the service account `216725826063-compute@developer.gserviceaccount.com` and grant it
   "View financial data" and "Manage orders and subscriptions".
3. **Real-time notifications** (Monetize → Monetization setup): set the topic to
   `projects/rada-app-78472/topics/play-rtdn`, subscription notifications only, and send a test.
4. **Testing:** upload a build to the internal testing track and add yourself as a licence tester so
   test purchases are free and renew quickly.
