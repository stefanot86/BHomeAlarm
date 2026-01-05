package it.bhomealarm.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.HashSet;
import java.util.Set;

import it.bhomealarm.R;
import it.bhomealarm.model.entity.Zone;

/**
 * Adapter per la visualizzazione e selezione multipla delle zone in una RecyclerView.
 * <p>
 * Questo adapter gestisce una lista di {@link Zone} permettendo all'utente di selezionare
 * o deselezionare singole zone tramite checkbox. Utilizza il layout {@code item_zone.xml}
 * che presenta una card con checkbox e nome della zona.
 * </p>
 * <p>
 * Caratteristiche principali:
 * <ul>
 *     <li>Selezione multipla delle zone tramite checkbox</li>
 *     <li>Gestione dello stato di selezione tramite Set di slot</li>
 *     <li>Utilizzo di DiffUtil per aggiornamenti efficienti della lista</li>
 *     <li>Callback per notificare i cambiamenti di selezione</li>
 * </ul>
 * </p>
 *
 * @see Zone
 * @see OnZoneToggleListener
 */
public class ZonesAdapter extends ListAdapter<Zone, ZonesAdapter.ZoneViewHolder> {

    /**
     * Interfaccia listener per gestire gli eventi di selezione/deselezione delle zone.
     * <p>
     * Implementare questa interfaccia per ricevere notifiche quando l'utente
     * tocca una zona nella lista, modificandone lo stato di selezione.
     * </p>
     */
    public interface OnZoneToggleListener {
        /**
         * Chiamato quando lo stato di selezione di una zona viene modificato.
         *
         * @param zone     la zona il cui stato di selezione e' cambiato
         * @param selected {@code true} se la zona e' stata selezionata,
         *                 {@code false} se e' stata deselezionata
         */
        void onZoneToggle(Zone zone, boolean selected);
    }

    private OnZoneToggleListener listener;
    private final Set<Integer> selectedZoneSlots = new HashSet<>();

    private static final DiffUtil.ItemCallback<Zone> DIFF_CALLBACK = new DiffUtil.ItemCallback<Zone>() {
        @Override
        public boolean areItemsTheSame(@NonNull Zone oldItem, @NonNull Zone newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Zone oldItem, @NonNull Zone newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.isEnabled() == newItem.isEnabled();
        }
    };

    public ZonesAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnZoneToggleListener(OnZoneToggleListener listener) {
        this.listener = listener;
    }

    public void setSelectedZones(Set<Integer> selectedSlots) {
        selectedZoneSlots.clear();
        if (selectedSlots != null) {
            selectedZoneSlots.addAll(selectedSlots);
        }
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedZoneSlots() {
        return new HashSet<>(selectedZoneSlots);
    }

    @NonNull
    @Override
    public ZoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_zone, parent, false);
        return new ZoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ZoneViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * ViewHolder per la visualizzazione di un singolo elemento zona.
     * <p>
     * Gestisce il binding dei dati della zona e l'interazione utente per la selezione.
     * Il click sull'intera card alterna lo stato di selezione della zona.
     * </p>
     */
    class ZoneViewHolder extends RecyclerView.ViewHolder {
        /** Card contenitore dell'elemento, supporta lo stato checked per feedback visivo. */
        private final MaterialCardView cardView;
        /** Checkbox per indicare e modificare lo stato di selezione della zona. */
        private final MaterialCheckBox checkboxZone;
        /** TextView per visualizzare il nome della zona o il placeholder "Zona X". */
        private final TextView textZoneName;

        /**
         * Costruisce un nuovo ViewHolder per l'elemento zona.
         * <p>
         * Configura il click listener sull'intera vista per gestire la selezione.
         * </p>
         *
         * @param itemView la vista radice dell'elemento
         */
        ZoneViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            checkboxZone = itemView.findViewById(R.id.checkbox_zone);
            textZoneName = itemView.findViewById(R.id.text_zone_name);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Zone zone = getItem(position);
                    boolean wasSelected = selectedZoneSlots.contains(zone.getSlot());
                    boolean isNowSelected = !wasSelected;

                    if (isNowSelected) {
                        selectedZoneSlots.add(zone.getSlot());
                    } else {
                        selectedZoneSlots.remove(zone.getSlot());
                    }

                    checkboxZone.setChecked(isNowSelected);
                    cardView.setChecked(isNowSelected);

                    if (listener != null) {
                        listener.onZoneToggle(zone, isNowSelected);
                    }
                }
            });
        }

        /**
         * Associa i dati di una zona a questa vista.
         * <p>
         * Visualizza il nome della zona (o "Zona X" se il nome e' vuoto)
         * e aggiorna lo stato visivo della checkbox e della card in base
         * allo stato di selezione corrente.
         * </p>
         *
         * @param zone la zona da visualizzare
         */
        void bind(Zone zone) {
            String displayName = zone.getName().isEmpty()
                    ? "Zona " + zone.getSlot()
                    : zone.getName();
            textZoneName.setText(displayName);

            boolean isSelected = selectedZoneSlots.contains(zone.getSlot());
            checkboxZone.setChecked(isSelected);
            cardView.setChecked(isSelected);
        }
    }
}
