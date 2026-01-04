package it.bhomealarm.controller.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.bhomealarm.model.entity.AlarmConfig;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;

/**
 * ViewModel per HomeFragment.
 * Gestisce lo stato dell'allarme e le azioni principali.
 */
public class HomeViewModel extends AndroidViewModel {

    private final AlarmRepository repository;

    // UI State
    private final MutableLiveData<String> alarmStatus = new MutableLiveData<>(Constants.STATUS_UNKNOWN);
    private final MutableLiveData<String> lastCheckTime = new MutableLiveData<>();
    private final MutableLiveData<String> activeScenario = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isConfigured = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Data
    private final LiveData<AlarmConfig> alarmConfig;
    private final LiveData<List<Scenario>> scenarios;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);

        alarmConfig = repository.getAlarmConfig();
        scenarios = repository.getAllScenarios();
    }

    // ========== Getters for LiveData ==========

    public LiveData<String> getAlarmStatus() {
        return alarmStatus;
    }

    public LiveData<String> getLastCheckTime() {
        return lastCheckTime;
    }

    public LiveData<String> getActiveScenario() {
        return activeScenario;
    }

    public LiveData<Boolean> getIsConfigured() {
        return isConfigured;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<AlarmConfig> getAlarmConfig() {
        return alarmConfig;
    }

    public LiveData<List<Scenario>> getScenarios() {
        return scenarios;
    }

    // ========== Actions ==========

    /**
     * Richiede attivazione allarme con scenario specifico.
     *
     * @param scenarioId ID scenario (1-16)
     */
    public void armWithScenario(int scenarioId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // TODO: Implementare invio SMS tramite SmsService
        // String command = String.format(Constants.CMD_ARM_SCENARIO, scenarioId);
        // smsService.sendCommand(command, callback);
    }

    /**
     * Richiede attivazione allarme con zone personalizzate.
     *
     * @param zoneMask Maschera zone (8 caratteri 0/1)
     */
    public void armWithCustomZones(String zoneMask) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // TODO: Implementare invio SMS tramite SmsService
        // String command = String.format(Constants.CMD_ARM_CUSTOM, zoneMask);
    }

    /**
     * Richiede disattivazione allarme.
     */
    public void disarm() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // TODO: Implementare invio SMS tramite SmsService
        // smsService.sendCommand(Constants.CMD_DISARM, callback);
    }

    /**
     * Richiede verifica stato sistema.
     */
    public void checkStatus() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // TODO: Implementare invio SMS tramite SmsService
        // smsService.sendCommand(Constants.CMD_STATUS, callback);
    }

    /**
     * Aggiorna lo stato dell'allarme da risposta SMS.
     *
     * @param status Nuovo stato
     * @param scenario Scenario attivo (opzionale)
     */
    public void updateStatus(String status, String scenario) {
        isLoading.setValue(false);
        alarmStatus.setValue(status);
        activeScenario.setValue(scenario);
        lastCheckTime.setValue(getCurrentTimeString());
    }

    /**
     * Gestisce errore durante operazione.
     *
     * @param error Messaggio errore
     */
    public void handleError(String error) {
        isLoading.setValue(false);
        errorMessage.setValue(error);
    }

    /**
     * Pulisce il messaggio di errore.
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    private String getCurrentTimeString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }
}
