package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.bhomealarm.model.entity.Zone;

@Dao
public interface ZoneDao {

    @Query("SELECT * FROM zones ORDER BY slot ASC")
    LiveData<List<Zone>> getAllZones();

    @Query("SELECT * FROM zones ORDER BY slot ASC")
    List<Zone> getAllZonesSync();

    @Query("SELECT * FROM zones WHERE slot = :slot LIMIT 1")
    LiveData<Zone> getZoneBySlot(int slot);

    @Query("SELECT * FROM zones WHERE slot = :slot LIMIT 1")
    Zone getZoneBySlotSync(int slot);

    @Query("SELECT * FROM zones WHERE enabled = 1 ORDER BY slot ASC")
    LiveData<List<Zone>> getEnabledZones();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Zone zone);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Zone> zones);

    @Update
    void update(Zone zone);

    @Delete
    void delete(Zone zone);

    @Query("DELETE FROM zones")
    void deleteAll();

    @Query("UPDATE zones SET name = :name, enabled = :enabled, updated_at = :timestamp WHERE slot = :slot")
    void updateZone(int slot, String name, boolean enabled, long timestamp);
}
