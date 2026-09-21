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
MPESA_CALLBACK_BASE=https://mpesacallback-<hash>-ew.a.run.app   # the deployed mpesaCallback URL
```

## Deploy

```
cd functions && npm install && npm test
firebase deploy --only functions,firestore:rules
```

First deploy: leave `MPESA_CALLBACK_BASE` empty, deploy, copy the `mpesaCallback` URL from the
output into `.env`, then deploy again.
