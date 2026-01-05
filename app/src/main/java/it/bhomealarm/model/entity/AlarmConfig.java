package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entità Room che rappresenta la configurazione principale del sistema allarme.
 * <p>
 * Questa classe memorizza:
 * <ul>
 *     <li>Numero di telefono dell'allarme</li>
 *     <li>Versione firmware (ricevuta da CONF1)</li>
 *     <li>Flag di configurazione principale</li>
 *     <li>Ultimo stato conosciuto del sistema</li>
 *     <li>Preferenze SIM per dispositivi dual-SIM</li>
 * </ul>
 * <p>
 * I flag di configurazione sono ricevuti dalla risposta CONF1 dell'allarme
 * e indicano quali funzionalità sono abilitate per l'utente corrente.
 *
 * @see it.bhomealarm.model.dao.AlarmConfigDao
 */
@Entity(tableName = "alarm_config")
public class AlarmConfig {

    // ========== Costanti Flag Permessi ==========
    // Questi flag indicano i permessi dell'utente sul sistema allarme

    /** Flag RX1: l'utente riceve notifiche di allarme */
    public static final int FLAG_RX1 = 1;         // 0b00000001

    /** Flag RX2: l'utente riceve notifiche di stato */
    public static final int FLAG_RX2 = 2;         // 0b00000010

    /** Flag VERIFY: l'utente può verificare lo stato del sistema */
    public static final int FLAG_VERIFY = 4;      // 0b00000100

    /** Flag CMD_ON_OFF: l'utente può attivare/disattivare l'allarme */
    public static final int FLAG_CMD_ON_OFF = 8;  // 0b00001000

    /** Flag MAIN_OTHER: altri permessi principali */
    public static final int FLAG_MAIN_OTHER = 64; // 0b01000000

    // ========== Costanti Stato Sistema ==========

    /** Stato sconosciuto (nessuna verifica effettuata) */
    public static final int STATUS_UNKNOWN = 0;

    /** Sistema allarme attivo (armato) */
    public static final int STATUS_ARMED = 1;

    /** Sistema allarme disattivo (disarmato) */
    public static final int STATUS_DISARMED = 2;

    // ========== Campi Database ==========

    /** ID univoco nel database (auto-generato) */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Numero di telefono della SIM dell'allarme */
    @ColumnInfo(name = "phone_number")
    @NonNull
    private String phoneNumber = "";

    /** Versione firmware dell'allarme (es. "3.2.1") */
    @ColumnInfo(name = "version")
    private String version;

    /** Bitmask dei flag di configurazione principale */
    @ColumnInfo(name = "main_flags")
    private int mainFlags;

    /** Ultimo stato conosciuto del sistema (usa costanti STATUS_*) */
    @ColumnInfo(name = "last_status")
    private int lastStatus = STATUS_UNKNOWN;

    /** Testo descrittivo dell'ultimo stato */
    @ColumnInfo(name = "last_status_text")
    private String lastStatusText;

    /** Timestamp dell'ultima verifica stato (millisecondi) */
    @ColumnInfo(name = "last_check")
    private long lastCheck;

    /** True se la configurazione CONF1-5 è stata completata */
    @ColumnInfo(name = "config_complete")
    private boolean configComplete;

    /** Slot SIM preferito per l'invio (0 = default, 1 = SIM1, 2 = SIM2) */
    @ColumnInfo(name = "preferred_sim")
    private int preferredSim;

    /** Timestamp di creazione del record */
    @ColumnInfo(name = "created_at")
    private long createdAt;

    /** Timestamp dell'ultimo aggiornamento */
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

    // ========== Metodi Helper ==========

    /**
     * Verifica se un permesso specifico è abilitato.
     *
     * @param permissionBit Costante FLAG_* da verificare
     * @return true se il permesso è presente nei mainFlags
     */
    public boolean hasPermission(int permissionBit) {
        return (mainFlags & permissionBit) != 0;
    }

    /**
     * Imposta o rimuove un permesso specifico.
     *
     * @param permissionBit Costante FLAG_* da modificare
     * @param enabled true per abilitare, false per disabilitare
     */
    public void setPermission(int permissionBit, boolean enabled) {
        if (enabled) {
            mainFlags |= permissionBit;
        } else {
            mainFlags &= ~permissionBit;
        }
    }
}
