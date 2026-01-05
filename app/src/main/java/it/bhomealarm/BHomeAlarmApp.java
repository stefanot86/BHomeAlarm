package it.bhomealarm;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import it.bhomealarm.model.database.AppDatabase;
import it.bhomealarm.model.repository.AlarmRepository;

/**
 * Classe Application principale dell'applicazione BHomeAlarm.
 * <p>
 * Questa classe rappresenta il punto di ingresso dell'applicazione Android e si occupa
 * dell'inizializzazione di tutti i componenti globali necessari al funzionamento dell'app.
 * </p>
 *
 * <h2>Responsabilita principali:</h2>
 * <ul>
 *   <li>Inizializzazione del database Room ({@link AppDatabase})</li>
 *   <li>Inizializzazione del repository per la gestione degli allarmi ({@link AlarmRepository})</li>
 *   <li>Creazione dei canali di notifica per Android 8.0+ (API 26+)</li>
 * </ul>
 *
 * <h2>Canali di notifica:</h2>
 * <ul>
 *   <li><b>CHANNEL_ID_ALARM</b>: Canale ad alta priorita per gli eventi di allarme</li>
 *   <li><b>CHANNEL_ID_SMS</b>: Canale a priorita normale per lo stato delle comunicazioni SMS</li>
 * </ul>
 *
 * <h2>Pattern Singleton:</h2>
 * <p>
 * La classe implementa un pattern Singleton per permettere l'accesso all'istanza
 * dell'Application da qualsiasi parte dell'app tramite {@link #getInstance()}.
 * </p>
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see Application
 * @see AppDatabase
 * @see AlarmRepository
 */
public class BHomeAlarmApp extends Application {

    /**
     * Identificatore del canale di notifica per gli eventi di allarme.
     * <p>
     * Questo canale e configurato con priorita alta (IMPORTANCE_HIGH) e vibrazione abilitata
     * per garantire che l'utente riceva immediatamente le notifiche relative agli allarmi.
     * </p>
     */
    public static final String CHANNEL_ID_ALARM = "bhomealarm_alarm_channel";

    /**
     * Identificatore del canale di notifica per lo stato degli SMS.
     * <p>
     * Questo canale e configurato con priorita normale (IMPORTANCE_DEFAULT) per le
     * notifiche relative allo stato delle comunicazioni SMS con la centrale di allarme.
     * </p>
     */
    public static final String CHANNEL_ID_SMS = "bhomealarm_sms_channel";

    /** Istanza singleton dell'applicazione. */
    private static BHomeAlarmApp instance;

    /**
     * Metodo chiamato alla creazione dell'applicazione.
     * <p>
     * Esegue l'inizializzazione di tutti i componenti globali nell'ordine seguente:
     * </p>
     * <ol>
     *   <li>Salvataggio dell'istanza singleton</li>
     *   <li>Inizializzazione del database Room</li>
     *   <li>Inizializzazione del repository degli allarmi</li>
     *   <li>Creazione dei canali di notifica (solo Android 8.0+)</li>
     * </ol>
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Inizializzazione del database
        AppDatabase.getInstance(this);

        // Inizializzazione del repository
        AlarmRepository.getInstance(this);

        // Creazione dei canali di notifica
        createNotificationChannels();
    }

    /**
     * Restituisce l'istanza singleton dell'applicazione.
     * <p>
     * Questo metodo permette di accedere al contesto dell'applicazione da qualsiasi
     * parte del codice senza dover passare esplicitamente il Context.
     * </p>
     *
     * @return l'istanza singleton di {@link BHomeAlarmApp}, oppure {@code null} se
     *         l'applicazione non e ancora stata creata
     */
    public static BHomeAlarmApp getInstance() {
        return instance;
    }

    /**
     * Crea i canali di notifica richiesti per Android 8.0 (API 26) e versioni successive.
     * <p>
     * Vengono creati due canali distinti:
     * </p>
     * <ul>
     *   <li><b>Canale Allarme</b>: Priorita alta con vibrazione per eventi critici</li>
     *   <li><b>Canale SMS</b>: Priorita normale per notifiche sullo stato delle comunicazioni</li>
     * </ul>
     * <p>
     * Questo metodo non ha effetto su versioni Android precedenti alla 8.0.
     * </p>
     */
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
