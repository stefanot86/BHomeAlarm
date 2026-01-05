package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.bhomealarm.model.entity.Zone;

/**
 * Data Access Object (DAO) per la gestione delle zone dell'allarme.
 * <p>
 * Questa interfaccia fornisce i metodi per accedere e manipolare i dati
 * delle zone dell'allarme memorizzate nel database Room.
 * Una zona rappresenta un'area monitorata dal sistema di allarme
 * (es. ingresso, finestra, sensore movimento).
 * </p>
 *
 * @see Zone
 * @see androidx.room.Dao
 */
@Dao
public interface ZoneDao {

    /**
     * Recupera tutte le zone ordinate per slot come LiveData.
     * <p>
     * Questo metodo restituisce la lista delle zone in modo osservabile,
     * permettendo all'UI di aggiornarsi automaticamente quando i dati cambiano.
     * </p>
     *
     * @return {@link LiveData} contenente la lista di tutte le zone ordinate per slot
     */
    @Query("SELECT * FROM zones ORDER BY slot ASC")
    LiveData<List<Zone>> getAllZones();

    /**
     * Recupera tutte le zone in modo sincrono ordinate per slot.
     * <p>
     * Questo metodo blocca il thread chiamante fino al completamento della query.
     * Non deve essere chiamato dal thread principale dell'UI.
     * </p>
     *
     * @return la lista di tutte le zone ordinate per slot
     */
    @Query("SELECT * FROM zones ORDER BY slot ASC")
    List<Zone> getAllZonesSync();

    /**
     * Recupera una zona specifica tramite il suo slot come LiveData.
     *
     * @param slot il numero dello slot della zona da cercare
     * @return {@link LiveData} contenente la zona corrispondente allo slot,
     *         o null se non trovata
     */
    @Query("SELECT * FROM zones WHERE slot = :slot LIMIT 1")
    LiveData<Zone> getZoneBySlot(int slot);

    /**
     * Recupera una zona specifica tramite il suo slot in modo sincrono.
     * <p>
     * Questo metodo blocca il thread chiamante fino al completamento della query.
     * Non deve essere chiamato dal thread principale dell'UI.
     * </p>
     *
     * @param slot il numero dello slot della zona da cercare
     * @return la zona corrispondente allo slot, o null se non trovata
     */
    @Query("SELECT * FROM zones WHERE slot = :slot LIMIT 1")
    Zone getZoneBySlotSync(int slot);

    /**
     * Recupera solo le zone abilitate ordinate per slot come LiveData.
     * <p>
     * Utile per mostrare solo le zone attive nel monitoraggio dell'allarme.
     * </p>
     *
     * @return {@link LiveData} contenente la lista delle zone abilitate
     */
    @Query("SELECT * FROM zones WHERE enabled = 1 ORDER BY slot ASC")
    LiveData<List<Zone>> getEnabledZones();

    /**
     * Inserisce una nuova zona nel database.
     * <p>
     * Se esiste gia' una zona con lo stesso ID, viene sostituita.
     * </p>
     *
     * @param zone la zona da inserire
     * @return l'ID della riga inserita
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Zone zone);

    /**
     * Inserisce una lista di zone nel database.
     * <p>
     * Se esistono zone con lo stesso ID, vengono sostituite.
     * Utile per l'inizializzazione o la sincronizzazione batch delle zone.
     * </p>
     *
     * @param zones la lista di zone da inserire
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Zone> zones);

    /**
     * Aggiorna una zona esistente nel database.
     *
     * @param zone la zona con i dati aggiornati
     */
    @Update
    void update(Zone zone);

    /**
     * Elimina una specifica zona dal database.
     *
     * @param zone la zona da eliminare
     */
    @Delete
    void delete(Zone zone);

    /**
     * Elimina tutte le zone dal database.
     * <p>
     * Utilizzare con cautela: questa operazione non e' reversibile.
     * </p>
     */
    @Query("DELETE FROM zones")
    void deleteAll();

    /**
     * Aggiorna i dati di una zona specifica tramite il suo slot.
     * <p>
     * Permette di aggiornare nome, stato di abilitazione e timestamp
     * senza dover recuperare e modificare l'intera entita'.
     * </p>
     *
     * @param slot il numero dello slot della zona da aggiornare
     * @param name il nuovo nome della zona
     * @param enabled true se la zona deve essere abilitata, false altrimenti
     * @param timestamp il timestamp dell'aggiornamento in millisecondi
     */
    @Query("UPDATE zones SET name = :name, enabled = :enabled, updated_at = :timestamp WHERE slot = :slot")
    void updateZone(int slot, String name, boolean enabled, long timestamp);
}
