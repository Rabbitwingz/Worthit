# Worth It ⏳

🌐 **[worthitapp.vercel.app](https://worthitapp.vercel.app/)**

**Turn money into time.** Worth It shows what a purchase really costs you in working days and hours. It also works out the cost per use, keeps you from buying on impulse with a short pause, and later learns which of your purchases were actually worth it.

> Spend less on things you don't care about. Spend confidently on things you do.

## Install

Every push to `main` builds a fresh APK with GitHub Actions. You don't need Android Studio.

1. Open [worthitapp.vercel.app](https://worthitapp.vercel.app/#download) (or the [latest release](../../releases/latest)) on your phone.
2. Download `WorthIt-1.0.x.apk` and open it. Allow "install unknown apps" if Android asks.
3. Later builds install over the previous one, so your data is kept.

## What's inside

- **Live calculator**: as you type a price, it rolls into working days.
- **Decide**: a dot grid of your working month, affordability, cost per use, "what else could this money do", how much you want it, and a 0–10 worth-it score that's a decision aid, not advice.
- **Wishlist with cooling-off**: a pause based on how many working days the item costs (a day, two days, a week or two weeks).
- **Check-ins** at 7, 30, 90 and 180 days, which drive your worth-it rate, regret rate, category patterns and price sweet spot.
- **Goals** with wavy progress and an estimated finish date. **Compare** up to 5 items. **Subscriptions** shown as a yearly cost in working time.
- **Material 3 Expressive**: spring motion, morphing `MaterialShapes`, buttons that squish when pressed, confetti and haptics.
- **Local-first**: everything stays on the phone. You can export your data as JSON or delete all of it.

## Tech

Kotlin · Jetpack Compose · Material 3 (expressive APIs) · kotlinx.serialization · single-activity with no extra navigation library.
All the money and time maths lives in [`WorthMath.kt`](app/src/main/java/app/worthit/data/WorthMath.kt) and is unit tested in CI.
