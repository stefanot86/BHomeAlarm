package it.bhomealarm.model.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import it.bhomealarm.model.dao.AlarmConfigDao;
import it.bhomealarm.model.dao.AppSettingsDao;
import it.bhomealarm.model.dao.ScenarioDao;
import it.bhomealarm.model.dao.SmsLogDao;
import it.bhomealarm.model.dao.UserDao;
import it.bhomealarm.model.dao.ZoneDao;
import it.bhomealarm.model.entity.AlarmConfig;
import it.bhomealarm.model.entity.AppSettings;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.model.entity.Zone;

@Database(
    entities = {
        AlarmConfig.class,
        Zone.class,
        Scenario.class,
        User.class,
        SmsLog.class,
        AppSettings.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "bhomealarm_db";
    private static volatile AppDatabase INSTANCE;

    public abstract AlarmConfigDao alarmConfigDao();
    public abstract ZoneDao zoneDao();
    public abstract ScenarioDao scenarioDao();
    public abstract UserDao userDao();
    public abstract SmsLogDao smsLogDao();
    public abstract AppSettingsDao appSettingsDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
