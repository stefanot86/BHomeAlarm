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
 * ViewModel per UsersFragment.
 * Gestisce lista utenti e loro permessi.
 */
public class UsersViewModel extends AndroidViewModel {

    private final AlarmRepository repository;

    // Data
    private final LiveData<List<User>> users;

    // UI State
    private final MutableLiveData<User> selectedUser = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UsersViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
        users = repository.getAllUsers();
    }

    // ========== Getters ==========

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public LiveData<User> getSelectedUser() {
        return selectedUser;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // ========== Actions ==========

    /**
     * Seleziona un utente per visualizzare/modificare permessi.
     *
     * @param user Utente selezionato
     */
    public void selectUser(User user) {
        selectedUser.setValue(user);
    }

    /**
     * Aggiorna i permessi di un utente.
     *
     * @param user Utente da aggiornare
     * @param permissions Nuovi permessi (bitmask)
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
     * Applica gli stessi permessi a tutti gli utenti.
     *
     * @param permissions Permessi da applicare
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
     */
    public void clearSelection() {
        selectedUser.setValue(null);
    }

    /**
     * Pulisce il messaggio di errore.
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    // ========== Permission Helpers ==========

    /**
     * Verifica se l'utente ha il permesso RX1.
     */
    public static boolean hasRx1(User user) {
        return (user.getPermissions() & Constants.PERM_RX1) != 0;
    }

    /**
     * Verifica se l'utente ha il permesso RX2.
     */
    public static boolean hasRx2(User user) {
        return (user.getPermissions() & Constants.PERM_RX2) != 0;
    }

    /**
     * Verifica se l'utente ha il permesso VERIFY.
     */
    public static boolean hasVerify(User user) {
        return (user.getPermissions() & Constants.PERM_VERIFY) != 0;
    }

    /**
     * Verifica se l'utente ha il permesso CMD.
     */
    public static boolean hasCmd(User user) {
        return (user.getPermissions() & Constants.PERM_CMD_ON_OFF) != 0;
    }

    /**
     * Costruisce bitmask permessi da singoli flag.
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
