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
 * Fragment per configurazione numero telefono allarme.
 * Permette inserimento manuale o selezione da rubrica.
 */
public class SetupPhoneFragment extends Fragment {

    private SharedPreferences prefs;

    // Views
    private MaterialToolbar toolbar;
    private TextInputLayout inputLayoutPhone;
    private TextInputEditText editPhone;
    private MaterialButton buttonContinue;
    private MaterialButton buttonSkip;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> contactPickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, 0);
        setupActivityResultLaunchers();
    }

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setup_phone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupClickListeners();
        setupTextWatcher();
    }

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

    private void setupClickListeners() {
        // Click su icona rubrica
        inputLayoutPhone.setEndIconOnClickListener(v -> checkContactsPermissionAndPick());

        buttonContinue.setOnClickListener(v -> saveAndContinue());

        buttonSkip.setOnClickListener(v -> skipAndContinue());
    }

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

    private void checkContactsPermissionAndPick() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            openContactPicker();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        contactPickerLauncher.launch(intent);
    }

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

    private void skipAndContinue() {
        // Naviga a Home senza salvare numero
        Navigation.findNavController(requireView())
                .navigate(R.id.action_setup_phone_to_home);
    }
}
