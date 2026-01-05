package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.ConfigurationViewModel;
import it.bhomealarm.view.adapter.ConfigStepsAdapter;

/**
 * Fragment per la configurazione iniziale della centralina allarme tramite SMS.
 * <p>
 * Questo fragment guida l'utente attraverso il processo di configurazione iniziale,
 * inviando una serie di comandi SMS alla centralina per impostare i parametri di base.
 * <p>
 * Il flusso di configurazione prevede:
 * <ol>
 *     <li>L'utente avvia la configurazione toccando il pulsante START</li>
 *     <li>Il sistema invia una serie di SMS di configurazione</li>
 *     <li>Ogni step viene mostrato nella lista con il suo stato</li>
 *     <li>Il progresso viene visualizzato con barra e percentuale</li>
 *     <li>Al termine viene mostrato un messaggio di completamento</li>
 * </ol>
 * <p>
 * L'utente puo' annullare la configurazione in qualsiasi momento con conferma.
 * In caso di errore, viene offerta la possibilita' di riprovare.
 *
 * @see ConfigurationViewModel ViewModel che gestisce il processo di configurazione
 * @see ConfigStepsAdapter Adapter per la visualizzazione degli step di configurazione
 */
public class ConfigurationFragment extends Fragment {

    /** ViewModel per la gestione del processo di configurazione */
    private ConfigurationViewModel viewModel;

    /** Adapter per la lista degli step di configurazione */
    private ConfigStepsAdapter adapter;

    /** Toolbar con pulsante di navigazione indietro */
    private MaterialToolbar toolbar;

    /** Barra di progresso della configurazione */
    private LinearProgressIndicator progressBar;

    /** Testo che mostra la percentuale di completamento */
    private TextView textProgressPercent;

    /** Testo che mostra lo stato corrente della configurazione */
    private TextView textStatus;

    /** RecyclerView per visualizzare gli step di configurazione */
    private RecyclerView recyclerSteps;

    /** Card che contiene il log di debug */
    private MaterialCardView cardDebug;

    /** Testo che mostra il log di debug degli SMS */
    private TextView textDebugLog;

    /** Pulsante per annullare/chiudere la configurazione */
    private MaterialButton buttonCancel;

    /** Pulsante per avviare la configurazione */
    private MaterialButton buttonStart;

    /**
     * Inizializza il ViewModel all'avvio del Fragment.
     *
     * @param savedInstanceState stato salvato dell'istanza precedente, puo' essere null
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ConfigurationViewModel.class);
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
        return inflater.inflate(R.layout.fragment_configuration, container, false);
    }

    /**
     * Chiamato dopo che la view e' stata creata.
     * Inizializza le views, la RecyclerView, i listener e avvia l'osservazione dei dati.
     *
     * @param view la view root del fragment
     * @param savedInstanceState stato salvato dell'istanza precedente
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    /**
     * Inizializza i riferimenti alle views del layout.
     * Configura la toolbar con gestione della navigazione indietro durante la configurazione.
     *
     * @param view la view root del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        progressBar = view.findViewById(R.id.progress_bar);
        textProgressPercent = view.findViewById(R.id.text_progress_percent);
        textStatus = view.findViewById(R.id.text_status);
        recyclerSteps = view.findViewById(R.id.recycler_steps);
        cardDebug = view.findViewById(R.id.card_debug);
        textDebugLog = view.findViewById(R.id.text_debug_log);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonStart = view.findViewById(R.id.button_start);

        toolbar.setNavigationOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getIsRunning().getValue())) {
                showCancelConfirmation();
            } else {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    /**
     * Configura la RecyclerView con l'adapter per gli step di configurazione.
     */
    private void setupRecyclerView() {
        adapter = new ConfigStepsAdapter();
        recyclerSteps.setAdapter(adapter);
    }

    /**
     * Configura i listener per i pulsanti Start e Cancel.
     * Gestisce la richiesta di conferma se la configurazione e' in corso.
     */
    private void setupClickListeners() {
        buttonStart.setOnClickListener(v -> viewModel.startConfiguration());

        buttonCancel.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getIsRunning().getValue())) {
                showCancelConfirmation();
            } else {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    /**
     * Configura gli observer sui LiveData del ViewModel.
     * Osserva: progresso, messaggio stato, step, stato esecuzione, completamento, errori, log debug.
     */
    private void observeData() {
        viewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                progressBar.setProgress(progress);
                textProgressPercent.setText(getString(R.string.percent_format, progress));
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                textStatus.setText(message);
            }
        });

        viewModel.getSteps().observe(getViewLifecycleOwner(), steps -> {
            if (steps != null) {
                adapter.submitList(steps);
            }
        });

        viewModel.getIsRunning().observe(getViewLifecycleOwner(), isRunning -> {
            buttonStart.setEnabled(!isRunning);
            buttonStart.setText(isRunning ? R.string.config_running : R.string.action_start);
            buttonCancel.setEnabled(true);
        });

        viewModel.getIsComplete().observe(getViewLifecycleOwner(), isComplete -> {
            if (Boolean.TRUE.equals(isComplete)) {
                Snackbar.make(requireView(), R.string.config_complete, Snackbar.LENGTH_LONG).show();
                buttonStart.setEnabled(false);
                buttonCancel.setText(R.string.action_close);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_retry, v -> viewModel.startConfiguration())
                        .show();
            }
        });

        viewModel.getDebugLog().observe(getViewLifecycleOwner(), log -> {
            if (log != null && !log.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String line : log) {
                    sb.append(line).append("\n");
                }
                textDebugLog.setText(sb.toString());
                cardDebug.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Mostra un dialog di conferma per annullare la configurazione in corso.
     * Se l'utente conferma, la configurazione viene annullata e si torna indietro.
     */
    private void showCancelConfirmation() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_cancel_config_title)
                .setMessage(R.string.dialog_cancel_config_message)
                .setNegativeButton(R.string.action_no, null)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> {
                    viewModel.cancelConfiguration();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .show();
    }
}
