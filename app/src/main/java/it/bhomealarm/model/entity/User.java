package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entità Room che rappresenta un utente nella rubrica dell'allarme.
 * <p>
 * Il sistema Bticino supporta fino a 16 utenti più un utente speciale "Joker".
 * Ogni utente ha un insieme di permessi che determinano cosa può fare:
 * <ul>
 *     <li><b>RX1</b>: riceve SMS di notifica allarme</li>
 *     <li><b>RX2</b>: riceve SMS di notifica stato</li>
 *     <li><b>VERIFY</b>: può richiedere lo stato del sistema (SYS?)</li>
 *     <li><b>CMD</b>: può attivare/disattivare l'allarme</li>
 * </ul>
 * <p>
 * I dati degli utenti vengono scaricati dall'allarme tramite CONF4 e CONF5.
 *
 * @see it.bhomealarm.model.dao.UserDao
 */
@Entity(tableName = "users")
public class User {

    // ========== Costanti Permessi ==========

    /** Permesso RX1: riceve notifiche di allarme/intrusione */
    public static final int PERM_RX1 = 1;

    /** Permesso RX2: riceve notifiche di cambio stato */
    public static final int PERM_RX2 = 2;

    /** Permesso VERIFY: può verificare lo stato del sistema */
    public static final int PERM_VERIFY = 4;

    /** Permesso CMD_ON_OFF: può attivare e disattivare l'allarme */
    public static final int PERM_CMD_ON_OFF = 8;

    // ========== Campi Database ==========

    /** ID univoco nel database (auto-generato) */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Numero slot dell'utente (1-16 per utenti normali, 0 per Joker) */
    @ColumnInfo(name = "slot")
    private int slot;

    /** Nome dell'utente (es. "Mario", "Casa") */
    @ColumnInfo(name = "name")
    @NonNull
    private String name = "";

    /** Bitmask dei permessi (usa costanti PERM_*) */
    @ColumnInfo(name = "permissions")
    private int permissions;

    /** True se è l'utente speciale Joker (slot 0) */
    @ColumnInfo(name = "is_joker")
    private boolean isJoker;

    /** True se l'utente è abilitato */
    @ColumnInfo(name = "enabled")
    private boolean enabled;

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

    public int getPermissions() { return permissions; }
    public void setPermissions(int permissions) { this.permissions = permissions; }

    public boolean isJoker() { return isJoker; }
    public void setJoker(boolean joker) { isJoker = joker; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // ========== Metodi Helper ==========

    /**
     * Verifica se l'utente ha un permesso specifico.
     *
     * @param permissionBit Costante PERM_* da verificare
     * @return true se il permesso è presente
     */
    public boolean hasPermission(int permissionBit) {
        return (permissions & permissionBit) != 0;
    }

    /**
     * Imposta o rimuove un permesso specifico.
     *
     * @param permissionBit Costante PERM_* da modificare
     * @param enabled true per abilitare, false per disabilitare
     */
    public void setPermission(int permissionBit, boolean enabled) {
        if (enabled) {
            permissions |= permissionBit;
        } else {
            permissions &= ~permissionBit;
        }
    }

    /**
     * Restituisce una stringa leggibile dei permessi attivi.
     * Esempio: "RX1 RX2 CMD"
     *
     * @return Stringa con i nomi dei permessi separati da spazi
     */
    public String getPermissionsString() {
        StringBuilder sb = new StringBuilder();
        if (hasPermission(PERM_RX1)) sb.append("RX1 ");
        if (hasPermission(PERM_RX2)) sb.append("RX2 ");
        if (hasPermission(PERM_VERIFY)) sb.append("VERIFY ");
        if (hasPermission(PERM_CMD_ON_OFF)) sb.append("CMD ");
        return sb.toString().trim();
    }
}
