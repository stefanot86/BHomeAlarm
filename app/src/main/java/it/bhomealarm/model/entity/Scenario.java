package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta uno scenario di attivazione (max 16 scenari).
 */
@Entity(tableName = "scenarios")
public class Scenario {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "slot")
    private int slot; // 1-16

    @ColumnInfo(name = "name")
    @NonNull
    private String name = "";

    @ColumnInfo(name = "zone_mask")
    private int zoneMask; // Bitmask: bit 0 = zona 1, bit 7 = zona 8

    @ColumnInfo(name = "enabled")
    private boolean enabled;

    @ColumnInfo(name = "is_custom")
    private boolean isCustom;

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

    public int getZoneMask() { return zoneMask; }
    public void setZoneMask(int zoneMask) { this.zoneMask = zoneMask; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

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
