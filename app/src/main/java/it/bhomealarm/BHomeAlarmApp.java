package it.bhomealarm;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import it.bhomealarm.model.database.AppDatabase;
import it.bhomealarm.model.repository.AlarmRepository;

/**
 * Application class principale.
 * Inizializza componenti globali come database e notification channels.
 */
public class BHomeAlarmApp extends Application {

    public static final String CHANNEL_ID_ALARM = "bhomealarm_alarm_channel";
    public static final String CHANNEL_ID_SMS = "bhomealarm_sms_channel";

    private static BHomeAlarmApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize database
        AppDatabase.getInstance(this);

        // Initialize repository
        AlarmRepository.getInstance(this);

        // Create notification channels
        createNotificationChannels();
    }

    public static BHomeAlarmApp getInstance() {
        return instance;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                // Alarm events channel (high priority)
                NotificationChannel alarmChannel = new NotificationChannel(
                        CHANNEL_ID_ALARM,
                        "Allarme",
                        NotificationManager.IMPORTANCE_HIGH
                );
                alarmChannel.setDescription("Notifiche eventi allarme");
                alarmChannel.enableVibration(true);
                manager.createNotificationChannel(alarmChannel);

                // SMS status channel (default priority)
                NotificationChannel smsChannel = new NotificationChannel(
                        CHANNEL_ID_SMS,
                        "Stato SMS",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                smsChannel.setDescription("Stato comunicazione SMS");
                manager.createNotificationChannel(smsChannel);
            }
        }
    }
}
