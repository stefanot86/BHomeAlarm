package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entità Room che rappresenta una zona del sistema allarme.
 * <p>
 * Il sistema Bticino supporta fino a 8 zone, ognuna identificata da uno slot (1-8).
 * Le zone vengono configurate fisicamente sull'allarme e i loro nomi vengono
 * scaricati tramite la risposta CONF1.
 * <p>
 * Esempi di zone tipiche:
 * <ul>
 *     <li>Zona 1: Ingresso</li>
 *     <li>Zona 2: Soggiorno</li>
 *     <li>Zona 3: Camera da letto</li>
 *     <li>Zona 4: Garage</li>
 * </ul>
 *
 * @see it.bhomealarm.model.dao.ZoneDao
 */
@Entity(tableName = "zones")
public class Zone {

    /** ID univoco nel database (auto-generato) */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Numero slot della zona (1-8) */
    @ColumnInfo(name = "slot")
    private int slot;

    /** Nome della zona (es. "Ingresso", "Soggiorno") */
    @ColumnInfo(name = "name")
    @NonNull
    private String name = "";

    /** True se la zona è abilitata/configurata */
    @ColumnInfo(name = "enabled")
    private boolean enabled;

    /** Descrizione aggiuntiva della zona (opzionale) */
    @ColumnInfo(name = "description")
    private String description;

    /** Timestamp dell'ultimo aggiornamento */
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
