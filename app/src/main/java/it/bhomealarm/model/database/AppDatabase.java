package it.bhomealarm.model.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import it.bhomealarm.model.dao.AlarmConfigDao;
import it.bhomealarm.model.dao.AppSettingsDao;
import it.bhomealarm.model.dao.ScenarioDao;
import it.bhomealarm.model.dao.SmsLogDao;
import it.bhomealarm.model.dao.UserDao;
import it.bhomealarm.model.dao.ZoneDao;
import it.bhomealarm.model.entity.AlarmConfig;
import it.bhomealarm.model.entity.AppSettings;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.model.entity.Zone;

/**
 * Database Room principale dell'applicazione BHomeAlarm.
 * <p>
 * Questa classe astratta rappresenta il database locale dell'applicazione, implementato
 * utilizzando la libreria Room Persistence di Android Jetpack. Room fornisce un layer
 * di astrazione sopra SQLite che permette un accesso al database fluido e type-safe.
 * </p>
 *
 * <h2>Entity gestite:</h2>
 * <p>Il database gestisce le seguenti entita:</p>
 * <ul>
 *   <li>{@link AlarmConfig} - Configurazione della centrale di allarme (numero SIM, PIN, ecc.)</li>
 *   <li>{@link Zone} - Zone di rilevamento della centrale (sensori, aree protette)</li>
 *   <li>{@link Scenario} - Scenari di attivazione/disattivazione dell'allarme</li>
 *   <li>{@link User} - Utenti autorizzati a gestire la centrale</li>
 *   <li>{@link SmsLog} - Log dei messaggi SMS scambiati con la centrale</li>
 *   <li>{@link AppSettings} - Impostazioni generali dell'applicazione</li>
 * </ul>
 *
 * <h2>DAO disponibili:</h2>
 * <p>Per ogni entita e disponibile un Data Access Object (DAO) che espone le operazioni CRUD:</p>
 * <ul>
 *   <li>{@link AlarmConfigDao} - Operazioni sulla configurazione allarme</li>
 *   <li>{@link ZoneDao} - Operazioni sulle zone</li>
 *   <li>{@link ScenarioDao} - Operazioni sugli scenari</li>
 *   <li>{@link UserDao} - Operazioni sugli utenti</li>
 *   <li>{@link SmsLogDao} - Operazioni sul log SMS</li>
 *   <li>{@link AppSettingsDao} - Operazioni sulle impostazioni</li>
 * </ul>
 *
 * <h2>Pattern Singleton:</h2>
 * <p>
 * La classe implementa il pattern Singleton thread-safe con double-checked locking
 * per garantire che esista una sola istanza del database nell'applicazione.
 * </p>
 *
 * <h2>Strategia di migrazione:</h2>
 * <p>
 * Il database utilizza {@code fallbackToDestructiveMigration()} che ricrea il database
 * da zero in caso di cambiamento di versione. Questo approccio e adatto durante lo
 * sviluppo ma potrebbe richiedere migrazioni esplicite in produzione.
 * </p>
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see RoomDatabase
 * @see Database
 */
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
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    /** Nome del file del database SQLite. */
    private static final String DATABASE_NAME = "bhomealarm_db";

    /** Istanza singleton del database (volatile per thread-safety). */
    private static volatile AppDatabase INSTANCE;

    /**
     * Restituisce il DAO per le operazioni sulla configurazione dell'allarme.
     *
     * @return istanza di {@link AlarmConfigDao} per operazioni CRUD su {@link AlarmConfig}
     */
    public abstract AlarmConfigDao alarmConfigDao();

    /**
     * Restituisce il DAO per le operazioni sulle zone.
     *
     * @return istanza di {@link ZoneDao} per operazioni CRUD su {@link Zone}
     */
    public abstract ZoneDao zoneDao();

    /**
     * Restituisce il DAO per le operazioni sugli scenari.
     *
     * @return istanza di {@link ScenarioDao} per operazioni CRUD su {@link Scenario}
     */
    public abstract ScenarioDao scenarioDao();

    /**
     * Restituisce il DAO per le operazioni sugli utenti.
     *
     * @return istanza di {@link UserDao} per operazioni CRUD su {@link User}
     */
    public abstract UserDao userDao();

    /**
     * Restituisce il DAO per le operazioni sul log SMS.
     *
     * @return istanza di {@link SmsLogDao} per operazioni CRUD su {@link SmsLog}
     */
    public abstract SmsLogDao smsLogDao();

    /**
     * Restituisce il DAO per le operazioni sulle impostazioni dell'app.
     *
     * @return istanza di {@link AppSettingsDao} per operazioni CRUD su {@link AppSettings}
     */
    public abstract AppSettingsDao appSettingsDao();

    /**
     * Restituisce l'istanza singleton del database.
     * <p>
     * Questo metodo implementa il pattern Singleton con double-checked locking
     * per garantire thread-safety e performance ottimali. Il database viene
     * creato una sola volta e riutilizzato per tutta la durata dell'applicazione.
     * </p>
     *
     * @param context il contesto dell'applicazione (verra convertito in ApplicationContext
     *                per evitare memory leak)
     * @return l'istanza singleton di {@link AppDatabase}
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
