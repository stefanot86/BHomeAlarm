package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.LogViewModel;
import it.bhomealarm.model.entity.SmsLog;
import it.bhomealarm.view.adapter.SmsLogAdapter;

/**
 * Fragment per la visualizzazione del log SMS.
 */
public class LogFragment extends Fragment {

    private LogViewModel viewModel;
    private SmsLogAdapter adapter;

    // Views
    private MaterialToolbar toolbar;
    private RecyclerView recyclerLogs;
    private LinearLayout layoutEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LogViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        setupRecyclerView();
        observeData();
    }

    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerLogs = view.findViewById(R.id.recycler_logs);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clear) {
                showClearConfirmation();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new SmsLogAdapter();
        adapter.setOnLogClickListener(this::showLogDetails);
        recyclerLogs.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getSmsLogs().observe(getViewLifecycleOwner(), logs -> {
            if (logs != null && !logs.isEmpty()) {
                adapter.submitList(logs);
                recyclerLogs.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                recyclerLogs.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLogDetails(SmsLog log) {
        String direction = log.isOutgoing()
                ? getString(R.string.log_direction_outgoing)
                : getString(R.string.log_direction_incoming);

        String status;
        switch (log.getStatus()) {
            case SmsLog.STATUS_SENT:
                status = getString(R.string.log_status_sent);
                break;
            case SmsLog.STATUS_DELIVERED:
                status = getString(R.string.log_status_delivered);
                break;
            case SmsLog.STATUS_RECEIVED:
                status = getString(R.string.log_status_received);
                break;
            case SmsLog.STATUS_FAILED:
                status = getString(R.string.log_status_failed);
                break;
            default:
                status = getString(R.string.log_status_pending);
                break;
        }

        String message = getString(R.string.log_detail_format,
                direction, log.getMessage(), status);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_log_detail_title)
                .setMessage(message)
                .setPositiveButton(R.string.action_close, null)
                .show();
    }

    private void showClearConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_clear_logs_title)
                .setMessage(R.string.dialog_clear_logs_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_clear, (dialog, which) -> {
                    viewModel.clearAllLogs();
                    Snackbar.make(requireView(), R.string.logs_cleared, Snackbar.LENGTH_SHORT).show();
                })
                .show();
    }
}
