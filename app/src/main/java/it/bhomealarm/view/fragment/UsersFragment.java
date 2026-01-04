package it.bhomealarm.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.UsersViewModel;
import it.bhomealarm.model.entity.User;
import it.bhomealarm.view.adapter.UsersAdapter;

/**
 * Fragment per la gestione degli utenti e dei loro permessi.
 */
public class UsersFragment extends Fragment {

    private UsersViewModel viewModel;
    private UsersAdapter adapter;

    // Views
    private MaterialToolbar toolbar;
    private RecyclerView recyclerUsers;
    private LinearLayout layoutEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UsersViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_users, container, false);
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
        recyclerUsers = view.findViewById(R.id.recycler_users);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void setupRecyclerView() {
        adapter = new UsersAdapter();
        adapter.setOnUserClickListener(this::showUserPermissionsDialog);
        recyclerUsers.setAdapter(adapter);
    }

    private void observeData() {
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null && !users.isEmpty()) {
                adapter.submitList(users);
                recyclerUsers.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
            } else {
                recyclerUsers.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showUserPermissionsDialog(User user) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_user_permissions, null);

        CheckBox checkRx1 = dialogView.findViewById(R.id.check_rx1);
        CheckBox checkRx2 = dialogView.findViewById(R.id.check_rx2);
        CheckBox checkVerify = dialogView.findViewById(R.id.check_verify);
        CheckBox checkCmd = dialogView.findViewById(R.id.check_cmd);
        CheckBox checkApplyAll = dialogView.findViewById(R.id.check_apply_all);

        checkRx1.setChecked(user.hasPermission(User.PERM_RX1));
        checkRx2.setChecked(user.hasPermission(User.PERM_RX2));
        checkVerify.setChecked(user.hasPermission(User.PERM_VERIFY));
        checkCmd.setChecked(user.hasPermission(User.PERM_CMD_ON_OFF));

        String title = user.getName().isEmpty()
                ? getString(R.string.user_slot_title, user.getSlot())
                : user.getName();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    int permissions = UsersViewModel.buildPermissions(
                            checkRx1.isChecked(),
                            checkRx2.isChecked(),
                            checkVerify.isChecked(),
                            checkCmd.isChecked()
                    );

                    if (checkApplyAll.isChecked()) {
                        viewModel.applyPermissionsToAll(permissions);
                    } else {
                        viewModel.updateUserPermissions(user, permissions);
                    }
                })
                .show();
    }
}
