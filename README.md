<p align="center">
  <img src="docs/brand/icon.png" alt="Rada icon: a giraffe whose eye is a camera lens" width="132">
</p>

<h1 align="center">🦒 Rada</h1>

<p align="center">
  <b>kaa rada na ntsa</b> · <i>stay aware on the road</i>
</p>

<p align="center">
  <img alt="Android" src="https://img.shields.io/badge/Android-Jetpack%20Compose-CCFF00?style=for-the-badge&logo=android&logoColor=12161B&labelColor=12161B">
  <img alt="Kotlin Multiplatform" src="https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=12161B">
  <img alt="Firebase" src="https://img.shields.io/badge/Firebase-Auth%20%C2%B7%20Firestore%20%C2%B7%20Functions-FFCA28?style=for-the-badge&logo=firebase&logoColor=white&labelColor=12161B">
  <img alt="M-Pesa" src="https://img.shields.io/badge/M--Pesa-Daraja%20STK%20Push-4CAF50?style=for-the-badge&labelColor=12161B">
  <img alt="Private" src="https://img.shields.io/badge/repo-private-9A2C50?style=for-the-badge&labelColor=12161B">
</p>

<p align="center">
  A Kenyan driving-awareness app that speaks up <b>before</b> the camera, the checkpoint or the
  alcoblow, so you never find out the hard way. 🇰🇪
</p>

---

## 🌍 The idea

*Rada* is Swahili for **aware**. It isn't a radar detector; it's a community of drivers looking out
for each other. Rada combines a seeded map of known **speed cameras** with live reports of **police
checkpoints** and **alcohol checks** from other drivers, and warns you with a chime and a spoken
message in time to react.

The mascot is the **Twiga** (giraffe): the tallest lookout on the savanna, with a camera lens for an
eye. 👁️

## 📸 Screenshots

<p align="center"><b>🚀 First run: onboarding and sign-in</b></p>

<table align="center">
  <tr>
    <td align="center"><img src="docs/screenshots/01-splash.png" width="190"><br><sub>Splash</sub></td>
    <td align="center"><img src="docs/screenshots/09-onboarding-cameras.png" width="190"><br><sub>Cameras, before you reach them</sub></td>
    <td align="center"><img src="docs/screenshots/10-onboarding-privacy.png" width="190"><br><sub>What's shared, and what isn't</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/11-onboarding-alerts.png" width="190"><br><sub>Alerts, even when Rada is closed</sub></td>
    <td align="center"><img src="docs/screenshots/08-create-account.png" width="190"><br><sub>Create account</sub></td>
    <td align="center"><img src="docs/screenshots/07-sign-in.png" width="190"><br><sub>Sign in</sub></td>
  </tr>
</table>

<p align="center"><b>🛣️ Every day: home, drive, report, help</b></p>

<table align="center">
  <tr>
    <td align="center"><img src="docs/screenshots/02-home.png" width="190"><br><sub>Home (dark)</sub></td>
    <td align="center"><img src="docs/screenshots/03-home-light.png" width="190"><br><sub>Home (light)</sub></td>
    <td align="center"><img src="docs/screenshots/04-drive.png" width="190"><br><sub>Drive mode</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/screenshots/05-report.png" width="190"><br><sub>One-tap reporting</sub></td>
    <td align="center"><img src="docs/screenshots/06-help.png" width="190"><br><sub>Roadside help</sub></td>
    <td align="center"><img src="docs/screenshots/12-settings.png" width="190"><br><sub>Settings</sub></td>
  </tr>
  <tr>
    <td align="center" colspan="3"><img src="docs/screenshots/13-paywall.png" width="190"><br><sub>Go Pro: pay by card through Google Play, or M-Pesa</sub></td>
  </tr>
</table>

<p align="center"><sub>Android, captured on an emulator. Alerts and chart data in these shots are test reports.</sub></p>

## ✨ What it does

| | Feature | The details |
|---|---|---|
| 🚗 | **Drive mode** | A live map that stays locked on your car (a burgundy car marker), your speed, and how many cameras are near. The zoom adapts to your speed and widens to keep an approaching camera on screen. |
| 🔔 | **Loud, timely alerts** | A two-tone chime, then a spoken warning such as *"Speed camera ahead, Thika Road. 300 metres."*, then a three-beep final warning about 120 m out. Music ducks instead of stopping, and alerts keep working with the screen off. |
| ⏱️ | **Warning distance** | Choose how early you're warned: **Standard** (about 7 s ahead), **Early** (10 s) or **Earliest** (14 s), so highway drivers get more time. |
| 👆 | **One-tap reporting** | Camera, checkpoint or alcohol check, one tap each. The tile floods with colour, you feel a haptic tick, and an **Undo** chip stays for five seconds so a mis-tap while driving is recoverable. |
| 📊 | **Home dashboard** | Estimated fines avoided, a weekly line chart of reports by type (amber camera, blue checkpoint, red alcohol), and the newest alerts near you. |
| 🆘 | **Roadside help** | Emergency numbers, tow companies and mechanics, one tap to call. |
| 🎛️ | **Settings** | Profile photo, name, alert switch, warning distance, a **Test alert sound** button, and a dark-mode switch. Everything saves as you change it; there is no Save button. |
| 🌗 | **Light and dark themes** | A lime-on-ink identity with burgundy accents, and an adaptive launcher icon (with a themed variant on Android 13+). |
| 👑 | **Rada Pro** | Try Drive **free for 7 days**, then unlock it with a Google Play subscription or a one-time M-Pesa payment. See [Pricing and payments](#-pricing-and-payments). |

## 🚦 How an alert happens

```mermaid
flowchart LR
    A["📍 Fused location<br/>lat, lng, speed, bearing"] --> B["🧠 SpeedAlertEngine"]
    B -->|"in range and heading toward"| C{"Type?"}
    C -->|"speed camera"| D["only if you are over the limit"]
    C -->|"checkpoint / alcoblow"| E["always"]
    D --> F["🔊 AlertSpeaker<br/>chime + voice"]
    E --> F
    F --> G["🟡 Drive banner<br/>live distance"]
    F --> H["📲 Notification<br/>if app is in background"]
    G --> I["🔁 Final beep at ~120 m"]
```

- **Distance scales with speed.** The warning fires about 7 to 14 seconds ahead (your setting),
  clamped between 60 m and 350 to 700 m.
- **Direction matters.** Only cameras you're heading toward (within ±20°) count, so the camera on
  the opposite carriageway stays quiet.
- **No repeat spam.** Each camera goes `NOT_ALERTED → ALERTED → PASSED`, with hysteresis so it
  doesn't re-fire as you drive by.
- **Fast lookups.** `CameraGridIndex` buckets cameras into ~1 km cells so each location update
  only checks the cameras around you.
- **Survives the background.** A foreground location service keeps tracking while you use Maps or
  music, and a watchdog restarts location if GPS updates stall.

> [!NOTE]
> Camera locations come from public sources and reports. Some are **approximate** and are flagged as
> such in the app. Spots still missing usable coordinates are listed in
> [`docs/cameras-todo.md`](docs/cameras-todo.md).

## 💳 Pricing and payments

| Plan | Price | What you get |
|---|---|---|
| 🎁 **Free trial** | 7 days, once per account | Full Drive access |
| 📅 **Monthly** | KES 200 for 30 days | Keep Drive after the trial, plus alcohol-check alerts |
| 🏆 **Annual** | KES 1,800 for 365 days | Everything in Monthly, about KES 150 a month |

Two ways to pay, side by side on the paywall, so everyone can use what they already have:

| | 🟢 **Google Play subscription** | 💚 **M-Pesa, pay once** |
|---|---|---|
| Best for | Hands-off, automatic renewal | Paying straight from your M-Pesa balance |
| Pay with | Visa or Mastercard (including **M-PESA GlobalPay** virtual cards), or M-Pesa through Google Play | M-Pesa STK push |
| Renewal | **Automatic**, cancel any time in Google Play | Manual: pay again when you like |
| Built on | Google Play Billing | Safaricom Daraja |

Both routes write the same entitlement, `proUntil` on the account: the later of the two expiries
wins, so switching or combining never loses paid time.

```mermaid
sequenceDiagram
    participant App as 📱 Rada app
    participant F as ☁️ Cloud Functions
    participant P as 🟢 Google Play
    participant D as 💚 Daraja / M-Pesa
    participant DB as 🔥 Firestore
    rect rgb(40, 60, 40)
    Note over App,DB: Google Play subscription
    App->>P: subscribe (card, GlobalPay or M-Pesa)
    App->>F: verifyPlaySubscription(purchase token)
    F->>P: check the purchase
    P-->>F: state and expiry
    F->>DB: playProUntil, then proUntil
    P->>F: renewal / cancel / refund notifications
    end
    rect rgb(30, 55, 45)
    Note over App,DB: M-Pesa, pay once
    App->>F: initiateMpesaPayment(phone, plan)
    F->>D: STK push (price set server-side)
    D->>F: callback (secret token)
    F->>DB: verify amount, mpesaProUntil, then proUntil
    end
    DB-->>App: proUntil changes, Pro unlocks
```

The app never sees Daraja credentials. Prices for M-Pesa live on the server, callbacks are
authenticated with a secret token and processed exactly once, a Play purchase is bound to the
account that bought it, and only Cloud Functions can write entitlements. Details are in
[`functions/README.md`](functions/README.md).

## 🔐 Accounts and security

- 👤 **Username and password**, or **Google** or **Apple**. Sign in once and stay signed in.
- ⏳ **The free trial can't be reset.** The start date is stored on the account in Firestore with a
  server timestamp, so clearing app data or reinstalling doesn't restart it.
- 🛡️ **Firestore rules** ([`firestore.rules`](firestore.rules)): reports need a signed-in user and
  can only be deleted by their author; payments are readable only by their owner; no client can
  grant itself Pro or edit its trial.
- ✅ **Tested against the emulators:** `npm run test:e2e` in `functions/` replays Safaricom-style
  callbacks and checks the rules on a real Firestore emulator.

<details>
<summary><b>How usernames work, and console notes</b></summary>

<br>

Firebase signs people in with an email address, so a username such as `wanjiru` is turned into an
internal address, `wanjiru@users.rada.invalid`, which never receives mail. That makes usernames
unique for free. The trade-off is that **there is no password recovery**. To add it later, collect a
real email address at sign-up and store it on the account.

1. **Authentication → Sign-in method:** enable **Email/Password**. That's all username and password
   needs: no fingerprint, SMS region or Play Integrity.
2. **Google (optional):** enable the Google provider, add your SHA-1 and SHA-256
   (`./gradlew :androidApp:signingReport`) under Project settings → your Android app, then download
   `google-services.json` again.
3. **Apple (optional):** needs an Apple Developer account: create a Services ID and a Sign in with
   Apple key, enter them in Firebase, and add Firebase's callback URL to the Services ID.
4. **Firestore rules:** publish [`firestore.rules`](firestore.rules).

</details>

## 🧱 Tech stack, and why

| | Layer | Choice | Why |
|---|---|---|---|
| 🧠 | Shared logic | **Kotlin Multiplatform** (`:shared`) | The alert engine has to be right, so it is written once and reused by both apps instead of being re-implemented per platform. |
| 🎨 | Android UI | **Jetpack Compose** + Material 3 | Declarative UI makes the animated screens (tab bar, report tiles, charts, the moving map) straightforward. |
| 🍎 | iOS UI | **SwiftUI** | Native feel, haptics and animation on iPhone. |
| 🏛️ | Architecture | **MVI**: one immutable state per screen, intents in, effects out | Predictable and easy to reason about. Each feature is a `Route` (owns the ViewModel) plus a `Content` screen that is a pure function of state. |
| 🧭 | Navigation | **Navigation Compose**, one tab bar above the `NavHost` | The active-tab circle slides between destinations instead of being redrawn per screen. |
| 💾 | Local storage | **Jetpack DataStore** | Small key-value data (theme, name, alert settings, report history), written as it changes. |
| 🔥 | Backend | **Firebase Auth + Firestore + Cloud Functions** | Realtime crowd reports with an offline write queue, and payment logic on a server with no servers to run. |
| 📍 | Location | **Play Services Fused Location** + a foreground service | One high-accuracy request while Drive is tracking, kept alive in the background. |
| 🗺️ | Maps | **Maps Compose** (Google Maps SDK) | Fastest route to a good map with a custom animated car marker. |
| 🔊 | Audio | **AudioTrack chimes + Android TextToSpeech** | No audio assets to ship; navigation-guidance audio focus ducks your music. |
| 💚 | Payments | **Google Play Billing** + **M-Pesa Daraja STK push** | Cards, GlobalPay and automatic renewal through Play; M-Pesa for people who want to pay directly. |
| ⚡ | Async | **Kotlin Coroutines / Flow** | Location, Firestore and DataStore are all streams. |
| 🛠️ | Build | Gradle Kotlin DSL · AGP 8.13 · Kotlin 2.3 · JDK 17 · minSdk 26 · targetSdk 36 · Node 24 for functions | |

## 🗂️ Project layout

```
shared/          Kotlin Multiplatform: SpeedAlertEngine, CameraGridIndex, models,
                 LocationProvider / AlertNotifier (expect/actual for Android and iOS)
androidApp/      Jetpack Compose app
  └ features/    splash · onboarding · login · signup · dashboard · drive · report · help ·
                 settings · paywall   (each: State + ViewModel + Route + Content)
  └ data/        Firestore repositories, DataStore preferences, auth and trial sync
  └ tracking/    foreground service, alert notifications, AlertSpeaker (chime + voice)
  └ theme/       palette + light/dark colour schemes
functions/       Cloud Functions (Node 24): M-Pesa STK push, callback, status query, tests
iosApp/          SwiftUI app (see iosApp/SETUP.md)
docs/            screenshots, brand assets, camera data notes
firestore.rules  Security rules for reports, payments and accounts
```

## 🗺️ Where things stand

Legend: ✅ working · 🧪 tested, needs a live check · 🚧 in progress · 🔜 next

| | Area | Status |
|---|---|---|
| ✅ | Alert engine, grid index, live map, camera-following and adaptive zoom | Working; **audio alerts confirmed on a real drive** |
| ✅ | Crowd reports: submit, undo, live alerts, weekly chart | Working |
| ✅ | Username and password sign-in, persistent sessions | Working end to end |
| ✅ | Free trial tied to the account | Verified: wiping app data no longer restarts it |
| ✅ | Firestore security rules | Published and covered by emulator tests |
| 🧪 | M-Pesa payments | Verified against the Daraja sandbox and the emulator suite; **waiting on a production Till** for a live payment |
| 🧪 | Google Play subscriptions | App, verification and notification handling built and unit-tested; **needs the Play Console setup** (products, API access) before a real purchase |
| 🧪 | Google sign-in | Implemented; needs the SHA-1 registered, then a device check |
| 🚧 | Apple sign-in | Implemented; needs Apple Developer setup |
| 🚧 | iOS app | A SwiftUI prototype with mock data; no location, map or sign-in yet |
| 🔜 | Password recovery | Collect a real email at sign-up |
| 🔜 | Renewal reminders | A nudge a few days before Pro ends |
| 🔜 | Offline maps | Promised on the paywall, not built yet |
| 🔜 | Remaining camera coordinates | See [`docs/cameras-todo.md`](docs/cameras-todo.md) |

> [!IMPORTANT]
> Some numbers are still placeholders: Home's "estimated fines avoided" figure is mock data, and
> the tow and mechanic phone numbers are placeholders.

## 📜 License

Private project. No licence is granted. 🔒

<p align="center"><sub>Made in Nairobi 🇰🇪 · <b>kaa rada na ntsa</b></sub></p>
