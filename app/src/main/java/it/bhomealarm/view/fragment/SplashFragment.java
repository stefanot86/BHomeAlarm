package it.bhomealarm.view.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import it.bhomealarm.BuildConfig;
import it.bhomealarm.R;
import it.bhomealarm.util.Constants;

/**
 * Fragment di splash screen.
 * Mostra logo e versione, poi naviga al fragment appropriato:
 * - Se primo avvio → DisclaimerFragment
 * - Se disclaimer accettato ma no numero → SetupPhoneFragment
 * - Altrimenti → HomeFragment
 */
public class SplashFragment extends Fragment {

    private static final long SPLASH_DELAY = 1500L; // 1.5 secondi

    private SharedPreferences prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, 0);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
        scheduleNavigation();
    }

    private void setupViews(View view) {
        TextView textVersion = view.findViewById(R.id.text_version);
        textVersion.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));
    }

    private void scheduleNavigation() {
        handler.postDelayed(this::navigateToNextScreen, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        if (!isAdded() || getView() == null) {
            return;
        }

        boolean disclaimerAccepted = prefs.getBoolean(Constants.PREF_DISCLAIMER_ACCEPTED, false);
        String alarmPhone = prefs.getString(Constants.PREF_ALARM_PHONE, "");

        int destinationId;

        if (!disclaimerAccepted) {
            // Primo avvio - mostra disclaimer
            destinationId = R.id.action_splash_to_disclaimer;
        } else if (alarmPhone.isEmpty()) {
            // Disclaimer accettato ma no numero configurato
            destinationId = R.id.action_splash_to_setup_phone;
        } else {
            // Tutto configurato - vai a home
            destinationId = R.id.action_splash_to_home;
        }

        Navigation.findNavController(requireView()).navigate(destinationId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
