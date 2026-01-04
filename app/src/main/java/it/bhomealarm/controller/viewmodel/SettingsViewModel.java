package it.bhomealarm.controller.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import it.bhomealarm.model.entity.AlarmConfig;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;

/**
 * ViewModel per SettingsFragment.
 * Gestisce impostazioni app e configurazione sistema.
 */
public class SettingsViewModel extends AndroidViewModel {

    private final AlarmRepository repository;
    private final SharedPreferences prefs;

    // UI State
    private final MutableLiveData<String> alarmPhoneNumber = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedSimSlot = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isConfigured = new MutableLiveData<>(false);
    private final MutableLiveData<String> firmwareVersion = new MutableLiveData<>();

    // Data
    private final LiveData<AlarmConfig> alarmConfig;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        prefs = application.getSharedPreferences(Constants.PREF_NAME, 0);

        alarmConfig = repository.getAlarmConfig();

        loadSettings();
    }

    // ========== Getters ==========

    public LiveData<String> getAlarmPhoneNumber() {
        return alarmPhoneNumber;
    }

    public LiveData<Integer> getSelectedSimSlot() {
        return selectedSimSlot;
    }

    public LiveData<Boolean> getIsConfigured() {
        return isConfigured;
    }

    public LiveData<String> getFirmwareVersion() {
        return firmwareVersion;
    }

    public LiveData<AlarmConfig> getAlarmConfig() {
        return alarmConfig;
    }

    // ========== Actions ==========

    /**
     * Carica impostazioni da SharedPreferences.
     */
    private void loadSettings() {
        String phone = prefs.getString(Constants.PREF_ALARM_PHONE, "");
        int sim = prefs.getInt(Constants.PREF_SELECTED_SIM, 0);
        boolean configured = prefs.getBoolean(Constants.PREF_CONFIGURED, false);

        alarmPhoneNumber.setValue(phone);
        selectedSimSlot.setValue(sim);
        isConfigured.setValue(configured);
    }

    /**
     * Salva numero telefonico allarme.
     *
     * @param phoneNumber Numero da salvare
     */
    public void saveAlarmPhoneNumber(String phoneNumber) {
        prefs.edit()
                .putString(Constants.PREF_ALARM_PHONE, phoneNumber)
                .apply();
        alarmPhoneNumber.setValue(phoneNumber);
    }

    /**
     * Salva SIM selezionata.
     *
     * @param simSlot Slot SIM (0 o 1)
     */
    public void saveSelectedSim(int simSlot) {
        prefs.edit()
                .putInt(Constants.PREF_SELECTED_SIM, simSlot)
                .apply();
        selectedSimSlot.setValue(simSlot);
    }

    /**
     * Segna il sistema come configurato.
     *
     * @param configured true se configurato
     */
    public void setConfigured(boolean configured) {
        prefs.edit()
                .putBoolean(Constants.PREF_CONFIGURED, configured)
                .apply();
        isConfigured.setValue(configured);
    }

    /**
     * Aggiorna versione firmware dalla configurazione.
     *
     * @param version Versione firmware
     */
    public void updateFirmwareVersion(String version) {
        firmwareVersion.setValue(version);
    }

    /**
     * Resetta tutte le impostazioni e configurazione.
     */
    public void resetAll() {
        prefs.edit().clear().apply();
        repository.clearAllData();
        loadSettings();
    }

    /**
     * Verifica se il numero allarme è configurato.
     *
     * @return true se configurato
     */
    public boolean hasAlarmPhoneNumber() {
        String phone = alarmPhoneNumber.getValue();
        return phone != null && !phone.isEmpty();
    }
}
