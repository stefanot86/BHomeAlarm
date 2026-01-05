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
 * Adapter RecyclerView per la visualizzazione della lista scenari.
 * <p>
 * Ogni item mostra:
 * <ul>
 *     <li>Icona (diversa per scenari predefiniti e personalizzati)</li>
 *     <li>Nome dello scenario</li>
 *     <li>Elenco delle zone incluse</li>
 * </ul>
 * <p>
 * Supporta due tipi di interazione:
 * <ul>
 *     <li><b>Click</b>: per selezionare uno scenario da attivare</li>
 *     <li><b>Long click</b>: per eliminare uno scenario</li>
 * </ul>
 * <p>
 * Utilizza {@link DiffUtil} per ottimizzare gli aggiornamenti della lista.
 *
 * @see it.bhomealarm.view.fragment.ScenariosFragment
 * @see Scenario
 */
public class ScenariosAdapter extends ListAdapter<Scenario, ScenariosAdapter.ScenarioViewHolder> {

    /**
     * Interfaccia per il listener del click su uno scenario.
     * Usato per mostrare il dialog di conferma attivazione.
     */
    public interface OnScenarioClickListener {
        /**
         * Chiamato quando l'utente tocca uno scenario.
         *
         * @param scenario Lo scenario selezionato
         */
        void onScenarioClick(Scenario scenario);
    }

    /**
     * Interfaccia per il listener del long click su uno scenario.
     * Usato per mostrare il dialog di eliminazione.
     */
    public interface OnScenarioLongClickListener {
        /**
         * Chiamato quando l'utente tiene premuto su uno scenario.
         *
         * @param scenario Lo scenario su cui è stato fatto long press
         */
        void onScenarioLongClick(Scenario scenario);
    }

    /** Listener per il click normale */
    private OnScenarioClickListener listener;

    /** Listener per il long click */
    private OnScenarioLongClickListener longClickListener;

    /**
     * Callback per il calcolo delle differenze tra liste.
     * Utilizzato da {@link ListAdapter} per animare gli aggiornamenti.
     */
    private static final DiffUtil.ItemCallback<Scenario> DIFF_CALLBACK = new DiffUtil.ItemCallback<Scenario>() {
        /**
         * Verifica se due item rappresentano lo stesso scenario.
         * Usa l'ID del database come identificatore univoco.
         */
        @Override
        public boolean areItemsTheSame(@NonNull Scenario oldItem, @NonNull Scenario newItem) {
            return oldItem.getId() == newItem.getId();
        }

        /**
         * Verifica se il contenuto di due item è identico.
         * Confronta nome, zone e stato abilitazione.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Scenario oldItem, @NonNull Scenario newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getZoneMask() == newItem.getZoneMask() &&
                    oldItem.isEnabled() == newItem.isEnabled();
        }
    };

    /**
     * Costruttore dell'adapter.
     * Inizializza con il callback per il calcolo delle differenze.
     */
    public ScenariosAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Imposta il listener per il click sugli scenari.
     *
     * @param listener Il listener da notificare al click
     */
    public void setOnScenarioClickListener(OnScenarioClickListener listener) {
        this.listener = listener;
    }

    /**
     * Imposta il listener per il long click sugli scenari.
     *
     * @param listener Il listener da notificare al long click
     */
    public void setOnScenarioLongClickListener(OnScenarioLongClickListener listener) {
        this.longClickListener = listener;
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

    /**
     * ViewHolder per un singolo item scenario.
     * <p>
     * Gestisce:
     * <ul>
     *     <li>La visualizzazione dei dati dello scenario</li>
     *     <li>Il click per la selezione</li>
     *     <li>Il long click per l'eliminazione</li>
     * </ul>
     */
    class ScenarioViewHolder extends RecyclerView.ViewHolder {

        /** Icona dello scenario (diversa per predefiniti/custom) */
        private final ImageView iconScenario;

        /** Nome dello scenario */
        private final TextView textScenarioName;

        /** Elenco delle zone incluse */
        private final TextView textScenarioZones;

        /**
         * Costruttore del ViewHolder.
         * Inizializza le view e configura i click listener.
         *
         * @param itemView La view root dell'item
         */
        ScenarioViewHolder(@NonNull View itemView) {
            super(itemView);
            iconScenario = itemView.findViewById(R.id.icon_scenario);
            textScenarioName = itemView.findViewById(R.id.text_scenario_name);
            textScenarioZones = itemView.findViewById(R.id.text_scenario_zones);

            // Click normale: seleziona lo scenario per l'attivazione
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onScenarioClick(getItem(position));
                }
            });

            // Long click: mostra opzione per eliminare lo scenario
            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    longClickListener.onScenarioLongClick(getItem(position));
                    return true;  // Evento consumato
                }
                return false;
            });
        }

        /**
         * Popola le view con i dati dello scenario.
         * <p>
         * Mostra:
         * <ul>
         *     <li>Nome: il nome dello scenario o "Scenario N" se vuoto</li>
         *     <li>Zone: elenco separato da virgole o "Nessuna zona"</li>
         *     <li>Icona: ic_settings per custom, ic_home per predefiniti</li>
         * </ul>
         *
         * @param scenario Lo scenario da visualizzare
         */
        void bind(Scenario scenario) {
            // Nome: usa il nome salvato o genera un default
            String displayName = scenario.getName().isEmpty()
                    ? "Scenario " + scenario.getSlot()
                    : scenario.getName();
            textScenarioName.setText(displayName);

            // Zone: mostra l'elenco delle zone incluse
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

            // Icona: differenzia visivamente scenari custom da predefiniti
            if (scenario.isCustom()) {
                iconScenario.setImageResource(R.drawable.ic_settings);
            } else {
                iconScenario.setImageResource(R.drawable.ic_home);
            }
        }
    }
}
