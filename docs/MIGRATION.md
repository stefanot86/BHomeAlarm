# Piano di Migrazione e Sviluppo - BHomeAlarm

## Overview

Questo documento descrive il piano di migrazione dall'app esistente (MyHomeAlarm) alla nuova versione (BHomeAlarm) con architettura MVC.

---

## Fasi di Sviluppo

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         PIANO DI SVILUPPO                                │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  FASE 1: Fondamenta                                                      │
│  ├── Setup progetto Android Studio                                       │
│  ├── Configurazione Gradle e dipendenze                                  │
│  ├── Struttura package MVC                                               │
│  └── Database Room base                                                  │
│                                                                          │
│  FASE 2: Model Layer                                                     │
│  ├── Entities Room complete                                              │
│  ├── DAOs implementati                                                   │
│  ├── Repository pattern                                                  │
│  └── Migrazione dati legacy                                              │
│                                                                          │
│  FASE 3: Core Services                                                   │
│  ├── SmsService (invio/ricezione)                                        │
│  ├── SmsReceiver (BroadcastReceiver)                                     │
│  ├── Parser SMS                                                          │
│  └── NotificationService                                                 │
│                                                                          │
│  FASE 4: Controller Layer                                                │
│  ├── NavigationController                                                │
│  ├── ConfigController (state machine)                                    │
│  ├── AlarmController                                                     │
│  └── Altri controller                                                    │
│                                                                          │
│  FASE 5: View Layer - Navigazione                                        │
│  ├── MainActivity (Single Activity)                                      │
│  ├── Navigation component setup                                          │
│  ├── SplashFragment                                                      │
│  └── Transizioni base                                                    │
│                                                                          │
│  FASE 6: View Layer - Onboarding                                         │
│  ├── DisclaimerFragment                                                  │
│  ├── SetupPhoneFragment                                                  │
│  ├── PasswordSetFragment                                                 │
│  └── PasswordInputFragment                                               │
│                                                                          │
│  FASE 7: View Layer - Core                                               │
│  ├── MainFragment                                                        │
│  ├── SettingsFragment                                                    │
│  └── ConfigProgressFragment                                              │
│                                                                          │
│  FASE 8: View Layer - Gestione                                           │
│  ├── UsersFragment + UserPermissionsFragment                             │
│  ├── ScenariosFragment                                                   │
│  └── ZonesFragment                                                       │
│                                                                          │
│  FASE 9: Polish & Testing                                                │
│  ├── Material Design 3 refinements                                       │
│  ├── Animazioni e transizioni                                            │
│  ├── Testing completo                                                    │
│  └── Bug fixing                                                          │
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## FASE 1: Fondamenta

### 1.1 Setup Progetto

**Nuovo progetto Android Studio**:
- Nome: BHomeAlarm
- Package: `it.bhomealarm`
- Min SDK: 23 (Android 6.0)
- Target SDK: 34 (Android 14)
- Compile SDK: 35 (Android 15)
- Linguaggio: Java
- Build configuration: Kotlin DSL (opzionale) o Groovy

### 1.2 Struttura Directory

```
BHomeAlarm/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bhomealarm/
│   │   │   │   ├── model/
│   │   │   │   │   ├── entity/
│   │   │   │   │   ├── dao/
│   │   │   │   │   ├── database/
│   │   │   │   │   ├── repository/
│   │   │   │   │   └── dto/
│   │   │   │   ├── view/
│   │   │   │   │   ├── activity/
│   │   │   │   │   ├── fragment/
│   │   │   │   │   ├── adapter/
│   │   │   │   │   ├── dialog/
│   │   │   │   │   └── widget/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── util/
│   │   │   │   ├── callback/
│   │   │   │   └── BHomeAlarmApp.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   ├── values-it/
│   │   │   │   ├── values-night/
│   │   │   │   ├── drawable/
│   │   │   │   ├── mipmap/
│   │   │   │   ├── navigation/
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle
├── docs/
│   ├── ARCHITECTURE.md
│   ├── FEATURES.md
│   ├── SCREENS.md
│   ├── DATA_MODEL.md
│   ├── SMS_PROTOCOL.md
│   └── MIGRATION.md
├── build.gradle
├── settings.gradle
├── gradle.properties
├── CLAUDE.md
└── README.md
```

### 1.3 Dipendenze Gradle

```groovy
dependencies {
    // AndroidX Core
    implementation 'androidx.core:core:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.activity:activity:1.8.2'
    implementation 'androidx.fragment:fragment:1.6.2'

    // Material Design 3
    implementation 'com.google.android.material:material:1.11.0'

    // ConstraintLayout
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    annotationProcessor 'androidx.room:room-compiler:2.6.1'

    // Lifecycle
    implementation 'androidx.lifecycle:lifecycle-runtime:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'

    // Navigation Component
    implementation 'androidx.navigation:navigation-fragment:2.7.6'
    implementation 'androidx.navigation:navigation-ui:2.7.6'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.room:room-testing:2.6.1'
}
```

---

## FASE 2: Model Layer

### 2.1 Task

| Task | File | Priorità |
|------|------|----------|
| Creare AlarmConfig entity | `model/entity/AlarmConfig.java` | Alta |
| Creare Zone entity | `model/entity/Zone.java` | Alta |
| Creare Scenario entity | `model/entity/Scenario.java` | Alta |
| Creare User entity | `model/entity/User.java` | Alta |
| Creare SmsLog entity | `model/entity/SmsLog.java` | Media |
| Creare AppSettings entity | `model/entity/AppSettings.java` | Alta |
| Creare tutti i DAO | `model/dao/*.java` | Alta |
| Creare AppDatabase | `model/database/AppDatabase.java` | Alta |
| Creare AlarmRepository | `model/repository/AlarmRepository.java` | Alta |
| Creare SettingsRepository | `model/repository/SettingsRepository.java` | Media |
| Creare Converters | `model/database/Converters.java` | Alta |

### 2.2 Migrazione Dati Legacy

**File legacy**: `DATA_file` (formato binario custom)

**Mapping**:

| Legacy | Nuovo |
|--------|-------|
| `n_Telefono` | `AlarmConfig.phoneNumber` |
| `bVet_Flag[0-7]` | `AlarmConfig.mainFlags` |
| `sZONE_List[0-7]` | `Zone` (8 records) |
| `sSCEN_List[0-15]` | `Scenario` (16 records) |
| `sRUB_List[0-15]` | `User` (16 records) |
| `bVet_Flag[8-23]` | `User.permissions` |
| `sPsw_App` | `AppSettings.password_hash` |
| `bVet_Options[0]` | `AppSettings.disclaimer_accepted` |

---

## FASE 3: Core Services

### 3.1 Task

| Task | File | Priorità |
|------|------|----------|
| Creare SmsService | `service/SmsService.java` | Critica |
| Creare SmsReceiver | `service/SmsReceiver.java` | Critica |
| Creare SmsParser | `util/SmsParser.java` | Critica |
| Creare PhoneNumberUtils | `util/PhoneNumberUtils.java` | Alta |
| Creare NotificationService | `service/NotificationService.java` | Alta |
| Creare Constants | `util/Constants.java` | Alta |
| Creare PermissionHelper | `util/PermissionHelper.java` | Alta |

### 3.2 SmsService - Caratteristiche

```java
public class SmsService {

    // Singleton
    private static SmsService instance;

    // Dual-SIM support
    public int getSimCount();
    public List<SimInfo> getAvailableSims();
    public void setPreferredSim(int simSlot);

    // Send
    public void sendSms(String phoneNumber, String message,
                       OnSmsSentListener listener);

    // Status
    public boolean isSmsSending();
    public void cancelPending();
}
```

### 3.3 SmsReceiver - Implementazione

```java
public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // 1. Estrai SMS dai PDU
        // 2. Verifica mittente
        // 3. Notifica controller
        // 4. Mostra notifica
    }
}
```

---

## FASE 4: Controller Layer

### 4.1 Task

| Task | File | Priorità |
|------|------|----------|
| Creare callback interfaces | `callback/*.java` | Alta |
| Creare NavigationController | `controller/NavigationController.java` | Alta |
| Creare ConfigController | `controller/ConfigController.java` | Critica |
| Creare AlarmController | `controller/AlarmController.java` | Critica |
| Creare UserController | `controller/UserController.java` | Media |
| Creare ScenarioController | `controller/ScenarioController.java` | Media |

### 4.2 ConfigController - State Machine

```java
public class ConfigController {

    public enum State {
        IDLE,
        CONF1_TX, CONF1_RX,
        CONF2_TX, CONF2_RX,
        CONF3_TX, CONF3_RX,
        CONF4_TX, CONF4_RX,
        CONF5_TX, CONF5_RX,
        COMPLETE,
        ERROR
    }

    private State currentState = State.IDLE;
    private OnConfigProgressListener listener;

    public void startConfiguration() {
        currentState = State.CONF1_TX;
        sendNextConfig();
    }

    public void onSmsReceived(String response) {
        // Parsing e avanzamento stato
    }

    public void onTimeout() {
        // Gestione timeout
    }

    public void abort() {
        currentState = State.IDLE;
    }
}
```

---

## FASE 5-8: View Layer

### 5.1 Navigation Graph

```xml
<!-- res/navigation/nav_graph.xml -->
<navigation
    android:id="@+id/nav_graph"
    app:startDestination="@id/splashFragment">

    <fragment
        android:id="@+id/splashFragment"
        android:name="com.bhomealarm.view.fragment.SplashFragment">
        <action
            android:id="@+id/action_splash_to_disclaimer"
            app:destination="@id/disclaimerFragment" />
        <action
            android:id="@+id/action_splash_to_main"
            app:destination="@id/mainFragment" />
        <action
            android:id="@+id/action_splash_to_password"
            app:destination="@id/passwordInputFragment" />
    </fragment>

    <!-- Altri fragment... -->

</navigation>
```

### 5.2 Fragment Implementation Pattern

```java
public class ExampleFragment extends Fragment {

    private FragmentExampleBinding binding;
    private ExampleController controller;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
        binding = FragmentExampleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get controller
        controller = ((MainActivity) requireActivity()).getExampleController();

        // Setup UI
        setupViews();
        setupListeners();
        observeData();
    }

    private void setupViews() {
        // Initialize UI components
    }

    private void setupListeners() {
        binding.button.setOnClickListener(v -> {
            controller.doSomething();
        });
    }

    private void observeData() {
        // Observe LiveData if needed
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

---

## Mapping Classi Legacy → Nuove

### Activities

| Legacy | Nuovo |
|--------|-------|
| `Splash_activity` | `SplashFragment` |
| `MainActivity` | `MainActivity` (refactored) |

### Fragments

| Legacy | Nuovo | Note |
|--------|-------|------|
| `Frag_main` | `MainFragment` | Redesign UI |
| `Frag_n_tel` | `SetupPhoneFragment` | Material TextInput |
| `Frag_Comm_01` | `ConfigProgressFragment` | Nuova state machine |
| `Frag_Config_02` | `UsersFragment` | RecyclerView |
| `Frag_Config_03` | `UserPermissionsFragment` | Checkboxes MD3 |
| `Frag_Settings_01` | `SettingsFragment` | Preference-style |
| `Frag_SCE_01` | `ScenariosFragment` | RecyclerView |
| `Frag_ZONE_02` | `ZonesFragment` | Checkboxes |
| `Frag_Disclaimer` | `DisclaimerFragment` | Material Dialog style |
| `Frag_Psw_Input` | `PasswordInputFragment` | Material TextInput |
| `Frag_Psw_Set` | `PasswordSetFragment` | Material TextInput |

### Services

| Legacy | Nuovo | Note |
|--------|-------|------|
| `Service_Timer` | Rimosso | Usa Handler/Coroutines |
| `SMS_Incoming` | `SmsReceiver` | Refactored |
| - | `NotificationService` | Nuovo |

### Utility Classes

| Legacy | Nuovo | Note |
|--------|-------|------|
| `SMS_Class` | `SmsService` + `SmsParser` | Separazione responsabilità |
| `Storage_class` | `AlarmRepository` | Room instead of file |
| `Tool` | `Constants` | Rinominato |
| - | `PhoneNumberUtils` | Nuovo |
| - | `PermissionHelper` | Nuovo |

---

## Checklist Pre-Release

### Funzionalità Core

- [ ] Setup numero telefonico
- [ ] Invio SMS
- [ ] Ricezione SMS
- [ ] Configurazione CONF1
- [ ] Configurazione CONF2
- [ ] Configurazione CONF3
- [ ] Configurazione CONF4
- [ ] Configurazione CONF5
- [ ] Arm con scenario
- [ ] Arm custom
- [ ] Disarm
- [ ] Check status
- [ ] Gestione utenti
- [ ] Gestione permessi
- [ ] Gestione scenari
- [ ] Gestione zone

### UI/UX

- [ ] Tema chiaro
- [ ] Tema scuro
- [ ] Dynamic colors (Android 12+)
- [ ] Animazioni transizioni
- [ ] Feedback azioni
- [ ] Progress indicators
- [ ] Error states
- [ ] Empty states

### Compatibilità

- [ ] Test Android 6.0 (API 23)
- [ ] Test Android 8.0 (API 26)
- [ ] Test Android 10 (API 29)
- [ ] Test Android 12 (API 31)
- [ ] Test Android 13 (API 33)
- [ ] Test Android 14 (API 34)
- [ ] Test dispositivo Dual-SIM
- [ ] Test tablet

### Qualità

- [ ] No crash
- [ ] No memory leaks
- [ ] Performance OK
- [ ] Battery OK
- [ ] Accessibilità base
- [ ] Localizzazione IT
- [ ] Localizzazione EN

### Sicurezza

- [ ] Permessi minimi
- [ ] No dati sensibili in log
- [ ] Validazione input
- [ ] Password hash (se usata)

---

## Rischi e Mitigazioni

| Rischio | Probabilità | Impatto | Mitigazione |
|---------|-------------|---------|-------------|
| SMS non ricevuti su Android 13+ | Media | Alto | Test approfonditi, permessi corretti |
| Incompatibilità Dual-SIM | Media | Medio | Fallback a SIM default |
| Room migration issues | Bassa | Alto | Test migration, backup dati |
| UI non responsive | Bassa | Medio | Background threading |
| Timeout comunicazione | Alta | Medio | Retry logic, feedback utente |

---

## Note Tecniche

### Permessi Android

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### Permessi Runtime (Android 6+)

```java
String[] permissions = {
    Manifest.permission.SEND_SMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.READ_SMS,
    Manifest.permission.READ_PHONE_STATE
};

// Android 13+ richiede anche
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    permissions = Arrays.copyOf(permissions, permissions.length + 1);
    permissions[permissions.length - 1] = Manifest.permission.POST_NOTIFICATIONS;
}
```

### ProGuard Rules

```proguard
# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep model classes
-keep class com.bhomealarm.model.entity.** { *; }
```
