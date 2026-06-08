package it.bhomealarm.controller;

import android.content.Context;
import android.content.SharedPreferences;

import it.bhomealarm.service.SmsService;
import it.bhomealarm.util.Constants;

/**
 * Controller per i comandi di alto livello verso il sistema di allarme.
 * <p>
 * Questa classe rappresenta l'unico punto dell'applicazione che conosce i comandi
 * SMS dell'allarme ({@code SCE:NN}, {@code SYS OFF}, ...) e il numero della centrale.
 * Si colloca nel layer <b>Controller</b>, sopra al servizio di basso livello
 * {@link SmsService} (che si occupa dell'invio fisico dell'SMS, del Dual-SIM e del
 * logging) e sotto al layer View/ViewModel.
 * <p>
 * Tutti i chiamanti che devono attivare/disattivare l'allarme o leggere lo stato
 * corrente passano da qui, evitando duplicazione di logica:
 * <ul>
 *     <li>{@link it.bhomealarm.controller.viewmodel.HomeViewModel} (UI in-app)</li>
 *     <li>{@code AlarmActionActivity} (mini-schermata del widget)</li>
 *     <li>{@code AlarmWidgetProvider} (widget home screen, per leggere lo stato)</li>
 * </ul>
 * <p>
 * Il controller <b>non</b> gestisce timeout, LiveData, toast o aggiornamenti UI:
 * quelle responsabilità restano al chiamante. I metodi di invio restituiscono
 * il {@code messageId} generato da {@link SmsService} (o {@code null} in caso di errore)
 * per permettere al chiamante di tracciare la risposta.
 *
 * @see SmsService
 * @see Constants
 */
public class AlarmController {

    /** Istanza singleton del controller. */
    private static volatile AlarmController instance;

    /** Servizio di basso livello per l'invio degli SMS. */
    private final SmsService smsService;

    /** SharedPreferences dell'applicazione (numero allarme, ultimo stato, ...). */
    private final SharedPreferences prefs;

    /**
     * Costruttore privato per il pattern Singleton.
     *
     * @param context Contesto usato per ottenere SmsService e SharedPreferences
     */
    private AlarmController(Context context) {
        Context appContext = context.getApplicationContext();
        this.smsService = SmsService.getInstance(appContext);
        this.prefs = appContext.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Restituisce l'istanza singleton del controller, creandola se necessario.
     *
     * @param context Contesto per l'inizializzazione (usato solo alla prima chiamata)
     * @return Istanza singleton di AlarmController
     */
    public static AlarmController getInstance(Context context) {
        if (instance == null) {
            synchronized (AlarmController.class) {
                if (instance == null) {
                    instance = new AlarmController(context);
                }
            }
        }
        return instance;
    }

    // ========== Invio comandi ==========

    /**
     * Attiva l'allarme con uno scenario predefinito.
     *
     * @param scenarioSlot Numero slot dello scenario (1-16)
     * @return messageId dell'SMS inviato, o {@code null} se il numero non è
     *         configurato o l'invio è fallito
     */
    public String armWithScenario(int scenarioSlot) {
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            return null;
        }
        String command = String.format(Constants.CMD_ARM_SCENARIO, scenarioSlot);
        return smsService.sendCommand(phone, command);
    }

    /**
     * Attiva l'allarme con un insieme di zone personalizzato.
     *
     * @param zoneNumbers Numeri delle zone da attivare (es. "134" per zone 1,3,4)
     * @return messageId dell'SMS inviato, o {@code null} in caso di errore
     */
    public String armWithCustomZones(String zoneNumbers) {
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            return null;
        }
        String command = String.format(Constants.CMD_ARM_CUSTOM, zoneNumbers);
        return smsService.sendCommand(phone, command);
    }

    /**
     * Disattiva l'allarme.
     *
     * @return messageId dell'SMS inviato, o {@code null} in caso di errore
     */
    public String disarm() {
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            return null;
        }
        return smsService.sendCommand(phone, Constants.CMD_DISARM);
    }

    /**
     * Richiede lo stato corrente del sistema.
     *
     * @return messageId dell'SMS inviato, o {@code null} in caso di errore
     */
    public String checkStatus() {
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            return null;
        }
        return smsService.sendCommand(phone, Constants.CMD_STATUS);
    }

    // ========== Lettura stato / configurazione ==========

    /**
     * Restituisce il numero di telefono della centrale allarme configurato.
     *
     * @return Numero allarme, o {@code null} se non configurato (vuoto)
     */
    public String getAlarmPhoneNumber() {
        String phone = prefs.getString(Constants.PREF_ALARM_PHONE, "");
        return phone.isEmpty() ? null : phone;
    }

    /**
     * Indica se il numero della centrale allarme è stato configurato.
     *
     * @return {@code true} se è presente un numero allarme valido
     */
    public boolean isPhoneConfigured() {
        return getAlarmPhoneNumber() != null;
    }

    /**
     * Restituisce l'ultimo stato noto del sistema, salvato dall'ultima risposta SMS.
     *
     * @return Uno dei valori {@code Constants.STATUS_*} (default {@link Constants#STATUS_UNKNOWN})
     */
    public String getLastStatus() {
        return prefs.getString(Constants.PREF_LAST_STATUS, Constants.STATUS_UNKNOWN);
    }

    /**
     * Restituisce il timestamp dell'ultimo aggiornamento di stato ricevuto.
     *
     * @return Timestamp in millisecondi, o 0 se non c'è mai stato un aggiornamento
     */
    public long getLastCheckTime() {
        return prefs.getLong(Constants.PREF_LAST_CHECK_TIME, 0);
    }
}
