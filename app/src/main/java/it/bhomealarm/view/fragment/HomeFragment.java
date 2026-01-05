package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.HomeViewModel;
import it.bhomealarm.util.Constants;

/**
 * Fragment principale dell'applicazione per il controllo dell'allarme.
 * <p>
 * Questo fragment rappresenta la schermata home e permette all'utente di:
 * <ul>
 *     <li>Visualizzare lo stato corrente dell'allarme (armato, disarmato, allarme, manomissione)</li>
 *     <li>Attivare l'allarme selezionando uno scenario</li>
 *     <li>Disattivare l'allarme con conferma</li>
 *     <li>Verificare lo stato dell'allarme inviando un SMS</li>
 * </ul>
 * <p>
 * Il flusso utente prevede:
 * <ol>
 *     <li>L'utente visualizza lo stato corrente dell'allarme</li>
 *     <li>Puo' attivare l'allarme toccando la card ARM (naviga alla selezione scenari)</li>
 *     <li>Puo' disattivare l'allarme toccando la card DISARM (con dialog di conferma)</li>
 *     <li>Puo' verificare lo stato toccando la card CHECK STATUS</li>
 *     <li>Puo' accedere alle impostazioni dalla toolbar</li>
 * </ol>
 *
 * @see HomeViewModel ViewModel che gestisce la logica di business e lo stato dell'allarme
 */
public class HomeFragment extends Fragment {

    /** ViewModel per la gestione dello stato e delle operazioni sull'allarme */
    private HomeViewModel viewModel;

    /** Toolbar principale con accesso alle impostazioni */
    private MaterialToolbar toolbar;

    /** Card che mostra lo stato corrente dell'allarme */
    private MaterialCardView cardStatus;

    /** Card per attivare l'allarme (naviga alla selezione scenari) */
    private MaterialCardView cardArm;

    /** Card per disattivare l'allarme */
    private MaterialCardView cardDisarm;

    /** Card per verificare lo stato dell'allarme via SMS */
    private MaterialCardView cardCheckStatus;

    /** Icona che rappresenta visivamente lo stato dell'allarme */
    private ImageView iconStatus;

    /** Testo che descrive lo stato corrente dell'allarme */
    private TextView textStatus;

    /** Testo che mostra data/ora dell'ultimo controllo stato */
    private TextView textLastCheck;

    /** Indicatore di progresso per operazioni in corso */
    private LinearProgressIndicator progressIndicator;

    /**
     * Inizializza il ViewModel all'avvio del Fragment.
     *
     * @param savedInstanceState stato salvato dell'istanza precedente, puo' essere null
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
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
        return inflater.inflate(R.layout.fragment_home, container, false);
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
     * Chiamato quando il fragment torna in primo piano.
     * Ricarica lo stato dell'allarme per gestire eventuali SMS ricevuti in background.
     */
    @Override
    public void onResume() {
        super.onResume();
        // Ricarica lo stato in caso di SMS ricevuti mentre l'app era in background
        viewModel.refreshStatus();
    }

    /**
     * Inizializza i riferimenti alle views del layout e configura la toolbar.
     *
     * @param view la view root del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        cardStatus = view.findViewById(R.id.card_status);
        cardArm = view.findViewById(R.id.card_arm);
        cardDisarm = view.findViewById(R.id.card_disarm);
        cardCheckStatus = view.findViewById(R.id.card_check_status);
        iconStatus = view.findViewById(R.id.icon_status);
        textStatus = view.findViewById(R.id.text_status);
        textLastCheck = view.findViewById(R.id.text_last_check);
        progressIndicator = view.findViewById(R.id.progress_indicator);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_settings);
                return true;
            }
            return false;
        });
    }

    /**
     * Configura i listener per le interazioni utente sulle card.
     * <ul>
     *     <li>Card ARM: naviga alla selezione scenari</li>
     *     <li>Card DISARM: mostra dialog di conferma disattivazione</li>
     *     <li>Card CHECK STATUS: richiede verifica stato via SMS</li>
     * </ul>
     */
    private void setupClickListeners() {
        cardArm.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_to_scenarios);
        });

        cardDisarm.setOnClickListener(v -> showDisarmConfirmation());

        cardCheckStatus.setOnClickListener(v -> viewModel.checkStatus());
    }

    /**
     * Configura gli observer sui LiveData del ViewModel.
     * Osserva: stato allarme, ultimo controllo, stato caricamento, messaggi di errore.
     */
    private void observeData() {
        viewModel.getAlarmStatus().observe(getViewLifecycleOwner(), this::updateStatusUI);

        viewModel.getLastCheckTime().observe(getViewLifecycleOwner(), time -> {
            if (time != null && !time.isEmpty()) {
                textLastCheck.setText(getString(R.string.last_check_format, time));
                textLastCheck.setVisibility(View.VISIBLE);
            } else {
                textLastCheck.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            cardArm.setEnabled(!isLoading);
            cardDisarm.setEnabled(!isLoading);
            cardCheckStatus.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_dismiss, v -> viewModel.clearError())
                        .show();
            }
        });
    }

    /**
     * Aggiorna l'interfaccia utente in base allo stato dell'allarme.
     * Imposta icona, colore e testo appropriati per ogni stato.
     *
     * @param status lo stato corrente dell'allarme (ARMED, DISARMED, ALARM, TAMPER, UNKNOWN)
     */
    private void updateStatusUI(String status) {
        if (status == null) {
            status = Constants.STATUS_UNKNOWN;
        }

        int iconRes;
        int colorRes;
        int textRes;

        switch (status) {
            case Constants.STATUS_ARMED:
                iconRes = R.drawable.ic_lock;
                colorRes = R.color.md_theme_light_primary;
                textRes = R.string.status_armed;
                break;
            case Constants.STATUS_DISARMED:
                iconRes = R.drawable.ic_lock_open;
                colorRes = R.color.md_theme_light_secondary;
                textRes = R.string.status_disarmed;
                break;
            case Constants.STATUS_ALARM:
                iconRes = R.drawable.ic_warning;
                colorRes = R.color.md_theme_light_error;
                textRes = R.string.status_alarm;
                break;
            case Constants.STATUS_TAMPER:
                iconRes = R.drawable.ic_warning;
                colorRes = R.color.md_theme_light_error;
                textRes = R.string.status_tamper;
                break;
            default:
                iconRes = R.drawable.ic_status_unknown;
                colorRes = R.color.md_theme_light_onSurfaceVariant;
                textRes = R.string.status_unknown;
                break;
        }

        iconStatus.setImageResource(iconRes);
        iconStatus.setColorFilter(ContextCompat.getColor(requireContext(), colorRes));
        textStatus.setText(textRes);
        textStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    /**
     * Mostra un dialog di conferma prima di disattivare l'allarme.
     * Se l'utente conferma, viene richiesta la disattivazione al ViewModel.
     */
    private void showDisarmConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_disarm_title)
                .setMessage(R.string.dialog_disarm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_disarm, (dialog, which) -> viewModel.disarm())
                .show();
    }
}
