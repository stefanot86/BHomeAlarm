package it.bhomealarm.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import it.bhomealarm.BHomeAlarmApp;
import it.bhomealarm.R;
import it.bhomealarm.util.Constants;
import it.bhomealarm.view.activity.MainActivity;

/**
 * Service per gestione notifiche push.
 */
public class NotificationService {

    private static NotificationService instance;

    private final Context context;
    private final NotificationManager notificationManager;

    private NotificationService(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static synchronized NotificationService getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationService(context);
        }
        return instance;
    }

    // ========== Alarm Notifications ==========

    /**
     * Mostra notifica per stato allarme.
     *
     * @param status Stato attuale (ARMED, DISARMED, etc.)
     * @param scenario Scenario attivo (opzionale)
     */
    public void showAlarmStatus(String status, String scenario) {
        String title;
        String message;
        int iconRes = R.drawable.ic_notification;

        switch (status) {
            case Constants.STATUS_ARMED:
                title = "Allarme Attivato";
                message = scenario != null
                        ? "Sistema armato con scenario \"" + scenario + "\""
                        : "Sistema armato";
                break;
            case Constants.STATUS_DISARMED:
                title = "Allarme Disattivato";
                message = "Sistema disarmato";
                break;
            case Constants.STATUS_ALARM:
                title = "ALLARME IN CORSO";
                message = "Rilevata intrusione!";
                break;
            case Constants.STATUS_TAMPER:
                title = "Manomissione Rilevata";
                message = "Possibile tentativo di manomissione";
                break;
            default:
                title = "Stato Allarme";
                message = "Stato: " + status;
        }

        showNotification(
                Constants.NOTIFICATION_ID_ALARM,
                BHomeAlarmApp.CHANNEL_ID_ALARM,
                title,
                message,
                NotificationCompat.PRIORITY_HIGH
        );
    }

    /**
     * Mostra notifica per errore comunicazione.
     *
     * @param errorMessage Messaggio di errore
     */
    public void showError(String errorMessage) {
        showNotification(
                Constants.NOTIFICATION_ID_ALARM,
                BHomeAlarmApp.CHANNEL_ID_ALARM,
                "Errore Comunicazione",
                errorMessage,
                NotificationCompat.PRIORITY_HIGH
        );
    }

    // ========== SMS Notifications ==========

    /**
     * Mostra notifica per SMS inviato.
     *
     * @param command Comando inviato
     */
    public void showSmsSent(String command) {
        showNotification(
                Constants.NOTIFICATION_ID_SMS,
                BHomeAlarmApp.CHANNEL_ID_SMS,
                "SMS Inviato",
                "Comando: " + command,
                NotificationCompat.PRIORITY_DEFAULT
        );
    }

    /**
     * Mostra notifica per SMS ricevuto.
     *
     * @param response Risposta ricevuta
     */
    public void showSmsReceived(String response) {
        // Tronca messaggio se troppo lungo
        String displayResponse = response.length() > 50
                ? response.substring(0, 50) + "..."
                : response;

        showNotification(
                Constants.NOTIFICATION_ID_SMS,
                BHomeAlarmApp.CHANNEL_ID_SMS,
                "Risposta Ricevuta",
                displayResponse,
                NotificationCompat.PRIORITY_DEFAULT
        );
    }

    // ========== Configuration Notifications ==========

    /**
     * Mostra notifica per progresso configurazione.
     *
     * @param step Step corrente
     * @param total Totale step
     */
    public void showConfigProgress(int step, int total) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, BHomeAlarmApp.CHANNEL_ID_SMS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Configurazione in corso")
                .setContentText("Step " + step + " di " + total)
                .setProgress(total, step, false)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        notificationManager.notify(Constants.NOTIFICATION_ID_CONFIG, builder.build());
    }

    /**
     * Mostra notifica per configurazione completata.
     */
    public void showConfigComplete() {
        notificationManager.cancel(Constants.NOTIFICATION_ID_CONFIG);

        showNotification(
                Constants.NOTIFICATION_ID_SMS,
                BHomeAlarmApp.CHANNEL_ID_SMS,
                "Configurazione Completata",
                "Sistema configurato con successo",
                NotificationCompat.PRIORITY_DEFAULT
        );
    }

    /**
     * Mostra notifica per errore configurazione.
     *
     * @param errorMessage Messaggio errore
     */
    public void showConfigError(String errorMessage) {
        notificationManager.cancel(Constants.NOTIFICATION_ID_CONFIG);

        showNotification(
                Constants.NOTIFICATION_ID_ALARM,
                BHomeAlarmApp.CHANNEL_ID_ALARM,
                "Errore Configurazione",
                errorMessage,
                NotificationCompat.PRIORITY_HIGH
        );
    }

    // ========== Common ==========

    /**
     * Cancella tutte le notifiche.
     */
    public void cancelAll() {
        notificationManager.cancelAll();
    }

    /**
     * Cancella una notifica specifica.
     *
     * @param notificationId ID notifica
     */
    public void cancel(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    private void showNotification(int id, String channelId, String title, String message, int priority) {
        // Intent per aprire l'app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Aggiungi vibrazione per notifiche importanti
        if (priority >= NotificationCompat.PRIORITY_HIGH) {
            builder.setVibrate(new long[]{0, 250, 250, 250});
        }

        notificationManager.notify(id, builder.build());
    }
}
