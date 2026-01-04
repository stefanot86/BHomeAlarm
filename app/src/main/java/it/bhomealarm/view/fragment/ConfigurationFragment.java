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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.ConfigurationViewModel;
import it.bhomealarm.view.adapter.ConfigStepsAdapter;

/**
 * Fragment per la configurazione iniziale tramite SMS.
 */
public class ConfigurationFragment extends Fragment {

    private ConfigurationViewModel viewModel;
    private ConfigStepsAdapter adapter;

    // Views
    private MaterialToolbar toolbar;
    private LinearProgressIndicator progressBar;
    private TextView textProgressPercent;
    private TextView textStatus;
    private RecyclerView recyclerSteps;
    private MaterialCardView cardDebug;
    private TextView textDebugLog;
    private MaterialButton buttonCancel;
    private MaterialButton buttonStart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ConfigurationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuration, container, false);
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
        progressBar = view.findViewById(R.id.progress_bar);
        textProgressPercent = view.findViewById(R.id.text_progress_percent);
        textStatus = view.findViewById(R.id.text_status);
        recyclerSteps = view.findViewById(R.id.recycler_steps);
        cardDebug = view.findViewById(R.id.card_debug);
        textDebugLog = view.findViewById(R.id.text_debug_log);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonStart = view.findViewById(R.id.button_start);

        toolbar.setNavigationOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getIsRunning().getValue())) {
                showCancelConfirmation();
            } else {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ConfigStepsAdapter();
        recyclerSteps.setAdapter(adapter);
    }

    private void setupClickListeners() {
        buttonStart.setOnClickListener(v -> viewModel.startConfiguration());

        buttonCancel.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(viewModel.getIsRunning().getValue())) {
                showCancelConfirmation();
            } else {
                Navigation.findNavController(requireView()).navigateUp();
            }
        });
    }

    private void observeData() {
        viewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            if (progress != null) {
                progressBar.setProgress(progress);
                textProgressPercent.setText(getString(R.string.percent_format, progress));
            }
        });

        viewModel.getStatusMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                textStatus.setText(message);
            }
        });

        viewModel.getSteps().observe(getViewLifecycleOwner(), steps -> {
            if (steps != null) {
                adapter.submitList(steps);
            }
        });

        viewModel.getIsRunning().observe(getViewLifecycleOwner(), isRunning -> {
            buttonStart.setEnabled(!isRunning);
            buttonStart.setText(isRunning ? R.string.config_running : R.string.action_start);
            buttonCancel.setEnabled(true);
        });

        viewModel.getIsComplete().observe(getViewLifecycleOwner(), isComplete -> {
            if (Boolean.TRUE.equals(isComplete)) {
                Snackbar.make(requireView(), R.string.config_complete, Snackbar.LENGTH_LONG).show();
                buttonStart.setEnabled(false);
                buttonCancel.setText(R.string.action_close);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_retry, v -> viewModel.startConfiguration())
                        .show();
            }
        });

        viewModel.getDebugLog().observe(getViewLifecycleOwner(), log -> {
            if (log != null && !log.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (String line : log) {
                    sb.append(line).append("\n");
                }
                textDebugLog.setText(sb.toString());
                cardDebug.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showCancelConfirmation() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_cancel_config_title)
                .setMessage(R.string.dialog_cancel_config_message)
                .setNegativeButton(R.string.action_no, null)
                .setPositiveButton(R.string.action_yes, (dialog, which) -> {
                    viewModel.cancelConfiguration();
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .show();
    }
}
