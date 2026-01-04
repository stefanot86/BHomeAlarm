package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.HomeViewModel;
import it.bhomealarm.util.Constants;

/**
 * Fragment principale - Controllo allarme (ARM/DISARM/STATUS).
 */
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    // Views
    private MaterialToolbar toolbar;
    private MaterialCardView cardStatus;
    private MaterialCardView cardArm;
    private MaterialCardView cardDisarm;
    private MaterialCardView cardCheckStatus;
    private ImageView iconStatus;
    private TextView textStatus;
    private TextView textLastCheck;
    private LinearProgressIndicator progressIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
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
        cardStatus = view.findViewById(R.id.card_status);
        cardArm = view.findViewById(R.id.card_arm);
        cardDisarm = view.findViewById(R.id.card_disarm);
        cardCheckStatus = view.findViewById(R.id.card_check_status);
        iconStatus = view.findViewById(R.id.icon_status);
        textStatus = view.findViewById(R.id.text_status);
        textLastCheck = view.findViewById(R.id.text_last_check);
        progressIndicator = view.findViewById(R.id.progress_indicator);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_settings);
                return true;
            } else if (item.getItemId() == R.id.action_log) {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_home_to_settings);
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        cardArm.setOnClickListener(v -> {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_home_to_scenarios);
        });

        cardDisarm.setOnClickListener(v -> showDisarmConfirmation());

        cardCheckStatus.setOnClickListener(v -> viewModel.checkStatus());
    }

    private void observeData() {
        viewModel.getAlarmStatus().observe(getViewLifecycleOwner(), this::updateStatusUI);

        viewModel.getLastCheckTime().observe(getViewLifecycleOwner(), time -> {
            if (time != null && !time.isEmpty()) {
                textLastCheck.setText(getString(R.string.last_check_format, time));
                textLastCheck.setVisibility(View.VISIBLE);
            } else {
                textLastCheck.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            cardArm.setEnabled(!isLoading);
            cardDisarm.setEnabled(!isLoading);
            cardCheckStatus.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_dismiss, v -> viewModel.clearError())
                        .show();
            }
        });
    }

    private void updateStatusUI(String status) {
        if (status == null) {
            status = Constants.STATUS_UNKNOWN;
        }

        int iconRes;
        int colorRes;
        int textRes;

        switch (status) {
            case Constants.STATUS_ARMED:
                iconRes = R.drawable.ic_lock;
                colorRes = R.color.md_theme_light_primary;
                textRes = R.string.status_armed;
                break;
            case Constants.STATUS_DISARMED:
                iconRes = R.drawable.ic_lock_open;
                colorRes = R.color.md_theme_light_secondary;
                textRes = R.string.status_disarmed;
                break;
            case Constants.STATUS_ALARM:
                iconRes = R.drawable.ic_warning;
                colorRes = R.color.md_theme_light_error;
                textRes = R.string.status_alarm;
                break;
            case Constants.STATUS_TAMPER:
                iconRes = R.drawable.ic_warning;
                colorRes = R.color.md_theme_light_error;
                textRes = R.string.status_tamper;
                break;
            default:
                iconRes = R.drawable.ic_status_unknown;
                colorRes = R.color.md_theme_light_onSurfaceVariant;
                textRes = R.string.status_unknown;
                break;
        }

        iconStatus.setImageResource(iconRes);
        iconStatus.setColorFilter(ContextCompat.getColor(requireContext(), colorRes));
        textStatus.setText(textRes);
        textStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
    }

    private void showDisarmConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.dialog_disarm_title)
                .setMessage(R.string.dialog_disarm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_disarm, (dialog, which) -> viewModel.disarm())
                .show();
    }
}
