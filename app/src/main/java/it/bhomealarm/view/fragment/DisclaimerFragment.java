package it.bhomealarm.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.bhomealarm.R;
import it.bhomealarm.util.Constants;

/**
 * Fragment per la visualizzazione e accettazione del disclaimer e dei termini d'uso.
 * <p>
 * Questo fragment viene mostrato al primo avvio dell'applicazione e richiede
 * all'utente di leggere e accettare i termini d'uso prima di poter procedere.
 * <p>
 * Il flusso utente prevede:
 * <ol>
 *     <li>L'utente legge il testo del disclaimer</li>
 *     <li>L'utente spunta la checkbox di accettazione</li>
 *     <li>Il pulsante ACCETTA si abilita solo dopo aver spuntato la checkbox</li>
 *     <li>Toccando ACCETTA, l'accettazione viene salvata e si naviga a {@link SetupPhoneFragment}</li>
 *     <li>Toccando RIFIUTA, viene mostrato un dialog di conferma e l'app si chiude</li>
 * </ol>
 * <p>
 * L'accettazione del disclaimer viene salvata nelle SharedPreferences e non
 * verra' piu' richiesta nei successivi avvii dell'applicazione.
 *
 * @see SetupPhoneFragment Fragment successivo nel flusso di setup
 * @see SplashFragment Fragment che determina se mostrare questo disclaimer
 */
public class DisclaimerFragment extends Fragment {

    /** SharedPreferences per salvare l'accettazione del disclaimer */
    private SharedPreferences prefs;

    /** Toolbar della schermata (senza navigazione indietro) */
    private MaterialToolbar toolbar;

    /** Checkbox che l'utente deve spuntare per accettare i termini */
    private MaterialCheckBox checkboxAccept;

    /** Pulsante per rifiutare i termini e chiudere l'app */
    private MaterialButton buttonDecline;

    /** Pulsante per accettare i termini e procedere */
    private MaterialButton buttonAccept;

    /**
     * Inizializza le SharedPreferences all'avvio del Fragment.
     *
     * @param savedInstanceState stato salvato dell'istanza precedente, puo' essere null
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, 0);
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
        return inflater.inflate(R.layout.fragment_disclaimer, container, false);
    }

    /**
     * Chiamato dopo che la view e' stata creata.
     * Inizializza le views e configura i listener.
     *
     * @param view la view root del fragment
     * @param savedInstanceState stato salvato dell'istanza precedente
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupClickListeners();
    }

    /**
     * Inizializza i riferimenti alle views del layout.
     * Disabilita il pulsante ACCETTA finche' la checkbox non viene selezionata.
     *
     * @param view la view root del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        checkboxAccept = view.findViewById(R.id.checkbox_accept);
        buttonDecline = view.findViewById(R.id.button_decline);
        buttonAccept = view.findViewById(R.id.button_accept);

        // Abilita bottone accetta solo se checkbox selezionato
        buttonAccept.setEnabled(false);
    }

    /**
     * Configura i listener per la checkbox e i pulsanti.
     * <ul>
     *     <li>Checkbox: abilita/disabilita il pulsante ACCETTA</li>
     *     <li>Pulsante RIFIUTA: mostra dialog di conferma chiusura app</li>
     *     <li>Pulsante ACCETTA: salva accettazione e naviga avanti</li>
     * </ul>
     */
    private void setupClickListeners() {
        checkboxAccept.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonAccept.setEnabled(isChecked);
        });

        buttonDecline.setOnClickListener(v -> showDeclineConfirmation());

        buttonAccept.setOnClickListener(v -> acceptAndContinue());
    }

    /**
     * Mostra un dialog di conferma per il rifiuto dei termini.
     * Se l'utente conferma, l'applicazione viene chiusa.
     */
    private void showDeclineConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_decline_title)
                .setMessage(R.string.dialog_decline_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_close, (dialog, which) -> {
                    // Chiudi l'applicazione
                    requireActivity().finishAffinity();
                })
                .show();
    }

    /**
     * Salva l'accettazione del disclaimer nelle SharedPreferences
     * e naviga al fragment di configurazione numero telefono.
     */
    private void acceptAndContinue() {
        // Salva accettazione disclaimer
        prefs.edit()
                .putBoolean(Constants.PREF_DISCLAIMER_ACCEPTED, true)
                .apply();

        // Naviga a SetupPhoneFragment
        Navigation.findNavController(requireView())
                .navigate(R.id.action_disclaimer_to_setup_phone);
    }
}
