package it.bhomealarm.controller.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import it.bhomealarm.callback.OnConfigProgressListener;
import it.bhomealarm.callback.OnSmsResultListener;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.service.SmsReceiver;
import it.bhomealarm.service.SmsService;
import it.bhomealarm.util.Constants;
import it.bhomealarm.util.SmsParser;

/**
 * ViewModel per la gestione della procedura di configurazione iniziale.
 * <p>
 * Questa classe implementa una macchina a stati che gestisce la sequenza di
 * configurazione CONF1-5 per sincronizzare l'applicazione con la centralina
 * di allarme. La procedura comprende:
 * <ul>
 *     <li><b>CONF1:</b> Configurazione base (versione firmware, zone)</li>
 *     <li><b>CONF2:</b> Scenari 1-8</li>
 *     <li><b>CONF3:</b> Scenari 9-16</li>
 *     <li><b>CONF4:</b> Utenti 1-8</li>
 *     <li><b>CONF5:</b> Utenti 9-16</li>
 * </ul>
 * <p>
 * Il ViewModel gestisce automaticamente:
 * <ul>
 *     <li>L'invio sequenziale dei comandi SMS</li>
 *     <li>La ricezione e il parsing delle risposte</li>
 *     <li>I timeout di comunicazione</li>
 *     <li>La persistenza dei dati ricevuti</li>
 *     <li>L'aggiornamento dello stato della UI</li>
 * </ul>
 *
 * @see it.bhomealarm.view.fragment.ConfigurationFragment
 * @see OnConfigProgressListener
 * @see OnSmsResultListener
 * @see SmsParser
 */
public class ConfigurationViewModel extends AndroidViewModel implements OnConfigProgressListener, OnSmsResultListener {

    /**
     * Enumerazione degli stati possibili per un singolo step di configurazione.
     * <p>
     * Utilizzata per tracciare il progresso di ogni fase della configurazione
     * e visualizzare lo stato nella UI.
     */
    public enum StepStatus {
        /** Step non ancora iniziato */
        PENDING,
        /** Step attualmente in esecuzione */
        IN_PROGRESS,
        /** Step completato con successo */
        COMPLETED,
        /** Step fallito per errore */
        ERROR
    }

    /**
     * Classe che rappresenta i dati di un singolo step di configurazione.
     * <p>
     * Contiene le informazioni necessarie per visualizzare lo stato
     * di ogni step nella UI, inclusi numero, nome, stato e messaggio.
     */
    public static class ConfigStep {
        /** Numero progressivo dello step (1-5) */
        public final int stepNumber;

        /** Nome descrittivo dello step */
        public final String name;

        /** Stato corrente dello step */
        public StepStatus status;

        /** Messaggio di stato o errore */
        public String message;

        /**
         * Costruttore per creare un nuovo step di configurazione.
         *
         * @param stepNumber Numero progressivo dello step
         * @param name Nome descrittivo dello step
         */
        public ConfigStep(int stepNumber, String name) {
            this.stepNumber = stepNumber;
            this.name = name;
            this.status = StepStatus.PENDING;
        }
    }

    /** Repository per l'accesso ai dati dell'allarme */
    private final AlarmRepository repository;

    /** Servizio per l'invio di SMS */
    private final SmsService smsService;

    /** SharedPreferences per la persistenza delle impostazioni */
    private final SharedPreferences prefs;

    /** Handler per la gestione dei timeout sul main thread */
    private final Handler timeoutHandler;

    /** Runnable per il timeout corrente */
    private Runnable timeoutRunnable;

    /** ID del messaggio SMS in attesa di risposta */
    private String pendingMessageId;

    /** Stato corrente della macchina a stati di configurazione */
    private int currentState = Constants.CONFIG_STATE_IDLE;

    // ========== UI State ==========

    /** Progresso della configurazione in percentuale (0-100) */
    private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);

    /** Messaggio di stato corrente da visualizzare */
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    /** Flag che indica se la configurazione e' in corso */
    private final MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);

    /** Flag che indica se la configurazione e' stata completata */
    private final MutableLiveData<Boolean> isComplete = new MutableLiveData<>(false);

    /** Messaggio di errore in caso di fallimento */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /** Lista degli step di configurazione con il loro stato */
    private final MutableLiveData<List<ConfigStep>> steps = new MutableLiveData<>();

    /** Log di debug per la visualizzazione delle comunicazioni SMS */
    private final MutableLiveData<List<String>> debugLog = new MutableLiveData<>(new ArrayList<>());

    /**
     * Costruttore del ViewModel.
     * <p>
     * Inizializza tutti i componenti necessari per la gestione della
     * configurazione: repository, servizio SMS, preferences e handler
     * per i timeout.
     *
     * @param application Contesto dell'applicazione Android
     */
    public ConfigurationViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        smsService = SmsService.getInstance(application);
        prefs = application.getSharedPreferences(Constants.PREF_NAME, 0);
        timeoutHandler = new Handler(Looper.getMainLooper());
        initializeSteps();
    }

    /**
     * Chiamato quando il ViewModel viene distrutto.
     * <p>
     * Annulla eventuali timeout pendenti e rimuove il listener SMS
     * per evitare memory leak.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimeout();
        // Rimuovi listener solo se siamo noi
        SmsReceiver.setListener(null);
    }

    // ========== Getters ==========

    /**
     * Restituisce il LiveData contenente il progresso della configurazione.
     *
     * @return LiveData con il valore percentuale del progresso (0-100)
     */
    public LiveData<Integer> getProgress() {
        return progress;
    }

    /**
     * Restituisce il LiveData contenente il messaggio di stato.
     *
     * @return LiveData con il messaggio di stato corrente
     */
    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    /**
     * Restituisce il LiveData che indica se la configurazione e' in corso.
     *
     * @return LiveData con true se la configurazione e' attiva, false altrimenti
     */
    public LiveData<Boolean> getIsRunning() {
        return isRunning;
    }

    /**
     * Restituisce il LiveData che indica se la configurazione e' completata.
     *
     * @return LiveData con true se la configurazione e' terminata con successo
     */
    public LiveData<Boolean> getIsComplete() {
        return isComplete;
    }

    /**
     * Restituisce il LiveData contenente eventuali messaggi di errore.
     *
     * @return LiveData con il messaggio di errore, o null se non ci sono errori
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Restituisce il LiveData contenente la lista degli step di configurazione.
     *
     * @return LiveData con la lista di ConfigStep rappresentanti ogni fase
     */
    public LiveData<List<ConfigStep>> getSteps() {
        return steps;
    }

    /**
     * Restituisce il LiveData contenente il log di debug.
     *
     * @return LiveData con la lista di stringhe del log di debug
     */
    public LiveData<List<String>> getDebugLog() {
        return debugLog;
    }

    /**
     * Restituisce lo stato corrente della macchina a stati.
     *
     * @return Valore intero rappresentante lo stato corrente (vedi Constants.CONFIG_STATE_*)
     */
    public int getCurrentState() {
        return currentState;
    }

    // ========== Actions ==========

    /**
     * Inizializza la lista degli step di configurazione.
     * <p>
     * Crea i 5 step corrispondenti ai comandi CONF1-5 con i relativi
     * nomi descrittivi.
     */
    private void initializeSteps() {
        List<ConfigStep> stepList = new ArrayList<>();
        stepList.add(new ConfigStep(1, "CONF1 - Configurazione base"));
        stepList.add(new ConfigStep(2, "CONF2 - Scenari 1-8"));
        stepList.add(new ConfigStep(3, "CONF3 - Scenari 9-16"));
        stepList.add(new ConfigStep(4, "CONF4 - Utenti 1-8"));
        stepList.add(new ConfigStep(5, "CONF5 - Utenti 9-16"));
        steps.setValue(stepList);
    }

    /**
     * Avvia la procedura di configurazione.
     * <p>
     * Verifica che il numero di allarme sia configurato, inizializza
     * lo stato e avvia il primo step (CONF1).
     * <p>
     * Se la configurazione e' gia' in corso, il metodo non fa nulla.
     */
    public void startConfiguration() {
        if (Boolean.TRUE.equals(isRunning.getValue())) {
            return;
        }

        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            errorMessage.setValue("Numero allarme non configurato");
            return;
        }

        // Registra listener
        SmsReceiver.setListener(this);

        initializeSteps();
        currentState = Constants.CONFIG_STATE_CONF1;
        isRunning.setValue(true);
        isComplete.setValue(false);
        errorMessage.setValue(null);
        progress.setValue(0);

        // Avvia CONF1
        sendConfigCommand(Constants.CMD_CONF1, 1);
    }

    /**
     * Invia un comando di configurazione alla centralina.
     * <p>
     * Aggiorna lo stato dello step, invia il comando SMS e avvia
     * il timer di timeout per la risposta.
     *
     * @param command Comando SMS da inviare
     * @param stepNumber Numero dello step corrente (1-5)
     */
    private void sendConfigCommand(String command, int stepNumber) {
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            handleError("Numero allarme non configurato");
            return;
        }

        addDebugLog("TX: " + command);
        updateStepStatus(stepNumber, StepStatus.IN_PROGRESS, "Invio richiesta...");

        pendingMessageId = smsService.sendCommand(phone, command);

        if (pendingMessageId != null) {
            startTimeout();
        } else {
            handleError("Errore invio comando");
        }
    }

    /**
     * Processa una risposta SMS ricevuta dalla centralina.
     * <p>
     * Annulla il timeout, salva il log nel database e delega il parsing
     * al metodo appropriato in base allo stato corrente della macchina a stati.
     *
     * @param response Corpo del messaggio SMS ricevuto
     */
    public void processResponse(String response) {
        cancelTimeout();
        addDebugLog("RX: " + response);

        // Salva log
        SmsLog log = new SmsLog();
        log.setMessage(response);
        log.setDirection(SmsLog.DIRECTION_INCOMING);
        log.setStatus(SmsLog.STATUS_RECEIVED);
        log.setTimestamp(System.currentTimeMillis());
        repository.insertSmsLog(log);

        String responseType = SmsParser.identifyResponse(response);
        if (responseType == null) {
            handleError("Risposta non riconosciuta");
            return;
        }

        switch (currentState) {
            case Constants.CONFIG_STATE_CONF1:
                if ("CONF1".equals(responseType)) {
                    processConf1(response);
                }
                break;
            case Constants.CONFIG_STATE_CONF2:
                if ("CONF2".equals(responseType)) {
                    processConf2(response);
                }
                break;
            case Constants.CONFIG_STATE_CONF3:
                if ("CONF3".equals(responseType)) {
                    processConf3(response);
                }
                break;
            case Constants.CONFIG_STATE_CONF4:
                if ("CONF4".equals(responseType)) {
                    processConf4(response);
                }
                break;
            case Constants.CONFIG_STATE_CONF5:
                if ("CONF5".equals(responseType)) {
                    processConf5(response);
                }
                break;
        }
    }

    /**
     * Processa la risposta CONF1 (configurazione base).
     * <p>
     * Estrae e salva la versione firmware e le zone configurate,
     * poi avanza allo step CONF2.
     *
     * @param response Risposta SMS CONF1 da parsare
     */
    private void processConf1(String response) {
        SmsParser.Conf1Data data = SmsParser.parseConf1(response);
        if (data == null) {
            handleError("Errore parsing CONF1");
            return;
        }

        // Salva dati nel repository
        repository.saveZones(data.zones);
        repository.updateConfigVersion(data.version);

        updateStepStatus(1, StepStatus.COMPLETED, "Completato");
        progress.setValue(20);

        // Avanza a CONF2
        currentState = Constants.CONFIG_STATE_CONF2;
        sendConfigCommand(Constants.CMD_CONF2, 2);
    }

    /**
     * Processa la risposta CONF2 (scenari 1-8).
     * <p>
     * Estrae e salva gli scenari, poi avanza allo step CONF3.
     *
     * @param response Risposta SMS CONF2 da parsare
     */
    private void processConf2(String response) {
        List<Scenario> scenarios = SmsParser.parseScenarios(response);
        repository.saveScenarios(scenarios);

        updateStepStatus(2, StepStatus.COMPLETED, "Completato");
        progress.setValue(40);

        // Avanza a CONF3
        currentState = Constants.CONFIG_STATE_CONF3;
        sendConfigCommand(Constants.CMD_CONF3, 3);
    }

    /**
     * Processa la risposta CONF3 (scenari 9-16).
     * <p>
     * Estrae e salva gli scenari, poi avanza allo step CONF4.
     *
     * @param response Risposta SMS CONF3 da parsare
     */
    private void processConf3(String response) {
        List<Scenario> scenarios = SmsParser.parseScenarios(response);
        repository.saveScenarios(scenarios);

        updateStepStatus(3, StepStatus.COMPLETED, "Completato");
        progress.setValue(60);

        // Avanza a CONF4
        currentState = Constants.CONFIG_STATE_CONF4;
        sendConfigCommand(Constants.CMD_CONF4, 4);
    }

    /**
     * Processa la risposta CONF4 (utenti 1-8).
     * <p>
     * Estrae e salva gli utenti, poi avanza allo step CONF5.
     *
     * @param response Risposta SMS CONF4 da parsare
     */
    private void processConf4(String response) {
        List<User> users = SmsParser.parseUsers(response);
        repository.saveUsers(users);

        updateStepStatus(4, StepStatus.COMPLETED, "Completato");
        progress.setValue(80);

        // Avanza a CONF5
        currentState = Constants.CONFIG_STATE_CONF5;
        sendConfigCommand(Constants.CMD_CONF5, 5);
    }

    /**
     * Processa la risposta CONF5 (utenti 9-16).
     * <p>
     * Estrae e salva gli utenti, completa la configurazione e
     * salva lo stato nelle preferences.
     *
     * @param response Risposta SMS CONF5 da parsare
     */
    private void processConf5(String response) {
        List<User> users = SmsParser.parseUsers(response);
        repository.saveUsers(users);

        updateStepStatus(5, StepStatus.COMPLETED, "Completato");
        progress.setValue(100);

        // Configurazione completata
        currentState = Constants.CONFIG_STATE_COMPLETE;
        isRunning.setValue(false);
        isComplete.setValue(true);
        statusMessage.setValue("Configurazione completata!");

        // Salva che la configurazione è stata completata
        prefs.edit().putBoolean(Constants.PREF_CONFIGURED, true).apply();

        addDebugLog("Configurazione completata");
    }

    /**
     * Annulla la configurazione in corso.
     * <p>
     * Ferma il timeout, resetta lo stato della macchina e aggiorna la UI.
     * Non ha effetto se la configurazione non e' in corso.
     */
    public void cancelConfiguration() {
        if (!Boolean.TRUE.equals(isRunning.getValue())) {
            return;
        }

        cancelTimeout();
        currentState = Constants.CONFIG_STATE_IDLE;
        isRunning.setValue(false);
        statusMessage.setValue("Configurazione annullata");
        addDebugLog("Configurazione annullata dall'utente");
    }

    /**
     * Gestisce un errore durante la configurazione.
     * <p>
     * Annulla il timeout, aggiorna lo stato dello step corrente come ERROR
     * e ferma la macchina a stati.
     *
     * @param error Messaggio di errore da visualizzare
     */
    public void handleError(String error) {
        cancelTimeout();

        int stepNum = getStepNumberFromState(currentState);
        currentState = Constants.CONFIG_STATE_ERROR;
        isRunning.setValue(false);
        errorMessage.setValue(error);

        // Marca step corrente come errore
        if (stepNum > 0) {
            updateStepStatus(stepNum, StepStatus.ERROR, error);
        }

        addDebugLog("ERRORE: " + error);
    }

    /**
     * Gestisce il timeout di attesa risposta.
     * <p>
     * Chiamato quando non si riceve risposta dalla centralina entro
     * il tempo limite configurato.
     */
    public void handleTimeout() {
        handleError("Timeout: nessuna risposta ricevuta");
    }

    /**
     * Aggiorna lo stato di uno step di configurazione.
     * <p>
     * Modifica lo stato e il messaggio dello step specificato e forza
     * l'aggiornamento del LiveData per notificare la UI.
     *
     * @param stepNumber Numero dello step da aggiornare (1-5)
     * @param status Nuovo stato dello step
     * @param message Messaggio da associare allo step
     */
    private void updateStepStatus(int stepNumber, StepStatus status, String message) {
        List<ConfigStep> currentSteps = steps.getValue();
        if (currentSteps != null && stepNumber > 0 && stepNumber <= currentSteps.size()) {
            ConfigStep step = currentSteps.get(stepNumber - 1);
            step.status = status;
            step.message = message;
            steps.setValue(new ArrayList<>(currentSteps)); // Forza update

            statusMessage.setValue(step.name + ": " + message);
        }
    }

    /**
     * Converte lo stato della macchina a stati nel numero dello step corrispondente.
     *
     * @param state Stato della macchina a stati (Constants.CONFIG_STATE_*)
     * @return Numero dello step (1-5) o 0 se lo stato non corrisponde a nessuno step
     */
    private int getStepNumberFromState(int state) {
        switch (state) {
            case Constants.CONFIG_STATE_CONF1:
                return 1;
            case Constants.CONFIG_STATE_CONF2:
                return 2;
            case Constants.CONFIG_STATE_CONF3:
                return 3;
            case Constants.CONFIG_STATE_CONF4:
                return 4;
            case Constants.CONFIG_STATE_CONF5:
                return 5;
            default:
                return 0;
        }
    }

    /**
     * Aggiunge un messaggio al log di debug.
     * <p>
     * Il messaggio viene preceduto da un timestamp nel formato HH:mm:ss
     * per facilitare il debugging delle comunicazioni.
     *
     * @param message Messaggio da aggiungere al log
     */
    private void addDebugLog(String message) {
        List<String> log = debugLog.getValue();
        if (log == null) {
            log = new ArrayList<>();
        }
        String timestamp = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
        log.add("[" + timestamp + "] " + message);
        debugLog.setValue(new ArrayList<>(log)); // Forza update
    }

    /**
     * Recupera il numero telefonico dell'allarme dalle preferences.
     *
     * @return Numero telefonico configurato, o null se vuoto o non configurato
     */
    private String getAlarmPhoneNumber() {
        String phone = prefs.getString(Constants.PREF_ALARM_PHONE, "");
        return phone.isEmpty() ? null : phone;
    }

    /**
     * Avvia il timer di timeout per la risposta SMS.
     * <p>
     * Il timeout e' impostato a 60 secondi per permettere alla centralina
     * tempo sufficiente per elaborare e rispondere.
     */
    private void startTimeout() {
        cancelTimeout();
        timeoutRunnable = () -> {
            if (Boolean.TRUE.equals(isRunning.getValue())) {
                handleTimeout();
            }
        };
        // Timeout più lungo per configurazione (60 secondi)
        timeoutHandler.postDelayed(timeoutRunnable, 60000);
    }

    /**
     * Annulla il timer di timeout corrente.
     * <p>
     * Chiamato quando si riceve una risposta o si annulla la configurazione.
     */
    private void cancelTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    // ========== OnSmsResultListener ==========

    /**
     * Callback chiamato quando un SMS e' stato inviato con successo.
     * <p>
     * Aggiorna lo stato dello step corrente per indicare che si e' in
     * attesa della risposta.
     *
     * @param messageId ID del messaggio inviato
     */
    @Override
    public void onSmsSent(String messageId) {
        // SMS inviato, aspettiamo risposta
        int stepNum = getStepNumberFromState(currentState);
        if (stepNum > 0) {
            updateStepStatus(stepNum, StepStatus.IN_PROGRESS, "Attesa risposta...");
        }
    }

    /**
     * Callback chiamato quando un SMS e' stato consegnato al destinatario.
     * <p>
     * Non richiede azioni specifiche, si continua ad attendere la risposta.
     *
     * @param messageId ID del messaggio consegnato
     */
    @Override
    public void onSmsDelivered(String messageId) {
        // SMS consegnato, continuiamo ad aspettare risposta
    }

    /**
     * Callback chiamato quando si riceve un SMS dalla centralina.
     * <p>
     * Delega l'elaborazione al metodo processResponse.
     *
     * @param sender Numero del mittente
     * @param body Corpo del messaggio ricevuto
     */
    @Override
    public void onSmsReceived(String sender, String body) {
        // Processa la risposta
        processResponse(body);
    }

    /**
     * Callback chiamato in caso di errore nell'invio SMS.
     *
     * @param errorCode Codice numerico dell'errore
     * @param errorMsg Messaggio descrittivo dell'errore
     */
    @Override
    public void onSmsError(int errorCode, String errorMsg) {
        handleError(errorMsg);
    }

    /**
     * Callback chiamato quando scade il timeout di attesa risposta.
     */
    @Override
    public void onSmsTimeout() {
        handleTimeout();
    }

    // ========== OnConfigProgressListener ==========

    /**
     * Callback chiamato quando viene avviata la configurazione.
     * <p>
     * Avvia la procedura di configurazione interna.
     */
    @Override
    public void onConfigStarted() {
        startConfiguration();
    }

    /**
     * Callback chiamato per aggiornare il progresso della configurazione.
     *
     * @param currentStep Step corrente
     * @param totalSteps Numero totale di step
     * @param message Messaggio di stato
     */
    @Override
    public void onProgressUpdate(int currentStep, int totalSteps, String message) {
        int progressPercent = (currentStep * 100) / totalSteps;
        progress.setValue(progressPercent);
        statusMessage.setValue(message);
    }

    /**
     * Callback chiamato quando uno step e' completato.
     *
     * @param step Numero dello step completato
     */
    @Override
    public void onStepCompleted(int step) {
        updateStepStatus(step, StepStatus.COMPLETED, "Completato");
    }

    /**
     * Callback chiamato quando la configurazione e' completata.
     */
    @Override
    public void onConfigComplete() {
        currentState = Constants.CONFIG_STATE_COMPLETE;
        isRunning.setValue(false);
        isComplete.setValue(true);
        progress.setValue(100);
    }

    /**
     * Callback chiamato in caso di errore durante la configurazione.
     *
     * @param step Step in cui si e' verificato l'errore
     * @param errorCode Codice dell'errore
     * @param errorMessage Messaggio descrittivo dell'errore
     */
    @Override
    public void onConfigError(int step, String errorCode, String errorMessage) {
        this.errorMessage.setValue(errorMessage);
        updateStepStatus(step, StepStatus.ERROR, errorMessage);
        currentState = Constants.CONFIG_STATE_ERROR;
        isRunning.setValue(false);
    }

    /**
     * Callback chiamato quando la configurazione viene annullata.
     */
    @Override
    public void onConfigCancelled() {
        cancelConfiguration();
    }
}
