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
}
