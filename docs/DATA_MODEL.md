# Data Model - BHomeAlarm

## Overview

L'applicazione utilizza **Room Database** per la persistenza dei dati, con il pattern Repository per l'astrazione dell'accesso ai dati.

---

## Schema Database

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          BHomeAlarm Database                            │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐   │
│  │  alarm_config   │     │      zones      │     │   scenarios     │   │
│  ├─────────────────┤     ├─────────────────┤     ├─────────────────┤   │
│  │ id (PK)         │     │ id (PK)         │     │ id (PK)         │   │
│  │ phone_number    │     │ slot (1-8)      │     │ slot (1-16)     │   │
│  │ version         │     │ name            │     │ name            │   │
│  │ main_flags      │     │ enabled         │     │ zone_mask       │   │
│  │ last_status     │     │ updated_at      │     │ enabled         │   │
│  │ last_check      │     └─────────────────┘     │ updated_at      │   │
│  │ created_at      │                             └─────────────────┘   │
│  │ updated_at      │                                                   │
│  └─────────────────┘                                                   │
│                                                                         │
│  ┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐   │
│  │     users       │     │    sms_log      │     │  app_settings   │   │
│  ├─────────────────┤     ├─────────────────┤     ├─────────────────┤   │
│  │ id (PK)         │     │ id (PK)         │     │ id (PK)         │   │
│  │ slot (1-16)     │     │ direction       │     │ key             │   │
│  │ name            │     │ phone_number    │     │ value           │   │
│  │ permissions     │     │ content         │     │ updated_at      │   │
│  │ enabled         │     │ status          │     └─────────────────┘   │
│  │ updated_at      │     │ error_code      │                           │
│  └─────────────────┘     │ created_at      │                           │
│                          └─────────────────┘                           │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Entities

### 1. AlarmConfig

Configurazione principale del sistema allarme.

```java
@Entity(tableName = "alarm_config")
public class AlarmConfig {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "phone_number")
    @NonNull
    private String phoneNumber;        // Numero telefonico allarme

    @ColumnInfo(name = "version")
    private String version;            // Versione firmware (da CONF1)

    @ColumnInfo(name = "main_flags")
    private int mainFlags;             // Flag abilitazioni (bitmask)
                                       // Bit 0: RX1
                                       // Bit 1: RX2
                                       // Bit 2: VERIFY
                                       // Bit 3: CMD_ON_OFF
                                       // Bit 6: MAIN_OTHER

    @ColumnInfo(name = "last_status")
    private int lastStatus;            // Ultimo stato conosciuto
                                       // 0=Unknown, 1=Armed, 2=Disarmed

    @ColumnInfo(name = "last_status_text")
    private String lastStatusText;     // Descrizione ultimo stato

    @ColumnInfo(name = "last_check")
    private long lastCheck;            // Timestamp ultimo check (ms)

    @ColumnInfo(name = "config_complete")
    private boolean configComplete;    // true se CONF1-5 completati

    @ColumnInfo(name = "preferred_sim")
    private int preferredSim;          // SIM preferita (0=ask, 1=SIM1, 2=SIM2)

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructors, Getters, Setters...

    // Helper methods
    public boolean hasPermission(int permissionBit) {
        return (mainFlags & permissionBit) != 0;
    }

    public void setPermission(int permissionBit, boolean enabled) {
        if (enabled) {
            mainFlags |= permissionBit;
        } else {
            mainFlags &= ~permissionBit;
        }
    }
}
```

**Costanti Flag**:
```java
public static final int FLAG_RX1 = 1;         // 0b00000001
public static final int FLAG_RX2 = 2;         // 0b00000010
public static final int FLAG_VERIFY = 4;      // 0b00000100
public static final int FLAG_CMD_ON_OFF = 8;  // 0b00001000
public static final int FLAG_MAIN_OTHER = 64; // 0b01000000
```

---

### 2. Zone

Rappresenta una zona del sistema allarme (max 8 zone).

```java
@Entity(tableName = "zones")
public class Zone {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "slot")
    private int slot;                  // Posizione 1-8

    @ColumnInfo(name = "name")
    @NonNull
    private String name;               // Nome zona (es. "Ingresso")

    @ColumnInfo(name = "enabled")
    private boolean enabled;           // Zona abilitata nel sistema

    @ColumnInfo(name = "description")
    private String description;        // Descrizione opzionale

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructors, Getters, Setters...
}
```

**Note**:
- Lo `slot` è fisso (1-8) e corrisponde alla posizione nell'allarme
- Il `name` viene popolato da CONF1 (Z1=nome, Z2=nome, ...)
- Zone con nome "NE" (Not Enabled) non sono configurate

---

### 3. Scenario

Rappresenta uno scenario di attivazione (max 16 scenari).

```java
@Entity(tableName = "scenarios")
public class Scenario {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "slot")
    private int slot;                  // Posizione 1-16

    @ColumnInfo(name = "name")
    @NonNull
    private String name;               // Nome scenario (es. "Casa", "Notte")

    @ColumnInfo(name = "zone_mask")
    private int zoneMask;              // Bitmask zone incluse
                                       // Bit 0 = Zona 1, Bit 7 = Zona 8

    @ColumnInfo(name = "enabled")
    private boolean enabled;           // Scenario disponibile

    @ColumnInfo(name = "is_custom")
    private boolean isCustom;          // true se creato dall'utente

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructors, Getters, Setters...

    // Helper methods
    public boolean includesZone(int zoneSlot) {
        return (zoneMask & (1 << (zoneSlot - 1))) != 0;
    }

    public void setZoneIncluded(int zoneSlot, boolean included) {
        if (included) {
            zoneMask |= (1 << (zoneSlot - 1));
        } else {
            zoneMask &= ~(1 << (zoneSlot - 1));
        }
    }

    public List<Integer> getIncludedZones() {
        List<Integer> zones = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            if (includesZone(i)) {
                zones.add(i);
            }
        }
        return zones;
    }
}
```

**Zone Mask Example**:
```
zoneMask = 0b00001111 = 15
Zone 1: inclusa (bit 0 = 1)
Zone 2: inclusa (bit 1 = 1)
Zone 3: inclusa (bit 2 = 1)
Zone 4: inclusa (bit 3 = 1)
Zone 5-8: escluse (bit 4-7 = 0)
```

---

### 4. User

Rappresenta un utente nella rubrica dell'allarme (max 16 utenti).

```java
@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "slot")
    private int slot;                  // Posizione 1-16

    @ColumnInfo(name = "name")
    @NonNull
    private String name;               // Nome utente

    @ColumnInfo(name = "permissions")
    private int permissions;           // Bitmask permessi
                                       // Bit 0: RX1
                                       // Bit 1: RX2
                                       // Bit 2: VERIFY
                                       // Bit 3: CMD_ON_OFF

    @ColumnInfo(name = "is_joker")
    private boolean isJoker;           // true se è l'utente "Joker"

    @ColumnInfo(name = "enabled")
    private boolean enabled;           // Utente attivo

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructors, Getters, Setters...

    // Permission constants (same as AlarmConfig)
    public static final int PERM_RX1 = 1;
    public static final int PERM_RX2 = 2;
    public static final int PERM_VERIFY = 4;
    public static final int PERM_CMD_ON_OFF = 8;

    // Helper methods
    public boolean hasPermission(int permissionBit) {
        return (permissions & permissionBit) != 0;
    }

    public void setPermission(int permissionBit, boolean enabled) {
        if (enabled) {
            permissions |= permissionBit;
        } else {
            permissions &= ~permissionBit;
        }
    }

    public String getPermissionsString() {
        StringBuilder sb = new StringBuilder();
        if (hasPermission(PERM_RX1)) sb.append("RX1 ");
        if (hasPermission(PERM_RX2)) sb.append("RX2 ");
        if (hasPermission(PERM_VERIFY)) sb.append("VERIFY ");
        if (hasPermission(PERM_CMD_ON_OFF)) sb.append("CMD ");
        return sb.toString().trim();
    }
}
```

---

### 5. SmsLog

Log delle comunicazioni SMS (inviate e ricevute).

```java
@Entity(tableName = "sms_log")
public class SmsLog {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "direction")
    private int direction;             // 0=OUTGOING, 1=INCOMING

    @ColumnInfo(name = "phone_number")
    private String phoneNumber;        // Numero mittente/destinatario

    @ColumnInfo(name = "content")
    @NonNull
    private String content;            // Testo SMS

    @ColumnInfo(name = "status")
    private int status;                // Stato invio/ricezione
                                       // 0=PENDING, 1=SENT, 2=DELIVERED,
                                       // 3=RECEIVED, 4=FAILED

    @ColumnInfo(name = "error_code")
    private int errorCode;             // Codice errore (se status=FAILED)

    @ColumnInfo(name = "command_type")
    private String commandType;        // Tipo comando (CONF1, ARM, DISARM, etc.)

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Constructors, Getters, Setters...

    // Direction constants
    public static final int DIR_OUTGOING = 0;
    public static final int DIR_INCOMING = 1;

    // Status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_RECEIVED = 3;
    public static final int STATUS_FAILED = 4;
}
```

---

### 6. AppSettings

Impostazioni applicazione (key-value store).

```java
@Entity(tableName = "app_settings")
public class AppSettings {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "key")
    private String key;

    @ColumnInfo(name = "value")
    private String value;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Constructors, Getters, Setters...

    // Known keys
    public static final String KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted";
    public static final String KEY_FIRST_RUN = "first_run";
    public static final String KEY_THEME_MODE = "theme_mode";  // "light", "dark", "system"
    public static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String KEY_SOUND_ENABLED = "sound_enabled";
    public static final String KEY_VIBRATION_ENABLED = "vibration_enabled";
}
```

---

## DAOs (Data Access Objects)

### AlarmConfigDao

```java
@Dao
public interface AlarmConfigDao {

    @Query("SELECT * FROM alarm_config LIMIT 1")
    AlarmConfig getConfig();

    @Query("SELECT * FROM alarm_config LIMIT 1")
    LiveData<AlarmConfig> getConfigLive();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AlarmConfig config);

    @Update
    void update(AlarmConfig config);

    @Query("UPDATE alarm_config SET phone_number = :phoneNumber, updated_at = :timestamp")
    void updatePhoneNumber(String phoneNumber, long timestamp);

    @Query("UPDATE alarm_config SET last_status = :status, last_status_text = :text, " +
           "last_check = :timestamp, updated_at = :timestamp")
    void updateStatus(int status, String text, long timestamp);

    @Query("UPDATE alarm_config SET config_complete = :complete, updated_at = :timestamp")
    void updateConfigComplete(boolean complete, long timestamp);

    @Query("DELETE FROM alarm_config")
    void deleteAll();
}
```

---

### ZoneDao

```java
@Dao
public interface ZoneDao {

    @Query("SELECT * FROM zones ORDER BY slot ASC")
    List<Zone> getAll();

    @Query("SELECT * FROM zones ORDER BY slot ASC")
    LiveData<List<Zone>> getAllLive();

    @Query("SELECT * FROM zones WHERE slot = :slot")
    Zone getBySlot(int slot);

    @Query("SELECT * FROM zones WHERE enabled = 1 ORDER BY slot ASC")
    List<Zone> getEnabled();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Zone> zones);

    @Update
    void update(Zone zone);

    @Query("UPDATE zones SET name = :name, enabled = :enabled, updated_at = :timestamp " +
           "WHERE slot = :slot")
    void updateBySlot(int slot, String name, boolean enabled, long timestamp);

    @Query("DELETE FROM zones")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM zones WHERE enabled = 1")
    int getEnabledCount();
}
```

---

### ScenarioDao

```java
@Dao
public interface ScenarioDao {

    @Query("SELECT * FROM scenarios ORDER BY slot ASC")
    List<Scenario> getAll();

    @Query("SELECT * FROM scenarios ORDER BY slot ASC")
    LiveData<List<Scenario>> getAllLive();

    @Query("SELECT * FROM scenarios WHERE slot = :slot")
    Scenario getBySlot(int slot);

    @Query("SELECT * FROM scenarios WHERE enabled = 1 ORDER BY slot ASC")
    List<Scenario> getEnabled();

    @Query("SELECT * FROM scenarios WHERE is_custom = 1 LIMIT 1")
    Scenario getCustomScenario();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Scenario> scenarios);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Scenario scenario);

    @Update
    void update(Scenario scenario);

    @Query("DELETE FROM scenarios")
    void deleteAll();

    @Query("DELETE FROM scenarios WHERE is_custom = 1")
    void deleteCustomScenarios();
}
```

---

### UserDao

```java
@Dao
public interface UserDao {

    @Query("SELECT * FROM users ORDER BY slot ASC")
    List<User> getAll();

    @Query("SELECT * FROM users ORDER BY slot ASC")
    LiveData<List<User>> getAllLive();

    @Query("SELECT * FROM users WHERE slot = :slot")
    User getBySlot(int slot);

    @Query("SELECT * FROM users WHERE enabled = 1 ORDER BY slot ASC")
    List<User> getEnabled();

    @Query("SELECT * FROM users WHERE is_joker = 1 LIMIT 1")
    User getJoker();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    @Update
    void update(User user);

    @Query("UPDATE users SET permissions = :permissions, updated_at = :timestamp " +
           "WHERE slot = :slot")
    void updatePermissions(int slot, int permissions, long timestamp);

    @Query("UPDATE users SET permissions = :permissions, updated_at = :timestamp")
    void updateAllPermissions(int permissions, long timestamp);

    @Query("DELETE FROM users")
    void deleteAll();
}
```

---

### SmsLogDao

```java
@Dao
public interface SmsLogDao {

    @Query("SELECT * FROM sms_log ORDER BY created_at DESC")
    List<SmsLog> getAll();

    @Query("SELECT * FROM sms_log ORDER BY created_at DESC LIMIT :limit")
    List<SmsLog> getRecent(int limit);

    @Query("SELECT * FROM sms_log ORDER BY created_at DESC")
    LiveData<List<SmsLog>> getAllLive();

    @Query("SELECT * FROM sms_log WHERE direction = :direction ORDER BY created_at DESC")
    List<SmsLog> getByDirection(int direction);

    @Query("SELECT * FROM sms_log WHERE command_type = :commandType ORDER BY created_at DESC")
    List<SmsLog> getByCommandType(String commandType);

    @Insert
    long insert(SmsLog log);

    @Query("UPDATE sms_log SET status = :status, error_code = :errorCode WHERE id = :id")
    void updateStatus(long id, int status, int errorCode);

    @Query("DELETE FROM sms_log")
    void deleteAll();

    @Query("DELETE FROM sms_log WHERE created_at < :timestamp")
    void deleteOlderThan(long timestamp);

    @Query("SELECT COUNT(*) FROM sms_log")
    int getCount();
}
```

---

### AppSettingsDao

```java
@Dao
public interface AppSettingsDao {

    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    AppSettings get(String key);

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    String getValue(String key);

    @Query("SELECT * FROM app_settings")
    List<AppSettings> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void set(AppSettings setting);

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    void delete(String key);

    @Query("DELETE FROM app_settings")
    void deleteAll();

    // Convenience methods
    @Transaction
    default void setValue(String key, String value) {
        AppSettings setting = new AppSettings();
        setting.setKey(key);
        setting.setValue(value);
        setting.setUpdatedAt(System.currentTimeMillis());
        set(setting);
    }

    @Transaction
    default boolean getBoolean(String key, boolean defaultValue) {
        String value = getValue(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    @Transaction
    default void setBoolean(String key, boolean value) {
        setValue(key, String.valueOf(value));
    }
}
```

---

## Database Class

```java
@Database(
    entities = {
        AlarmConfig.class,
        Zone.class,
        Scenario.class,
        User.class,
        SmsLog.class,
        AppSettings.class
    },
    version = 1,
    exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // DAOs
    public abstract AlarmConfigDao alarmConfigDao();
    public abstract ZoneDao zoneDao();
    public abstract ScenarioDao scenarioDao();
    public abstract UserDao userDao();
    public abstract SmsLogDao smsLogDao();
    public abstract AppSettingsDao appSettingsDao();

    // Singleton
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "bhomealarm_database"
                    )
                    .addCallback(new RoomCallback())
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    // Callback per inizializzazione dati
    private static class RoomCallback extends RoomDatabase.Callback {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Inizializza dati di default in background
            Executors.newSingleThreadExecutor().execute(() -> {
                // Crea 8 zone vuote
                // Crea 16 scenari vuoti
                // Crea 16 utenti vuoti
            });
        }
    }
}
```

---

## Type Converters

```java
public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static List<Integer> fromIntList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = value.split(",");
        List<Integer> list = new ArrayList<>();
        for (String part : parts) {
            list.add(Integer.parseInt(part.trim()));
        }
        return list;
    }

    @TypeConverter
    public static String intListToString(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
```

---

## Repository Pattern

### AlarmRepository

```java
public class AlarmRepository {

    private final AlarmConfigDao configDao;
    private final ZoneDao zoneDao;
    private final ScenarioDao scenarioDao;
    private final UserDao userDao;
    private final SmsLogDao smsLogDao;
    private final ExecutorService executor;

    public AlarmRepository(AppDatabase database) {
        this.configDao = database.alarmConfigDao();
        this.zoneDao = database.zoneDao();
        this.scenarioDao = database.scenarioDao();
        this.userDao = database.userDao();
        this.smsLogDao = database.smsLogDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    // Config
    public AlarmConfig getConfig() {
        return configDao.getConfig();
    }

    public LiveData<AlarmConfig> getConfigLive() {
        return configDao.getConfigLive();
    }

    public void saveConfig(AlarmConfig config, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                config.setUpdatedAt(System.currentTimeMillis());
                if (config.getId() == 0) {
                    config.setCreatedAt(System.currentTimeMillis());
                    configDao.insert(config);
                } else {
                    configDao.update(config);
                }
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // Zones
    public List<Zone> getAllZones() {
        return zoneDao.getAll();
    }

    public LiveData<List<Zone>> getZonesLive() {
        return zoneDao.getAllLive();
    }

    public void saveZones(List<Zone> zones, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                long timestamp = System.currentTimeMillis();
                for (Zone zone : zones) {
                    zone.setUpdatedAt(timestamp);
                }
                zoneDao.insertAll(zones);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // Scenarios
    public List<Scenario> getAllScenarios() {
        return scenarioDao.getAll();
    }

    public LiveData<List<Scenario>> getScenariosLive() {
        return scenarioDao.getAllLive();
    }

    public void saveScenario(Scenario scenario, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                scenario.setUpdatedAt(System.currentTimeMillis());
                scenarioDao.insert(scenario);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // Users
    public List<User> getAllUsers() {
        return userDao.getAll();
    }

    public LiveData<List<User>> getUsersLive() {
        return userDao.getAllLive();
    }

    public void updateUserPermissions(int slot, int permissions, Callback<Void> callback) {
        executor.execute(() -> {
            try {
                userDao.updatePermissions(slot, permissions, System.currentTimeMillis());
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // SMS Log
    public void logSms(SmsLog log, Callback<Long> callback) {
        executor.execute(() -> {
            try {
                log.setCreatedAt(System.currentTimeMillis());
                long id = smsLogDao.insert(log);
                if (callback != null) callback.onSuccess(id);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public List<SmsLog> getRecentLogs(int limit) {
        return smsLogDao.getRecent(limit);
    }

    // Callback interface
    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
```

---

## Migrazione Dati

### Da File a Room

Per migrare i dati dall'app esistente (file "DATA_file"):

```java
public class DataMigrator {

    public static void migrateFromLegacy(Context context, AppDatabase database) {
        // Leggi vecchio file
        LegacyData legacyData = readLegacyFile(context);
        if (legacyData == null) return;

        // Migra configurazione
        AlarmConfig config = new AlarmConfig();
        config.setPhoneNumber(legacyData.phoneNumber);
        config.setMainFlags(legacyData.mainFlags);
        database.alarmConfigDao().insert(config);

        // Migra zone
        List<Zone> zones = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Zone zone = new Zone();
            zone.setSlot(i + 1);
            zone.setName(legacyData.zoneNames[i]);
            zone.setEnabled(!legacyData.zoneNames[i].equals("NE"));
            zones.add(zone);
        }
        database.zoneDao().insertAll(zones);

        // Migra scenari
        List<Scenario> scenarios = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            Scenario scenario = new Scenario();
            scenario.setSlot(i + 1);
            scenario.setName(legacyData.scenarioNames[i]);
            scenario.setEnabled(!legacyData.scenarioNames[i].isEmpty());
            scenarios.add(scenario);
        }
        database.scenarioDao().insertAll(scenarios);

        // Migra utenti
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            User user = new User();
            user.setSlot(i + 1);
            user.setName(legacyData.userNames[i]);
            user.setPermissions(legacyData.userFlags[i]);
            user.setEnabled(!legacyData.userNames[i].isEmpty());
            users.add(user);
        }
        database.userDao().insertAll(users);

        // Migra impostazioni
        if (legacyData.disclaimerAccepted) {
            database.appSettingsDao().setBoolean(
                AppSettings.KEY_DISCLAIMER_ACCEPTED,
                true
            );
        }

        // Elimina vecchio file dopo migrazione riuscita
        deleteLegacyFile(context);
    }

    private static LegacyData readLegacyFile(Context context) {
        // Implementazione lettura file legacy
        // ...
    }
}
```

---

## Inizializzazione Database

Nell'Application class:

```java
public class BHomeAlarmApp extends Application {

    private AppDatabase database;
    private AlarmRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();

        // Inizializza database
        database = AppDatabase.getInstance(this);
        repository = new AlarmRepository(database);

        // Migra dati legacy se necessario
        if (isFirstRunAfterUpdate()) {
            DataMigrator.migrateFromLegacy(this, database);
        }
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public AlarmRepository getRepository() {
        return repository;
    }

    private boolean isFirstRunAfterUpdate() {
        // Controlla se esistono dati legacy da migrare
        File legacyFile = new File(getFilesDir(), "DATA_file");
        return legacyFile.exists();
    }
}
```
