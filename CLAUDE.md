# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Android app (Java) to control a **Bticino 3500/3500N** home alarm panel **over SMS**. Single Gradle module (`:app`), package `it.bhomealarm`. UI strings, comments, and docs are in Italian.

- compileSdk/targetSdk **35**, minSdk **23**, Java **17**
- `viewBinding` and `buildConfig` are enabled (see `app/build.gradle`)
- Dependencies: AndroidX, Material 3, Room, Lifecycle/LiveData, Navigation Component. No DI framework, no Kotlin.

## Build & Run

```powershell
.\gradlew.bat assembleDebug      # build debug APK
.\gradlew.bat installDebug       # build + install on connected device/emulator
.\gradlew.bat clean
.\gradlew.bat lint               # Android Lint
```

There are **no unit or instrumentation tests** in the repo (`app/src/test` and `app/src/androidTest` do not exist), so there is no test command. If you add tests, also wire up the `test`/`androidTest` source sets and JUnit/Robolectric deps — they are not currently configured.

The app cannot be meaningfully exercised on an emulator: it requires SMS send/receive against a real alarm panel. `docs/SMS_PROTOCOL.md` sketches a `TestSmsService` mock-response approach for offline testing, but **it is not implemented** in the codebase.

## Architecture — read this before trusting `docs/`

⚠️ **The `docs/` folder is aspirational and partly stale.** `docs/ARCHITECTURE.md` describes an MVC design with `controller/AlarmController`, `SmsController`, `ServiceLocator`, a `model/dto/` package, `TimerService`, etc. **None of those classes exist.** The real app is **MVVM**:

- **View** (`view/`): single `MainActivity` (Navigation Component host + `BottomNavigationView`), `fragment/`, `adapter/`. Navigation is defined in `res/navigation/nav_graph.xml`. Also `view/activity/AlarmActionActivity` — a translucent dialog Activity launched by the home-screen widget.
- **ViewModel** (`controller/viewmodel/`): `AndroidViewModel` subclasses holding `MutableLiveData`; one per screen (Home, Configuration, Users, Scenarios, Zones, Settings, Log).
- **Controller** (`controller/AlarmController.java`): singleton that is the **single source** for sending alarm commands (`armWithScenario`/`armWithCustomZones`/`disarm`/`checkStatus`, returning the `messageId`) and reading phone/status from prefs. It sits above `SmsService`. `HomeViewModel`, `AlarmActionActivity`, and the widget all delegate here — do **not** re-implement "read phone → format command → `SmsService.sendCommand`" anywhere else (the user explicitly wants no duplication and layer separation respected).
- **Model** (`model/`): Room `entity/`, `dao/`, `database/AppDatabase`, and a single `repository/AlarmRepository`.
- **widget/** (`widget/AlarmWidgetProvider.java`): `AppWidgetProvider` (`RemoteViews`, no extra Gradle deps). Buttons fire `PendingIntent`s into `AlarmActionActivity` (modes in `Constants.WIDGET_MODE_*`); status display is refreshed by `SmsReceiver` via `AlarmWidgetProvider.updateAllWidgets(context)`.
- **service/**, **util/**, **callback/** as named.

Treat the **code and `util/Constants.java` as the source of truth** for the SMS protocol; the prose in `docs/SMS_PROTOCOL.md` disagrees with `Constants` in places (e.g. the `CUST:` arm-custom format and the FLAGS bit layout).

### SMS round-trip (the core of the app)

This is the central flow and spans several files:

1. **Send** — `SmsService` (singleton, `getInstance`). `sendSms(...)` / `sendCommand(...)` writes an outgoing `SmsLog` (status `PENDING`), then calls `SmsManager.sendTextMessage` with two `PendingIntent`s for sent/delivered callbacks. Returns a UUID `messageId` used for tracking. Handles **Dual-SIM** via `SubscriptionManager` and a selected slot persisted in SharedPreferences.
2. **Receive** — `SmsReceiver` (`BroadcastReceiver`, manifest-registered, priority 999). On `SMS_RECEIVED` it filters by sender via `PhoneNumberUtils.matches`, saves to DB, parses status into SharedPreferences (so status updates even when the app is backgrounded), notifies the listener, and `abortBroadcast()` to suppress the system SMS notification. It also handles the app's own `SMS_SENT`/`SMS_DELIVERED` actions and forwards them to `SmsService`.
3. **Parse** — `SmsParser` (static utility): `identifyResponse()` returns a type string (`"CONF1"`, `"OK"`, `"STATUS"`, ...); `parseConf1`, `parseScenarios`, `parseUsers`, `parseResponse` turn bodies into entities/data objects.
4. **React** — a ViewModel processes the response and persists via `AlarmRepository`.

**Gotcha — single static listener:** `SmsReceiver` holds **one** `OnSmsResultListener` set via the static `SmsReceiver.setListener(...)`. A ViewModel registers itself in (e.g.) `startConfiguration()` and **must** clear it (`SmsReceiver.setListener(null)`) in `onCleared()`. Two screens cannot listen at once; forgetting to unregister leaks the ViewModel and steals callbacks from other screens.

### Configuration state machine

`ConfigurationViewModel` drives the initial sync as a state machine over `Constants.CONFIG_STATE_*` (IDLE → CONF1 → ... → CONF5 → COMPLETE / ERROR). It sends `CONF1?`..`CONF5?` sequentially, advancing only when the matching response arrives, with a 60s timeout per step (`Handler.postDelayed`). CONF1 = firmware + 8 zones, CONF2/3 = 16 scenarios, CONF4/5 = 16 users + joker. On completion it sets `PREF_CONFIGURED`.

### Persistence — two stores, intentionally

- **Room** (`AppDatabase`, db name `bhomealarm_db`, version 1) for entities and the SMS log. Uses `fallbackToDestructiveMigration()` — **bumping the schema wipes the DB**; add a real `Migration` before changing entities in anything resembling production.
- **SharedPreferences** (`Constants.PREF_NAME = "bhomealarm_prefs"`) for the alarm phone number, selected SIM slot, last known status + check time, disclaimer-accepted, and configured flags. `SmsReceiver` relies on SharedPreferences specifically because it runs without an Activity.

`AlarmRepository` is a singleton: **reads return `LiveData`**, **writes run on a fixed `ExecutorService` (pool size 4)** — never block the main thread on a DAO call. Get it with `AlarmRepository.getInstance(application)`.

### App init & permissions

`BHomeAlarmApp` (the `Application`) initializes the DB/repository and creates two notification channels (`CHANNEL_ID_ALARM` high-importance, `CHANNEL_ID_SMS` default). `MainActivity` requests runtime permissions on startup: `SEND_SMS`, `RECEIVE_SMS`, `READ_SMS`, `READ_PHONE_STATE`, and (API 33+) `POST_NOTIFICATIONS`.

## Conventions

- Existing classes carry **extensive Italian Javadoc**; match that style and density when adding/modifying code (see Issue #9 commits — documenting the codebase is an active goal).
- Constants live in `util/Constants.java` — add SMS commands, prefixes, separators, permission bits, timeouts, pref keys, and intent actions there rather than inlining literals.
- User permissions are a 4-bit bitmask: `PERM_RX1`/`PERM_RX2`/`PERM_VERIFY`/`PERM_CMD_ON_OFF` (`Constants`).
