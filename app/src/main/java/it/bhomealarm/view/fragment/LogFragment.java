package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.LogViewModel;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.view.adapter.SmsLogAdapter;

/**
 * Fragment per la visualizzazione dello storico dei messaggi SMS scambiati con la centralina.
 * <p>
 * Questo fragment mostra una lista cronologica di tutti gli SMS inviati e ricevuti,
 * permettendo all'utente di:
 * <ul>
 *     <li>Visualizzare lo storico completo delle comunicazioni</li>
 *     <li>Vedere i dettagli di ogni messaggio (direzione, contenuto, stato)</li>
 *     <li>Cancellare lo storico dei log</li>
 * </ul>
 * <p>
 * Gli stati possibili per un messaggio sono:
 * <ul>
 *     <li>PENDING - In attesa di invio</li>
 *     <li>SENT - Inviato</li>
 *     <li>DELIVERED - Consegnato</li>
 *     <li>RECEIVED - Ricevuto dalla centralina</li>
 *     <li>FAILED - Invio fallito</li>
 * </ul>
 * <p>
 * Il flusso utente prevede:
 * <ol>
 *     <li>Visualizzazione della lista log in ordine cronologico</li>
 *     <li>Tocco su un log per vedere i dettagli completi</li>
 *     <li>Utilizzo del menu per cancellare tutti i log</li>
 * </ol>
 *
 * @see LogViewModel ViewModel che gestisce il recupero e la gestione dei log
 * @see SmsLogAdapter Adapter per la visualizzazione della lista log
 * @see SmsLog Entita' che rappresenta un singolo log SMS
 */
public class LogFragment extends Fragment {

    /** ViewModel per la gestione dei log SMS */
    private LogViewModel viewModel;

    /** Adapter per la lista dei log SMS */
    private SmsLogAdapter adapter;

    /** Toolbar con menu per cancellare i log */
    private MaterialToolbar toolbar;

    /** RecyclerView per visualizzare la lista dei log */
    private RecyclerView recyclerLogs;

    /** Layout mostrato quando non ci sono log */
    private LinearLayout layoutEmpty;

    /**
     * Inizializza il ViewModel all'avvio del Fragment.
     *
     * @param savedInstanceState stato salvato dell'istanza precedente, puo' essere null
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LogViewModel.class);
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
        return inflater.inflate(R.layout.fragment_log, container, false);
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
     * Configura la toolbar con navigazione indietro e menu per cancellare i log.
     *
     * @param view la view root del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerLogs = view.findViewById(R.id.recycler_logs);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear) {
                showClearConfirmation();
                return true;
            }
            return false;
        });
    }

    /**
     * Configura la RecyclerView con l'adapter per la lista dei log SMS.
     * Imposta il listener per il click su un log.
     */
    private void setupRecyclerView() {
        adapter = new SmsLogAdapter();
        adapter.setOnLogClickListener(this::showLogDetails);
        recyclerLogs.setAdapter(adapter);
    }

    /**
     * Configura l'observer sulla lista log del ViewModel.
     * Gestisce la visibilita' della lista e del layout empty state.
     */
    private void observeData() {
        viewModel.getSmsLogs().observe(getViewLifecycleOwner(), logs -> {
            if (logs != null && !logs.isEmpty()) {
                adapter.submitList(logs);
                recyclerLogs.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                recyclerLogs.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Mostra un dialog con i dettagli completi di un log SMS.
     * Visualizza direzione, contenuto del messaggio e stato.
     *
     * @param log il log SMS di cui mostrare i dettagli
     */
    private void showLogDetails(SmsLog log) {
        String direction = log.isOutgoing()
                ? getString(R.string.log_direction_outgoing)
                : getString(R.string.log_direction_incoming);

        String status;
        switch (log.getStatus()) {
            case SmsLog.STATUS_SENT:
                status = getString(R.string.log_status_sent);
                break;
            case SmsLog.STATUS_DELIVERED:
                status = getString(R.string.log_status_delivered);
                break;
            case SmsLog.STATUS_RECEIVED:
                status = getString(R.string.log_status_received);
                break;
            case SmsLog.STATUS_FAILED:
                status = getString(R.string.log_status_failed);
                break;
            default:
                status = getString(R.string.log_status_pending);
                break;
        }

        String message = getString(R.string.log_detail_format,
                direction, log.getMessage(), status);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_log_detail_title)
                .setMessage(message)
                .setPositiveButton(R.string.action_close, null)
                .show();
    }

    /**
     * Mostra un dialog di conferma per cancellare tutti i log SMS.
     * Se l'utente conferma, tutti i log vengono eliminati.
     */
    private void showClearConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_clear_logs_title)
                .setMessage(R.string.dialog_clear_logs_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_clear, (dialog, which) -> {
                    viewModel.clearAllLogs();
                    Snackbar.make(requireView(), R.string.logs_cleared, Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }
}
