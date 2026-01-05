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
 * ViewModel per ZonesFragment.
 * Gestisce selezione zone per scenario personalizzato.
 */
public class ZonesViewModel extends AndroidViewModel {

    private final AlarmRepository repository;

    // Data
    private final LiveData<List<Zone>> zones;

    // Selection State
    private final MutableLiveData<Set<Integer>> selectedZones = new MutableLiveData<>(new HashSet<>());

    // UI State
    private final MutableLiveData<String> zoneNumbers = new MutableLiveData<>("");
    private final MutableLiveData<Integer> selectedCount = new MutableLiveData<>(0);
    private final MutableLiveData<String> scenarioName = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);

    public ZonesViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        zones = repository.getAllZones();
    }

    // ========== Getters ==========

    public LiveData<List<Zone>> getZones() {
        return zones;
    }

    public LiveData<Set<Integer>> getSelectedZones() {
        return selectedZones;
    }

    public LiveData<String> getZoneNumbers() {
        return zoneNumbers;
    }

    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
    }

    public LiveData<String> getScenarioName() {
        return scenarioName;
    }

    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

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
     * Restituisce i numeri delle zone per il comando CUST.
     *
     * @return Stringa con i numeri delle zone selezionate (es. "134" per zone 1,3,4)
     */
    public String getZoneNumbersString() {
        String numbers = zoneNumbers.getValue();
        return numbers != null ? numbers : "";
    }

    /**
     * Verifica se almeno una zona è selezionata.
     *
     * @return true se c'è almeno una selezione
     */
    public boolean hasSelection() {
        Integer count = selectedCount.getValue();
        return count != null && count > 0;
    }

    /**
     * Restituisce la maschera zone come bitmask intero.
     *
     * @return Bitmask dove bit 0 = zona 1, bit 7 = zona 8
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
     * Salva lo scenario personalizzato nel database.
     *
     * @param callback Callback chiamata quando lo scenario è salvato
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

    public interface OnScenarioSavedListener {
        void onSaved(it.bhomealarm.model.entity.Scenario scenario);
    }

    private void updateMask(Set<Integer> selected) {
        // Genera stringa con i numeri delle zone ordinate (es. "134" per zone 1,3,4)
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
