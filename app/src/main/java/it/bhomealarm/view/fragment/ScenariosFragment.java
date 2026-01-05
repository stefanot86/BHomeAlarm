package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.HomeViewModel;
import it.bhomealarm.controller.viewmodel.ScenariosViewModel;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.view.adapter.ScenariosAdapter;

/**
 * Fragment per la selezione e gestione degli scenari di attivazione allarme.
 * <p>
 * Questa schermata mostra:
 * <ul>
 *     <li>La lista degli scenari predefiniti (scaricati dall'allarme)</li>
 *     <li>La lista degli scenari personalizzati (creati localmente)</li>
 *     <li>Un pulsante per creare nuovi scenari personalizzati</li>
 * </ul>
 * <p>
 * Interazioni supportate:
 * <ul>
 *     <li><b>Tap</b> su uno scenario: mostra dialog di conferma per attivare l'allarme</li>
 *     <li><b>Long press</b> su uno scenario: mostra dialog per eliminare lo scenario</li>
 *     <li><b>Tap</b> su "Crea scenario": naviga a {@link ZonesFragment}</li>
 * </ul>
 * <p>
 * Il comando SMS inviato dipende dal tipo di scenario:
 * <ul>
 *     <li>Scenario predefinito: {@code SCE:XX} dove XX è il numero slot</li>
 *     <li>Scenario personalizzato: {@code CUST:NNN} dove NNN sono i numeri delle zone</li>
 * </ul>
 *
 * @see ScenariosViewModel
 * @see ScenariosAdapter
 * @see ZonesFragment
 */
public class ScenariosFragment extends Fragment {

    /** ViewModel per la gestione degli scenari */
    private ScenariosViewModel viewModel;

    /** ViewModel condiviso per l'invio dei comandi all'allarme */
    private HomeViewModel homeViewModel;

    /** Adapter per la lista degli scenari */
    private ScenariosAdapter adapter;

    // ========== Riferimenti alle View ==========

    /** Toolbar con titolo e pulsante indietro */
    private MaterialToolbar toolbar;

    /** Lista degli scenari disponibili */
    private RecyclerView recyclerScenarios;

    /** Card per la creazione di scenari personalizzati */
    private MaterialCardView cardCustom;

    /**
     * Inizializza i ViewModel.
     * ScenariosViewModel è locale, HomeViewModel è condiviso con l'activity.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ScenariosViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scenarios, container, false);
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
     * Inizializza i riferimenti alle view e configura la navigazione.
     *
     * @param view La root view del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerScenarios = view.findViewById(R.id.recycler_scenarios);
        cardCustom = view.findViewById(R.id.card_custom);

        // Configura la navigazione indietro dalla toolbar
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    /**
     * Configura la RecyclerView con l'adapter degli scenari.
     * Imposta i listener per tap e long press sugli item.
     */
    private void setupRecyclerView() {
        adapter = new ScenariosAdapter();
        adapter.setOnScenarioClickListener(this::onScenarioSelected);
        adapter.setOnScenarioLongClickListener(this::onScenarioLongPressed);
        recyclerScenarios.setAdapter(adapter);
    }

    /**
     * Configura il click listener per la card "Crea scenario".
     * Naviga alla schermata di selezione zone.
     */
    private void setupClickListeners() {
        cardCustom.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_scenarios_to_zones);
        });
    }

    /**
     * Configura l'observer per la lista degli scenari.
     */
    private void observeData() {
        viewModel.getScenarios().observe(getViewLifecycleOwner(), scenarios -> {
            if (scenarios != null) {
                adapter.submitList(scenarios);
            }
        });
    }

    /**
     * Gestisce il tap su uno scenario.
     * <p>
     * Mostra un dialog di conferma. Se l'utente conferma, invia il comando
     * di attivazione appropriato in base al tipo di scenario:
     * <ul>
     *     <li>Scenario predefinito: comando {@code SCE:XX}</li>
     *     <li>Scenario personalizzato: comando {@code CUST:NNN}</li>
     * </ul>
     *
     * @param scenario Lo scenario selezionato
     */
    private void onScenarioSelected(Scenario scenario) {
        // Determina il titolo da mostrare nel dialog
        String title = scenario.getName().isEmpty()
                ? getString(R.string.scenario_slot_title, scenario.getSlot())
                : scenario.getName();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_arm_title)
                .setMessage(getString(R.string.dialog_arm_message, title))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_arm, (dialog, which) -> {
                    if (scenario.isCustom()) {
                        // Scenario custom: invia CUST: con i numeri delle zone
                        // Esempio: CUST:134 per attivare zone 1, 3 e 4
                        String zoneNumbers = getZoneNumbersFromMask(scenario.getZoneMask());
                        homeViewModel.armWithCustomZones(zoneNumbers);
                    } else {
                        // Scenario predefinito: invia SCE:XX
                        // Esempio: SCE:03 per attivare scenario 3
                        homeViewModel.armWithScenario(scenario.getSlot());
                    }
                    // Torna alla home
                    Navigation.findNavController(requireView())
                            .popBackStack(R.id.homeFragment, false);
                })
                .show();
    }

    /**
     * Converte un bitmask delle zone in una stringa di numeri.
     * <p>
     * La stringa risultante contiene i numeri delle zone attive in ordine crescente,
     * pronti per essere inviati con il comando CUST:.
     * <p>
     * Esempio di conversione:
     * <ul>
     *     <li>mask = 0b00001101 (13 decimale) → "134" (zone 1, 3, 4)</li>
     *     <li>mask = 0b11111111 (255 decimale) → "12345678" (tutte le zone)</li>
     *     <li>mask = 0b00000001 (1 decimale) → "1" (solo zona 1)</li>
     * </ul>
     *
     * @param mask Bitmask delle zone (bit 0 = zona 1, bit 7 = zona 8)
     * @return Stringa con i numeri delle zone concatenati
     */
    private String getZoneNumbersFromMask(int mask) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 8; i++) {
            if ((mask & (1 << (i - 1))) != 0) {
                sb.append(i);
            }
        }
        return sb.toString();
    }

    /**
     * Gestisce il long press su uno scenario.
     * <p>
     * Mostra un dialog di conferma per l'eliminazione dello scenario.
     * Funziona sia per scenari predefiniti che personalizzati.
     * <p>
     * Nota: gli scenari predefiniti eliminati possono essere ripristinati
     * eseguendo nuovamente la configurazione CONF2/CONF3 dall'allarme.
     *
     * @param scenario Lo scenario su cui è stato fatto long press
     */
    private void onScenarioLongPressed(Scenario scenario) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_delete_scenario_title)
                .setMessage(getString(R.string.dialog_delete_scenario_message, scenario.getName()))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    viewModel.deleteScenario(scenario);
                })
                .show();
    }
}
