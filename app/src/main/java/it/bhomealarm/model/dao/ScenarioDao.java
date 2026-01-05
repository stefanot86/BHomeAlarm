package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import it.bhomealarm.model.entity.Scenario;

/**
 * Data Access Object (DAO) per la gestione degli scenari nel database Room.
 * <p>
 * Questo DAO gestisce due tipi di scenari:
 * <ul>
 *     <li><b>Scenari predefiniti (slot 1-16)</b>: scaricati dall'allarme tramite CONF2/CONF3</li>
 *     <li><b>Scenari personalizzati (slot > 100)</b>: creati localmente dall'utente</li>
 * </ul>
 * <p>
 * La convenzione slot > 100 per gli scenari custom permette di distinguerli
 * facilmente dagli scenari predefiniti senza aggiungere campi extra.
 *
 * @see it.bhomealarm.model.entity.Scenario
 * @see it.bhomealarm.model.repository.AlarmRepository
 */
@Dao
public interface ScenarioDao {

    /**
     * Recupera tutti gli scenari ordinati per slot.
     * Include sia scenari predefiniti che personalizzati.
     *
     * @return LiveData con la lista di tutti gli scenari
     */
    @Query("SELECT * FROM scenarios ORDER BY slot ASC")
    LiveData<List<Scenario>> getAllScenarios();

    /**
     * Recupera tutti gli scenari in modo sincrono (blocca il thread).
     * Da usare solo in background thread.
     *
     * @return Lista di tutti gli scenari
     */
    @Query("SELECT * FROM scenarios ORDER BY slot ASC")
    List<Scenario> getAllScenariosSync();

    /**
     * Recupera uno scenario specifico per numero slot.
     *
     * @param slot Numero slot dello scenario (1-16 predefiniti, >100 custom)
     * @return LiveData con lo scenario, null se non trovato
     */
    @Query("SELECT * FROM scenarios WHERE slot = :slot LIMIT 1")
    LiveData<Scenario> getScenarioBySlot(int slot);

    /**
     * Recupera uno scenario per slot in modo sincrono.
     *
     * @param slot Numero slot dello scenario
     * @return Lo scenario trovato o null
     */
    @Query("SELECT * FROM scenarios WHERE slot = :slot LIMIT 1")
    Scenario getScenarioBySlotSync(int slot);

    /**
     * Recupera solo gli scenari abilitati.
     *
     * @return LiveData con gli scenari che hanno enabled = true
     */
    @Query("SELECT * FROM scenarios WHERE enabled = 1 ORDER BY slot ASC")
    LiveData<List<Scenario>> getEnabledScenarios();

    /**
     * Inserisce o aggiorna uno scenario.
     * Se esiste già uno scenario con lo stesso slot, viene sostituito.
     *
     * @param scenario Lo scenario da inserire/aggiornare
     * @return L'ID della riga inserita
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Scenario scenario);

    /**
     * Inserisce o aggiorna una lista di scenari.
     *
     * @param scenarios Lista degli scenari da inserire
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Scenario> scenarios);

    /**
     * Aggiorna uno scenario esistente.
     *
     * @param scenario Lo scenario con i dati aggiornati
     */
    @Update
    void update(Scenario scenario);

    /**
     * Elimina uno scenario.
     *
     * @param scenario Lo scenario da eliminare
     */
    @Delete
    void delete(Scenario scenario);

    /**
     * Elimina tutti gli scenari dal database.
     * Usato durante il reset della configurazione.
     */
    @Query("DELETE FROM scenarios")
    void deleteAll();

    /**
     * Aggiorna i campi di uno scenario specifico.
     *
     * @param slot Numero slot dello scenario
     * @param name Nuovo nome
     * @param zoneMask Nuovo bitmask delle zone
     * @param enabled Stato abilitazione
     * @param timestamp Timestamp dell'aggiornamento
     */
    @Query("UPDATE scenarios SET name = :name, zone_mask = :zoneMask, enabled = :enabled, updated_at = :timestamp WHERE slot = :slot")
    void updateScenario(int slot, String name, int zoneMask, boolean enabled, long timestamp);

    /**
     * Recupera solo gli scenari personalizzati (creati dall'utente).
     * Gli scenari custom hanno il flag is_custom = true.
     *
     * @return LiveData con gli scenari personalizzati
     */
    @Query("SELECT * FROM scenarios WHERE is_custom = 1 ORDER BY slot ASC")
    LiveData<List<Scenario>> getCustomScenarios();

    /**
     * Calcola il prossimo slot disponibile per un nuovo scenario personalizzato.
     * <p>
     * Gli scenari custom usano slot > 100 per distinguersi dai predefiniti (1-16).
     * Questa query trova il MAX(slot) tra i custom e restituisce MAX + 1.
     * Se non ci sono scenari custom, restituisce 101.
     * <p>
     * Esempio:
     * <ul>
     *     <li>Nessun custom → restituisce 101</li>
     *     <li>Esiste slot 101 → restituisce 102</li>
     *     <li>Esistono slot 101, 102, 103 → restituisce 104</li>
     * </ul>
     *
     * @return Il prossimo slot libero (minimo 101)
     */
    @Query("SELECT COALESCE(MAX(slot), 100) + 1 FROM scenarios WHERE slot > 100")
    int getNextCustomSlot();

    /**
     * Elimina uno scenario specifico per numero slot.
     * Usato per eliminare sia scenari predefiniti che personalizzati.
     *
     * @param slot Numero slot dello scenario da eliminare
     */
    @Query("DELETE FROM scenarios WHERE slot = :slot")
    void deleteBySlot(int slot);
}
