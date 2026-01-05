package it.bhomealarm.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import it.bhomealarm.model.entity.AppSettings;

/**
 * Data Access Object (DAO) per la gestione delle impostazioni dell'applicazione.
 * <p>
 * Questa interfaccia fornisce i metodi per accedere e manipolare le
 * impostazioni generali dell'applicazione memorizzate nel database Room.
 * Le impostazioni includono preferenze come il tema (dark mode),
 * le notifiche e lo stato del primo avvio.
 * </p>
 *
 * @see AppSettings
 * @see androidx.room.Dao
 */
@Dao
public interface AppSettingsDao {

    /**
     * Recupera le impostazioni dell'applicazione come LiveData.
     * <p>
     * Questo metodo restituisce le impostazioni in modo osservabile,
     * permettendo all'UI di aggiornarsi automaticamente quando i dati cambiano.
     * Le impostazioni sono memorizzate con ID fisso uguale a 1.
     * </p>
     *
     * @return {@link LiveData} contenente le impostazioni dell'applicazione,
     *         o null se non esistono impostazioni
     */
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    LiveData<AppSettings> getSettings();

    /**
     * Recupera le impostazioni dell'applicazione in modo sincrono.
     * <p>
     * Questo metodo blocca il thread chiamante fino al completamento della query.
     * Non deve essere chiamato dal thread principale dell'UI.
     * </p>
     *
     * @return le impostazioni dell'applicazione, o null se non esistono
     */
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    AppSettings getSettingsSync();

    /**
     * Inserisce nuove impostazioni dell'applicazione nel database.
     * <p>
     * Se esistono gia' impostazioni con lo stesso ID, vengono sostituite.
     * </p>
     *
     * @param settings le impostazioni da inserire
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppSettings settings);

    /**
     * Aggiorna le impostazioni dell'applicazione esistenti.
     *
     * @param settings le impostazioni con i dati aggiornati
     */
    @Update
    void update(AppSettings settings);

    /**
     * Segna il primo avvio dell'applicazione come completato.
     * <p>
     * Questo metodo imposta il flag first_launch a false, indicando
     * che l'utente ha completato la procedura di setup iniziale.
     * </p>
     *
     * @param timestamp il timestamp dell'aggiornamento in millisecondi
     */
    @Query("UPDATE app_settings SET first_launch = 0, updated_at = :timestamp WHERE id = 1")
    void markFirstLaunchComplete(long timestamp);

    /**
     * Abilita o disabilita le notifiche dell'applicazione.
     *
     * @param enabled true per abilitare le notifiche, false per disabilitarle
     * @param timestamp il timestamp dell'aggiornamento in millisecondi
     */
    @Query("UPDATE app_settings SET notifications_enabled = :enabled, updated_at = :timestamp WHERE id = 1")
    void setNotificationsEnabled(boolean enabled, long timestamp);

    /**
     * Imposta la modalita' del tema dell'applicazione (dark mode).
     * <p>
     * I valori tipici sono:
     * <ul>
     *   <li>0 - Segui impostazioni di sistema</li>
     *   <li>1 - Modalita' chiara</li>
     *   <li>2 - Modalita' scura</li>
     * </ul>
     * </p>
     *
     * @param mode il codice della modalita' del tema
     * @param timestamp il timestamp dell'aggiornamento in millisecondi
     */
    @Query("UPDATE app_settings SET dark_mode = :mode, updated_at = :timestamp WHERE id = 1")
    void setDarkMode(int mode, long timestamp);
}
