package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entità Room che rappresenta un log SMS nella cronologia.
 * <p>
 * Memorizza tutti gli SMS inviati all'allarme e ricevuti dall'allarme,
 * permettendo all'utente di consultare la cronologia delle comunicazioni.
 * <p>
 * Ogni log include:
 * <ul>
 *     <li>Direzione (in uscita o in entrata)</li>
 *     <li>Contenuto del messaggio</li>
 *     <li>Stato dell'invio/ricezione</li>
 *     <li>Eventuale messaggio di errore</li>
 *     <li>Timestamp</li>
 * </ul>
 *
 * @see it.bhomealarm.model.dao.SmsLogDao
 */
@Entity(tableName = "sms_log")
public class SmsLog {

    // ========== Costanti Direzione ==========

    /** SMS inviato dall'app all'allarme */
    public static final int DIRECTION_OUTGOING = 0;

    /** SMS ricevuto dall'allarme */
    public static final int DIRECTION_INCOMING = 1;

    // ========== Costanti Stato ==========

    /** SMS in attesa di invio */
    public static final int STATUS_PENDING = 0;

    /** SMS inviato con successo */
    public static final int STATUS_SENT = 1;

    /** SMS consegnato al destinatario */
    public static final int STATUS_DELIVERED = 2;

    /** Errore durante l'invio dell'SMS */
    public static final int STATUS_FAILED = 3;

    /** SMS ricevuto con successo */
    public static final int STATUS_RECEIVED = 4;

    // ========== Campi Database ==========

    /** ID univoco nel database (auto-generato) */
    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Direzione del messaggio (usa costanti DIRECTION_*) */
    @ColumnInfo(name = "direction")
    private int direction;

    /** Contenuto del messaggio SMS */
    @ColumnInfo(name = "message")
    @NonNull
    private String message = "";

    /** Stato attuale del messaggio (usa costanti STATUS_*) */
    @ColumnInfo(name = "status")
    private int status;

    /** Messaggio di errore in caso di fallimento */
    @ColumnInfo(name = "error_message")
    private String errorMessage;

    /** Timestamp di invio/ricezione (millisecondi) */
    @ColumnInfo(name = "timestamp")
    private long timestamp;

    /** ID univoco del messaggio per tracciamento consegna */
    @ColumnInfo(name = "message_id")
    private String messageId;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }

    @NonNull
    public String getMessage() { return message; }
    public void setMessage(@NonNull String message) { this.message = message; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    // ========== Metodi Helper ==========

    /**
     * Verifica se il messaggio è stato inviato dall'app.
     *
     * @return true se è un messaggio in uscita
     */
    public boolean isOutgoing() {
        return direction == DIRECTION_OUTGOING;
    }

    /**
     * Verifica se il messaggio è stato ricevuto dall'allarme.
     *
     * @return true se è un messaggio in entrata
     */
    public boolean isIncoming() {
        return direction == DIRECTION_INCOMING;
    }

    /**
     * Verifica se l'operazione SMS è andata a buon fine.
     *
     * @return true se lo stato indica successo (inviato, consegnato o ricevuto)
     */
    public boolean isSuccessful() {
        return status == STATUS_SENT || status == STATUS_DELIVERED || status == STATUS_RECEIVED;
    }
}
