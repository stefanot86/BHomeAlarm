package it.bhomealarm.controller.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.model.repository.AlarmRepository;

/**
 * ViewModel per LogFragment.
 * Gestisce cronologia comunicazioni SMS.
 */
public class LogViewModel extends AndroidViewModel {

    private static final int DEFAULT_LOG_LIMIT = 50;

    private final AlarmRepository repository;

    // Data
    private final LiveData<List<SmsLog>> smsLogs;

    // UI State
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<SmsLog> selectedLog = new MutableLiveData<>();

    public LogViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        smsLogs = repository.getRecentLogs(DEFAULT_LOG_LIMIT);
    }

    // ========== Getters ==========

    public LiveData<List<SmsLog>> getSmsLogs() {
        return smsLogs;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<SmsLog> getSelectedLog() {
        return selectedLog;
    }

    // ========== Actions ==========

    /**
     * Seleziona un log per visualizzare dettagli.
     *
     * @param log Log selezionato
     */
    public void selectLog(SmsLog log) {
        selectedLog.setValue(log);
    }

    /**
     * Deseleziona il log corrente.
     */
    public void clearSelection() {
        selectedLog.setValue(null);
    }

    /**
     * Cancella tutti i log.
     */
    public void clearAllLogs() {
        isLoading.setValue(true);
        repository.clearAllLogs();
        isLoading.setValue(false);
    }

    /**
     * Ricarica i log.
     */
    public void refresh() {
        isLoading.setValue(true);
        // LiveData si aggiorna automaticamente
        isLoading.setValue(false);
    }

    /**
     * Conta i log di tipo errore.
     *
     * @return Numero di errori
     */
    public int getErrorCount() {
        List<SmsLog> logs = smsLogs.getValue();
        if (logs == null) return 0;

        int count = 0;
        for (SmsLog log : logs) {
            if ("ERROR".equals(log.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Restituisce l'ultimo log.
     *
     * @return Ultimo SmsLog o null
     */
    public SmsLog getLastLog() {
        List<SmsLog> logs = smsLogs.getValue();
        if (logs == null || logs.isEmpty()) {
            return null;
        }
        return logs.get(0);
    }
}
