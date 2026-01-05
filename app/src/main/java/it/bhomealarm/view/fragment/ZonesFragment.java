package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.HomeViewModel;
import it.bhomealarm.controller.viewmodel.ZonesViewModel;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.view.adapter.ZonesAdapter;

/**
 * Fragment per la creazione di scenari personalizzati.
 * <p>
 * Questa schermata permette all'utente di:
 * <ul>
 *     <li>Inserire un nome per lo scenario personalizzato</li>
 *     <li>Selezionare le zone da includere nello scenario</li>
 *     <li>Salvare lo scenario nel database locale</li>
 *     <li>Opzionalmente attivare immediatamente l'allarme con lo scenario creato</li>
 * </ul>
 * <p>
 * Il flusso di utilizzo è:
 * <ol>
 *     <li>L'utente inserisce un nome per lo scenario</li>
 *     <li>L'utente seleziona una o più zone dalla lista</li>
 *     <li>L'utente preme "Salva"</li>
 *     <li>Appare un dialog che chiede se attivare l'allarme</li>
 *     <li>In entrambi i casi, l'utente torna alla schermata home</li>
 * </ol>
 *
 * @see ZonesViewModel
 * @see ZonesAdapter
 */
public class ZonesFragment extends Fragment {

    /** ViewModel per la gestione delle zone e del salvataggio */
    private ZonesViewModel viewModel;

    /** ViewModel condiviso per l'invio di comandi all'allarme */
    private HomeViewModel homeViewModel;

    /** Adapter per la lista delle zone */
    private ZonesAdapter adapter;

    // ========== Riferimenti alle View ==========

    /** Toolbar con titolo e pulsante indietro */
    private MaterialToolbar toolbar;

    /** Container per il campo di input del nome scenario */
    private TextInputLayout inputLayoutName;

    /** Campo di input per il nome dello scenario */
    private TextInputEditText inputScenarioName;

    /** Lista delle zone selezionabili */
    private RecyclerView recyclerZones;

    /** Testo che mostra il conteggio delle zone selezionate */
    private TextView textSelectedCount;

    /** Pulsante per annullare e tornare indietro */
    private MaterialButton buttonCancel;

    /** Pulsante per salvare lo scenario */
    private MaterialButton buttonSave;

    /** Handler per eseguire operazioni sul thread principale */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Inizializza i ViewModel.
     * ZonesViewModel è locale al fragment, HomeViewModel è condiviso con l'activity.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ZonesViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    /**
     * Inizializza i riferimenti alle view e configura la toolbar.
     * Imposta anche il TextWatcher per il campo nome scenario.
     *
     * @param view La root view del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_create_scenario);
        inputLayoutName = view.findViewById(R.id.input_layout_name);
        inputScenarioName = view.findViewById(R.id.input_scenario_name);
        recyclerZones = view.findViewById(R.id.recycler_zones);
        textSelectedCount = view.findViewById(R.id.text_selected_count);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonSave = view.findViewById(R.id.button_save);

        // Configura la navigazione indietro dalla toolbar
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        // TextWatcher per aggiornare il nome scenario nel ViewModel
        // e abilitare/disabilitare il pulsante Salva
        inputScenarioName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setScenarioName(s.toString());
                inputLayoutName.setError(null);  // Rimuove l'errore quando l'utente digita
                updateSaveButtonState();
            }
        });
    }

    /**
     * Configura la RecyclerView con l'adapter delle zone.
     * Imposta il listener per la selezione/deselezione delle zone.
     */
    private void setupRecyclerView() {
        adapter = new ZonesAdapter();
        adapter.setOnZoneToggleListener((zone, selected) -> {
            viewModel.setZoneSelected(zone.getSlot(), selected);
        });
        recyclerZones.setAdapter(adapter);
    }

    /**
     * Configura i listener per i pulsanti Annulla e Salva.
     */
    private void setupClickListeners() {
        buttonCancel.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        buttonSave.setOnClickListener(v -> saveScenario());
    }

    /**
     * Configura gli observer per i LiveData del ViewModel.
     * Osserva le zone, il conteggio selezioni, le zone selezionate e lo stato di salvataggio.
     */
    private void observeData() {
        // Osserva la lista delle zone e aggiorna l'adapter
        viewModel.getZones().observe(getViewLifecycleOwner(), zones -> {
            if (zones != null) {
                adapter.submitList(zones);
            }
        });

        // Osserva il conteggio delle zone selezionate e aggiorna il testo
        viewModel.getSelectedCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                textSelectedCount.setText(getResources().getQuantityString(
                        R.plurals.zones_selected_count, count, count));
                updateSaveButtonState();
            }
        });

        // Osserva le zone selezionate e aggiorna i checkbox nell'adapter
        viewModel.getSelectedZones().observe(getViewLifecycleOwner(), selectedZones -> {
            if (selectedZones != null) {
                adapter.setSelectedZones(selectedZones);
            }
        });

        // Osserva lo stato di salvataggio e disabilita i pulsanti durante il salvataggio
        viewModel.getIsSaving().observe(getViewLifecycleOwner(), isSaving -> {
            buttonSave.setEnabled(!isSaving && canSave());
            buttonCancel.setEnabled(!isSaving);
        });
    }

    /**
     * Aggiorna lo stato del pulsante Salva in base alla validità dei dati inseriti.
     */
    private void updateSaveButtonState() {
        buttonSave.setEnabled(canSave());
    }

    /**
     * Verifica se i dati inseriti sono validi per il salvataggio.
     *
     * @return true se è stato inserito un nome e almeno una zona è selezionata
     */
    private boolean canSave() {
        String name = viewModel.getScenarioName().getValue();
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasZones = viewModel.hasSelection();
        return hasName && hasZones;
    }

    /**
     * Esegue la validazione e il salvataggio dello scenario personalizzato.
     * <p>
     * Verifica che:
     * <ul>
     *     <li>Il nome dello scenario sia stato inserito</li>
     *     <li>Almeno una zona sia stata selezionata</li>
     * </ul>
     * <p>
     * Se la validazione passa, salva lo scenario nel database e mostra
     * il dialog per chiedere se attivare l'allarme.
     */
    private void saveScenario() {
        // Valida nome
        String name = viewModel.getScenarioName().getValue();
        if (name == null || name.trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.error_scenario_name_required));
            inputScenarioName.requestFocus();
            return;
        }

        // Valida zone
        if (!viewModel.hasSelection()) {
            Snackbar.make(requireView(), R.string.error_zones_required, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Salva lo scenario nel database
        // Il callback viene eseguito in background, quindi usiamo mainHandler
        // per mostrare il dialog sul thread principale
        viewModel.saveCustomScenario(scenario -> {
            mainHandler.post(() -> showArmAfterSaveDialog(scenario));
        });
    }

    /**
     * Mostra il dialog che chiede all'utente se vuole attivare l'allarme
     * con lo scenario appena salvato.
     * <p>
     * Se l'utente conferma, invia il comando CUST: con i numeri delle zone.
     * In entrambi i casi, torna alla schermata home.
     *
     * @param scenario Lo scenario appena salvato
     */
    private void showArmAfterSaveDialog(Scenario scenario) {
        // Recupera la stringa dei numeri zone per il comando CUST:
        String zoneNumbers = viewModel.getZoneNumbersString();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_arm_after_save_title)
                .setMessage(getString(R.string.dialog_arm_after_save_message, scenario.getName()))
                .setNegativeButton(R.string.action_no, (dialog, which) -> {
                    // Torna alla home senza armare
                    Navigation.findNavController(requireView())
                            .popBackStack(R.id.homeFragment, false);
                })
                .setPositiveButton(R.string.action_yes, (dialog, which) -> {
                    // Invia comando di attivazione e torna alla home
                    homeViewModel.armWithCustomZones(zoneNumbers);
                    Navigation.findNavController(requireView())
                            .popBackStack(R.id.homeFragment, false);
                })
                .setCancelable(false)  // L'utente deve scegliere un'opzione
                .show();
    }
}
