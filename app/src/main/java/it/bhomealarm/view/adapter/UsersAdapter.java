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
 * Adapter per la visualizzazione degli utenti registrati nel sistema di allarme.
 * <p>
 * Questo adapter gestisce una lista di {@link User} mostrando per ciascuno il nome
 * e i permessi assegnati sotto forma di chip. Utilizza il layout {@code item_user.xml}
 * che presenta il nome utente e fino a quattro chip per i permessi (RX1, RX2, Verify, Cmd).
 * </p>
 * <p>
 * I chip dei permessi vengono mostrati o nascosti dinamicamente in base ai permessi
 * effettivamente assegnati all'utente:
 * <ul>
 *     <li><b>RX1</b>: ricezione notifiche gruppo 1</li>
 *     <li><b>RX2</b>: ricezione notifiche gruppo 2</li>
 *     <li><b>Verify</b>: verifica stato sistema</li>
 *     <li><b>Cmd</b>: comandi di attivazione/disattivazione</li>
 * </ul>
 * </p>
 *
 * @see User
 * @see OnUserClickListener
 */
public class UsersAdapter extends ListAdapter<User, UsersAdapter.UserViewHolder> {

    /**
     * Interfaccia listener per gestire gli eventi di click sugli utenti.
     * <p>
     * Implementare questa interfaccia per ricevere notifiche quando l'utente
     * tocca un elemento della lista, tipicamente per aprire la schermata di dettaglio.
     * </p>
     */
    public interface OnUserClickListener {
        /**
         * Chiamato quando un utente viene selezionato dalla lista.
         *
         * @param user l'utente su cui e' stato effettuato il click
         */
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

    /**
     * ViewHolder per la visualizzazione di un singolo elemento utente.
     * <p>
     * Mostra il nome dell'utente e i chip dei permessi assegnati.
     * Supporta il click per selezionare l'utente.
     * </p>
     */
    class UserViewHolder extends RecyclerView.ViewHolder {
        /** TextView per visualizzare il nome dell'utente o il placeholder "Utente X". */
        private final TextView textUserName;
        /** Chip per indicare il permesso di ricezione notifiche gruppo 1. */
        private final Chip chipRx1;
        /** Chip per indicare il permesso di ricezione notifiche gruppo 2. */
        private final Chip chipRx2;
        /** Chip per indicare il permesso di verifica stato sistema. */
        private final Chip chipVerify;
        /** Chip per indicare il permesso di comandi ON/OFF. */
        private final Chip chipCmd;

        /**
         * Costruisce un nuovo ViewHolder per l'elemento utente.
         * <p>
         * Configura il click listener per notificare la selezione dell'utente.
         * </p>
         *
         * @param itemView la vista radice dell'elemento
         */
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

        /**
         * Associa i dati di un utente a questa vista.
         * <p>
         * Visualizza il nome dell'utente (o "Utente X" se il nome e' vuoto)
         * e mostra/nasconde i chip dei permessi in base ai permessi effettivamente
         * assegnati all'utente.
         * </p>
         *
         * @param user l'utente da visualizzare
         */
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
