package it.bhomealarm.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import it.bhomealarm.R;
import it.bhomealarm.model.entity.User;

/**
 * Adapter per la lista utenti.
 */
public class UsersAdapter extends ListAdapter<User, UsersAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private OnUserClickListener listener;

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getPermissions() == newItem.getPermissions() &&
                    oldItem.isEnabled() == newItem.isEnabled();
        }
    };

    public UsersAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView textUserName;
        private final Chip chipRx1;
        private final Chip chipRx2;
        private final Chip chipVerify;
        private final Chip chipCmd;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserName = itemView.findViewById(R.id.text_user_name);
            chipRx1 = itemView.findViewById(R.id.chip_rx1);
            chipRx2 = itemView.findViewById(R.id.chip_rx2);
            chipVerify = itemView.findViewById(R.id.chip_verify);
            chipCmd = itemView.findViewById(R.id.chip_cmd);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUserClick(getItem(position));
                }
            });
        }

        void bind(User user) {
            String displayName = user.getName().isEmpty()
                    ? "Utente " + user.getSlot()
                    : user.getName();
            textUserName.setText(displayName);

            chipRx1.setVisibility(user.hasPermission(User.PERM_RX1) ? View.VISIBLE : View.GONE);
            chipRx2.setVisibility(user.hasPermission(User.PERM_RX2) ? View.VISIBLE : View.GONE);
            chipVerify.setVisibility(user.hasPermission(User.PERM_VERIFY) ? View.VISIBLE : View.GONE);
            chipCmd.setVisibility(user.hasPermission(User.PERM_CMD_ON_OFF) ? View.VISIBLE : View.GONE);
        }
    }
}
