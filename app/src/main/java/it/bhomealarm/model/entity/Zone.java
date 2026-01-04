package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Rappresenta una zona del sistema allarme (max 8 zone).
 */
@Entity(tableName = "zones")
public class Zone {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "slot")
    private int slot; // 1-8

    @ColumnInfo(name = "name")
    @NonNull
    private String name = "";

    @ColumnInfo(name = "enabled")
    private boolean enabled;

    @ColumnInfo(name = "description")
    private String description;

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

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
