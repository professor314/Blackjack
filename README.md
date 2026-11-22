# Blackjack

An Android app for learning blackjack basic strategy.

- **Strategy tab:** the basic strategy table (hard totals, soft totals, pairs). Change the rules and the table updates:
  dealer hits soft 17, double after split, late surrender, and double restrictions (any two, 9-11, 10-11).
- **Practice tab:** random hands, answer with Hit / Stand / Double / Split / Surrender. Each hand-vs-dealer combination
  is tracked. Unseen and often-missed combinations come up more often; ones you reliably get right fade out.
  Progress and rules are saved on the device.

The chart is multi-deck (4-8 decks) basic strategy.

## Layout

- `engine/`: pure Kotlin (JVM) module with the strategy rules, hand picker and stats. Unit tested.
- `app/`: Android app (Kotlin, Jetpack Compose, Material 3). minSdk 24.

## Build

Open the project in Android Studio and run the `app` configuration, or use Gradle 8.9+:

```
gradle :engine:test
gradle :app:assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
```

GitHub Actions (`.github/workflows/build.yml`) runs the tests and uploads the debug APK as a build artifact.
