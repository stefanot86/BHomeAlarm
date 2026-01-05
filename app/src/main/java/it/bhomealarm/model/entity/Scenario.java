package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Entità Room che rappresenta uno scenario di attivazione dell'allarme.
 * <p>
 * Esistono due tipi di scenari:
 * <ul>
 *     <li><b>Predefiniti (slot 1-16)</b>: configurati sull'allarme e scaricati via CONF2/CONF3</li>
 *     <li><b>Personalizzati (slot > 100)</b>: creati localmente dall'utente nell'app</li>
 * </ul>
 * <p>
 * Ogni scenario definisce quali zone vengono attivate tramite un bitmask.
 * Ad esempio, uno scenario "Notte" potrebbe attivare solo le zone perimetrali,
 * mentre uno scenario "Vacanza" attiva tutte le zone.
 * <p>
 * Il comando SMS per attivare uno scenario predefinito è {@code SCE:XX},
 * mentre per uno personalizzato è {@code CUST:NNN} dove NNN sono i numeri delle zone.
 *
 * @see it.bhomealarm.model.dao.ScenarioDao
 */
@Entity(tableName = "scenarios")
public class Scenario {

    /** ID univoco nel database (auto-generato) */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /**
     * Numero slot dello scenario.
     * <ul>
     *     <li>1-16: scenari predefiniti dall'allarme</li>
     *     <li>>100: scenari personalizzati creati dall'utente</li>
     * </ul>
     */
    @ColumnInfo(name = "slot")
    private int slot;

    /** Nome dello scenario (es. "Casa", "Notte", "Vacanza") */
    @ColumnInfo(name = "name")
    @NonNull
    private String name = "";

    /**
     * Bitmask delle zone incluse nello scenario.
     * <ul>
     *     <li>Bit 0 (valore 1) = Zona 1</li>
     *     <li>Bit 1 (valore 2) = Zona 2</li>
     *     <li>...</li>
     *     <li>Bit 7 (valore 128) = Zona 8</li>
     * </ul>
     * Esempio: zoneMask = 13 (0b00001101) = Zone 1, 3, 4
     */
    @ColumnInfo(name = "zone_mask")
    private int zoneMask;

    /** True se lo scenario è abilitato e selezionabile */
    @ColumnInfo(name = "enabled")
    private boolean enabled;

    /** True se è uno scenario personalizzato (creato dall'utente) */
    @ColumnInfo(name = "is_custom")
    private boolean isCustom;

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

    public int getZoneMask() { return zoneMask; }
    public void setZoneMask(int zoneMask) { this.zoneMask = zoneMask; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // ========== Metodi Helper ==========

    /**
     * Verifica se una specifica zona è inclusa nello scenario.
     *
     * @param zoneSlot Numero della zona (1-8)
     * @return true se la zona è inclusa nel bitmask
     */
    public boolean includesZone(int zoneSlot) {
        return (zoneMask & (1 << (zoneSlot - 1))) != 0;
    }

    /**
     * Aggiunge o rimuove una zona dallo scenario.
     *
     * @param zoneSlot Numero della zona (1-8)
     * @param included true per includere, false per escludere
     */
    public void setZoneIncluded(int zoneSlot, boolean included) {
        if (included) {
            zoneMask |= (1 << (zoneSlot - 1));
        } else {
            zoneMask &= ~(1 << (zoneSlot - 1));
        }
    }

    /**
     * Restituisce la lista delle zone incluse nello scenario.
     *
     * @return Lista dei numeri delle zone (1-8) incluse
     */
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
