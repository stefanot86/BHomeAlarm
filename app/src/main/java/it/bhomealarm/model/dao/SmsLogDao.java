package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.bhomealarm.model.entity.SmsLog;

@Dao
public interface SmsLogDao {

    @Query("SELECT * FROM sms_log ORDER BY timestamp DESC")
    LiveData<List<SmsLog>> getAllLogs();

    @Query("SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<SmsLog>> getRecentLogs(int limit);

    @Query("SELECT * FROM sms_log ORDER BY timestamp DESC")
    List<SmsLog> getAllLogsSync();

    @Query("SELECT * FROM sms_log WHERE direction = :direction ORDER BY timestamp DESC")
    LiveData<List<SmsLog>> getLogsByDirection(int direction);

    @Insert
    long insert(SmsLog log);

    @Delete
    void delete(SmsLog log);

    @Query("DELETE FROM sms_log")
    void deleteAll();

    @Query("DELETE FROM sms_log WHERE timestamp < :beforeTimestamp")
    void deleteOldLogs(long beforeTimestamp);

    @Query("SELECT COUNT(*) FROM sms_log")
    int getLogCount();

    @Query("SELECT * FROM sms_log WHERE message_id = :messageId LIMIT 1")
    SmsLog getByMessageId(String messageId);

    @Query("UPDATE sms_log SET status = :status WHERE message_id = :messageId")
    void updateStatus(String messageId, int status);

    @Query("UPDATE sms_log SET status = :status, error_message = :errorMessage WHERE message_id = :messageId")
    void updateStatusWithError(String messageId, int status, String errorMessage);
}
