package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.UsersViewModel;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.view.adapter.UsersAdapter;

/**
 * Fragment per la gestione degli utenti e dei loro permessi sulla centralina allarme.
 * <p>
 * Questo fragment mostra la lista degli utenti configurati sulla centralina
 * e permette di modificare i permessi di ciascun utente.
 * <p>
 * I permessi disponibili per ogni utente sono:
 * <ul>
 *     <li>RX1 - Ricezione notifiche canale 1</li>
 *     <li>RX2 - Ricezione notifiche canale 2</li>
 *     <li>VERIFY - Permesso di verifica stato</li>
 *     <li>CMD ON/OFF - Permesso di attivazione/disattivazione</li>
 * </ul>
 * <p>
 * Il flusso utente prevede:
 * <ol>
 *     <li>Visualizzazione della lista utenti</li>
 *     <li>Tocco su un utente per aprire il dialog dei permessi</li>
 *     <li>Modifica dei permessi desiderati</li>
 *     <li>Opzione per applicare i permessi a tutti gli utenti</li>
 *     <li>Salvataggio con invio SMS alla centralina</li>
 * </ol>
 *
 * @see UsersViewModel ViewModel che gestisce gli utenti e i loro permessi
 * @see UsersAdapter Adapter per la visualizzazione della lista utenti
 * @see User Entita' che rappresenta un utente con i suoi permessi
 */
public class UsersFragment extends Fragment {

    /** ViewModel per la gestione degli utenti */
    private UsersViewModel viewModel;

    /** Adapter per la lista degli utenti */
    private UsersAdapter adapter;

    /** Toolbar con pulsante di navigazione indietro */
    private MaterialToolbar toolbar;

    /** RecyclerView per visualizzare la lista degli utenti */
    private RecyclerView recyclerUsers;

    /** Layout mostrato quando non ci sono utenti configurati */
    private LinearLayout layoutEmpty;

    /**
     * Inizializza il ViewModel all'avvio del Fragment.
     *
     * @param savedInstanceState stato salvato dell'istanza precedente, puo' essere null
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UsersViewModel.class);
    }

    /**
     * Crea e restituisce la view hierarchy associata al fragment.
     *
     * @param inflater inflater per creare la view dal layout XML
     * @param container contenitore padre della view
     * @param savedInstanceState stato salvato dell'istanza precedente
     * @return la view root del fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    /**
     * Chiamato dopo che la view e' stata creata.
     * Inizializza le views, la RecyclerView e avvia l'osservazione dei dati.
     *
     * @param view la view root del fragment
     * @param savedInstanceState stato salvato dell'istanza precedente
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupRecyclerView();
        observeData();
    }

    /**
     * Inizializza i riferimenti alle views del layout.
     * Configura la toolbar con navigazione indietro.
     *
     * @param view la view root del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerUsers = view.findViewById(R.id.recycler_users);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    /**
     * Configura la RecyclerView con l'adapter per la lista utenti.
     * Imposta il listener per il click su un utente.
     */
    private void setupRecyclerView() {
        adapter = new UsersAdapter();
        adapter.setOnUserClickListener(this::showUserPermissionsDialog);
        recyclerUsers.setAdapter(adapter);
    }

    /**
     * Configura l'observer sulla lista utenti del ViewModel.
     * Gestisce la visibilita' della lista e del layout empty state.
     */
    private void observeData() {
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null && !users.isEmpty()) {
                adapter.submitList(users);
                recyclerUsers.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                recyclerUsers.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Mostra un dialog per la modifica dei permessi di un utente.
     * Permette di modificare i permessi RX1, RX2, VERIFY e CMD ON/OFF.
     * Include l'opzione per applicare i permessi a tutti gli utenti.
     *
     * @param user l'utente di cui modificare i permessi
     */
    private void showUserPermissionsDialog(User user) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_user_permissions, null);

        CheckBox checkRx1 = dialogView.findViewById(R.id.check_rx1);
        CheckBox checkRx2 = dialogView.findViewById(R.id.check_rx2);
        CheckBox checkVerify = dialogView.findViewById(R.id.check_verify);
        CheckBox checkCmd = dialogView.findViewById(R.id.check_cmd);
        CheckBox checkApplyAll = dialogView.findViewById(R.id.check_apply_all);

        checkRx1.setChecked(user.hasPermission(User.PERM_RX1));
        checkRx2.setChecked(user.hasPermission(User.PERM_RX2));
        checkVerify.setChecked(user.hasPermission(User.PERM_VERIFY));
        checkCmd.setChecked(user.hasPermission(User.PERM_CMD_ON_OFF));

        String title = user.getName().isEmpty()
                ? getString(R.string.user_slot_title, user.getSlot())
                : user.getName();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    int permissions = UsersViewModel.buildPermissions(
                            checkRx1.isChecked(),
                            checkRx2.isChecked(),
                            checkVerify.isChecked(),
                            checkCmd.isChecked()
                    );

                    if (checkApplyAll.isChecked()) {
                        viewModel.applyPermissionsToAll(permissions);
                    } else {
                        viewModel.updateUserPermissions(user, permissions);
                    }
                })
                .show();
    }
}
