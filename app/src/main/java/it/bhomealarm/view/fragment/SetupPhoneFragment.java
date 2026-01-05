package it.bhomealarm.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import it.bhomealarm.R;
import it.bhomealarm.util.Constants;
import it.bhomealarm.util.PhoneNumberUtils;

/**
 * Fragment per la configurazione del numero di telefono della centralina allarme.
 * <p>
 * Questo fragment permette all'utente di inserire il numero di telefono
 * della centralina allarme, che verra' utilizzato per l'invio e la ricezione
 * degli SMS di controllo.
 * <p>
 * L'inserimento del numero puo' avvenire in due modi:
 * <ul>
 *     <li>Digitazione manuale del numero</li>
 *     <li>Selezione da rubrica contatti (richiede permesso READ_CONTACTS)</li>
 * </ul>
 * <p>
 * Il flusso utente prevede:
 * <ol>
 *     <li>L'utente inserisce il numero manualmente o lo seleziona dalla rubrica</li>
 *     <li>Il numero viene validato in tempo reale</li>
 *     <li>Se valido, il pulsante CONTINUA si abilita</li>
 *     <li>Toccando CONTINUA, il numero viene normalizzato, salvato e si naviga a {@link HomeFragment}</li>
 *     <li>L'utente puo' anche saltare questo passaggio toccando SALTA</li>
 * </ol>
 * <p>
 * La validazione verifica che il numero sia in un formato telefonico valido
 * e lo normalizza rimuovendo spazi e caratteri non necessari.
 *
 * @see HomeFragment Fragment di destinazione dopo la configurazione
 * @see DisclaimerFragment Fragment precedente nel flusso di setup
 * @see PhoneNumberUtils Classe utility per la validazione e normalizzazione dei numeri
 */
public class SetupPhoneFragment extends Fragment {

    /** SharedPreferences per salvare il numero telefono configurato */
    private SharedPreferences prefs;

    /** Toolbar della schermata (senza navigazione indietro) */
    private MaterialToolbar toolbar;

    /** Layout del campo di input con supporto per icona rubrica */
    private TextInputLayout inputLayoutPhone;

    /** Campo di input per il numero di telefono */
    private TextInputEditText editPhone;

    /** Pulsante per salvare e procedere alla home */
    private MaterialButton buttonContinue;

    /** Pulsante per saltare la configurazione del numero */
    private MaterialButton buttonSkip;

    /** Launcher per l'Activity di selezione contatto dalla rubrica */
    private ActivityResultLauncher<Intent> contactPickerLauncher;

    /** Launcher per la richiesta del permesso READ_CONTACTS */
    private ActivityResultLauncher<String> permissionLauncher;

    /**
     * Inizializza le SharedPreferences e i launcher per i risultati delle Activity.
     *
     * @param savedInstanceState stato salvato dell'istanza precedente, puo' essere null
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, 0);
        setupActivityResultLaunchers();
    }

    /**
     * Configura i launcher per la selezione contatto e la richiesta permessi.
     * Devono essere registrati prima che il fragment sia in stato STARTED.
     */
    private void setupActivityResultLaunchers() {
        // Launcher per selezionare contatto
        contactPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleContactPicked(result.getData());
                    }
                }
        );

        // Launcher per permesso READ_CONTACTS
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openContactPicker();
                    } else {
                        Snackbar.make(requireView(), R.string.error_contacts_permission, Snackbar.LENGTH_LONG).show();
                    }
                }
        );
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
        return inflater.inflate(R.layout.fragment_setup_phone, container, false);
    }

    /**
     * Chiamato dopo che la view e' stata creata.
     * Inizializza le views, configura i listener e il text watcher.
     *
     * @param view la view root del fragment
     * @param savedInstanceState stato salvato dell'istanza precedente
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupClickListeners();
        setupTextWatcher();
    }

    /**
     * Inizializza i riferimenti alle views del layout.
     * Disabilita il pulsante CONTINUA e pre-compila il numero se esistente.
     *
     * @param view la view root del fragment
     */
    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        inputLayoutPhone = view.findViewById(R.id.input_layout_phone);
        editPhone = view.findViewById(R.id.edit_phone);
        buttonContinue = view.findViewById(R.id.button_continue);
        buttonSkip = view.findViewById(R.id.button_skip);

        buttonContinue.setEnabled(false);

        // Carica numero esistente se presente
        String existingPhone = prefs.getString(Constants.PREF_ALARM_PHONE, "");
        if (!existingPhone.isEmpty()) {
            editPhone.setText(existingPhone);
        }
    }

    /**
     * Configura i listener per l'icona rubrica e i pulsanti.
     * <ul>
     *     <li>Icona rubrica: apre il picker contatti</li>
     *     <li>Pulsante CONTINUA: salva il numero e naviga alla home</li>
     *     <li>Pulsante SALTA: naviga alla home senza salvare</li>
     * </ul>
     */
    private void setupClickListeners() {
        // Click su icona rubrica
        inputLayoutPhone.setEndIconOnClickListener(v -> checkContactsPermissionAndPick());

        buttonContinue.setOnClickListener(v -> saveAndContinue());

        buttonSkip.setOnClickListener(v -> skipAndContinue());
    }

    /**
     * Configura il TextWatcher per la validazione in tempo reale del numero telefono.
     */
    private void setupTextWatcher() {
        editPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validatePhoneNumber(s.toString());
            }
        });
    }

    /**
     * Valida il numero di telefono inserito e aggiorna l'UI di conseguenza.
     * Abilita il pulsante CONTINUA solo se il numero e' valido.
     *
     * @param phone il numero di telefono da validare
     */
    private void validatePhoneNumber(String phone) {
        if (phone.isEmpty()) {
            inputLayoutPhone.setError(null);
            buttonContinue.setEnabled(false);
            return;
        }

        if (PhoneNumberUtils.isValid(phone)) {
            inputLayoutPhone.setError(null);
            buttonContinue.setEnabled(true);
        } else {
            inputLayoutPhone.setError(getString(R.string.error_invalid_phone));
            buttonContinue.setEnabled(false);
        }
    }

    /**
     * Verifica il permesso READ_CONTACTS e apre il picker contatti.
     * Se il permesso non e' concesso, lo richiede all'utente.
     */
    private void checkContactsPermissionAndPick() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            openContactPicker();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    /**
     * Apre l'Activity di sistema per la selezione di un contatto dalla rubrica.
     */
    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        contactPickerLauncher.launch(intent);
    }

    /**
     * Gestisce il risultato della selezione di un contatto.
     * Estrae il numero di telefono dal contatto selezionato e lo normalizza.
     *
     * @param data l'Intent contenente l'URI del contatto selezionato
     */
    private void handleContactPicked(Intent data) {
        Uri contactUri = data.getData();
        if (contactUri == null) return;

        String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

        try (Cursor cursor = requireContext().getContentResolver()
                .query(contactUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = cursor.getString(numberIndex);

                // Normalizza il numero
                String normalized = PhoneNumberUtils.normalize(phoneNumber);
                editPhone.setText(normalized);
            }
        } catch (Exception e) {
            Snackbar.make(requireView(), R.string.error_reading_contact, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Valida, normalizza e salva il numero di telefono nelle SharedPreferences.
     * Se valido, naviga alla schermata home.
     */
    private void saveAndContinue() {
        String phone = editPhone.getText() != null ? editPhone.getText().toString().trim() : "";

        if (phone.isEmpty() || !PhoneNumberUtils.isValid(phone)) {
            inputLayoutPhone.setError(getString(R.string.error_invalid_phone));
            return;
        }

        // Normalizza e salva
        String normalized = PhoneNumberUtils.normalize(phone);
        prefs.edit()
                .putString(Constants.PREF_ALARM_PHONE, normalized)
                .apply();

        // Naviga a Home
        Navigation.findNavController(requireView())
                .navigate(R.id.action_setup_phone_to_home);
    }

    /**
     * Salta la configurazione del numero e naviga direttamente alla home.
     * L'utente potra' configurare il numero successivamente dalle impostazioni.
     */
    private void skipAndContinue() {
        // Naviga a Home senza salvare numero
        Navigation.findNavController(requireView())
                .navigate(R.id.action_setup_phone_to_home);
    }
}
