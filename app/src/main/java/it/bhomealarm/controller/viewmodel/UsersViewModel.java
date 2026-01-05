package it.bhomealarm.controller.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.bhomealarm.model.entity.User;
import it.bhomealarm.model.repository.AlarmRepository;
import it.bhomealarm.util.Constants;

/**
 * ViewModel per la gestione degli utenti del sistema di allarme.
 * <p>
 * Questa classe gestisce la lista degli utenti configurati sulla centralina
 * e i loro permessi. Ogni utente puo' avere diversi permessi che determinano
 * quali operazioni puo' eseguire:
 * <ul>
 *     <li><b>RX1:</b> Ricezione notifiche allarme primarie</li>
 *     <li><b>RX2:</b> Ricezione notifiche allarme secondarie</li>
 *     <li><b>VERIFY:</b> Ricezione notifiche di verifica stato</li>
 *     <li><b>CMD:</b> Autorizzazione invio comandi ON/OFF</li>
 * </ul>
 * <p>
 * Gli utenti sono organizzati in slot numerati (1-16) e i loro dati
 * vengono sincronizzati con la centralina tramite comandi SMS.
 *
 * @see it.bhomealarm.view.fragment.UsersFragment
 * @see User
 * @see AlarmRepository
 */
public class UsersViewModel extends AndroidViewModel {

    /** Repository per l'accesso ai dati dell'allarme */
    private final AlarmRepository repository;

    // ========== Data ==========

    /** Lista di tutti gli utenti configurati nel sistema */
    private final LiveData<List<User>> users;

    // ========== UI State ==========

    /** Utente attualmente selezionato per visualizzazione/modifica */
    private final MutableLiveData<User> selectedUser = new MutableLiveData<>();

    /** Flag che indica se e' in corso un'operazione asincrona */
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    /** Messaggio di errore da visualizzare all'utente */
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Costruttore del ViewModel.
     * <p>
     * Inizializza il repository e carica la lista degli utenti dal database.
     *
     * @param application Contesto dell'applicazione Android
     */
    public UsersViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        users = repository.getAllUsers();
    }

    // ========== Getters ==========

    /**
     * Restituisce il LiveData contenente la lista degli utenti.
     *
     * @return LiveData con la lista di tutti gli utenti configurati
     */
    public LiveData<List<User>> getUsers() {
        return users;
    }

    /**
     * Restituisce il LiveData contenente l'utente selezionato.
     *
     * @return LiveData con l'utente attualmente selezionato, o null se nessuno e' selezionato
     */
    public LiveData<User> getSelectedUser() {
        return selectedUser;
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
     * Restituisce il LiveData contenente eventuali messaggi di errore.
     *
     * @return LiveData con il messaggio di errore, o null se non ci sono errori
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // ========== Actions ==========

    /**
     * Seleziona un utente per visualizzare o modificare i suoi permessi.
     * <p>
     * L'utente selezionato viene esposto tramite LiveData per permettere
     * alla UI di mostrare i dettagli o un dialog di modifica.
     *
     * @param user Utente da selezionare
     */
    public void selectUser(User user) {
        selectedUser.setValue(user);
    }

    /**
     * Aggiorna i permessi di un utente.
     * <p>
     * I permessi vengono aggiornati sia nel database locale che sulla centralina
     * tramite invio di un comando SMS.
     * <p>
     * I permessi sono rappresentati come bitmask dove ogni bit corrisponde
     * a un permesso specifico (RX1, RX2, VERIFY, CMD).
     *
     * @param user Utente di cui aggiornare i permessi
     * @param permissions Nuovi permessi come valore bitmask
     */
    public void updateUserPermissions(User user, int permissions) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Aggiorna localmente
        user.setPermissions(permissions);
        repository.updateUser(user);

        // Invia comando SMS per aggiornare allarme
        String permString = String.format("%04d", Integer.parseInt(Integer.toBinaryString(permissions)));
        String command = String.format(Constants.CMD_SET_USER, user.getSlot(), permString);

        // TODO: Inviare SMS tramite SmsService
        // smsService.sendCommand(command, callback);

        isLoading.setValue(false);
    }

    /**
     * Applica gli stessi permessi a tutti gli utenti abilitati.
     * <p>
     * Questa operazione e' utile per configurare rapidamente tutti gli utenti
     * con le stesse autorizzazioni. Gli utenti disabilitati vengono ignorati.
     *
     * @param permissions Permessi da applicare a tutti gli utenti abilitati
     */
    public void applyPermissionsToAll(int permissions) {
        isLoading.setValue(true);

        List<User> allUsers = users.getValue();
        if (allUsers != null) {
            for (User user : allUsers) {
                if (user.isEnabled()) {
                    user.setPermissions(permissions);
                    repository.updateUser(user);
                }
            }
        }

        // TODO: Inviare comandi SMS per ogni utente

        isLoading.setValue(false);
    }

    /**
     * Deseleziona l'utente corrente.
     * <p>
     * Utile per chiudere dialog o pannelli di dettaglio nella UI.
     */
    public void clearSelection() {
        selectedUser.setValue(null);
    }

    /**
     * Pulisce il messaggio di errore corrente.
     * <p>
     * Chiamare questo metodo dopo che l'utente ha visualizzato l'errore.
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    // ========== Permission Helpers ==========

    /**
     * Verifica se l'utente ha il permesso RX1 (ricezione allarmi primari).
     *
     * @param user Utente da verificare
     * @return true se l'utente ha il permesso RX1
     */
    public static boolean hasRx1(User user) {
        return (user.getPermissions() & Constants.PERM_RX1) != 0;
    }

    /**
     * Verifica se l'utente ha il permesso RX2 (ricezione allarmi secondari).
     *
     * @param user Utente da verificare
     * @return true se l'utente ha il permesso RX2
     */
    public static boolean hasRx2(User user) {
        return (user.getPermissions() & Constants.PERM_RX2) != 0;
    }

    /**
     * Verifica se l'utente ha il permesso VERIFY (notifiche di verifica).
     *
     * @param user Utente da verificare
     * @return true se l'utente ha il permesso VERIFY
     */
    public static boolean hasVerify(User user) {
        return (user.getPermissions() & Constants.PERM_VERIFY) != 0;
    }

    /**
     * Verifica se l'utente ha il permesso CMD (invio comandi ON/OFF).
     *
     * @param user Utente da verificare
     * @return true se l'utente ha il permesso CMD
     */
    public static boolean hasCmd(User user) {
        return (user.getPermissions() & Constants.PERM_CMD_ON_OFF) != 0;
    }

    /**
     * Costruisce un valore bitmask dei permessi da singoli flag booleani.
     * <p>
     * Metodo di utilita' per convertire i checkbox della UI in un valore
     * di permessi da salvare.
     *
     * @param rx1 true se abilitare permesso RX1
     * @param rx2 true se abilitare permesso RX2
     * @param verify true se abilitare permesso VERIFY
     * @param cmd true se abilitare permesso CMD
     * @return Valore bitmask contenente tutti i permessi specificati
     */
    public static int buildPermissions(boolean rx1, boolean rx2, boolean verify, boolean cmd) {
        int perm = 0;
        if (rx1) perm |= Constants.PERM_RX1;
        if (rx2) perm |= Constants.PERM_RX2;
        if (verify) perm |= Constants.PERM_VERIFY;
        if (cmd) perm |= Constants.PERM_CMD_ON_OFF;
        return perm;
    }
}
