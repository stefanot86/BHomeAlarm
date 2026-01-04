package it.bhomealarm.model.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Log degli SMS inviati e ricevuti.
 */
@Entity(tableName = "sms_log")
public class SmsLog {

    // Direction constants
    public static final int DIRECTION_OUTGOING = 0;
    public static final int DIRECTION_INCOMING = 1;

    // Status constants
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_FAILED = 3;
    public static final int STATUS_RECEIVED = 4;

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "direction")
    private int direction;

    @ColumnInfo(name = "message")
    @NonNull
    private String message = "";

    @ColumnInfo(name = "status")
    private int status;

    @ColumnInfo(name = "error_message")
    private String errorMessage;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

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

    // Helper methods
    public boolean isOutgoing() {
        return direction == DIRECTION_OUTGOING;
    }

    public boolean isIncoming() {
        return direction == DIRECTION_INCOMING;
    }

    public boolean isSuccessful() {
        return status == STATUS_SENT || status == STATUS_DELIVERED || status == STATUS_RECEIVED;
    }
}
