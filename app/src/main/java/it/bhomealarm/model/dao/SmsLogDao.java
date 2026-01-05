package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import it.bhomealarm.model.entity.SmsLog;

/**
 * Data Access Object (DAO) per la gestione dei log degli SMS.
 * <p>
 * Questa interfaccia fornisce i metodi per accedere e manipolare i dati
 * dei messaggi SMS inviati e ricevuti dal sistema di allarme.
 * I log permettono di tracciare la comunicazione tra l'app e la centralina
 * dell'allarme, inclusi gli stati di consegna e gli eventuali errori.
 * </p>
 *
 * @see SmsLog
 * @see androidx.room.Dao
 */
@Dao
public interface SmsLogDao {

    /**
     * Recupera tutti i log degli SMS ordinati per timestamp decrescente come LiveData.
     * <p>
     * I log piu' recenti appaiono per primi nella lista.
     * Questo metodo restituisce i dati in modo osservabile,
     * permettendo all'UI di aggiornarsi automaticamente quando i dati cambiano.
     * </p>
     *
     * @return {@link LiveData} contenente la lista di tutti i log SMS
     */
    @Query("SELECT * FROM sms_log ORDER BY timestamp DESC")
    LiveData<List<SmsLog>> getAllLogs();

    /**
     * Recupera i log SMS piu' recenti con un limite specificato come LiveData.
     * <p>
     * Utile per mostrare solo gli ultimi N messaggi nell'interfaccia utente.
     * </p>
     *
     * @param limit il numero massimo di log da recuperare
     * @return {@link LiveData} contenente la lista limitata dei log SMS piu' recenti
     */
    @Query("SELECT * FROM sms_log ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<SmsLog>> getRecentLogs(int limit);

    /**
     * Recupera tutti i log degli SMS in modo sincrono.
     * <p>
     * Questo metodo blocca il thread chiamante fino al completamento della query.
     * Non deve essere chiamato dal thread principale dell'UI.
     * I log piu' recenti appaiono per primi nella lista.
     * </p>
     *
     * @return la lista di tutti i log SMS ordinati per timestamp decrescente
     */
    @Query("SELECT * FROM sms_log ORDER BY timestamp DESC")
    List<SmsLog> getAllLogsSync();

    /**
     * Recupera i log SMS filtrati per direzione come LiveData.
     * <p>
     * Permette di visualizzare solo i messaggi inviati o solo quelli ricevuti.
     * </p>
     *
     * @param direction la direzione del messaggio (es. 0 per inviato, 1 per ricevuto)
     * @return {@link LiveData} contenente la lista dei log SMS filtrati per direzione
     */
    @Query("SELECT * FROM sms_log WHERE direction = :direction ORDER BY timestamp DESC")
    LiveData<List<SmsLog>> getLogsByDirection(int direction);

    /**
     * Inserisce un nuovo log SMS nel database.
     *
     * @param log il log SMS da inserire
     * @return l'ID della riga inserita
     */
    @Insert
    long insert(SmsLog log);

    /**
     * Elimina uno specifico log SMS dal database.
     *
     * @param log il log SMS da eliminare
     */
    @Delete
    void delete(SmsLog log);

    /**
     * Elimina tutti i log SMS dal database.
     * <p>
     * Utilizzare con cautela: questa operazione non e' reversibile.
     * </p>
     */
    @Query("DELETE FROM sms_log")
    void deleteAll();

    /**
     * Elimina i log SMS precedenti a un determinato timestamp.
     * <p>
     * Utile per la pulizia periodica dei log vecchi e per
     * mantenere le dimensioni del database sotto controllo.
     * </p>
     *
     * @param beforeTimestamp il timestamp in millisecondi; i log precedenti verranno eliminati
     */
    @Query("DELETE FROM sms_log WHERE timestamp < :beforeTimestamp")
    void deleteOldLogs(long beforeTimestamp);

    /**
     * Conta il numero totale di log SMS nel database.
     *
     * @return il numero totale di log SMS memorizzati
     */
    @Query("SELECT COUNT(*) FROM sms_log")
    int getLogCount();

    /**
     * Recupera un log SMS tramite il suo identificatore univoco del messaggio.
     * <p>
     * Utile per tracciare lo stato di consegna di un messaggio specifico.
     * </p>
     *
     * @param messageId l'identificatore univoco del messaggio
     * @return il log SMS corrispondente, o null se non trovato
     */
    @Query("SELECT * FROM sms_log WHERE message_id = :messageId LIMIT 1")
    SmsLog getByMessageId(String messageId);

    /**
     * Aggiorna lo stato di un log SMS tramite l'identificatore del messaggio.
     * <p>
     * Utilizzato per aggiornare lo stato di consegna del messaggio
     * (es. inviato, consegnato, fallito).
     * </p>
     *
     * @param messageId l'identificatore univoco del messaggio
     * @param status il nuovo codice di stato
     */
    @Query("UPDATE sms_log SET status = :status WHERE message_id = :messageId")
    void updateStatus(String messageId, int status);

    /**
     * Aggiorna lo stato di un log SMS con un messaggio di errore.
     * <p>
     * Utilizzato quando l'invio o la ricezione di un messaggio fallisce
     * e si vuole registrare il motivo dell'errore.
     * </p>
     *
     * @param messageId l'identificatore univoco del messaggio
     * @param status il nuovo codice di stato (tipicamente un codice di errore)
     * @param errorMessage la descrizione dell'errore verificatosi
     */
    @Query("UPDATE sms_log SET status = :status, error_message = :errorMessage WHERE message_id = :messageId")
    void updateStatusWithError(String messageId, int status, String errorMessage);
}
