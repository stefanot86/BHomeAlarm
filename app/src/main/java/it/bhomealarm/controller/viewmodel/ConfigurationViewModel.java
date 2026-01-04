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
 * ViewModel per ConfigurationFragment.
 * Gestisce la macchina a stati per configurazione CONF1-5.
 */
public class ConfigurationViewModel extends AndroidViewModel implements OnConfigProgressListener, OnSmsResultListener {

    /**
     * Stato di un singolo step di configurazione.
     */
    public enum StepStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        ERROR
    }

    /**
     * Dati di uno step per UI.
     */
    public static class ConfigStep {
        public final int stepNumber;
        public final String name;
        public StepStatus status;
        public String message;

        public ConfigStep(int stepNumber, String name) {
            this.stepNumber = stepNumber;
            this.name = name;
            this.status = StepStatus.PENDING;
        }
    }

    private final AlarmRepository repository;
    private final SmsService smsService;
    private final SharedPreferences prefs;
    private final Handler timeoutHandler;

    // Timeout per attesa risposta SMS
    private Runnable timeoutRunnable;
    private String pendingMessageId;

    // State Machine
    private int currentState = Constants.CONFIG_STATE_IDLE;

    // UI State
    private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isComplete = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<List<ConfigStep>> steps = new MutableLiveData<>();

    // Debug Log
    private final MutableLiveData<List<String>> debugLog = new MutableLiveData<>(new ArrayList<>());

    public ConfigurationViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        smsService = SmsService.getInstance(application);
        prefs = application.getSharedPreferences(Constants.PREF_NAME, 0);
        timeoutHandler = new Handler(Looper.getMainLooper());
        initializeSteps();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimeout();
        // Rimuovi listener solo se siamo noi
        SmsReceiver.setListener(null);
    }

    // ========== Getters ==========

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public LiveData<Boolean> getIsRunning() {
        return isRunning;
    }

    public LiveData<Boolean> getIsComplete() {
        return isComplete;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<List<ConfigStep>> getSteps() {
        return steps;
    }

    public LiveData<List<String>> getDebugLog() {
        return debugLog;
    }

    public int getCurrentState() {
        return currentState;
    }

    // ========== Actions ==========

    /**
     * Inizializza la lista degli step.
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
     * Avvia la configurazione.
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
     * Invia un comando di configurazione.
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
     * Processa una risposta SMS ricevuta.
     *
     * @param response Corpo del messaggio SMS
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

    private void processConf2(String response) {
        List<Scenario> scenarios = SmsParser.parseScenarios(response);
        repository.saveScenarios(scenarios);

        updateStepStatus(2, StepStatus.COMPLETED, "Completato");
        progress.setValue(40);

        // Avanza a CONF3
        currentState = Constants.CONFIG_STATE_CONF3;
        sendConfigCommand(Constants.CMD_CONF3, 3);
    }

    private void processConf3(String response) {
        List<Scenario> scenarios = SmsParser.parseScenarios(response);
        repository.saveScenarios(scenarios);

        updateStepStatus(3, StepStatus.COMPLETED, "Completato");
        progress.setValue(60);

        // Avanza a CONF4
        currentState = Constants.CONFIG_STATE_CONF4;
        sendConfigCommand(Constants.CMD_CONF4, 4);
    }

    private void processConf4(String response) {
        List<User> users = SmsParser.parseUsers(response);
        repository.saveUsers(users);

        updateStepStatus(4, StepStatus.COMPLETED, "Completato");
        progress.setValue(80);

        // Avanza a CONF5
        currentState = Constants.CONFIG_STATE_CONF5;
        sendConfigCommand(Constants.CMD_CONF5, 5);
    }

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
     * Gestisce errore durante configurazione.
     *
     * @param error Messaggio errore
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
     * Gestisce timeout risposta.
     */
    public void handleTimeout() {
        handleError("Timeout: nessuna risposta ricevuta");
    }

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

    private String getAlarmPhoneNumber() {
        String phone = prefs.getString(Constants.PREF_ALARM_PHONE, "");
        return phone.isEmpty() ? null : phone;
    }

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

    private void cancelTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    // ========== OnSmsResultListener ==========

    @Override
    public void onSmsSent(String messageId) {
        // SMS inviato, aspettiamo risposta
        int stepNum = getStepNumberFromState(currentState);
        if (stepNum > 0) {
            updateStepStatus(stepNum, StepStatus.IN_PROGRESS, "Attesa risposta...");
        }
    }

    @Override
    public void onSmsDelivered(String messageId) {
        // SMS consegnato, continuiamo ad aspettare risposta
    }

    @Override
    public void onSmsReceived(String sender, String body) {
        // Processa la risposta
        processResponse(body);
    }

    @Override
    public void onSmsError(int errorCode, String errorMsg) {
        handleError(errorMsg);
    }

    @Override
    public void onSmsTimeout() {
        handleTimeout();
    }

    // ========== OnConfigProgressListener ==========

    @Override
    public void onConfigStarted() {
        startConfiguration();
    }

    @Override
    public void onProgressUpdate(int currentStep, int totalSteps, String message) {
        int progressPercent = (currentStep * 100) / totalSteps;
        progress.setValue(progressPercent);
        statusMessage.setValue(message);
    }

    @Override
    public void onStepCompleted(int step) {
        updateStepStatus(step, StepStatus.COMPLETED, "Completato");
    }

    @Override
    public void onConfigComplete() {
        currentState = Constants.CONFIG_STATE_COMPLETE;
        isRunning.setValue(false);
        isComplete.setValue(true);
        progress.setValue(100);
    }

    @Override
    public void onConfigError(int step, String errorCode, String errorMessage) {
        this.errorMessage.setValue(errorMessage);
        updateStepStatus(step, StepStatus.ERROR, errorMessage);
        currentState = Constants.CONFIG_STATE_ERROR;
        isRunning.setValue(false);
    }

    @Override
    public void onConfigCancelled() {
        cancelConfiguration();
    }
}
