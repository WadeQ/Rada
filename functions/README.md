# Rada payments backend (Google Play Billing)

Cloud Functions (Node 24, region `europe-west1`) that verify a Google Play subscription purchase
and grant Pro by writing `users/{uid}.proUntil`.

> M-Pesa STK push (Safaricom Daraja) was removed after the production Till application was
> rejected. Google Play — Visa, Mastercard and M-PESA GlobalPay virtual cards, all through Play
> Billing — is the only payment route for now.

| Function | Trigger | What it does |
|---|---|---|
| `verifyPlaySubscription` | callable, signed-in users | Checks a purchase token with the Google Play Developer API, binds it to the buying account, writes `playProUntil`/`proUntil`, acknowledges the purchase |
| `playNotifications` | Pub/Sub (`play-rtdn`) | Google's real-time renewal/cancellation/refund notifications; re-verifies and updates entitlement |

Plan prices and product IDs live in `entitlement.js` and the app's `PlayProducts` object — they
must match the subscriptions you create in Play Console exactly (`rada_pro_monthly`,
`rada_pro_annual`).

## Setup (Google Play Console)

1. **Subscriptions** (Monetize → Products → Subscriptions): create `rada_pro_monthly` (KES 200,
   every month) and `rada_pro_annual` (KES 1,800, every year), each with one base plan set to
   auto-renew.
2. **API access** (Setup → API access): link the Google Cloud project `rada-app-78472`, then
   invite the service account `216725826063-compute@developer.gserviceaccount.com` and grant it
   "View financial data" and "Manage orders and subscriptions".
3. **Real-time notifications** (Monetize → Monetization setup): set the topic to
   `projects/rada-app-78472/topics/play-rtdn`, subscription notifications only, and send a test.
4. **Testing:** upload a build to the internal testing track and add yourself as a licence
   tester so test purchases are free and renew quickly.

## Deploy

```
cd functions && npm install && npm test
firebase deploy --only functions,firestore:rules
```

## Tests

- `npm test`: unit tests — Play subscription state mapping, account-hash binding, and the
  entitlement logic against a fake database (grants, renewals, expiry, one token per account).
- `npm run test:e2e`: runs `firestore.rules` against the Firebase emulator — checks that no
  client can write `proUntil`, edit its own trial, or read `playPurchases`, and that reports are
  validated and owner-deletable only. Needs **JDK 21+** on the path.
