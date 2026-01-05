package it.bhomealarm.service;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import it.bhomealarm.callback.OnSmsResultListener;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;

/**
 * Servizio per l'invio di SMS con supporto completo per dispositivi Dual-SIM.
 * <p>
 * Questa classe implementa il pattern Singleton e fornisce funzionalita' per:
 * <ul>
 *     <li>Invio di SMS tramite SIM specifica o predefinita</li>
 *     <li>Gestione di dispositivi Dual-SIM con selezione dello slot</li>
 *     <li>Tracciamento dello stato di invio e consegna tramite PendingIntent</li>
 *     <li>Persistenza dei log SMS nel database tramite Repository</li>
 *     <li>Callback asincroni per notificare il risultato delle operazioni</li>
 * </ul>
 * <p>
 * L'invio degli SMS richiede il permesso {@link android.Manifest.permission#SEND_SMS}.
 * Per la gestione Dual-SIM e' necessario anche {@link android.Manifest.permission#READ_PHONE_STATE}.
 * <p>
 * Esempio di utilizzo:
 * <pre>
 * SmsService smsService = SmsService.getInstance(context);
 * smsService.setListener(new OnSmsResultListener() {...});
 * smsService.setSelectedSimSlot(0); // Usa SIM 1
 * String messageId = smsService.sendSms("+39123456789", "Messaggio");
 * </pre>
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see OnSmsResultListener
 * @see SmsReceiver
 * @see SmsSentReceiver
 */
public class SmsService {

    /**
     * Istanza singleton del servizio.
     * Garantisce un'unica istanza condivisa in tutta l'applicazione.
     */
    private static SmsService instance;

    /**
     * Contesto applicazione per accesso alle risorse di sistema.
     */
    private final Context context;

    /**
     * Repository per la persistenza dei log SMS nel database locale.
     */
    private final AlarmRepository repository;

    /**
     * SharedPreferences per memorizzare le impostazioni dell'utente,
     * inclusa la SIM selezionata per l'invio.
     */
    private final SharedPreferences prefs;

    /**
     * Listener per notificare i risultati delle operazioni SMS.
     * Puo' essere null se nessun componente e' in ascolto.
     */
    private OnSmsResultListener listener;

    /**
     * Contatore atomico per generare request code univoci per i PendingIntent.
     * Evita conflitti quando vengono inviati piu' SMS in rapida successione.
     */
    private static final AtomicInteger requestCodeCounter = new AtomicInteger(0);

    /**
     * Costruttore privato per implementare il pattern Singleton.
     * Inizializza il contesto, il repository e le SharedPreferences.
     *
     * @param context Contesto dell'applicazione
     */
    private SmsService(Context context) {
        this.context = context.getApplicationContext();
        this.repository = AlarmRepository.getInstance((android.app.Application) this.context);
        this.prefs = this.context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Restituisce l'istanza singleton del servizio SMS.
     * Se l'istanza non esiste, viene creata in modo thread-safe.
     *
     * @param context Contesto per l'inizializzazione (usato solo alla prima chiamata)
     * @return Istanza singleton di SmsService
     */
    public static synchronized SmsService getInstance(Context context) {
        if (instance == null) {
            instance = new SmsService(context);
        }
        return instance;
    }

    // ========== Configuration ==========

    /**
     * Imposta il listener per ricevere notifiche sui risultati delle operazioni SMS.
     * Il listener verra' chiamato quando un SMS viene inviato, consegnato o si verifica un errore.
     *
     * @param listener Implementazione di OnSmsResultListener, o null per rimuovere il listener
     * @see OnSmsResultListener
     */
    public void setListener(OnSmsResultListener listener) {
        this.listener = listener;
    }

    /**
     * Imposta lo slot SIM da utilizzare per l'invio degli SMS.
     * L'impostazione viene salvata nelle SharedPreferences e persiste tra i riavvii dell'app.
     *
     * @param simSlot Indice dello slot SIM (0 per SIM 1, 1 per SIM 2, -1 per usare la SIM predefinita del sistema)
     */
    public void setSelectedSimSlot(int simSlot) {
        prefs.edit().putInt(Constants.PREF_SELECTED_SIM, simSlot).apply();
    }

    /**
     * Restituisce lo slot SIM attualmente selezionato per l'invio degli SMS.
     *
     * @return Indice dello slot SIM (0 per SIM 1, 1 per SIM 2) oppure -1 se e' selezionata la SIM predefinita
     */
    public int getSelectedSimSlot() {
        return prefs.getInt(Constants.PREF_SELECTED_SIM, -1);
    }

    // ========== SIM Information ==========

    /**
     * Verifica se il dispositivo supporta e ha attive due SIM (Dual-SIM).
     *
     * @return true se il dispositivo ha piu' di una SIM attiva, false altrimenti
     */
    public boolean isDualSim() {
        return getSimCount() > 1;
    }

    /**
     * Restituisce il numero di SIM attive nel dispositivo.
     * Richiede il permesso READ_PHONE_STATE per funzionare correttamente.
     * Senza il permesso, restituisce sempre 1 come fallback.
     *
     * @return Numero di SIM attive (1 o superiore), o 1 se il permesso non e' concesso
     */
    public int getSimCount() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return 1;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager sm = SubscriptionManager.from(context);
            List<SubscriptionInfo> subscriptions = sm.getActiveSubscriptionInfoList();
            return subscriptions != null ? subscriptions.size() : 1;
        }

        return 1;
    }

    /**
     * Restituisce la lista delle SIM disponibili nel dispositivo con le relative informazioni.
     * Ogni SIM e' rappresentata da un oggetto {@link SimInfo} contenente slot, nome e operatore.
     * <p>
     * Se il permesso READ_PHONE_STATE non e' concesso o non ci sono SIM rilevate,
     * restituisce una lista con una singola "SIM Predefinita".
     *
     * @return Lista di oggetti SimInfo rappresentanti le SIM disponibili
     * @see SimInfo
     */
    public List<SimInfo> getAvailableSims() {
        List<SimInfo> sims = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            sims.add(new SimInfo(0, "SIM Predefinita", ""));
            return sims;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager sm = SubscriptionManager.from(context);
            List<SubscriptionInfo> subscriptions = sm.getActiveSubscriptionInfoList();

            if (subscriptions != null) {
                for (SubscriptionInfo info : subscriptions) {
                    String displayName = info.getDisplayName() != null
                            ? info.getDisplayName().toString()
                            : "SIM " + (info.getSimSlotIndex() + 1);
                    String carrierName = info.getCarrierName() != null
                            ? info.getCarrierName().toString()
                            : "";

                    sims.add(new SimInfo(
                            info.getSimSlotIndex(),
                            displayName,
                            carrierName
                    ));
                }
            }
        }

        if (sims.isEmpty()) {
            sims.add(new SimInfo(0, "SIM Predefinita", ""));
        }

        return sims;
    }

    /**
     * Classe contenitore per le informazioni di una SIM.
     * Fornisce dettagli sullo slot, nome visualizzato e nome dell'operatore.
     */
    public static class SimInfo {
        /**
         * Indice dello slot fisico della SIM (0 o 1).
         */
        public final int slot;

        /**
         * Nome visualizzato della SIM (es. "Vodafone IT", "SIM 1").
         */
        public final String displayName;

        /**
         * Nome dell'operatore di rete (es. "Vodafone", "TIM").
         * Puo' essere vuoto se non disponibile.
         */
        public final String carrierName;

        /**
         * Costruisce un nuovo oggetto SimInfo con le informazioni specificate.
         *
         * @param slot        Indice dello slot SIM (0 o 1)
         * @param displayName Nome visualizzato della SIM
         * @param carrierName Nome dell'operatore di rete
         */
        public SimInfo(int slot, String displayName, String carrierName) {
            this.slot = slot;
            this.displayName = displayName;
            this.carrierName = carrierName;
        }
    }

    // ========== Send SMS ==========

    /**
     * Invia un SMS al numero specificato usando la SIM selezionata nelle impostazioni.
     * Il messaggio viene tracciato nel database e il risultato notificato tramite listener.
     *
     * @param phoneNumber Numero di telefono del destinatario (formato internazionale consigliato)
     * @param message     Testo del messaggio da inviare
     * @return ID univoco (UUID) del messaggio per il tracciamento, o null in caso di errore
     * @see #sendSms(String, String, int)
     */
    public String sendSms(String phoneNumber, String message) {
        return sendSms(phoneNumber, message, getSelectedSimSlot());
    }

    /**
     * Invia un SMS al numero specificato usando una SIM specifica.
     * <p>
     * Il metodo esegue le seguenti operazioni:
     * <ol>
     *     <li>Verifica il permesso SEND_SMS</li>
     *     <li>Crea un log nel database con stato PENDING</li>
     *     <li>Configura i PendingIntent per i callback di invio e consegna</li>
     *     <li>Invia l'SMS tramite SmsManager</li>
     * </ol>
     *
     * @param phoneNumber Numero di telefono del destinatario
     * @param message     Testo del messaggio da inviare
     * @param simSlot     Slot SIM da utilizzare (0, 1) o -1 per la SIM predefinita
     * @return ID univoco (UUID) del messaggio per il tracciamento, o null in caso di errore
     */
    public String sendSms(String phoneNumber, String message, int simSlot) {
        String messageId = UUID.randomUUID().toString();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (listener != null) {
                listener.onSmsError(Activity.RESULT_CANCELED, "Permesso SMS non concesso");
            }
            return null;
        }

        try {
            // Salva il log nel database con stato PENDING
            SmsLog log = new SmsLog();
            log.setMessageId(messageId);
            log.setMessage(message);
            log.setDirection(SmsLog.DIRECTION_OUTGOING);
            log.setStatus(SmsLog.STATUS_PENDING);
            log.setTimestamp(System.currentTimeMillis());
            repository.insertSmsLog(log);

            SmsManager smsManager = getSmsManager(simSlot);

            // Genera request code univoci per evitare conflitti
            int sentRequestCode = requestCodeCounter.incrementAndGet();
            int deliveredRequestCode = requestCodeCounter.incrementAndGet();

            // Crea PendingIntent per callback invio
            Intent sentIntent = new Intent(Constants.ACTION_SMS_SENT);
            sentIntent.putExtra("message_id", messageId);
            sentIntent.setPackage(context.getPackageName());
            PendingIntent sentPI = PendingIntent.getBroadcast(
                    context,
                    sentRequestCode,
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Crea PendingIntent per callback consegna
            Intent deliveredIntent = new Intent(Constants.ACTION_SMS_DELIVERED);
            deliveredIntent.putExtra("message_id", messageId);
            deliveredIntent.setPackage(context.getPackageName());
            PendingIntent deliveredPI = PendingIntent.getBroadcast(
                    context,
                    deliveredRequestCode,
                    deliveredIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Invia SMS
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    sentPI,
                    deliveredPI
            );

            return messageId;

        } catch (Exception e) {
            if (listener != null) {
                listener.onSmsError(-1, "Errore invio SMS: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Invia un comando al sistema di allarme tramite SMS.
     * Metodo di convenienza che utilizza la SIM selezionata nelle impostazioni.
     *
     * @param alarmPhoneNumber Numero di telefono del sistema di allarme
     * @param command          Comando da inviare (es. "ARM", "DISARM", "STATUS")
     * @return ID univoco del messaggio per il tracciamento, o null in caso di errore
     */
    public String sendCommand(String alarmPhoneNumber, String command) {
        return sendSms(alarmPhoneNumber, command, getSelectedSimSlot());
    }

    /**
     * Ottiene l'istanza corretta di SmsManager per lo slot SIM specificato.
     * Gestisce le differenze tra versioni Android per la selezione della SIM.
     *
     * @param simSlot Slot SIM desiderato (0, 1) o -1 per il default
     * @return Istanza di SmsManager configurata per la SIM specificata
     */
    private SmsManager getSmsManager(int simSlot) {
        // Se simSlot è -1, usa la SIM predefinita del sistema
        if (simSlot >= 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager sm = SubscriptionManager.from(context);

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED) {

                List<SubscriptionInfo> subscriptions = sm.getActiveSubscriptionInfoList();
                if (subscriptions != null && simSlot < subscriptions.size()) {
                    int subscriptionId = subscriptions.get(simSlot).getSubscriptionId();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        return context.getSystemService(SmsManager.class)
                                .createForSubscriptionId(subscriptionId);
                    } else {
                        return SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
                    }
                }
            }
        }

        // Fallback a SmsManager default
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.getSystemService(SmsManager.class);
        } else {
            return SmsManager.getDefault();
        }
    }

    // ========== Callbacks from BroadcastReceiver ==========

    /**
     * Metodo di callback chiamato da {@link SmsSentReceiver} quando un SMS e' stato inviato.
     * Notifica il listener del risultato dell'operazione di invio.
     *
     * @param messageId  ID univoco del messaggio inviato
     * @param resultCode Codice risultato (Activity.RESULT_OK per successo, altrimenti codice errore)
     */
    public void onSmsSent(String messageId, int resultCode) {
        if (listener != null) {
            if (resultCode == Activity.RESULT_OK) {
                listener.onSmsSent(messageId);
            } else {
                listener.onSmsError(resultCode, getErrorMessage(resultCode));
            }
        }
    }

    /**
     * Metodo di callback chiamato da {@link SmsSentReceiver} quando un SMS e' stato consegnato.
     * Notifica il listener solo in caso di consegna avvenuta con successo.
     *
     * @param messageId  ID univoco del messaggio consegnato
     * @param resultCode Codice risultato (Activity.RESULT_OK per successo)
     */
    public void onSmsDelivered(String messageId, int resultCode) {
        if (listener != null) {
            if (resultCode == Activity.RESULT_OK) {
                listener.onSmsDelivered(messageId);
            }
        }
    }

    /**
     * Converte un codice di errore SmsManager in un messaggio leggibile in italiano.
     *
     * @param resultCode Codice errore restituito da SmsManager
     * @return Messaggio di errore in italiano
     */
    private String getErrorMessage(int resultCode) {
        switch (resultCode) {
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                return "Errore generico";
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                return "Nessun servizio";
            case SmsManager.RESULT_ERROR_NULL_PDU:
                return "PDU nullo";
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                return "Radio spenta";
            default:
                return "Errore sconosciuto (" + resultCode + ")";
        }
    }
}
