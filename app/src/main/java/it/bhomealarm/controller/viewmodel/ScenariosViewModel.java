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
 * ViewModel per ScenariosFragment.
 * Gestisce lista scenari disponibili.
 */
public class ScenariosViewModel extends AndroidViewModel {

    private final AlarmRepository repository;

    // Data
    private final LiveData<List<Scenario>> scenarios;

    // UI State
    private final MutableLiveData<Scenario> selectedScenario = new MutableLiveData<>();
    private final MutableLiveData<Boolean> showCustomOption = new MutableLiveData<>(true);

    public ScenariosViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        scenarios = repository.getAllScenarios();
    }

    // ========== Getters ==========

    public LiveData<List<Scenario>> getScenarios() {
        return scenarios;
    }

    public LiveData<Scenario> getSelectedScenario() {
        return selectedScenario;
    }

    public LiveData<Boolean> getShowCustomOption() {
        return showCustomOption;
    }

    // ========== Actions ==========

    /**
     * Seleziona uno scenario.
     *
     * @param scenario Scenario selezionato
     */
    public void selectScenario(Scenario scenario) {
        selectedScenario.setValue(scenario);
    }

    /**
     * Deseleziona lo scenario corrente.
     */
    public void clearSelection() {
        selectedScenario.setValue(null);
    }

    /**
     * Restituisce solo gli scenari abilitati.
     *
     * @return LiveData filtrato
     */
    public LiveData<List<Scenario>> getEnabledScenarios() {
        // TODO: Implementare filtro con Transformations.map()
        return scenarios;
    }

    /**
     * Conta gli scenari configurati.
     *
     * @return Numero di scenari abilitati
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
}
