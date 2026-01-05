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
 * ViewModel per la visualizzazione della cronologia delle comunicazioni SMS.
 * <p>
 * Questa classe gestisce il log di tutti i messaggi SMS scambiati tra
 * l'applicazione e la centralina di allarme. Permette di:
 * <ul>
 *     <li>Visualizzare la cronologia degli SMS inviati e ricevuti</li>
 *     <li>Selezionare un log per visualizzarne i dettagli</li>
 *     <li>Contare gli errori di comunicazione</li>
 *     <li>Cancellare la cronologia</li>
 * </ul>
 * <p>
 * I log sono ordinati cronologicamente con i piu' recenti in cima alla lista.
 *
 * @see it.bhomealarm.view.fragment.LogFragment
 * @see SmsLog
 * @see AlarmRepository
 */
public class LogViewModel extends AndroidViewModel {

    /** Numero massimo di log da caricare per default */
    private static final int DEFAULT_LOG_LIMIT = 50;

    /** Repository per l'accesso ai dati dell'allarme */
    private final AlarmRepository repository;

    // ========== Data ==========

    /** Lista dei log SMS recenti dal database */
    private final LiveData<List<SmsLog>> smsLogs;

    // ========== UI State ==========

    /** Flag che indica se e' in corso un'operazione asincrona */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /** Log attualmente selezionato per visualizzazione dettagli */
    private final MutableLiveData<SmsLog> selectedLog = new MutableLiveData<>();

    /**
     * Costruttore del ViewModel.
     * <p>
     * Inizializza il repository e carica i log SMS recenti dal database
     * con il limite predefinito di {@value #DEFAULT_LOG_LIMIT} elementi.
     *
     * @param application Contesto dell'applicazione Android
     */
    public LogViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        smsLogs = repository.getRecentLogs(DEFAULT_LOG_LIMIT);
    }

    // ========== Getters ==========

    /**
     * Restituisce il LiveData contenente la lista dei log SMS.
     *
     * @return LiveData con la lista dei log SMS ordinati per data decrescente
     */
    public LiveData<List<SmsLog>> getSmsLogs() {
        return smsLogs;
    }

    /**
     * Restituisce il LiveData che indica lo stato di caricamento.
     *
     * @return LiveData con true se e' in corso un'operazione, false altrimenti
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Restituisce il LiveData contenente il log selezionato.
     *
     * @return LiveData con il log attualmente selezionato, o null se nessuno e' selezionato
     */
    public LiveData<SmsLog> getSelectedLog() {
        return selectedLog;
    }

    // ========== Actions ==========

    /**
     * Seleziona un log per visualizzarne i dettagli.
     * <p>
     * Il log selezionato viene esposto tramite LiveData per permettere
     * alla UI di mostrare un dialog o un pannello con i dettagli completi
     * del messaggio SMS.
     *
     * @param log Log SMS da selezionare
     */
    public void selectLog(SmsLog log) {
        selectedLog.setValue(log);
    }

    /**
     * Deseleziona il log corrente.
     * <p>
     * Utile per chiudere dialog o pannelli di dettaglio nella UI.
     */
    public void clearSelection() {
        selectedLog.setValue(null);
    }

    /**
     * Cancella tutti i log dal database.
     * <p>
     * <b>Attenzione:</b> Questa operazione e' irreversibile e rimuove
     * permanentemente tutta la cronologia delle comunicazioni SMS.
     */
    public void clearAllLogs() {
        isLoading.setValue(true);
        repository.clearAllLogs();
        isLoading.setValue(false);
    }

    /**
     * Ricarica i log dal database.
     * <p>
     * Poiche' i dati sono esposti tramite LiveData, l'aggiornamento
     * avviene automaticamente quando il database cambia. Questo metodo
     * puo' essere usato per forzare un refresh manuale dell'indicatore
     * di caricamento nella UI.
     */
    public void refresh() {
        isLoading.setValue(true);
        // LiveData si aggiorna automaticamente
        isLoading.setValue(false);
    }

    /**
     * Conta il numero di log con stato di errore.
     * <p>
     * Utile per mostrare badge o indicatori nella UI che evidenziano
     * problemi di comunicazione con la centralina.
     *
     * @return Numero di log con status "ERROR", 0 se non ci sono errori o la lista e' vuota
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
     * Restituisce l'ultimo log registrato.
     * <p>
     * Poiche' i log sono ordinati per data decrescente, l'ultimo log
     * (piu' recente) si trova in posizione 0 della lista.
     *
     * @return L'ultimo SmsLog registrato, o null se la lista e' vuota
     */
    public SmsLog getLastLog() {
        List<SmsLog> logs = smsLogs.getValue();
        if (logs == null || logs.isEmpty()) {
            return null;
        }
        return logs.get(0);
    }
}
