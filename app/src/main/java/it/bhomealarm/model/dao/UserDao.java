package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.bhomealarm.model.entity.User;

/**
 * Data Access Object (DAO) per la gestione degli utenti del sistema di allarme.
 * <p>
 * Questa interfaccia fornisce i metodi per accedere e manipolare i dati
 * degli utenti memorizzati nel database Room.
 * Gli utenti possono avere diversi livelli di permessi e possono includere
 * un utente speciale "joker" con privilegi particolari.
 * </p>
 *
 * @see User
 * @see androidx.room.Dao
 */
@Dao
public interface UserDao {

    /**
     * Recupera tutti gli utenti ordinati per slot come LiveData.
     * <p>
     * Questo metodo restituisce la lista degli utenti in modo osservabile,
     * permettendo all'UI di aggiornarsi automaticamente quando i dati cambiano.
     * </p>
     *
     * @return {@link LiveData} contenente la lista di tutti gli utenti ordinati per slot
     */
    @Query("SELECT * FROM users ORDER BY slot ASC")
    LiveData<List<User>> getAllUsers();

    /**
     * Recupera tutti gli utenti in modo sincrono ordinati per slot.
     * <p>
     * Questo metodo blocca il thread chiamante fino al completamento della query.
     * Non deve essere chiamato dal thread principale dell'UI.
     * </p>
     *
     * @return la lista di tutti gli utenti ordinati per slot
     */
    @Query("SELECT * FROM users ORDER BY slot ASC")
    List<User> getAllUsersSync();

    /**
     * Recupera un utente specifico tramite il suo slot come LiveData.
     *
     * @param slot il numero dello slot dell'utente da cercare
     * @return {@link LiveData} contenente l'utente corrispondente allo slot,
     *         o null se non trovato
     */
    @Query("SELECT * FROM users WHERE slot = :slot LIMIT 1")
    LiveData<User> getUserBySlot(int slot);

    /**
     * Recupera un utente specifico tramite il suo slot in modo sincrono.
     * <p>
     * Questo metodo blocca il thread chiamante fino al completamento della query.
     * Non deve essere chiamato dal thread principale dell'UI.
     * </p>
     *
     * @param slot il numero dello slot dell'utente da cercare
     * @return l'utente corrispondente allo slot, o null se non trovato
     */
    @Query("SELECT * FROM users WHERE slot = :slot LIMIT 1")
    User getUserBySlotSync(int slot);

    /**
     * Recupera l'utente joker come LiveData.
     * <p>
     * L'utente joker e' un utente speciale con privilegi particolari
     * nel sistema di allarme.
     * </p>
     *
     * @return {@link LiveData} contenente l'utente joker, o null se non configurato
     */
    @Query("SELECT * FROM users WHERE is_joker = 1 LIMIT 1")
    LiveData<User> getJokerUser();

    /**
     * Recupera solo gli utenti abilitati ordinati per slot come LiveData.
     * <p>
     * Utile per mostrare solo gli utenti attivi nel sistema.
     * </p>
     *
     * @return {@link LiveData} contenente la lista degli utenti abilitati
     */
    @Query("SELECT * FROM users WHERE enabled = 1 ORDER BY slot ASC")
    LiveData<List<User>> getEnabledUsers();

    /**
     * Inserisce un nuovo utente nel database.
     * <p>
     * Se esiste gia' un utente con lo stesso ID, viene sostituito.
     * </p>
     *
     * @param user l'utente da inserire
     * @return l'ID della riga inserita
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    /**
     * Inserisce una lista di utenti nel database.
     * <p>
     * Se esistono utenti con lo stesso ID, vengono sostituiti.
     * Utile per l'inizializzazione o la sincronizzazione batch degli utenti.
     * </p>
     *
     * @param users la lista di utenti da inserire
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    /**
     * Aggiorna un utente esistente nel database.
     *
     * @param user l'utente con i dati aggiornati
     */
    @Update
    void update(User user);

    /**
     * Elimina uno specifico utente dal database.
     *
     * @param user l'utente da eliminare
     */
    @Delete
    void delete(User user);

    /**
     * Elimina tutti gli utenti dal database.
     * <p>
     * Utilizzare con cautela: questa operazione non e' reversibile.
     * </p>
     */
    @Query("DELETE FROM users")
    void deleteAll();

    /**
     * Aggiorna i dati di un utente specifico tramite il suo slot.
     * <p>
     * Permette di aggiornare nome, permessi, stato di abilitazione e timestamp
     * senza dover recuperare e modificare l'intera entita'.
     * </p>
     *
     * @param slot il numero dello slot dell'utente da aggiornare
     * @param name il nuovo nome dell'utente
     * @param permissions il nuovo valore dei permessi (bitmask)
     * @param enabled true se l'utente deve essere abilitato, false altrimenti
     * @param timestamp il timestamp dell'aggiornamento in millisecondi
     */
    @Query("UPDATE users SET name = :name, permissions = :permissions, enabled = :enabled, updated_at = :timestamp WHERE slot = :slot")
    void updateUser(int slot, String name, int permissions, boolean enabled, long timestamp);
}
