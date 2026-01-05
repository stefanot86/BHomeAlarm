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
 * ViewModel per la gestione delle impostazioni dell'applicazione.
 * <p>
 * Questa classe gestisce tutte le impostazioni dell'app e la configurazione del sistema
 * di allarme, inclusi:
 * <ul>
 *     <li>Numero telefonico della centralina di allarme</li>
 *     <li>Selezione della SIM da utilizzare per le comunicazioni</li>
 *     <li>Stato di configurazione del sistema</li>
 *     <li>Versione firmware della centralina</li>
 * </ul>
 * <p>
 * Le impostazioni vengono persistite tramite {@link SharedPreferences} e sono
 * esposte alla UI tramite {@link LiveData} per garantire aggiornamenti reattivi.
 *
 * @see it.bhomealarm.view.fragment.SettingsFragment
 * @see AlarmRepository
 */
public class SettingsViewModel extends AndroidViewModel {

    /** Repository per l'accesso ai dati dell'allarme */
    private final AlarmRepository repository;

    /** SharedPreferences per la persistenza delle impostazioni */
    private final SharedPreferences prefs;

    // ========== UI State ==========

    /** Numero telefonico della centralina di allarme */
    private final MutableLiveData<String> alarmPhoneNumber = new MutableLiveData<>();

    /** Slot SIM selezionato per le comunicazioni (0 = SIM1, 1 = SIM2) */
    private final MutableLiveData<Integer> selectedSimSlot = new MutableLiveData<>(0);

    /** Flag che indica se il sistema e' stato configurato correttamente */
    private final MutableLiveData<Boolean> isConfigured = new MutableLiveData<>(false);

    /** Versione firmware della centralina di allarme */
    private final MutableLiveData<String> firmwareVersion = new MutableLiveData<>();

    // ========== Data ==========

    /** Configurazione completa dell'allarme dal database */
    private final LiveData<AlarmConfig> alarmConfig;

    /**
     * Costruttore del ViewModel.
     * <p>
     * Inizializza il repository, le SharedPreferences e carica le impostazioni salvate.
     *
     * @param application Contesto dell'applicazione Android
     */
    public SettingsViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        prefs = application.getSharedPreferences(Constants.PREF_NAME, 0);

        alarmConfig = repository.getAlarmConfig();

        loadSettings();
    }

    // ========== Getters ==========

    /**
     * Restituisce il LiveData contenente il numero telefonico dell'allarme.
     *
     * @return LiveData con il numero telefonico della centralina
     */
    public LiveData<String> getAlarmPhoneNumber() {
        return alarmPhoneNumber;
    }

    /**
     * Restituisce il LiveData contenente lo slot SIM selezionato.
     *
     * @return LiveData con l'indice dello slot SIM (0 o 1)
     */
    public LiveData<Integer> getSelectedSimSlot() {
        return selectedSimSlot;
    }

    /**
     * Restituisce il LiveData che indica se il sistema e' configurato.
     *
     * @return LiveData con true se il sistema e' configurato, false altrimenti
     */
    public LiveData<Boolean> getIsConfigured() {
        return isConfigured;
    }

    /**
     * Restituisce il LiveData contenente la versione firmware.
     *
     * @return LiveData con la stringa della versione firmware
     */
    public LiveData<String> getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * Restituisce il LiveData contenente la configurazione completa dell'allarme.
     *
     * @return LiveData con l'oggetto AlarmConfig dal database
     */
    public LiveData<AlarmConfig> getAlarmConfig() {
        return alarmConfig;
    }

    // ========== Actions ==========

    /**
     * Carica le impostazioni salvate dalle SharedPreferences.
     * <p>
     * Questo metodo viene chiamato automaticamente all'inizializzazione
     * del ViewModel per ripristinare lo stato precedente dell'applicazione.
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
     * Salva il numero telefonico della centralina di allarme.
     * <p>
     * Il numero viene salvato nelle SharedPreferences e il LiveData viene
     * aggiornato per notificare gli observer.
     *
     * @param phoneNumber Numero telefonico da salvare (formato internazionale consigliato)
     */
    public void saveAlarmPhoneNumber(String phoneNumber) {
        prefs.edit()
                .putString(Constants.PREF_ALARM_PHONE, phoneNumber)
                .apply();
        alarmPhoneNumber.setValue(phoneNumber);
    }

    /**
     * Salva lo slot SIM selezionato per le comunicazioni.
     * <p>
     * Nei dispositivi dual-SIM, permette di scegliere quale SIM utilizzare
     * per inviare i comandi SMS alla centralina.
     *
     * @param simSlot Indice dello slot SIM (0 per SIM1, 1 per SIM2)
     */
    public void saveSelectedSim(int simSlot) {
        prefs.edit()
                .putInt(Constants.PREF_SELECTED_SIM, simSlot)
                .apply();
        selectedSimSlot.setValue(simSlot);
    }

    /**
     * Imposta lo stato di configurazione del sistema.
     * <p>
     * Questo flag indica se la procedura di configurazione iniziale
     * (CONF1-5) e' stata completata con successo.
     *
     * @param configured true se il sistema e' configurato correttamente, false altrimenti
     */
    public void setConfigured(boolean configured) {
        prefs.edit()
                .putBoolean(Constants.PREF_CONFIGURED, configured)
                .apply();
        isConfigured.setValue(configured);
    }

    /**
     * Aggiorna la versione firmware visualizzata.
     * <p>
     * La versione viene tipicamente estratta dalla risposta CONF1
     * durante la procedura di configurazione.
     *
     * @param version Stringa contenente la versione firmware della centralina
     */
    public void updateFirmwareVersion(String version) {
        firmwareVersion.setValue(version);
    }

    /**
     * Esegue un reset completo di tutte le impostazioni e i dati.
     * <p>
     * Questa operazione:
     * <ul>
     *     <li>Cancella tutte le SharedPreferences</li>
     *     <li>Elimina tutti i dati dal database locale</li>
     *     <li>Ricarica le impostazioni (che risulteranno vuote)</li>
     * </ul>
     * <p>
     * <b>Attenzione:</b> Questa operazione e' irreversibile.
     */
    public void resetAll() {
        prefs.edit().clear().apply();
        repository.clearAllData();
        loadSettings();
    }

    /**
     * Verifica se e' stato configurato un numero telefonico per l'allarme.
     * <p>
     * Utile per validare che l'applicazione sia pronta per comunicare
     * con la centralina prima di tentare operazioni SMS.
     *
     * @return true se il numero telefonico e' configurato e non vuoto, false altrimenti
     */
    public boolean hasAlarmPhoneNumber() {
        String phone = alarmPhoneNumber.getValue();
        return phone != null && !phone.isEmpty();
    }
}
