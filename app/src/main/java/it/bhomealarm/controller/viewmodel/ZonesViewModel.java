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
    private final MutableLiveData<String> zoneMask = new MutableLiveData<>("00000000");
    private final MutableLiveData<Integer> selectedCount = new MutableLiveData<>(0);

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

    public LiveData<String> getZoneMask() {
        return zoneMask;
    }

    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
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
     * Restituisce la maschera per il comando CUST.
     *
     * @return Stringa di 8 caratteri (0/1)
     */
    public String getZoneMaskString() {
        String mask = zoneMask.getValue();
        return mask != null ? mask : "00000000";
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

    private void updateMask(Set<Integer> selected) {
        StringBuilder mask = new StringBuilder();
        for (int i = 1; i <= Constants.ZONE_COUNT; i++) {
            mask.append(selected.contains(i) ? '1' : '0');
        }

        zoneMask.setValue(mask.toString());
        selectedCount.setValue(selected.size());
    }
}
