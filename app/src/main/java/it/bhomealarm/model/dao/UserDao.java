package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.bhomealarm.model.entity.User;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users ORDER BY slot ASC")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM users ORDER BY slot ASC")
    List<User> getAllUsersSync();

    @Query("SELECT * FROM users WHERE slot = :slot LIMIT 1")
    LiveData<User> getUserBySlot(int slot);

    @Query("SELECT * FROM users WHERE slot = :slot LIMIT 1")
    User getUserBySlotSync(int slot);

    @Query("SELECT * FROM users WHERE is_joker = 1 LIMIT 1")
    LiveData<User> getJokerUser();

    @Query("SELECT * FROM users WHERE enabled = 1 ORDER BY slot ASC")
    LiveData<List<User>> getEnabledUsers();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users")
    void deleteAll();

    @Query("UPDATE users SET name = :name, permissions = :permissions, enabled = :enabled, updated_at = :timestamp WHERE slot = :slot")
    void updateUser(int slot, String name, int permissions, boolean enabled, long timestamp);
}
