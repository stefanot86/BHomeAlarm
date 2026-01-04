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
 * Adapter per la lista zone con selezione multipla.
 */
public class ZonesAdapter extends ListAdapter<Zone, ZonesAdapter.ZoneViewHolder> {

    public interface OnZoneToggleListener {
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

    class ZoneViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final MaterialCheckBox checkboxZone;
        private final TextView textZoneName;

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
