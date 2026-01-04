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

import java.util.List;

import it.bhomealarm.R;
import it.bhomealarm.model.entity.Scenario;

/**
 * Adapter per la lista scenari.
 */
public class ScenariosAdapter extends ListAdapter<Scenario, ScenariosAdapter.ScenarioViewHolder> {

    public interface OnScenarioClickListener {
        void onScenarioClick(Scenario scenario);
    }

    private OnScenarioClickListener listener;

    private static final DiffUtil.ItemCallback<Scenario> DIFF_CALLBACK = new DiffUtil.ItemCallback<Scenario>() {
        @Override
        public boolean areItemsTheSame(@NonNull Scenario oldItem, @NonNull Scenario newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Scenario oldItem, @NonNull Scenario newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getZoneMask() == newItem.getZoneMask() &&
                    oldItem.isEnabled() == newItem.isEnabled();
        }
    };

    public ScenariosAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnScenarioClickListener(OnScenarioClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScenarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scenario, parent, false);
        return new ScenarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScenarioViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ScenarioViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconScenario;
        private final TextView textScenarioName;
        private final TextView textScenarioZones;

        ScenarioViewHolder(@NonNull View itemView) {
            super(itemView);
            iconScenario = itemView.findViewById(R.id.icon_scenario);
            textScenarioName = itemView.findViewById(R.id.text_scenario_name);
            textScenarioZones = itemView.findViewById(R.id.text_scenario_zones);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onScenarioClick(getItem(position));
                }
            });
        }

        void bind(Scenario scenario) {
            String displayName = scenario.getName().isEmpty()
                    ? "Scenario " + scenario.getSlot()
                    : scenario.getName();
            textScenarioName.setText(displayName);

            List<Integer> zones = scenario.getIncludedZones();
            if (zones.isEmpty()) {
                textScenarioZones.setText("Nessuna zona");
            } else {
                StringBuilder sb = new StringBuilder("Zone: ");
                for (int i = 0; i < zones.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(zones.get(i));
                }
                textScenarioZones.setText(sb.toString());
            }

            // Icon based on scenario type
            if (scenario.isCustom()) {
                iconScenario.setImageResource(R.drawable.ic_settings);
            } else {
                iconScenario.setImageResource(R.drawable.ic_home);
            }
        }
    }
}
