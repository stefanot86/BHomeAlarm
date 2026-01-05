package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import it.bhomealarm.model.entity.AlarmConfig;

/**
 * Data Access Object (DAO) per la gestione della configurazione dell'allarme.
 * <p>
 * Questa interfaccia fornisce i metodi per accedere e manipolare i dati
 * di configurazione dell'allarme memorizzati nel database Room.
 * La configurazione include informazioni come lo stato dell'allarme,
 * l'ultimo controllo effettuato e lo stato di completamento della configurazione.
 * </p>
 *
 * @see AlarmConfig
 * @see androidx.room.Dao
 */
@Dao
public interface AlarmConfigDao {

    /**
     * Recupera la configurazione dell'allarme come LiveData.
     * <p>
     * Questo metodo restituisce la configurazione in modo osservabile,
     * permettendo all'UI di aggiornarsi automaticamente quando i dati cambiano.
     * </p>
     *
     * @return {@link LiveData} contenente la configurazione dell'allarme,
     *         o null se non esiste alcuna configurazione
     */
    @Query("SELECT * FROM alarm_config LIMIT 1")
    LiveData<AlarmConfig> getConfig();

    /**
     * Recupera la configurazione dell'allarme in modo sincrono.
     * <p>
     * Questo metodo blocca il thread chiamante fino al completamento della query.
     * Non deve essere chiamato dal thread principale dell'UI.
     * </p>
     *
     * @return la configurazione dell'allarme, o null se non esiste
     */
    @Query("SELECT * FROM alarm_config LIMIT 1")
    AlarmConfig getConfigSync();

    /**
     * Inserisce una nuova configurazione dell'allarme nel database.
     * <p>
     * Se esiste gia' una configurazione con lo stesso ID, viene sostituita.
     * </p>
     *
     * @param config la configurazione dell'allarme da inserire
     * @return l'ID della riga inserita
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AlarmConfig config);

    /**
     * Aggiorna una configurazione dell'allarme esistente.
     *
     * @param config la configurazione dell'allarme con i dati aggiornati
     */
    @Update
    void update(AlarmConfig config);

    /**
     * Elimina una specifica configurazione dell'allarme dal database.
     *
     * @param config la configurazione dell'allarme da eliminare
     */
    @Delete
    void delete(AlarmConfig config);

    /**
     * Elimina tutte le configurazioni dell'allarme dal database.
     * <p>
     * Utilizzare con cautela: questa operazione non e' reversibile.
     * </p>
     */
    @Query("DELETE FROM alarm_config")
    void deleteAll();

    /**
     * Aggiorna lo stato dell'allarme per una specifica configurazione.
     * <p>
     * Questo metodo aggiorna lo stato corrente, il testo descrittivo
     * e il timestamp dell'ultimo controllo.
     * </p>
     *
     * @param id l'ID della configurazione da aggiornare
     * @param status il nuovo codice di stato dell'allarme
     * @param statusText la descrizione testuale dello stato
     * @param timestamp il timestamp dell'ultimo controllo in millisecondi
     */
    @Query("UPDATE alarm_config SET last_status = :status, last_status_text = :statusText, last_check = :timestamp WHERE id = :id")
    void updateStatus(long id, int status, String statusText, long timestamp);

    /**
     * Aggiorna lo stato di completamento della configurazione.
     * <p>
     * Indica se la configurazione iniziale dell'allarme e' stata completata.
     * </p>
     *
     * @param id l'ID della configurazione da aggiornare
     * @param complete true se la configurazione e' completa, false altrimenti
     * @param timestamp il timestamp dell'aggiornamento in millisecondi
     */
    @Query("UPDATE alarm_config SET config_complete = :complete, updated_at = :timestamp WHERE id = :id")
    void updateConfigComplete(long id, boolean complete, long timestamp);
}
