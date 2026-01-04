package it.bhomealarm.controller.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import it.bhomealarm.callback.OnConfigProgressListener;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.model.entity.Zone;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;
import it.bhomealarm.util.SmsParser;

/**
 * ViewModel per ConfigurationFragment.
 * Gestisce la macchina a stati per configurazione CONF1-5.
 */
public class ConfigurationViewModel extends AndroidViewModel implements OnConfigProgressListener {

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
        initializeSteps();
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

        initializeSteps();
        currentState = Constants.CONFIG_STATE_CONF1;
        isRunning.setValue(true);
        isComplete.setValue(false);
        errorMessage.setValue(null);
        progress.setValue(0);

        addDebugLog("TX: " + Constants.CMD_CONF1);
        updateStepStatus(1, StepStatus.IN_PROGRESS, "Invio richiesta...");

        // TODO: Inviare SMS CONF1? tramite SmsService
        // smsService.sendCommand(Constants.CMD_CONF1, callback);
    }

    /**
     * Processa una risposta SMS ricevuta.
     *
     * @param response Corpo del messaggio SMS
     */
    public void processResponse(String response) {
        addDebugLog("RX: " + response);

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
        addDebugLog("TX: " + Constants.CMD_CONF2);
        updateStepStatus(2, StepStatus.IN_PROGRESS, "Invio richiesta...");

        // TODO: Inviare SMS CONF2?
    }

    private void processConf2(String response) {
        List<Scenario> scenarios = SmsParser.parseScenarios(response);
        repository.saveScenarios(scenarios);

        updateStepStatus(2, StepStatus.COMPLETED, "Completato");
        progress.setValue(40);

        // Avanza a CONF3
        currentState = Constants.CONFIG_STATE_CONF3;
        addDebugLog("TX: " + Constants.CMD_CONF3);
        updateStepStatus(3, StepStatus.IN_PROGRESS, "Invio richiesta...");

        // TODO: Inviare SMS CONF3?
    }

    private void processConf3(String response) {
        List<Scenario> scenarios = SmsParser.parseScenarios(response);
        repository.saveScenarios(scenarios);

        updateStepStatus(3, StepStatus.COMPLETED, "Completato");
        progress.setValue(60);

        // Avanza a CONF4
        currentState = Constants.CONFIG_STATE_CONF4;
        addDebugLog("TX: " + Constants.CMD_CONF4);
        updateStepStatus(4, StepStatus.IN_PROGRESS, "Invio richiesta...");

        // TODO: Inviare SMS CONF4?
    }

    private void processConf4(String response) {
        List<User> users = SmsParser.parseUsers(response);
        repository.saveUsers(users);

        updateStepStatus(4, StepStatus.COMPLETED, "Completato");
        progress.setValue(80);

        // Avanza a CONF5
        currentState = Constants.CONFIG_STATE_CONF5;
        addDebugLog("TX: " + Constants.CMD_CONF5);
        updateStepStatus(5, StepStatus.IN_PROGRESS, "Invio richiesta...");

        // TODO: Inviare SMS CONF5?
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

        addDebugLog("Configurazione completata");
    }

    /**
     * Annulla la configurazione in corso.
     */
    public void cancelConfiguration() {
        if (!Boolean.TRUE.equals(isRunning.getValue())) {
            return;
        }

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
        currentState = Constants.CONFIG_STATE_ERROR;
        isRunning.setValue(false);
        errorMessage.setValue(error);

        // Marca step corrente come errore
        int stepNum = getStepNumberFromState(currentState);
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
            steps.setValue(currentSteps);

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
        debugLog.setValue(log);
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
