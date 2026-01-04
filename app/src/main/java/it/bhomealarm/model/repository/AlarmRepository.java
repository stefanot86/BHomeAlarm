package it.bhomealarm.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.bhomealarm.model.dao.AlarmConfigDao;
import it.bhomealarm.model.dao.AppSettingsDao;
import it.bhomealarm.model.dao.ScenarioDao;
import it.bhomealarm.model.dao.SmsLogDao;
import it.bhomealarm.model.dao.UserDao;
import it.bhomealarm.model.dao.ZoneDao;
import it.bhomealarm.model.database.AppDatabase;
import it.bhomealarm.model.entity.AlarmConfig;
import it.bhomealarm.model.entity.AppSettings;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.model.entity.Zone;

/**
 * Repository principale per l'accesso ai dati.
 * Fornisce un'API pulita per il layer Controller.
 */
public class AlarmRepository {

    private static volatile AlarmRepository INSTANCE;

    private final AlarmConfigDao alarmConfigDao;
    private final ZoneDao zoneDao;
    private final ScenarioDao scenarioDao;
    private final UserDao userDao;
    private final SmsLogDao smsLogDao;
    private final AppSettingsDao appSettingsDao;

    private final ExecutorService executorService;

    private AlarmRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        alarmConfigDao = database.alarmConfigDao();
        zoneDao = database.zoneDao();
        scenarioDao = database.scenarioDao();
        userDao = database.userDao();
        smsLogDao = database.smsLogDao();
        appSettingsDao = database.appSettingsDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public static AlarmRepository getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (AlarmRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AlarmRepository(application);
                }
            }
        }
        return INSTANCE;
    }

    // ========== AlarmConfig ==========

    public LiveData<AlarmConfig> getAlarmConfig() {
        return alarmConfigDao.getConfig();
    }

    public void insertAlarmConfig(AlarmConfig config) {
        executorService.execute(() -> alarmConfigDao.insert(config));
    }

    public void updateAlarmConfig(AlarmConfig config) {
        executorService.execute(() -> alarmConfigDao.update(config));
    }

    public void updateAlarmStatus(long id, int status, String statusText) {
        executorService.execute(() ->
            alarmConfigDao.updateStatus(id, status, statusText, System.currentTimeMillis())
        );
    }

    // ========== Zone ==========

    public LiveData<List<Zone>> getAllZones() {
        return zoneDao.getAllZones();
    }

    public LiveData<Zone> getZoneBySlot(int slot) {
        return zoneDao.getZoneBySlot(slot);
    }

    public void insertZone(Zone zone) {
        executorService.execute(() -> zoneDao.insert(zone));
    }

    public void insertAllZones(List<Zone> zones) {
        executorService.execute(() -> zoneDao.insertAll(zones));
    }

    public void updateZone(Zone zone) {
        executorService.execute(() -> zoneDao.update(zone));
    }

    public void deleteAllZones() {
        executorService.execute(zoneDao::deleteAll);
    }

    // ========== Scenario ==========

    public LiveData<List<Scenario>> getAllScenarios() {
        return scenarioDao.getAllScenarios();
    }

    public LiveData<Scenario> getScenarioBySlot(int slot) {
        return scenarioDao.getScenarioBySlot(slot);
    }

    public void insertScenario(Scenario scenario) {
        executorService.execute(() -> scenarioDao.insert(scenario));
    }

    public void insertAllScenarios(List<Scenario> scenarios) {
        executorService.execute(() -> scenarioDao.insertAll(scenarios));
    }

    public void updateScenario(Scenario scenario) {
        executorService.execute(() -> scenarioDao.update(scenario));
    }

    public void deleteAllScenarios() {
        executorService.execute(scenarioDao::deleteAll);
    }

    // ========== User ==========

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public LiveData<User> getUserBySlot(int slot) {
        return userDao.getUserBySlot(slot);
    }

    public LiveData<User> getJokerUser() {
        return userDao.getJokerUser();
    }

    public void insertUser(User user) {
        executorService.execute(() -> userDao.insert(user));
    }

    public void insertAllUsers(List<User> users) {
        executorService.execute(() -> userDao.insertAll(users));
    }

    public void updateUser(User user) {
        executorService.execute(() -> userDao.update(user));
    }

    public void deleteAllUsers() {
        executorService.execute(userDao::deleteAll);
    }

    // ========== SmsLog ==========

    public LiveData<List<SmsLog>> getAllSmsLogs() {
        return smsLogDao.getAllLogs();
    }

    public LiveData<List<SmsLog>> getRecentSmsLogs(int limit) {
        return smsLogDao.getRecentLogs(limit);
    }

    public void insertSmsLog(SmsLog log) {
        executorService.execute(() -> smsLogDao.insert(log));
    }

    public void deleteAllSmsLogs() {
        executorService.execute(smsLogDao::deleteAll);
    }

    public void deleteOldSmsLogs(long beforeTimestamp) {
        executorService.execute(() -> smsLogDao.deleteOldLogs(beforeTimestamp));
    }

    // ========== AppSettings ==========

    public LiveData<AppSettings> getAppSettings() {
        return appSettingsDao.getSettings();
    }

    public void insertAppSettings(AppSettings settings) {
        executorService.execute(() -> appSettingsDao.insert(settings));
    }

    public void updateAppSettings(AppSettings settings) {
        executorService.execute(() -> appSettingsDao.update(settings));
    }

    public void markFirstLaunchComplete() {
        executorService.execute(() ->
            appSettingsDao.markFirstLaunchComplete(System.currentTimeMillis())
        );
    }

    // ========== ViewModel Helper Methods ==========

    /**
     * Salva una lista di zone (cancella le esistenti e inserisce le nuove).
     */
    public void saveZones(List<Zone> zones) {
        executorService.execute(() -> {
            zoneDao.deleteAll();
            if (zones != null && !zones.isEmpty()) {
                zoneDao.insertAll(zones);
            }
        });
    }

    /**
     * Salva una lista di scenari (inserisce o aggiorna).
     */
    public void saveScenarios(List<Scenario> scenarios) {
        executorService.execute(() -> {
            if (scenarios != null) {
                for (Scenario s : scenarios) {
                    scenarioDao.insert(s);
                }
            }
        });
    }

    /**
     * Salva una lista di utenti (inserisce o aggiorna).
     */
    public void saveUsers(List<User> users) {
        executorService.execute(() -> {
            if (users != null) {
                for (User u : users) {
                    userDao.insert(u);
                }
            }
        });
    }

    /**
     * Aggiorna la versione firmware nella configurazione.
     */
    public void updateConfigVersion(String version) {
        executorService.execute(() -> {
            // Se non esiste, crea una nuova configurazione
            AlarmConfig config = new AlarmConfig();
            config.setId(1);
            config.setVersion(version);
            alarmConfigDao.insert(config);
        });
    }

    /**
     * Cancella tutti i dati dal database.
     */
    public void clearAllData() {
        executorService.execute(() -> {
            zoneDao.deleteAll();
            scenarioDao.deleteAll();
            userDao.deleteAll();
            smsLogDao.deleteAll();
        });
    }

    /**
     * Ottiene i log recenti con limite.
     */
    public LiveData<List<SmsLog>> getRecentLogs(int limit) {
        return smsLogDao.getRecentLogs(limit);
    }

    /**
     * Cancella tutti i log SMS.
     */
    public void clearAllLogs() {
        executorService.execute(smsLogDao::deleteAll);
    }

    /**
     * Aggiorna lo stato di un log SMS tramite messageId.
     */
    public void updateSmsLogStatus(String messageId, int status) {
        executorService.execute(() -> smsLogDao.updateStatus(messageId, status));
    }

    /**
     * Aggiorna lo stato di un log SMS con messaggio di errore.
     */
    public void updateSmsLogStatusWithError(String messageId, int status, String errorMessage) {
        executorService.execute(() -> smsLogDao.updateStatusWithError(messageId, status, errorMessage));
    }

    /**
     * Ottiene un log SMS tramite messageId (sincrono).
     */
    public SmsLog getSmsLogByMessageId(String messageId) {
        return smsLogDao.getByMessageId(messageId);
    }
}
