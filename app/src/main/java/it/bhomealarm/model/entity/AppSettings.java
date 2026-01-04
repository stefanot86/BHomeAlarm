package it.bhomealarm.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Impostazioni dell'applicazione.
 */
@Entity(tableName = "app_settings")
public class AppSettings {

    @PrimaryKey
    private int id = 1; // Singleton row

    @ColumnInfo(name = "notifications_enabled")
    private boolean notificationsEnabled = true;

    @ColumnInfo(name = "vibration_enabled")
    private boolean vibrationEnabled = true;

    @ColumnInfo(name = "sound_enabled")
    private boolean soundEnabled = true;

    @ColumnInfo(name = "dark_mode")
    private int darkMode = 0; // 0=system, 1=light, 2=dark

    @ColumnInfo(name = "first_launch")
    private boolean firstLaunch = true;

    @ColumnInfo(name = "sms_timeout_seconds")
    private int smsTimeoutSeconds = 60;

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
