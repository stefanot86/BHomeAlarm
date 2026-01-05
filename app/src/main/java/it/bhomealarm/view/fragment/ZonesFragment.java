package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.HomeViewModel;
import it.bhomealarm.controller.viewmodel.ZonesViewModel;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.view.adapter.ZonesAdapter;

/**
 * Fragment per la selezione delle zone per attivazione personalizzata.
 */
public class ZonesFragment extends Fragment {

    private ZonesViewModel viewModel;
    private HomeViewModel homeViewModel;
    private ZonesAdapter adapter;

    // Views
    private MaterialToolbar toolbar;
    private TextInputLayout inputLayoutName;
    private TextInputEditText inputScenarioName;
    private RecyclerView recyclerZones;
    private TextView textSelectedCount;
    private MaterialButton buttonCancel;
    private MaterialButton buttonSave;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ZonesViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_zones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupRecyclerView();
        setupClickListeners();
        observeData();
    }

    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_create_scenario);
        inputLayoutName = view.findViewById(R.id.input_layout_name);
        inputScenarioName = view.findViewById(R.id.input_scenario_name);
        recyclerZones = view.findViewById(R.id.recycler_zones);
        textSelectedCount = view.findViewById(R.id.text_selected_count);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonSave = view.findViewById(R.id.button_save);

        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        // Text watcher per il nome scenario
        inputScenarioName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setScenarioName(s.toString());
                inputLayoutName.setError(null);
                updateSaveButtonState();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ZonesAdapter();
        adapter.setOnZoneToggleListener((zone, selected) -> {
            viewModel.setZoneSelected(zone.getSlot(), selected);
        });
        recyclerZones.setAdapter(adapter);
    }

    private void setupClickListeners() {
        buttonCancel.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        buttonSave.setOnClickListener(v -> saveScenario());
    }

    private void observeData() {
        viewModel.getZones().observe(getViewLifecycleOwner(), zones -> {
            if (zones != null) {
                adapter.submitList(zones);
            }
        });

        viewModel.getSelectedCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                textSelectedCount.setText(getResources().getQuantityString(
                        R.plurals.zones_selected_count, count, count));
                updateSaveButtonState();
            }
        });

        viewModel.getSelectedZones().observe(getViewLifecycleOwner(), selectedZones -> {
            if (selectedZones != null) {
                adapter.setSelectedZones(selectedZones);
            }
        });


        viewModel.getIsSaving().observe(getViewLifecycleOwner(), isSaving -> {
            buttonSave.setEnabled(!isSaving && canSave());
            buttonCancel.setEnabled(!isSaving);
        });
    }

    private void updateSaveButtonState() {
        buttonSave.setEnabled(canSave());
    }

    private boolean canSave() {
        String name = viewModel.getScenarioName().getValue();
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasZones = viewModel.hasSelection();
        return hasName && hasZones;
    }

    private void saveScenario() {
        // Valida nome
        String name = viewModel.getScenarioName().getValue();
        if (name == null || name.trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.error_scenario_name_required));
            inputScenarioName.requestFocus();
            return;
        }

        // Valida zone
        if (!viewModel.hasSelection()) {
            Snackbar.make(requireView(), R.string.error_zones_required, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Salva lo scenario
        viewModel.saveCustomScenario(scenario -> {
            mainHandler.post(() -> showArmAfterSaveDialog(scenario));
        });
    }

    private void showArmAfterSaveDialog(Scenario scenario) {
        String zoneNumbers = viewModel.getZoneNumbersString();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_arm_after_save_title)
                .setMessage(getString(R.string.dialog_arm_after_save_message, scenario.getName()))
                .setNegativeButton(R.string.action_no, (dialog, which) -> {
                    // Torna alla home senza armare
                    Navigation.findNavController(requireView())
                            .popBackStack(R.id.homeFragment, false);
                })
                .setPositiveButton(R.string.action_yes, (dialog, which) -> {
                    // Arma e torna alla home
                    homeViewModel.armWithCustomZones(zoneNumbers);
                    Navigation.findNavController(requireView())
                            .popBackStack(R.id.homeFragment, false);
                })
                .setCancelable(false)
                .show();
    }
}
