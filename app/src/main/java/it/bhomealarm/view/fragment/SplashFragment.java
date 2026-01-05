package it.bhomealarm.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import it.bhomealarm.BuildConfig;
import it.bhomealarm.R;
import it.bhomealarm.util.Constants;

/**
 * Fragment di splash screen che gestisce il flusso iniziale dell'applicazione.
 * <p>
 * Questo fragment viene mostrato all'avvio dell'app, visualizza il logo
 * e la versione dell'applicazione, poi determina automaticamente
 * verso quale schermata navigare in base allo stato di configurazione.
 * <p>
 * La logica di navigazione segue questo albero decisionale:
 * <ul>
 *     <li>Se il disclaimer NON e' stato accettato: naviga a {@link DisclaimerFragment}</li>
 *     <li>Se il disclaimer e' stato accettato MA il numero telefono NON e' configurato:
 *         naviga a {@link SetupPhoneFragment}</li>
 *     <li>Se tutto e' configurato: naviga a {@link HomeFragment}</li>
 * </ul>
 * <p>
 * La navigazione avviene automaticamente dopo un ritardo di 1.5 secondi,
 * permettendo all'utente di visualizzare brevemente lo splash screen.
 *
 * @see DisclaimerFragment Fragment per l'accettazione dei termini d'uso
 * @see SetupPhoneFragment Fragment per la configurazione del numero telefono
 * @see HomeFragment Fragment principale dell'applicazione
 */
public class SplashFragment extends Fragment {

    /** Durata di visualizzazione dello splash screen in millisecondi */
    private static final long SPLASH_DELAY = 1500L;

    /** SharedPreferences per verificare lo stato di configurazione */
    private SharedPreferences prefs;

    /** Handler per la navigazione ritardata */
    private final Handler handler = new Handler(Looper.getMainLooper());

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
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    /**
     * Chiamato dopo che la view e' stata creata.
     * Inizializza le views e programma la navigazione automatica.
     *
     * @param view la view root del fragment
     * @param savedInstanceState stato salvato dell'istanza precedente
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        scheduleNavigation();
    }

    /**
     * Inizializza la view della versione con il numero versione dell'app.
     *
     * @param view la view root del fragment
     */
    private void setupViews(View view) {
        TextView textVersion = view.findViewById(R.id.text_version);
        textVersion.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));
    }

    /**
     * Programma la navigazione automatica dopo il ritardo definito.
     */
    private void scheduleNavigation() {
        handler.postDelayed(this::navigateToNextScreen, SPLASH_DELAY);
    }

    /**
     * Determina e naviga verso la schermata appropriata in base allo stato di configurazione.
     * <ul>
     *     <li>Se disclaimer non accettato: naviga a DisclaimerFragment</li>
     *     <li>Se numero telefono non configurato: naviga a SetupPhoneFragment</li>
     *     <li>Altrimenti: naviga a HomeFragment</li>
     * </ul>
     */
    private void navigateToNextScreen() {
        if (!isAdded() || getView() == null) {
            return;
        }

        boolean disclaimerAccepted = prefs.getBoolean(Constants.PREF_DISCLAIMER_ACCEPTED, false);
        String alarmPhone = prefs.getString(Constants.PREF_ALARM_PHONE, "");

        int destinationId;

        if (!disclaimerAccepted) {
            // Primo avvio - mostra disclaimer
            destinationId = R.id.action_splash_to_disclaimer;
        } else if (alarmPhone.isEmpty()) {
            // Disclaimer accettato ma no numero configurato
            destinationId = R.id.action_splash_to_setup_phone;
        } else {
            // Tutto configurato - vai a home
            destinationId = R.id.action_splash_to_home;
        }

        Navigation.findNavController(requireView()).navigate(destinationId);
    }

    /**
     * Chiamato quando la view viene distrutta.
     * Rimuove eventuali callback pendenti per evitare memory leak e crash.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
