# Architettura BHomeAlarm - MVC Pattern

## Panoramica

L'applicazione segue il pattern **Model-View-Controller (MVC)** adattato per Android, con separazione netta delle responsabilità.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              APPLICATION                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         VIEW LAYER                                   │   │
│  │  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐        │   │
│  │  │ Activity  │  │ Fragments │  │ Adapters  │  │  Dialogs  │        │   │
│  │  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘  └─────┬─────┘        │   │
│  │        │              │              │              │               │   │
│  │        └──────────────┴──────────────┴──────────────┘               │   │
│  │                              │                                       │   │
│  │                     Interfaces/Callbacks                             │   │
│  │                              │                                       │   │
│  └──────────────────────────────┼───────────────────────────────────────┘   │
│                                 │                                           │
│                                 ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      CONTROLLER LAYER                                │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐      │   │
│  │  │ AlarmController │  │ ConfigController│  │  SmsController  │      │   │
│  │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘      │   │
│  │           │                    │                    │                │   │
│  │  ┌────────┴────────┐  ┌────────┴────────┐  ┌────────┴────────┐      │   │
│  │  │ UserController  │  │ScenarioController│ │NavigationController│   │   │
│  │  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘      │   │
│  │           │                    │                    │                │   │
│  │           └────────────────────┴────────────────────┘                │   │
│  │                              │                                       │   │
│  └──────────────────────────────┼───────────────────────────────────────┘   │
│                                 │                                           │
│                                 ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                        MODEL LAYER                                   │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                  │   │
│  │  │  Entities   │  │ Repository  │  │  Services   │                  │   │
│  │  │  (Room DB)  │  │  (DAO)      │  │ (SMS/Timer) │                  │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘                  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Struttura Package

```
it.bhomealarm/
├── BHomeAlarmApp.java              # Application class
├── MainActivity.java               # Single Activity (Navigation Host)
│
├── model/                          # MODEL LAYER
│   ├── entity/                     # Room Entities
│   │   ├── AlarmConfig.java        # Configurazione allarme
│   │   ├── Zone.java               # Zona (8 zone)
│   │   ├── Scenario.java           # Scenario (16 scenari)
│   │   ├── User.java               # Utente rubrica (16 utenti)
│   │   ├── SmsLog.java             # Log SMS inviati/ricevuti
│   │   └── AppSettings.java        # Impostazioni app
│   │
│   ├── dao/                        # Room DAOs
│   │   ├── AlarmConfigDao.java
│   │   ├── ZoneDao.java
│   │   ├── ScenarioDao.java
│   │   ├── UserDao.java
│   │   ├── SmsLogDao.java
│   │   └── AppSettingsDao.java
│   │
│   ├── database/                   # Room Database
│   │   └── AppDatabase.java
│   │
│   ├── repository/                 # Repository Pattern
│   │   ├── AlarmRepository.java
│   │   └── SettingsRepository.java
│   │
│   └── dto/                        # Data Transfer Objects
│       ├── SmsCommand.java         # Comando SMS da inviare
│       ├── SmsResponse.java        # Risposta SMS parsata
│       └── ConfigurationData.java  # Dati configurazione CONF1-5
│
├── view/                           # VIEW LAYER
│   ├── activity/
│   │   └── MainActivity.java
│   │
│   ├── fragment/
│   │   ├── SplashFragment.java
│   │   ├── DisclaimerFragment.java
│   │   ├── SetupPhoneFragment.java
│   │   ├── MainFragment.java
│   │   ├── SettingsFragment.java
│   │   ├── ConfigProgressFragment.java
│   │   ├── UsersFragment.java
│   │   ├── UserPermissionsFragment.java
│   │   ├── ScenariosFragment.java
│   │   └── ZonesFragment.java
│   │
│   ├── adapter/
│   │   ├── MainMenuAdapter.java
│   │   ├── SettingsAdapter.java
│   │   ├── UsersAdapter.java
│   │   ├── ScenariosAdapter.java
│   │   └── ZonesAdapter.java
│   │
│   ├── dialog/
│   │   ├── SimSelectionDialog.java
│   │   ├── ConfirmationDialog.java
│   │   └── ErrorDialog.java
│   │
│   └── widget/                     # Custom Views
│       └── StatusIndicator.java
│
├── controller/                     # CONTROLLER LAYER
│   ├── AlarmController.java        # Controllo arm/disarm/status
│   ├── ConfigController.java       # Gestione configurazione CONF1-5
│   ├── SmsController.java          # Orchestrazione SMS
│   ├── UserController.java         # Gestione utenti
│   ├── ScenarioController.java     # Gestione scenari
│   └── NavigationController.java   # Navigazione tra schermate
│
├── service/                        # Android Services
│   ├── SmsService.java             # Invio SMS con Dual-SIM
│   ├── SmsReceiver.java            # BroadcastReceiver SMS
│   ├── TimerService.java           # Timer periodico
│   └── NotificationService.java    # Gestione notifiche push
│
├── util/                           # Utilities
│   ├── Constants.java              # Costanti app
│   ├── SmsParser.java              # Parser risposte SMS
│   ├── PhoneNumberUtils.java       # Normalizzazione numeri
│   ├── PermissionHelper.java       # Gestione permessi runtime
│   └── PreferencesManager.java     # SharedPreferences wrapper
│
└── callback/                       # Interfaces
    ├── OnSmsResultListener.java
    ├── OnConfigProgressListener.java
    ├── OnNavigationListener.java
    └── OnUserActionListener.java
```

---

## Descrizione Componenti

### MODEL LAYER

#### Entities (Room)

| Entity | Descrizione | Campi Principali |
|--------|-------------|------------------|
| `AlarmConfig` | Configurazione centrale allarme | version, phoneNumber, mainFlags |
| `Zone` | Singola zona allarme | id (1-8), name, enabled |
| `Scenario` | Scenario predefinito | id (1-16), name, zoneMask |
| `User` | Utente in rubrica | id (1-16), name, permissions (bitmask) |
| `SmsLog` | Log comunicazioni | timestamp, direction, content, status |
| `AppSettings` | Impostazioni app | disclaimerAccepted, selectedSim, theme |

#### Repository

```java
public class AlarmRepository {
    // Singleton con accesso al database

    // Config
    void saveConfig(AlarmConfig config);
    AlarmConfig getConfig();

    // Zones
    List<Zone> getAllZones();
    void updateZone(Zone zone);

    // Scenarios
    List<Scenario> getAllScenarios();
    void updateScenario(Scenario scenario);

    // Users
    List<User> getAllUsers();
    void updateUser(User user);

    // SMS Log
    void logSms(SmsLog log);
    List<SmsLog> getRecentLogs(int limit);
}
```

---

### VIEW LAYER

#### Activity

**MainActivity** - Single Activity Architecture
- Contiene `FragmentContainerView` per la navigazione
- Gestisce il back stack dei Fragment
- Implementa le interfacce callback per comunicazione View→Controller

#### Fragments

| Fragment | Scopo | UI Components |
|----------|-------|---------------|
| `SplashFragment` | Schermata avvio | Logo, versione, progress |
| `DisclaimerFragment` | Termini d'uso | ScrollView testo, bottoni Accept/Refuse |
| `SetupPhoneFragment` | Setup numero allarme | TextInputLayout, bottone rubrica |
| `MainFragment` | Menu principale | RecyclerView con 4 azioni |
| `SettingsFragment` | Impostazioni | RecyclerView menu dinamico |
| `ConfigProgressFragment` | Progresso config | ProgressBar, status text, log SMS |
| `UsersFragment` | Lista utenti | RecyclerView utenti |
| `UserPermissionsFragment` | Permessi utente | CheckBox per ogni permesso |
| `ScenariosFragment` | Lista scenari | RecyclerView scenari |
| `ZonesFragment` | Selezione zone | CheckBox per ogni zona |

---

### CONTROLLER LAYER

#### AlarmController

```java
public class AlarmController {
    // Comandi allarme
    void armSystem(Scenario scenario);
    void disarmSystem();
    void checkStatus();
    void sendCustomCommand(String command);

    // Callbacks
    void onSmsResult(SmsResponse response);
    void onTimeout();
    void onError(int errorCode);
}
```

#### ConfigController

```java
public class ConfigController {
    // Macchina a stati per CONF1-5
    private ConfigState currentState;

    void startConfiguration();
    void processResponse(SmsResponse response);
    void abort();

    // Stati: IDLE → CONF1_TX → CONF1_RX → CONF2_TX → ... → COMPLETE
    enum ConfigState {
        IDLE,
        CONF1_SENDING, CONF1_WAITING,
        CONF2_SENDING, CONF2_WAITING,
        CONF3_SENDING, CONF3_WAITING,
        CONF4_SENDING, CONF4_WAITING,
        CONF5_SENDING, CONF5_WAITING,
        COMPLETE, ERROR
    }
}
```

#### SmsController

```java
public class SmsController {
    // Gestione invio
    void sendSms(SmsCommand command);
    void selectSim(int simSlot);

    // Gestione ricezione
    void onSmsReceived(String sender, String body);
    boolean isFromAlarm(String sender);

    // Parsing
    SmsResponse parseResponse(String body);
}
```

---

### SERVICE LAYER

#### SmsService

```java
public class SmsService {
    // Invio con supporto Dual-SIM
    void send(String phoneNumber, String message, int simSlot);

    // Query SIM disponibili
    int getSimCount();
    List<SimInfo> getAvailableSims();
}
```

#### SmsReceiver (BroadcastReceiver)

```java
public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Intercetta SMS_RECEIVED
        // Filtra per numero allarme
        // Notifica SmsController
    }
}
```

#### NotificationService

```java
public class NotificationService {
    // Crea canale notifiche (Android 8+)
    void createNotificationChannel();

    // Mostra notifica risposta allarme
    void showAlarmResponse(String status);

    // Notifica errore comunicazione
    void showError(String message);
}
```

---

## Flusso Dati

### 1. Invio Comando (es. Arm)

```
User tap "Arm"
       │
       ▼
┌─────────────────┐
│  MainFragment   │ ──► onArmClicked()
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│AlarmController  │ ──► armSystem(scenario)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ SmsController   │ ──► sendSms(command)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   SmsService    │ ──► SmsManager.sendTextMessage()
└────────┬────────┘
         │
         ▼
    SMS inviato
```

### 2. Ricezione Risposta

```
    SMS ricevuto
         │
         ▼
┌─────────────────┐
│  SmsReceiver    │ ──► onReceive()
└────────┬────────┘
         │ isFromAlarm()?
         ▼
┌─────────────────┐
│ SmsController   │ ──► onSmsReceived()
└────────┬────────┘     parseResponse()
         │
         ▼
┌─────────────────┐
│AlarmController  │ ──► onSmsResult(response)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│AlarmRepository  │ ──► logSms(), updateConfig()
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│NotificationService│ ──► showAlarmResponse()
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ConfigProgress  │ ──► updateUI()
│    Fragment     │
└─────────────────┘
```

---

## Comunicazione tra Layer

### View → Controller

Tramite **interfacce callback** definite nel package `callback/`:

```java
public interface OnUserActionListener {
    void onArmRequested(int scenarioId);
    void onDisarmRequested();
    void onStatusRequested();
    void onSettingsRequested();
}
```

Il Fragment implementa i click listener e chiama i metodi dell'interfaccia.
MainActivity implementa l'interfaccia e delega ai Controller.

### Controller → View

Tramite **callback methods** o **LiveData-like pattern**:

```java
public interface OnConfigProgressListener {
    void onProgressUpdate(int step, int total, String message);
    void onConfigComplete(ConfigurationData data);
    void onConfigError(int errorCode, String message);
}
```

### Controller → Model

Chiamate dirette al Repository:

```java
// Nel Controller
alarmRepository.saveConfig(newConfig);
List<Zone> zones = alarmRepository.getAllZones();
```

### Model → Controller

Il Repository può usare callback per operazioni asincrone:

```java
public interface RepositoryCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}
```

---

## Gestione Stati

### Stati Applicazione

```java
public class AppState {
    // Stato navigazione
    int currentScreen;

    // Stato configurazione
    boolean isConfigured;
    boolean configInProgress;

    // Stato SMS
    boolean smsPending;
    boolean waitingResponse;
}
```

### Stati Configurazione (State Machine)

```
     ┌──────────┐
     │   IDLE   │
     └────┬─────┘
          │ startConfiguration()
          ▼
     ┌──────────┐
     │ CONF1_TX │─────────────┐
     └────┬─────┘             │
          │ sent              │ timeout/error
          ▼                   │
     ┌──────────┐             │
     │ CONF1_RX │─────────────┤
     └────┬─────┘             │
          │ received          │
          ▼                   │
     ┌──────────┐             │
     │ CONF2_TX │─────────────┤
     └────┬─────┘             │
          │                   │
         ...                  │
          │                   │
          ▼                   ▼
     ┌──────────┐       ┌──────────┐
     │ COMPLETE │       │  ERROR   │
     └──────────┘       └──────────┘
```

---

## Threading Model

| Operazione | Thread | Motivo |
|------------|--------|--------|
| UI updates | Main | Android requirement |
| Room queries | Background | I/O blocking |
| SMS send | Main | SmsManager requirement |
| SMS receive | Main | BroadcastReceiver |
| Parsing | Background | CPU intensive |
| File I/O | Background | I/O blocking |

Uso di `ExecutorService` per operazioni background:

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
Handler mainHandler = new Handler(Looper.getMainLooper());

executor.execute(() -> {
    // Background work
    Result result = repository.getData();

    mainHandler.post(() -> {
        // Update UI
        view.showResult(result);
    });
});
```

---

## Dependency Injection (Manual)

Per semplicità, usiamo Singleton pattern con lazy initialization:

```java
public class ServiceLocator {
    private static ServiceLocator instance;

    private AppDatabase database;
    private AlarmRepository alarmRepository;
    private SmsController smsController;
    // ...

    public static synchronized ServiceLocator getInstance(Context context) {
        if (instance == null) {
            instance = new ServiceLocator(context.getApplicationContext());
        }
        return instance;
    }

    public AlarmRepository getAlarmRepository() {
        if (alarmRepository == null) {
            alarmRepository = new AlarmRepository(getDatabase());
        }
        return alarmRepository;
    }
}
```
