package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.HomeViewModel;
import it.bhomealarm.controller.viewmodel.ScenariosViewModel;
import it.bhomealarm.model.entity.Scenario;
import it.bhomealarm.view.adapter.ScenariosAdapter;

/**
 * Fragment per la selezione degli scenari di attivazione.
 */
public class ScenariosFragment extends Fragment {

    private ScenariosViewModel viewModel;
    private HomeViewModel homeViewModel;
    private ScenariosAdapter adapter;

    // Views
    private MaterialToolbar toolbar;
    private RecyclerView recyclerScenarios;
    private MaterialCardView cardCustom;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ScenariosViewModel.class);
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scenarios, container, false);
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
        recyclerScenarios = view.findViewById(R.id.recycler_scenarios);
        cardCustom = view.findViewById(R.id.card_custom);

        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void setupRecyclerView() {
        adapter = new ScenariosAdapter();
        adapter.setOnScenarioClickListener(this::onScenarioSelected);
        recyclerScenarios.setAdapter(adapter);
    }

    private void setupClickListeners() {
        cardCustom.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_scenarios_to_zones);
        });
    }

    private void observeData() {
        viewModel.getScenarios().observe(getViewLifecycleOwner(), scenarios -> {
            if (scenarios != null) {
                adapter.submitList(scenarios);
            }
        });
    }

    private void onScenarioSelected(Scenario scenario) {
        String title = scenario.getName().isEmpty()
                ? getString(R.string.scenario_slot_title, scenario.getSlot())
                : scenario.getName();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_arm_title)
                .setMessage(getString(R.string.dialog_arm_message, title))
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_arm, (dialog, which) -> {
                    homeViewModel.armWithScenario(scenario.getSlot());
                    Navigation.findNavController(requireView())
                            .popBackStack(R.id.homeFragment, false);
                })
                .show();
    }
}
