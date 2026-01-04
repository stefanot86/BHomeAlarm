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
import it.bhomealarm.controller.viewmodel.ScenariosViewModel;

/**
 * Fragment per la gestione degli scenari (16 scenari max).
 */
public class ScenariosFragment extends Fragment {

    private ScenariosViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ScenariosViewModel.class);
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
        observeData();
    }

    private void setupViews(View view) {
        // TODO: Setup RecyclerView with scenarios list
    }

    private void observeData() {
        // TODO: Observe scenarios list from ViewModel
    }
}
