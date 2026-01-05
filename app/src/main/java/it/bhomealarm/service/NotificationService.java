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
 * Servizio per la gestione centralizzata delle notifiche push dell'applicazione.
 * <p>
 * Questa classe implementa il pattern Singleton e fornisce metodi per mostrare
 * diversi tipi di notifiche:
 * <ul>
 *     <li><b>Notifiche Allarme</b>: Stati dell'allarme (armato, disarmato, allarme in corso, manomissione)</li>
 *     <li><b>Notifiche SMS</b>: Conferme di invio e ricezione messaggi</li>
 *     <li><b>Notifiche Configurazione</b>: Progresso e risultato della configurazione iniziale</li>
 *     <li><b>Notifiche Errore</b>: Errori di comunicazione e configurazione</li>
 * </ul>
 * <p>
 * Le notifiche utilizzano i canali definiti in {@link BHomeAlarmApp}:
 * <ul>
 *     <li>{@link BHomeAlarmApp#CHANNEL_ID_ALARM}: Per notifiche critiche dell'allarme (alta priorita')</li>
 *     <li>{@link BHomeAlarmApp#CHANNEL_ID_SMS}: Per notifiche relative agli SMS (priorita' normale)</li>
 * </ul>
 * <p>
 * Tutte le notifiche includono un PendingIntent per aprire l'applicazione quando vengono toccate.
 * Le notifiche ad alta priorita' includono anche una vibrazione per attirare l'attenzione.
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see BHomeAlarmApp
 * @see Constants
 */
public class NotificationService {

    /**
     * Istanza singleton del servizio notifiche.
     */
    private static NotificationService instance;

    /**
     * Contesto applicazione per accesso alle risorse di sistema.
     */
    private final Context context;

    /**
     * Manager di sistema per la gestione delle notifiche.
     * Utilizzato per mostrare, aggiornare e cancellare le notifiche.
     */
    private final NotificationManager notificationManager;

    /**
     * Costruttore privato per implementare il pattern Singleton.
     * Inizializza il contesto e ottiene il NotificationManager dal sistema.
     *
     * @param context Contesto dell'applicazione
     */
    private NotificationService(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Restituisce l'istanza singleton del servizio notifiche.
     * Se l'istanza non esiste, viene creata in modo thread-safe.
     *
     * @param context Contesto per l'inizializzazione (usato solo alla prima chiamata)
     * @return Istanza singleton di NotificationService
     */
    public static synchronized NotificationService getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationService(context);
        }
        return instance;
    }

    // ========== Alarm Notifications ==========

    /**
     * Mostra una notifica per comunicare lo stato attuale del sistema di allarme.
     * <p>
     * La notifica viene personalizzata in base allo stato:
     * <ul>
     *     <li>ARMED: "Allarme Attivato" con eventuale scenario</li>
     *     <li>DISARMED: "Allarme Disattivato"</li>
     *     <li>ALARM: "ALLARME IN CORSO - Rilevata intrusione!"</li>
     *     <li>TAMPER: "Manomissione Rilevata"</li>
     * </ul>
     * <p>
     * Utilizza il canale ALARM con priorita' alta e vibrazione.
     *
     * @param status   Stato dell'allarme (usa le costanti in {@link Constants}:
     *                 STATUS_ARMED, STATUS_DISARMED, STATUS_ALARM, STATUS_TAMPER)
     * @param scenario Nome dello scenario attivo (opzionale, puo' essere null)
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
     * Mostra una notifica per segnalare un errore di comunicazione con il sistema di allarme.
     * Utilizza il canale ALARM con priorita' alta per attirare l'attenzione dell'utente.
     *
     * @param errorMessage Descrizione dell'errore da visualizzare
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
     * Mostra una notifica per confermare l'invio di un comando SMS al sistema di allarme.
     * Utilizza il canale SMS con priorita' normale.
     *
     * @param command Il comando che e' stato inviato (es. "ARM", "DISARM", "STATUS")
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
     * Mostra una notifica per segnalare la ricezione di una risposta SMS dal sistema di allarme.
     * Se la risposta supera i 50 caratteri, viene troncata e aggiunto "...".
     * Utilizza il canale SMS con priorita' normale.
     *
     * @param response Il testo della risposta ricevuta dal sistema di allarme
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
     * Mostra una notifica persistente con barra di progresso durante la configurazione.
     * La notifica e' di tipo "ongoing" e non puo' essere rimossa dall'utente.
     * Utilizza il canale SMS con priorita' bassa per non disturbare.
     *
     * @param step  Numero dello step corrente (1-based)
     * @param total Numero totale di step da completare
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
     * Mostra una notifica di conferma per il completamento della configurazione.
     * Prima di mostrare la notifica di successo, cancella la notifica di progresso.
     * Utilizza il canale SMS con priorita' normale.
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
     * Mostra una notifica per segnalare un errore durante la configurazione.
     * Prima di mostrare la notifica di errore, cancella la notifica di progresso.
     * Utilizza il canale ALARM con priorita' alta per attirare l'attenzione.
     *
     * @param errorMessage Descrizione dell'errore verificatosi durante la configurazione
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
     * Cancella tutte le notifiche attive dell'applicazione.
     * Utile quando l'utente apre l'app o effettua il logout.
     */
    public void cancelAll() {
        notificationManager.cancelAll();
    }

    /**
     * Cancella una notifica specifica identificata dal suo ID.
     *
     * @param notificationId ID della notifica da cancellare (usa le costanti in {@link Constants}:
     *                       NOTIFICATION_ID_ALARM, NOTIFICATION_ID_SMS, NOTIFICATION_ID_CONFIG)
     */
    public void cancel(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    /**
     * Metodo helper privato per costruire e mostrare una notifica.
     * <p>
     * Configura automaticamente:
     * <ul>
     *     <li>Icona piccola per la barra di stato</li>
     *     <li>PendingIntent per aprire MainActivity al tocco</li>
     *     <li>Auto-cancellazione al tocco</li>
     *     <li>Vibrazione per notifiche ad alta priorita'</li>
     * </ul>
     *
     * @param id        ID univoco della notifica (per aggiornamento/cancellazione)
     * @param channelId ID del canale di notifica da utilizzare
     * @param title     Titolo della notifica
     * @param message   Corpo del messaggio della notifica
     * @param priority  Livello di priorita' (costanti NotificationCompat.PRIORITY_*)
     */
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
