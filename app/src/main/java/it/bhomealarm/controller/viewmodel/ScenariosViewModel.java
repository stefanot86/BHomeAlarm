package it.bhomealarm.controller.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.model.repository.AlarmRepository;

/**
 * ViewModel per la gestione della lista scenari.
 * <p>
 * Questo ViewModel fornisce:
 * <ul>
 *     <li>La lista di tutti gli scenari (predefiniti + personalizzati)</li>
 *     <li>Lo stato di selezione dello scenario corrente</li>
 *     <li>Azioni per selezionare, deselezionare ed eliminare scenari</li>
 * </ul>
 * <p>
 * Gli scenari predefiniti (slot 1-16) vengono scaricati dall'allarme tramite
 * le risposte CONF2 e CONF3. Gli scenari personalizzati (slot > 100) vengono
 * creati localmente dall'utente.
 *
 * @see it.bhomealarm.view.fragment.ScenariosFragment
 * @see Scenario
 */
public class ScenariosViewModel extends AndroidViewModel {

    /** Repository per l'accesso ai dati persistenti */
    private final AlarmRepository repository;

    /** Lista di tutti gli scenari dal database */
    private final LiveData<List<Scenario>> scenarios;

    /** Scenario attualmente selezionato dall'utente */
    private final MutableLiveData<Scenario> selectedScenario = new MutableLiveData<>();

    /** Flag per mostrare/nascondere l'opzione scenario personalizzato */
    private final MutableLiveData<Boolean> showCustomOption = new MutableLiveData<>(true);

    /**
     * Costruttore del ViewModel.
     * Inizializza il repository e carica la lista degli scenari.
     *
     * @param application Contesto dell'applicazione Android
     */
    public ScenariosViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        scenarios = repository.getAllScenarios();
    }

    // ========== Getters ==========

    /**
     * Restituisce la lista di tutti gli scenari.
     * Include sia scenari predefiniti che personalizzati, ordinati per slot.
     *
     * @return LiveData con la lista degli scenari
     */
    public LiveData<List<Scenario>> getScenarios() {
        return scenarios;
    }

    /**
     * Restituisce lo scenario attualmente selezionato.
     *
     * @return LiveData con lo scenario selezionato, null se nessuna selezione
     */
    public LiveData<Scenario> getSelectedScenario() {
        return selectedScenario;
    }

    /**
     * Indica se mostrare l'opzione per creare scenari personalizzati.
     *
     * @return LiveData con true se l'opzione deve essere visibile
     */
    public LiveData<Boolean> getShowCustomOption() {
        return showCustomOption;
    }

    // ========== Actions ==========

    /**
     * Imposta lo scenario selezionato.
     * Chiamato quando l'utente tocca uno scenario nella lista.
     *
     * @param scenario Lo scenario selezionato
     */
    public void selectScenario(Scenario scenario) {
        selectedScenario.setValue(scenario);
    }

    /**
     * Rimuove la selezione corrente.
     * Utile per resettare lo stato dopo un'operazione.
     */
    public void clearSelection() {
        selectedScenario.setValue(null);
    }

    /**
     * Restituisce solo gli scenari abilitati.
     * <p>
     * Nota: attualmente restituisce tutti gli scenari.
     * Il filtro può essere implementato con Transformations.map().
     *
     * @return LiveData con gli scenari abilitati
     */
    public LiveData<List<Scenario>> getEnabledScenarios() {
        // TODO: Implementare filtro con Transformations.map()
        return scenarios;
    }

    /**
     * Conta il numero di scenari abilitati.
     * <p>
     * Utile per mostrare statistiche o verificare se ci sono
     * scenari disponibili per l'attivazione.
     *
     * @return Numero di scenari con enabled = true, 0 se la lista non è caricata
     */
    public int getEnabledCount() {
        List<Scenario> list = scenarios.getValue();
        if (list == null) return 0;

        int count = 0;
        for (Scenario s : list) {
            if (s.isEnabled()) count++;
        }
        return count;
    }

    /**
     * Elimina uno scenario dal database.
     * <p>
     * Funziona sia per scenari predefiniti che personalizzati.
     * Gli scenari predefiniti eliminati possono essere ripristinati
     * eseguendo nuovamente la configurazione CONF2/CONF3.
     * <p>
     * L'operazione è asincrona e viene eseguita in background.
     *
     * @param scenario Lo scenario da eliminare (non null)
     */
    public void deleteScenario(Scenario scenario) {
        if (scenario != null) {
            repository.deleteCustomScenario(scenario.getSlot());
        }
    }
}
