package it.bhomealarm.controller.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.bhomealarm.model.entity.Zone;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;

/**
 * ViewModel per la gestione delle zone nell'app BHomeAlarm.
 * <p>
 * Questa classe gestisce:
 * <ul>
 *     <li>La selezione delle zone per scenari personalizzati</li>
 *     <li>La generazione della stringa di numeri zone per il comando CUST:</li>
 *     <li>Il salvataggio di scenari personalizzati nel database</li>
 * </ul>
 * <p>
 * Utilizzato da {@link it.bhomealarm.view.fragment.ZonesFragment} per la schermata
 * di creazione scenario personalizzato.
 *
 * @see it.bhomealarm.view.fragment.ZonesFragment
 * @see it.bhomealarm.model.entity.Zone
 */
public class ZonesViewModel extends AndroidViewModel {

    /** Repository per l'accesso ai dati persistenti */
    private final AlarmRepository repository;

    /** Lista delle zone disponibili dal database */
    private final LiveData<List<Zone>> zones;

    /** Set delle zone attualmente selezionate (numeri zona 1-8) */
    private final MutableLiveData<Set<Integer>> selectedZones = new MutableLiveData<>(new HashSet<>());

    /** Stringa con i numeri delle zone selezionate per il comando CUST: (es. "134") */
    private final MutableLiveData<String> zoneNumbers = new MutableLiveData<>("");

    /** Conteggio delle zone selezionate */
    private final MutableLiveData<Integer> selectedCount = new MutableLiveData<>(0);

    /** Nome dello scenario personalizzato inserito dall'utente */
    private final MutableLiveData<String> scenarioName = new MutableLiveData<>("");

    /** Flag che indica se è in corso il salvataggio dello scenario */
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);

    /**
     * Costruttore del ViewModel.
     * Inizializza il repository e carica la lista delle zone.
     *
     * @param application Contesto dell'applicazione Android
     */
    public ZonesViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        zones = repository.getAllZones();
    }

    // ========== Getters ==========

    /**
     * Restituisce la lista delle zone configurate.
     *
     * @return LiveData con la lista delle zone dal database
     */
    public LiveData<List<Zone>> getZones() {
        return zones;
    }

    /**
     * Restituisce il set delle zone selezionate.
     *
     * @return LiveData con i numeri delle zone selezionate (1-8)
     */
    public LiveData<Set<Integer>> getSelectedZones() {
        return selectedZones;
    }

    /**
     * Restituisce la stringa dei numeri zone per il comando CUST.
     * Esempio: "134" se sono selezionate le zone 1, 3 e 4.
     *
     * @return LiveData con la stringa dei numeri zone concatenati
     */
    public LiveData<String> getZoneNumbers() {
        return zoneNumbers;
    }

    /**
     * Restituisce il conteggio delle zone selezionate.
     *
     * @return LiveData con il numero di zone selezionate
     */
    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
    }

    /**
     * Restituisce il nome dello scenario personalizzato.
     *
     * @return LiveData con il nome inserito dall'utente
     */
    public LiveData<String> getScenarioName() {
        return scenarioName;
    }

    /**
     * Indica se è in corso il salvataggio dello scenario.
     *
     * @return LiveData con true se il salvataggio è in corso
     */
    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

    /**
     * Imposta il nome dello scenario personalizzato.
     * Chiamato dal TextWatcher del campo di input nome.
     *
     * @param name Nome dello scenario inserito dall'utente
     */
    public void setScenarioName(String name) {
        scenarioName.setValue(name);
    }

    // ========== Actions ==========

    /**
     * Seleziona/deseleziona una zona.
     *
     * @param zoneNumber Numero zona (1-8)
     */
    public void toggleZone(int zoneNumber) {
        if (zoneNumber < 1 || zoneNumber > Constants.ZONE_COUNT) {
            return;
        }

        Set<Integer> current = selectedZones.getValue();
        if (current == null) {
            current = new HashSet<>();
        }

        Set<Integer> updated = new HashSet<>(current);
        if (updated.contains(zoneNumber)) {
            updated.remove(zoneNumber);
        } else {
            updated.add(zoneNumber);
        }

        selectedZones.setValue(updated);
        updateMask(updated);
    }

    /**
     * Imposta la selezione di una zona.
     *
     * @param zoneNumber Numero zona (1-8)
     * @param selected true per selezionare
     */
    public void setZoneSelected(int zoneNumber, boolean selected) {
        if (zoneNumber < 1 || zoneNumber > Constants.ZONE_COUNT) {
            return;
        }

        Set<Integer> current = selectedZones.getValue();
        if (current == null) {
            current = new HashSet<>();
        }

        Set<Integer> updated = new HashSet<>(current);
        if (selected) {
            updated.add(zoneNumber);
        } else {
            updated.remove(zoneNumber);
        }

        selectedZones.setValue(updated);
        updateMask(updated);
    }

    /**
     * Verifica se una zona è selezionata.
     *
     * @param zoneNumber Numero zona (1-8)
     * @return true se selezionata
     */
    public boolean isZoneSelected(int zoneNumber) {
        Set<Integer> current = selectedZones.getValue();
        return current != null && current.contains(zoneNumber);
    }

    /**
     * Seleziona tutte le zone abilitate.
     */
    public void selectAll() {
        List<Zone> allZones = zones.getValue();
        if (allZones == null) return;

        Set<Integer> updated = new HashSet<>();
        for (Zone zone : allZones) {
            if (zone.isEnabled()) {
                updated.add(zone.getSlot());
            }
        }

        selectedZones.setValue(updated);
        updateMask(updated);
    }

    /**
     * Deseleziona tutte le zone.
     */
    public void clearSelection() {
        selectedZones.setValue(new HashSet<>());
        updateMask(new HashSet<>());
    }

    /**
     * Restituisce i numeri delle zone selezionate come stringa.
     * <p>
     * Questa stringa viene utilizzata per comporre il comando SMS CUST:
     * che attiva le zone specificate sull'allarme.
     * <p>
     * Esempio: se sono selezionate le zone 1, 3 e 4, restituisce "134".
     *
     * @return Stringa con i numeri delle zone concatenati, vuota se nessuna selezione
     */
    public String getZoneNumbersString() {
        String numbers = zoneNumbers.getValue();
        return numbers != null ? numbers : "";
    }

    /**
     * Verifica se l'utente ha selezionato almeno una zona.
     * Necessario per la validazione prima del salvataggio.
     *
     * @return true se almeno una zona è selezionata, false altrimenti
     */
    public boolean hasSelection() {
        Integer count = selectedCount.getValue();
        return count != null && count > 0;
    }

    /**
     * Calcola il bitmask delle zone selezionate.
     * <p>
     * Il bitmask viene utilizzato per salvare la configurazione dello scenario
     * nel database. Ogni bit rappresenta una zona:
     * <ul>
     *     <li>Bit 0 (valore 1) = Zona 1</li>
     *     <li>Bit 1 (valore 2) = Zona 2</li>
     *     <li>Bit 7 (valore 128) = Zona 8</li>
     * </ul>
     * <p>
     * Esempio: zone 1, 3 e 4 selezionate = 0b00001101 = 13
     *
     * @return Intero bitmask con le zone selezionate
     */
    public int getZoneMaskInt() {
        Set<Integer> selected = selectedZones.getValue();
        if (selected == null || selected.isEmpty()) {
            return 0;
        }
        int mask = 0;
        for (int zone : selected) {
            mask |= (1 << (zone - 1));
        }
        return mask;
    }

    /**
     * Salva lo scenario personalizzato nel database locale.
     * <p>
     * Lo scenario viene salvato con:
     * <ul>
     *     <li>Slot > 100 per distinguerlo dagli scenari predefiniti (1-16)</li>
     *     <li>Nome inserito dall'utente</li>
     *     <li>Bitmask delle zone selezionate</li>
     *     <li>Flag isCustom = true</li>
     * </ul>
     * <p>
     * Il salvataggio avviene in background. Al termine viene invocato il callback
     * con lo scenario creato.
     *
     * @param callback Listener chiamato al completamento del salvataggio
     */
    public void saveCustomScenario(OnScenarioSavedListener callback) {
        String name = scenarioName.getValue();
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        if (!hasSelection()) {
            return;
        }

        isSaving.setValue(true);
        int zoneMask = getZoneMaskInt();

        repository.saveCustomScenario(name.trim(), zoneMask, scenario -> {
            isSaving.postValue(false);
            if (callback != null) {
                callback.onSaved(scenario);
            }
        });
    }

    /**
     * Interfaccia per il callback di salvataggio scenario.
     */
    public interface OnScenarioSavedListener {
        /**
         * Chiamato quando lo scenario è stato salvato con successo.
         *
         * @param scenario Lo scenario appena creato
         */
        void onSaved(it.bhomealarm.model.entity.Scenario scenario);
    }

    /**
     * Aggiorna la stringa dei numeri zone e il conteggio.
     * <p>
     * Genera una stringa con i numeri delle zone selezionate in ordine crescente.
     * Questa stringa viene poi utilizzata per il comando CUST: dell'allarme.
     * <p>
     * Esempio: se selected contiene {1, 3, 4}, genera "134".
     *
     * @param selected Set dei numeri delle zone selezionate
     */
    private void updateMask(Set<Integer> selected) {
        StringBuilder numbers = new StringBuilder();
        for (int i = 1; i <= Constants.ZONE_COUNT; i++) {
            if (selected.contains(i)) {
                numbers.append(i);
            }
        }

        zoneNumbers.setValue(numbers.toString());
        selectedCount.setValue(selected.size());
    }
}
