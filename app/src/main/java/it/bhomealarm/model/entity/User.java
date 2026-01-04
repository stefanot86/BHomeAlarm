package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Rappresenta un utente nella rubrica dell'allarme (max 16 utenti).
 */
@Entity(tableName = "users")
public class User {

    // Permission constants
    public static final int PERM_RX1 = 1;
    public static final int PERM_RX2 = 2;
    public static final int PERM_VERIFY = 4;
    public static final int PERM_CMD_ON_OFF = 8;

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "slot")
    private int slot; // 1-16, 0 for joker

    @ColumnInfo(name = "name")
    @NonNull
    private String name = "";

    @ColumnInfo(name = "permissions")
    private int permissions;

    @ColumnInfo(name = "is_joker")
    private boolean isJoker;

    @ColumnInfo(name = "enabled")
    private boolean enabled;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    @NonNull
    public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    public int getPermissions() { return permissions; }
    public void setPermissions(int permissions) { this.permissions = permissions; }

    public boolean isJoker() { return isJoker; }
    public void setJoker(boolean joker) { isJoker = joker; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

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
