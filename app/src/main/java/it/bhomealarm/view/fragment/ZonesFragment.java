package it.bhomealarm.view.fragment;

import android.os.Bundle;
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

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.HomeViewModel;
import it.bhomealarm.controller.viewmodel.ZonesViewModel;
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
    private RecyclerView recyclerZones;
    private TextView textSelectedCount;
    private MaterialButton buttonCancel;
    private MaterialButton buttonArm;

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
        recyclerZones = view.findViewById(R.id.recycler_zones);
        textSelectedCount = view.findViewById(R.id.text_selected_count);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonArm = view.findViewById(R.id.button_arm);

        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
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

        buttonArm.setOnClickListener(v -> showArmConfirmation());
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
                buttonArm.setEnabled(count > 0);
            }
        });

        viewModel.getSelectedZones().observe(getViewLifecycleOwner(), selectedZones -> {
            if (selectedZones != null) {
                adapter.setSelectedZones(selectedZones);
            }
        });
    }

    private void showArmConfirmation() {
        String zoneMask = viewModel.getZoneMaskString();
        Integer count = viewModel.getSelectedCount().getValue();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_arm_custom_title)
                .setMessage(getString(R.string.dialog_arm_custom_message, count != null ? count : 0))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_arm, (dialog, which) -> {
                    homeViewModel.armWithCustomZones(zoneMask);
                    Navigation.findNavController(requireView())
                            .popBackStack(R.id.homeFragment, false);
                })
                .show();
    }
}
