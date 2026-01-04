package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.bhomealarm.model.entity.Scenario;

@Dao
public interface ScenarioDao {

    @Query("SELECT * FROM scenarios ORDER BY slot ASC")
    LiveData<List<Scenario>> getAllScenarios();

    @Query("SELECT * FROM scenarios ORDER BY slot ASC")
    List<Scenario> getAllScenariosSync();

    @Query("SELECT * FROM scenarios WHERE slot = :slot LIMIT 1")
    LiveData<Scenario> getScenarioBySlot(int slot);

    @Query("SELECT * FROM scenarios WHERE slot = :slot LIMIT 1")
    Scenario getScenarioBySlotSync(int slot);

    @Query("SELECT * FROM scenarios WHERE enabled = 1 ORDER BY slot ASC")
    LiveData<List<Scenario>> getEnabledScenarios();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Scenario scenario);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Scenario> scenarios);

    @Update
    void update(Scenario scenario);

    @Delete
    void delete(Scenario scenario);

    @Query("DELETE FROM scenarios")
    void deleteAll();

    @Query("UPDATE scenarios SET name = :name, zone_mask = :zoneMask, enabled = :enabled, updated_at = :timestamp WHERE slot = :slot")
    void updateScenario(int slot, String name, int zoneMask, boolean enabled, long timestamp);
}
