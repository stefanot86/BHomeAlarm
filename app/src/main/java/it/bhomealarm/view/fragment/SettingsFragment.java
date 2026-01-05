package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import it.bhomealarm.BuildConfig;
import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.SettingsViewModel;
import it.bhomealarm.service.SmsService;

/**
 * Fragment per la gestione delle impostazioni dell'applicazione.
 * <p>
 * Questo fragment permette all'utente di configurare tutti gli aspetti dell'app:
 * <ul>
 *     <li>Numero di telefono della centralina allarme</li>
 *     <li>Configurazione iniziale della centralina via SMS</li>
 *     <li>Gestione degli utenti e relativi permessi</li>
 *     <li>Gestione degli scenari di attivazione</li>
 *     <li>Selezione della SIM da utilizzare per gli SMS</li>
 *     <li>Visualizzazione informazioni app</li>
 * </ul>
 * <p>
 * Il flusso utente prevede la navigazione verso le varie sezioni
 * toccando le relative voci del menu impostazioni.
 *
 * @see SettingsViewModel ViewModel che gestisce le preferenze e la configurazione
 */
public class SettingsFragment extends Fragment {

    /** ViewModel per la gestione delle impostazioni */
    private SettingsViewModel viewModel;

    /** Toolbar con pulsante di navigazione indietro */
    private MaterialToolbar toolbar;

    /** Voce menu per configurare il numero di telefono dell'allarme */
    private LinearLayout itemPhoneNumber;

    /** Voce menu per avviare la configurazione iniziale */
    private LinearLayout itemConfigure;

    /** Voce menu per gestire gli utenti */
    private LinearLayout itemUsers;

    /** Voce menu per gestire gli scenari */
    private LinearLayout itemScenarios;

    /** Voce menu per selezionare la SIM */
    private LinearLayout itemSim;

    /** Voce menu per visualizzare informazioni app */
    private LinearLayout itemInfo;

    /** Testo che mostra il numero di telefono configurato */
    private TextView textPhoneNumber;

    /** Testo che mostra il numero di scenari configurati */
    private TextView textScenariosCount;

    /** Testo che mostra la SIM selezionata */
    private TextView textSim;

    /** Testo che mostra la versione dell'app */
    private TextView textVersion;

    /**
     * Inizializza il ViewModel all'avvio del Fragment.
     *
     * @param savedInstanceState stato salvato dell'istanza precedente, puo' essere null
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
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
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /**
     * Chiamato dopo che la view e' stata creata.
     * Inizializza le views, configura i listener e avvia l'osservazione dei dati.
     *
     * @param view la view root del fragment
     * @param savedInstanceState stato salvato dell'istanza precedente
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupClickListeners();
        observeData();
    }

    /**
     * Inizializza i riferimenti alle views del layout.
     * Configura la toolbar con navigazione indietro e imposta la versione app.
     *
     * @param view la view root del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        itemPhoneNumber = view.findViewById(R.id.item_phone_number);
        itemConfigure = view.findViewById(R.id.item_configure);
        itemUsers = view.findViewById(R.id.item_users);
        itemScenarios = view.findViewById(R.id.item_scenarios);
        itemSim = view.findViewById(R.id.item_sim);
        itemInfo = view.findViewById(R.id.item_info);
        textPhoneNumber = view.findViewById(R.id.text_phone_number);
        textScenariosCount = view.findViewById(R.id.text_scenarios_count);
        textSim = view.findViewById(R.id.text_sim);
        textVersion = view.findViewById(R.id.text_version);

        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        textVersion.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));
    }

    /**
     * Configura i listener per le voci del menu impostazioni.
     * Ogni voce naviga alla relativa sezione o mostra un dialog.
     */
    private void setupClickListeners() {
        itemPhoneNumber.setOnClickListener(v -> showPhoneInputDialog());

        itemConfigure.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_settings_to_configuration);
        });

        itemUsers.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_settings_to_users);
        });

        itemScenarios.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_settings_to_scenarios);
        });

        itemSim.setOnClickListener(v -> showSimSelectionDialog());

        itemInfo.setOnClickListener(v -> showInfoDialog());
    }

    /**
     * Configura gli observer sui LiveData del ViewModel.
     * Osserva: numero telefono e SIM selezionata per aggiornare l'UI.
     */
    private void observeData() {
        viewModel.getAlarmPhoneNumber().observe(getViewLifecycleOwner(), phone -> {
            if (phone != null && !phone.isEmpty()) {
                textPhoneNumber.setText(phone);
            } else {
                textPhoneNumber.setText(R.string.not_configured);
            }
        });

        viewModel.getSelectedSimSlot().observe(getViewLifecycleOwner(), simSlot -> {
            if (simSlot != null && simSlot >= 0) {
                List<SmsService.SimInfo> sims = SmsService.getInstance(requireContext()).getAvailableSims();
                if (simSlot < sims.size()) {
                    SmsService.SimInfo sim = sims.get(simSlot);
                    String displayText = sim.carrierName.isEmpty() ? sim.displayName : sim.carrierName;
                    textSim.setText(displayText);
                } else {
                    textSim.setText(getString(R.string.sim_slot_format, simSlot + 1));
                }
            } else {
                textSim.setText(R.string.sim_default);
            }
        });
    }

    /**
     * Mostra un dialog per l'inserimento o modifica del numero telefono allarme.
     * Il numero esistente viene pre-compilato nel campo di input.
     */
    private void showPhoneInputDialog() {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint(R.string.hint_phone_number);
        input.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        input.setPadding(48, 32, 48, 32);

        String currentPhone = viewModel.getAlarmPhoneNumber().getValue();
        if (currentPhone != null) {
            input.setText(currentPhone);
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_phone_title)
                .setView(input)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String phone = input.getText().toString().trim();
                    if (!phone.isEmpty()) {
                        viewModel.saveAlarmPhoneNumber(phone);
                    }
                })
                .show();
    }

    /**
     * Mostra un dialog per la selezione della SIM da utilizzare per gli SMS.
     * Elenca tutte le SIM disponibili sul dispositivo piu' l'opzione default.
     */
    private void showSimSelectionDialog() {
        List<SmsService.SimInfo> sims = SmsService.getInstance(requireContext()).getAvailableSims();

        // Crea array con nomi operatori + opzione default
        String[] simOptions = new String[sims.size() + 1];
        for (int i = 0; i < sims.size(); i++) {
            SmsService.SimInfo sim = sims.get(i);
            String carrierName = sim.carrierName.isEmpty() ? sim.displayName : sim.carrierName;
            simOptions[i] = getString(R.string.sim_slot_format, i + 1) + " - " + carrierName;
        }
        simOptions[sims.size()] = getString(R.string.sim_default);

        Integer currentSim = viewModel.getSelectedSimSlot().getValue();
        int checkedItem = currentSim != null && currentSim >= 0 && currentSim < sims.size()
                ? currentSim
                : sims.size(); // Default option

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_sim_title)
                .setSingleChoiceItems(simOptions, checkedItem, (dialog, which) -> {
                    viewModel.saveSelectedSim(which < sims.size() ? which : -1);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    /**
     * Mostra un dialog informativo con nome app e versione.
     */
    private void showInfoDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.info_message, BuildConfig.VERSION_NAME))
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }
}
