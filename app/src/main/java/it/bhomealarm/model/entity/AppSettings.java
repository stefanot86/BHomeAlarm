package it.bhomealarm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entità Room che memorizza le impostazioni dell'applicazione.
 * <p>
 * Questa tabella usa il pattern Singleton: esiste sempre una sola riga con id=1.
 * Memorizza preferenze utente come:
 * <ul>
 *     <li>Impostazioni notifiche (suono, vibrazione)</li>
 *     <li>Tema dell'app (chiaro, scuro, sistema)</li>
 *     <li>Flag primo avvio (per mostrare disclaimer)</li>
 *     <li>Timeout per attesa risposte SMS</li>
 * </ul>
 *
 * @see it.bhomealarm.model.dao.AppSettingsDao
 */
@Entity(tableName = "app_settings")
public class AppSettings {

    /** ID fisso = 1 (pattern Singleton) */
    @PrimaryKey
    private int id = 1;

    /** True se le notifiche push sono abilitate */
    @ColumnInfo(name = "notifications_enabled")
    private boolean notificationsEnabled = true;

    /** True se la vibrazione per le notifiche è abilitata */
    @ColumnInfo(name = "vibration_enabled")
    private boolean vibrationEnabled = true;

    /** True se il suono per le notifiche è abilitato */
    @ColumnInfo(name = "sound_enabled")
    private boolean soundEnabled = true;

    /**
     * Modalità tema dell'app.
     * <ul>
     *     <li>0 = Segue impostazioni di sistema</li>
     *     <li>1 = Tema chiaro forzato</li>
     *     <li>2 = Tema scuro forzato</li>
     * </ul>
     */
    @ColumnInfo(name = "dark_mode")
    private int darkMode = 0;

    /** True se è il primo avvio dell'app (mostra disclaimer) */
    @ColumnInfo(name = "first_launch")
    private boolean firstLaunch = true;

    /** Timeout in secondi per attesa risposta SMS (default 60s) */
    @ColumnInfo(name = "sms_timeout_seconds")
    private int smsTimeoutSeconds = 60;

    /** Timestamp dell'ultimo aggiornamento */
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isVibrationEnabled() { return vibrationEnabled; }
    public void setVibrationEnabled(boolean vibrationEnabled) {
        this.vibrationEnabled = vibrationEnabled;
    }

    public boolean isSoundEnabled() { return soundEnabled; }
    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public int getDarkMode() { return darkMode; }
    public void setDarkMode(int darkMode) { this.darkMode = darkMode; }

    public boolean isFirstLaunch() { return firstLaunch; }
    public void setFirstLaunch(boolean firstLaunch) { this.firstLaunch = firstLaunch; }

    public int getSmsTimeoutSeconds() { return smsTimeoutSeconds; }
    public void setSmsTimeoutSeconds(int smsTimeoutSeconds) {
        this.smsTimeoutSeconds = smsTimeoutSeconds;
    }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
