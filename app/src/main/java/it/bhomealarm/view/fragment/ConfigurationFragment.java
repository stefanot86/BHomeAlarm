package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.ConfigurationViewModel;

/**
 * Fragment per la configurazione iniziale e sincronizzazione con l'allarme.
 * Gestisce la comunicazione CONF1-CONF5 via SMS.
 */
public class ConfigurationFragment extends Fragment {

    private ConfigurationViewModel viewModel;

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
        observeData();
    }

    private void setupViews(View view) {
        // TODO: Setup configuration UI with progress indicator
    }

    private void observeData() {
        // TODO: Observe configuration state from ViewModel
    }
}
