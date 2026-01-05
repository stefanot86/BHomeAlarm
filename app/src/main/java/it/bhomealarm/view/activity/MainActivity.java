package it.bhomealarm.view.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.bhomealarm.R;

/**
 * Activity principale dell'applicazione BHomeAlarm.
 * <p>
 * Questa Activity funge da container per tutti i Fragment dell'applicazione, utilizzando
 * il Navigation Component di Android Jetpack per gestire la navigazione tra le diverse
 * schermate dell'app.
 * </p>
 *
 * <h2>Architettura Single-Activity:</h2>
 * <p>
 * L'applicazione segue il pattern Single-Activity dove {@code MainActivity} e l'unica
 * Activity e tutti i contenuti vengono visualizzati tramite Fragment. Questo approccio
 * offre diversi vantaggi:
 * </p>
 * <ul>
 *   <li>Navigazione fluida e animazioni tra le schermate</li>
 *   <li>Gestione centralizzata dello stato di navigazione</li>
 *   <li>Migliore gestione del back stack</li>
 *   <li>Condivisione semplificata dei dati tra Fragment</li>
 * </ul>
 *
 * <h2>Componenti principali:</h2>
 * <ul>
 *   <li><b>NavHostFragment</b>: Container che ospita i Fragment di navigazione</li>
 *   <li><b>BottomNavigationView</b>: Barra di navigazione inferiore per le sezioni principali</li>
 *   <li><b>NavController</b>: Gestisce la navigazione tra i Fragment</li>
 * </ul>
 *
 * <h2>Gestione dei permessi:</h2>
 * <p>
 * All'avvio, l'Activity verifica e richiede i permessi necessari al funzionamento dell'app:
 * </p>
 * <ul>
 *   <li>SEND_SMS - Per inviare comandi alla centrale di allarme</li>
 *   <li>RECEIVE_SMS - Per ricevere risposte dalla centrale</li>
 *   <li>READ_SMS - Per leggere i messaggi ricevuti</li>
 *   <li>READ_PHONE_STATE - Per gestire lo stato del telefono</li>
 *   <li>POST_NOTIFICATIONS - Per mostrare notifiche (Android 13+)</li>
 * </ul>
 *
 * @author BHomeAlarm Team
 * @version 1.0
 * @see AppCompatActivity
 * @see NavController
 * @see NavHostFragment
 */
public class MainActivity extends AppCompatActivity {

    /** Controller per la gestione della navigazione tra i Fragment. */
    private NavController navController;

    /**
     * Launcher per la richiesta multipla di permessi.
     * <p>
     * Utilizza la nuova Activity Result API introdotta in AndroidX per gestire
     * in modo type-safe la richiesta e il risultato dei permessi.
     * </p>
     */
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    this::onPermissionsResult);

    /**
     * Metodo chiamato alla creazione dell'Activity.
     * <p>
     * Esegue le seguenti operazioni:
     * </p>
     * <ol>
     *   <li>Imposta il layout principale (activity_main.xml)</li>
     *   <li>Configura il sistema di navigazione</li>
     *   <li>Verifica e richiede i permessi necessari</li>
     * </ol>
     *
     * @param savedInstanceState stato salvato dell'istanza precedente, oppure {@code null}
     *                           se l'Activity viene creata per la prima volta
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
        checkPermissions();
    }

    /**
     * Configura il sistema di navigazione dell'applicazione.
     * <p>
     * Questo metodo:
     * </p>
     * <ul>
     *   <li>Recupera il NavHostFragment dal layout</li>
     *   <li>Ottiene il NavController per la gestione della navigazione</li>
     *   <li>Collega la BottomNavigationView al NavController per la navigazione automatica</li>
     * </ul>
     */
    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                NavigationUI.setupWithNavController(bottomNav, navController);
            }
        }
    }

    /**
     * Verifica e richiede i permessi necessari al funzionamento dell'applicazione.
     * <p>
     * Questo metodo controlla se l'app ha gia ottenuto tutti i permessi richiesti.
     * Per ogni permesso mancante, lo aggiunge alla lista dei permessi da richiedere
     * e lancia il dialog di sistema per la richiesta all'utente.
     * </p>
     *
     * <h3>Permessi verificati:</h3>
     * <ul>
     *   <li>SEND_SMS - Invio comandi SMS alla centrale</li>
     *   <li>RECEIVE_SMS - Ricezione risposte dalla centrale</li>
     *   <li>READ_SMS - Lettura messaggi SMS</li>
     *   <li>READ_PHONE_STATE - Accesso allo stato del telefono</li>
     *   <li>POST_NOTIFICATIONS - Notifiche push (solo Android 13+)</li>
     * </ul>
     */
    private void checkPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Permessi SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        // Permesso notifiche (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            permissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }

    /**
     * Callback invocato quando l'utente risponde alla richiesta di permessi.
     * <p>
     * Analizza i risultati per ogni permesso richiesto. Se anche solo uno dei
     * permessi viene negato, mostra un Toast per informare l'utente che
     * l'applicazione potrebbe non funzionare correttamente.
     * </p>
     *
     * @param results mappa contenente per ogni permesso richiesto (chiave) il valore
     *                booleano che indica se e stato concesso ({@code true}) o negato
     *                ({@code false})
     */
    private void onPermissionsResult(Map<String, Boolean> results) {
        boolean allGranted = true;
        for (Boolean granted : results.values()) {
            if (!granted) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            Toast.makeText(this, R.string.permissions_required, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Gestisce la navigazione verso l'alto (pulsante "back" nella ActionBar).
     * <p>
     * Delega la navigazione al NavController. Se il NavController non puo
     * navigare verso l'alto (es. siamo gia nella destinazione iniziale),
     * il comportamento viene delegato alla classe padre.
     * </p>
     *
     * @return {@code true} se la navigazione verso l'alto e stata gestita con successo,
     *         {@code false} altrimenti
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
