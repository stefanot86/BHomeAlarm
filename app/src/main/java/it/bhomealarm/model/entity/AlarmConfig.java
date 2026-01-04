package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Configurazione principale del sistema allarme Bticino 3500/3500N.
 */
@Entity(tableName = "alarm_config")
public class AlarmConfig {

    // Flag constants
    public static final int FLAG_RX1 = 1;         // 0b00000001
    public static final int FLAG_RX2 = 2;         // 0b00000010
    public static final int FLAG_VERIFY = 4;      // 0b00000100
    public static final int FLAG_CMD_ON_OFF = 8;  // 0b00001000
    public static final int FLAG_MAIN_OTHER = 64; // 0b01000000

    // Status constants
    public static final int STATUS_UNKNOWN = 0;
    public static final int STATUS_ARMED = 1;
    public static final int STATUS_DISARMED = 2;

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "phone_number")
    @NonNull
    private String phoneNumber = "";

    @ColumnInfo(name = "version")
    private String version;

    @ColumnInfo(name = "main_flags")
    private int mainFlags;

    @ColumnInfo(name = "last_status")
    private int lastStatus = STATUS_UNKNOWN;

    @ColumnInfo(name = "last_status_text")
    private String lastStatusText;

    @ColumnInfo(name = "last_check")
    private long lastCheck;

    @ColumnInfo(name = "config_complete")
    private boolean configComplete;

    @ColumnInfo(name = "preferred_sim")
    private int preferredSim;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(@NonNull String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public int getMainFlags() { return mainFlags; }
    public void setMainFlags(int mainFlags) { this.mainFlags = mainFlags; }

    public int getLastStatus() { return lastStatus; }
    public void setLastStatus(int lastStatus) { this.lastStatus = lastStatus; }

    public String getLastStatusText() { return lastStatusText; }
    public void setLastStatusText(String lastStatusText) { this.lastStatusText = lastStatusText; }

    public long getLastCheck() { return lastCheck; }
    public void setLastCheck(long lastCheck) { this.lastCheck = lastCheck; }

    public boolean isConfigComplete() { return configComplete; }
    public void setConfigComplete(boolean configComplete) { this.configComplete = configComplete; }

    public int getPreferredSim() { return preferredSim; }
    public void setPreferredSim(int preferredSim) { this.preferredSim = preferredSim; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

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
