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

import it.bhomealarm.BuildConfig;
import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.SettingsViewModel;

/**
 * Fragment per le impostazioni dell'app.
 */
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;

    // Views
    private MaterialToolbar toolbar;
    private LinearLayout itemPhoneNumber;
    private LinearLayout itemConfigure;
    private LinearLayout itemUsers;
    private LinearLayout itemScenarios;
    private LinearLayout itemSim;
    private LinearLayout itemInfo;
    private TextView textPhoneNumber;
    private TextView textScenariosCount;
    private TextView textSim;
    private TextView textVersion;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupClickListeners();
        observeData();
    }

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
                textSim.setText(getString(R.string.sim_slot_format, simSlot + 1));
            } else {
                textSim.setText(R.string.sim_default);
            }
        });
    }

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

    private void showSimSelectionDialog() {
        String[] simOptions = {
                getString(R.string.sim_slot_1),
                getString(R.string.sim_slot_2),
                getString(R.string.sim_default)
        };

        Integer currentSim = viewModel.getSelectedSimSlot().getValue();
        int checkedItem = currentSim != null && currentSim >= 0 ? currentSim : 2;

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_sim_title)
                .setSingleChoiceItems(simOptions, checkedItem, (dialog, which) -> {
                    viewModel.saveSelectedSim(which < 2 ? which : -1);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showInfoDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.info_message, BuildConfig.VERSION_NAME))
                .setPositiveButton(R.string.action_ok, null)
                .show();
    }
}
