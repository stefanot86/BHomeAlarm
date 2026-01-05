package it.bhomealarm.controller.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.bhomealarm.callback.OnSmsResultListener;
import it.bhomealarm.model.entity.AlarmConfig;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.service.SmsReceiver;
import it.bhomealarm.service.SmsService;
import it.bhomealarm.util.Constants;
import it.bhomealarm.util.SmsParser;

/**
 * ViewModel per HomeFragment.
 * Gestisce lo stato dell'allarme e le azioni principali.
 */
public class HomeViewModel extends AndroidViewModel implements OnSmsResultListener {

    private final AlarmRepository repository;
    private final SmsService smsService;
    private final SharedPreferences prefs;
    private final Handler timeoutHandler;

    // Timeout per attesa risposta SMS
    private Runnable timeoutRunnable;
    private String pendingMessageId;

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
        smsService = SmsService.getInstance(application);
        prefs = application.getSharedPreferences(Constants.PREF_NAME, 0);
        timeoutHandler = new Handler(Looper.getMainLooper());

        alarmConfig = repository.getAlarmConfig();
        scenarios = repository.getAllScenarios();

        // Registra listener per risposte SMS
        smsService.setListener(this);
        SmsReceiver.setListener(this);

        // Carica stato iniziale
        loadSavedStatus();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelTimeout();
        smsService.setListener(null);
        SmsReceiver.setListener(null);
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
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            errorMessage.setValue("Numero allarme non configurato");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        String command = String.format(Constants.CMD_ARM_SCENARIO, scenarioId);
        pendingMessageId = smsService.sendCommand(phone, command);

        if (pendingMessageId != null) {
            startTimeout();
        } else {
            isLoading.setValue(false);
            errorMessage.setValue("Errore invio comando");
        }
    }

    /**
     * Richiede attivazione allarme con zone personalizzate.
     *
     * @param zoneNumbers Numeri delle zone da attivare (es. "134" per zone 1,3,4)
     */
    public void armWithCustomZones(String zoneNumbers) {
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            errorMessage.setValue("Numero allarme non configurato");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        String command = String.format(Constants.CMD_ARM_CUSTOM, zoneNumbers);
        pendingMessageId = smsService.sendCommand(phone, command);

        if (pendingMessageId != null) {
            startTimeout();
        } else {
            isLoading.setValue(false);
            errorMessage.setValue("Errore invio comando");
        }
    }

    /**
     * Richiede disattivazione allarme.
     */
    public void disarm() {
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            errorMessage.setValue("Numero allarme non configurato");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        pendingMessageId = smsService.sendCommand(phone, Constants.CMD_DISARM);

        if (pendingMessageId != null) {
            startTimeout();
        } else {
            isLoading.setValue(false);
            errorMessage.setValue("Errore invio comando");
        }
    }

    /**
     * Richiede verifica stato sistema.
     */
    public void checkStatus() {
        String phone = getAlarmPhoneNumber();
        if (phone == null) {
            errorMessage.setValue("Numero allarme non configurato");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        pendingMessageId = smsService.sendCommand(phone, Constants.CMD_STATUS);

        if (pendingMessageId != null) {
            startTimeout();
        } else {
            isLoading.setValue(false);
            errorMessage.setValue("Errore invio comando");
        }
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
        saveStatus(status);
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

    // ========== OnSmsResultListener Implementation ==========

    @Override
    public void onSmsSent(String messageId) {
        // SMS inviato, aspettiamo la risposta
    }

    @Override
    public void onSmsDelivered(String messageId) {
        // SMS consegnato, aspettiamo la risposta
    }

    @Override
    public void onSmsReceived(String sender, String body) {
        cancelTimeout();

        // Il salvataggio nel database è già gestito da SmsReceiver
        // Qui processiamo solo per aggiornare l'UI

        // Parsa la risposta
        String responseType = SmsParser.identifyResponse(body);

        if ("OK".equals(responseType) || "STATUS".equals(responseType)) {
            SmsParser.ResponseData data = SmsParser.parseResponse(body);

            if (data.success) {
                String status = data.status != null ? data.status : Constants.STATUS_UNKNOWN;
                updateStatus(status, data.scenario);
            } else {
                handleError(getErrorDescription(data.errorCode));
            }
        } else if ("ERROR".equals(responseType)) {
            SmsParser.ResponseData data = SmsParser.parseResponse(body);
            handleError(getErrorDescription(data.errorCode));
        } else {
            // Risposta non riconosciuta, potrebbe essere una risposta CONF
            // Non è un errore, ignoriamo silenziosamente
            isLoading.setValue(false);
        }
    }

    @Override
    public void onSmsError(int errorCode, String errorMsg) {
        cancelTimeout();
        isLoading.setValue(false);
        errorMessage.setValue(errorMsg);
    }

    @Override
    public void onSmsTimeout() {
        isLoading.setValue(false);
        errorMessage.setValue("Timeout: nessuna risposta dal sistema");
    }

    // ========== Private Helper Methods ==========

    private String getAlarmPhoneNumber() {
        String phone = prefs.getString(Constants.PREF_ALARM_PHONE, "");
        return phone.isEmpty() ? null : phone;
    }

    private void startTimeout() {
        cancelTimeout();
        timeoutRunnable = () -> {
            if (Boolean.TRUE.equals(isLoading.getValue())) {
                onSmsTimeout();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, Constants.TIMEOUT_SMS_RESPONSE);
    }

    private void cancelTimeout() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    private void loadSavedStatus() {
        String savedStatus = prefs.getString(Constants.PREF_LAST_STATUS, Constants.STATUS_UNKNOWN);
        alarmStatus.setValue(savedStatus);

        long lastCheckTimestamp = prefs.getLong(Constants.PREF_LAST_CHECK_TIME, 0);
        if (lastCheckTimestamp > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            lastCheckTime.setValue(sdf.format(new java.util.Date(lastCheckTimestamp)));
        }

        boolean configured = prefs.getBoolean(Constants.PREF_CONFIGURED, false);
        isConfigured.setValue(configured);
    }

    /**
     * Ricarica lo stato dalle SharedPreferences.
     * Utile quando l'app torna in foreground dopo aver ricevuto SMS in background.
     */
    public void refreshStatus() {
        loadSavedStatus();
        // Assicura che il listener sia registrato
        SmsReceiver.setListener(this);
    }

    private void saveStatus(String status) {
        prefs.edit()
                .putString(Constants.PREF_LAST_STATUS, status)
                .putLong(Constants.PREF_LAST_CHECK_TIME, System.currentTimeMillis())
                .apply();
    }

    private String getCurrentTimeString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date());
    }

    private String getErrorDescription(String errorCode) {
        if (errorCode == null) {
            return "Errore sconosciuto";
        }

        switch (errorCode) {
            case Constants.ERROR_UNKNOWN_CMD:
                return "Comando non riconosciuto";
            case Constants.ERROR_INVALID_PARAM:
                return "Parametro non valido";
            case Constants.ERROR_UNAUTHORIZED:
                return "Non autorizzato";
            case Constants.ERROR_SYSTEM_BUSY:
                return "Sistema occupato";
            case Constants.ERROR_INTERNAL:
                return "Errore interno del sistema";
            case Constants.ERROR_TIMEOUT:
                return "Timeout comunicazione";
            default:
                return "Errore: " + errorCode;
        }
    }
}
