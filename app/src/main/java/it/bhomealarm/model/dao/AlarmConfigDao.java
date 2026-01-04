package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import it.bhomealarm.model.entity.AlarmConfig;

@Dao
public interface AlarmConfigDao {

    @Query("SELECT * FROM alarm_config LIMIT 1")
    LiveData<AlarmConfig> getConfig();

    @Query("SELECT * FROM alarm_config LIMIT 1")
    AlarmConfig getConfigSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AlarmConfig config);

    @Update
    void update(AlarmConfig config);

    @Delete
    void delete(AlarmConfig config);

    @Query("DELETE FROM alarm_config")
    void deleteAll();

    @Query("UPDATE alarm_config SET last_status = :status, last_status_text = :statusText, last_check = :timestamp WHERE id = :id")
    void updateStatus(long id, int status, String statusText, long timestamp);

    @Query("UPDATE alarm_config SET config_complete = :complete, updated_at = :timestamp WHERE id = :id")
    void updateConfigComplete(long id, boolean complete, long timestamp);
}
