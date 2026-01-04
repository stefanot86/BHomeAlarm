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
 * Fragment per accettazione disclaimer e termini d'uso.
 * L'utente deve accettare i termini per procedere con l'app.
 */
public class DisclaimerFragment extends Fragment {

    private SharedPreferences prefs;

    // Views
    private MaterialToolbar toolbar;
    private MaterialCheckBox checkboxAccept;
    private MaterialButton buttonDecline;
    private MaterialButton buttonAccept;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_disclaimer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupClickListeners();
    }

    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        checkboxAccept = view.findViewById(R.id.checkbox_accept);
        buttonDecline = view.findViewById(R.id.button_decline);
        buttonAccept = view.findViewById(R.id.button_accept);

        // Abilita bottone accetta solo se checkbox selezionato
        buttonAccept.setEnabled(false);
    }

    private void setupClickListeners() {
        checkboxAccept.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonAccept.setEnabled(isChecked);
        });

        buttonDecline.setOnClickListener(v -> showDeclineConfirmation());

        buttonAccept.setOnClickListener(v -> acceptAndContinue());
    }

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
