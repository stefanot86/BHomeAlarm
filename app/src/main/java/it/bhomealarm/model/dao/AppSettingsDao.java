package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import it.bhomealarm.model.entity.AppSettings;

@Dao
public interface AppSettingsDao {

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    LiveData<AppSettings> getSettings();

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    AppSettings getSettingsSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppSettings settings);

    @Update
    void update(AppSettings settings);

    @Query("UPDATE app_settings SET first_launch = 0, updated_at = :timestamp WHERE id = 1")
    void markFirstLaunchComplete(long timestamp);

    @Query("UPDATE app_settings SET notifications_enabled = :enabled, updated_at = :timestamp WHERE id = 1")
    void setNotificationsEnabled(boolean enabled, long timestamp);

    @Query("UPDATE app_settings SET dark_mode = :mode, updated_at = :timestamp WHERE id = 1")
    void setDarkMode(int mode, long timestamp);
}
