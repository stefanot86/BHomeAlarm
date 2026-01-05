package it.bhomealarm.util;

/**
 * Classe contenente tutte le costanti globali dell'applicazione BHomeAlarm.
 * <p>
 * Questa classe definisce le costanti utilizzate in tutta l'applicazione per:
 * <ul>
 *     <li>Comandi SMS da inviare al sistema di allarme</li>
 *     <li>Prefissi delle risposte SMS ricevute</li>
 *     <li>Separatori per il parsing dei messaggi</li>
 *     <li>Stati del sistema di allarme</li>
 *     <li>Configurazione di zone, scenari e utenti</li>
 *     <li>Permessi utente (bitmask)</li>
 *     <li>Timeout e tentativi di retry</li>
 *     <li>Stati del processo di configurazione</li>
 *     <li>Chiavi per SharedPreferences</li>
 *     <li>Codici di errore</li>
 *     <li>Azioni Intent per broadcast</li>
 *     <li>ID notifiche e codici richiesta</li>
 * </ul>
 * <p>
 * La classe e' dichiarata final e ha un costruttore privato per impedirne
 * l'istanziazione (pattern utility class).
 *
 * @author BHomeAlarm Team
 * @version 1.0
 */
public final class Constants {

    /**
     * Costruttore privato per impedire l'istanziazione della classe.
     */
    private Constants() {} // No instantiation

    // ========== SMS Commands ==========

    /**
     * Comando SMS per richiedere la configurazione parte 1.
     * Contiene: versione firmware, flags utente, elenco zone.
     */
    public static final String CMD_CONF1 = "CONF1?";

    /**
     * Comando SMS per richiedere la configurazione parte 2.
     * Contiene: scenari 1-8.
     */
    public static final String CMD_CONF2 = "CONF2?";

    /**
     * Comando SMS per richiedere la configurazione parte 3.
     * Contiene: scenari 9-16.
     */
    public static final String CMD_CONF3 = "CONF3?";

    /**
     * Comando SMS per richiedere la configurazione parte 4.
     * Contiene: utenti 1-8 e utente joker.
     */
    public static final String CMD_CONF4 = "CONF4?";

    /**
     * Comando SMS per richiedere la configurazione parte 5.
     * Contiene: utenti 9-16.
     */
    public static final String CMD_CONF5 = "CONF5?";

    /**
     * Formato comando SMS per attivare l'allarme con uno scenario predefinito.
     * Parametro: numero scenario (01-16).
     * Esempio: SCE:05 attiva lo scenario 5.
     */
    public static final String CMD_ARM_SCENARIO = "SCE:%02d";

    /**
     * Formato comando SMS per attivare l'allarme con zone personalizzate.
     * Parametro: maschera zone (8 caratteri 0/1).
     * Esempio: CUST:11001100 attiva zone 1,2,5,6.
     */
    public static final String CMD_ARM_CUSTOM = "CUST:%s";

    /**
     * Comando SMS per disattivare l'allarme.
     */
    public static final String CMD_DISARM = "SYS OFF";

    /**
     * Comando SMS per richiedere lo stato corrente del sistema.
     */
    public static final String CMD_STATUS = "SYS?";

    /**
     * Formato comando SMS per configurare un utente.
     * Parametri: numero utente (01-16), numero telefono.
     */
    public static final String CMD_SET_USER = "SET:U%02d%s";

    // ========== SMS Response Prefixes ==========

    /**
     * Prefisso risposta SMS per configurazione parte 1.
     */
    public static final String RESP_CONF1 = "CONF1:";

    /**
     * Prefisso risposta SMS per configurazione parte 2.
     */
    public static final String RESP_CONF2 = "CONF2:";

    /**
     * Prefisso risposta SMS per configurazione parte 3.
     */
    public static final String RESP_CONF3 = "CONF3:";

    /**
     * Prefisso risposta SMS per configurazione parte 4.
     */
    public static final String RESP_CONF4 = "CONF4:";

    /**
     * Prefisso risposta SMS per configurazione parte 5.
     */
    public static final String RESP_CONF5 = "CONF5:";

    /**
     * Prefisso risposta SMS per operazione completata con successo.
     */
    public static final String RESP_OK = "OK:";

    /**
     * Prefisso risposta SMS per stato del sistema.
     */
    public static final String RESP_STATUS = "STATUS:";

    /**
     * Prefisso risposta SMS per errore.
     */
    public static final String RESP_ERROR = "ERR:";

    // ========== SMS Separators ==========

    /**
     * Separatore tra comando e parametri nei messaggi SMS.
     */
    public static final char SEP_COMMAND = ':';

    /**
     * Separatore tra campi diversi nei messaggi SMS.
     */
    public static final char SEP_FIELD = '&';

    /**
     * Carattere di terminazione messaggio SMS.
     */
    public static final char SEP_END = '#';

    /**
     * Separatore tra chiave e valore nei campi SMS.
     */
    public static final char SEP_ASSIGN = '=';

    /**
     * Separatore per flags nei messaggi SMS.
     */
    public static final char SEP_FLAGS = '.';

    // ========== Alarm Status ==========

    /**
     * Stato allarme: sistema attivato.
     */
    public static final String STATUS_ARMED = "ARMED";

    /**
     * Stato allarme: sistema disattivato.
     */
    public static final String STATUS_DISARMED = "DISARMED";

    /**
     * Stato allarme: allarme in corso (intrusione rilevata).
     */
    public static final String STATUS_ALARM = "ALARM";

    /**
     * Stato allarme: manomissione rilevata.
     */
    public static final String STATUS_TAMPER = "TAMPER";

    /**
     * Stato allarme: stato non determinabile.
     */
    public static final String STATUS_UNKNOWN = "UNKNOWN";

    // ========== Zone Constants ==========

    /**
     * Numero totale di zone supportate dal sistema.
     */
    public static final int ZONE_COUNT = 8;

    /**
     * Valore che indica una zona non abilitata.
     */
    public static final String ZONE_NOT_ENABLED = "NE";

    // ========== Scenario Constants ==========

    /**
     * Numero totale di scenari supportati dal sistema.
     */
    public static final int SCENARIO_COUNT = 16;

    /**
     * Valore che indica uno scenario non abilitato.
     */
    public static final String SCENARIO_NOT_ENABLED = "NE";

    /**
     * Identificatore per scenario personalizzato (zone selezionate manualmente).
     */
    public static final String SCENARIO_CUSTOM = "CUSTOM";

    // ========== User Constants ==========

    /**
     * Numero totale di utenti supportati dal sistema.
     */
    public static final int USER_COUNT = 16;

    /**
     * Valore che indica un utente non abilitato.
     */
    public static final String USER_NOT_ENABLED = "NE";

    /**
     * Prefisso per identificare l'utente Joker nelle risposte SMS.
     * L'utente Joker ha permessi speciali e puo' ricevere tutte le notifiche.
     */
    public static final String USER_JOKER_PREFIX = "RJO";

    // ========== User Permission Bits ==========

    /**
     * Bit permesso: ricezione notifiche RX1 (allarme).
     * Bitmask: 1000 (8 in decimale).
     */
    public static final int PERM_RX1 = 0b1000;

    /**
     * Bit permesso: ricezione notifiche RX2 (avvisi).
     * Bitmask: 0100 (4 in decimale).
     */
    public static final int PERM_RX2 = 0b0100;

    /**
     * Bit permesso: ricezione conferme operazioni.
     * Bitmask: 0010 (2 in decimale).
     */
    public static final int PERM_VERIFY = 0b0010;

    /**
     * Bit permesso: invio comandi ON/OFF.
     * Bitmask: 0001 (1 in decimale).
     */
    public static final int PERM_CMD_ON_OFF = 0b0001;

    /**
     * Maschera per tutti i permessi abilitati.
     * Bitmask: 1111 (15 in decimale).
     */
    public static final int PERM_ALL = 0b1111;

    /**
     * Maschera per nessun permesso abilitato.
     * Bitmask: 0000 (0 in decimale).
     */
    public static final int PERM_NONE = 0b0000;

    // ========== Timeouts (milliseconds) ==========

    /**
     * Timeout per invio SMS in millisecondi (10 secondi).
     */
    public static final int TIMEOUT_SMS_SEND = 10_000;

    /**
     * Timeout per attesa risposta SMS in millisecondi (60 secondi).
     */
    public static final int TIMEOUT_SMS_RESPONSE = 60_000;

    /**
     * Timeout per operazioni di parsing in millisecondi (5 secondi).
     */
    public static final int TIMEOUT_PARSING = 5_000;

    /**
     * Ritardo tra tentativi di retry in millisecondi (5 secondi).
     */
    public static final int RETRY_DELAY = 5_000;

    /**
     * Numero massimo di tentativi di retry per operazione.
     */
    public static final int MAX_RETRIES = 2;

    // ========== Configuration States ==========

    /**
     * Stato configurazione: inattivo, nessuna operazione in corso.
     */
    public static final int CONFIG_STATE_IDLE = 0;

    /**
     * Stato configurazione: in attesa/elaborazione CONF1.
     */
    public static final int CONFIG_STATE_CONF1 = 1;

    /**
     * Stato configurazione: in attesa/elaborazione CONF2.
     */
    public static final int CONFIG_STATE_CONF2 = 2;

    /**
     * Stato configurazione: in attesa/elaborazione CONF3.
     */
    public static final int CONFIG_STATE_CONF3 = 3;

    /**
     * Stato configurazione: in attesa/elaborazione CONF4.
     */
    public static final int CONFIG_STATE_CONF4 = 4;

    /**
     * Stato configurazione: in attesa/elaborazione CONF5.
     */
    public static final int CONFIG_STATE_CONF5 = 5;

    /**
     * Stato configurazione: completata con successo.
     */
    public static final int CONFIG_STATE_COMPLETE = 6;

    /**
     * Stato configurazione: errore durante il processo.
     */
    public static final int CONFIG_STATE_ERROR = -1;

    /**
     * Numero totale di step nel processo di configurazione.
     */
    public static final int CONFIG_TOTAL_STEPS = 5;

    // ========== Shared Preferences Keys ==========

    /**
     * Nome del file SharedPreferences dell'applicazione.
     */
    public static final String PREF_NAME = "bhomealarm_prefs";

    /**
     * Chiave SharedPreferences: disclaimer accettato dall'utente.
     */
    public static final String PREF_DISCLAIMER_ACCEPTED = "disclaimer_accepted";

    /**
     * Chiave SharedPreferences: numero di telefono del sistema di allarme.
     */
    public static final String PREF_ALARM_PHONE = "alarm_phone_number";

    /**
     * Chiave SharedPreferences: slot SIM selezionato per l'invio SMS.
     */
    public static final String PREF_SELECTED_SIM = "selected_sim_slot";

    /**
     * Chiave SharedPreferences: ultimo stato conosciuto del sistema.
     */
    public static final String PREF_LAST_STATUS = "last_status";

    /**
     * Chiave SharedPreferences: timestamp ultimo controllo stato.
     */
    public static final String PREF_LAST_CHECK_TIME = "last_check_time";

    /**
     * Chiave SharedPreferences: sistema configurato correttamente.
     */
    public static final String PREF_CONFIGURED = "system_configured";

    // ========== Error Codes ==========

    /**
     * Codice errore: comando non riconosciuto dal sistema.
     */
    public static final String ERROR_UNKNOWN_CMD = "E01";

    /**
     * Codice errore: parametro non valido nel comando.
     */
    public static final String ERROR_INVALID_PARAM = "E02";

    /**
     * Codice errore: utente non autorizzato per l'operazione.
     */
    public static final String ERROR_UNAUTHORIZED = "E03";

    /**
     * Codice errore: sistema occupato, riprovare.
     */
    public static final String ERROR_SYSTEM_BUSY = "E04";

    /**
     * Codice errore: errore interno del sistema.
     */
    public static final String ERROR_INTERNAL = "E05";

    /**
     * Codice errore: timeout nell'attesa della risposta.
     */
    public static final String ERROR_TIMEOUT = "TIMEOUT";

    // ========== Intent Actions ==========

    /**
     * Azione Intent broadcast: SMS inviato.
     */
    public static final String ACTION_SMS_SENT = "it.bhomealarm.SMS_SENT";

    /**
     * Azione Intent broadcast: SMS consegnato.
     */
    public static final String ACTION_SMS_DELIVERED = "it.bhomealarm.SMS_DELIVERED";

    /**
     * Azione Intent broadcast: SMS ricevuto (azione di sistema Android).
     */
    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    // ========== Notification IDs ==========

    /**
     * ID notifica per eventi allarme (intrusione, manomissione).
     */
    public static final int NOTIFICATION_ID_ALARM = 1001;

    /**
     * ID notifica per eventi SMS (invio, ricezione).
     */
    public static final int NOTIFICATION_ID_SMS = 1002;

    /**
     * ID notifica per stato configurazione.
     */
    public static final int NOTIFICATION_ID_CONFIG = 1003;

    // ========== Request Codes ==========

    /**
     * Codice richiesta per permessi SMS runtime.
     */
    public static final int REQUEST_SMS_PERMISSION = 100;

    /**
     * Codice richiesta per permessi telefono runtime.
     */
    public static final int REQUEST_PHONE_PERMISSION = 101;

    /**
     * Codice richiesta per selezione contatto dalla rubrica.
     */
    public static final int REQUEST_CONTACT_PICKER = 102;
}
