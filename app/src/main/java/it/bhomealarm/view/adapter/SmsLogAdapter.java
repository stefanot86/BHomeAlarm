package it.bhomealarm.view.adapter;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import it.bhomealarm.R;
import it.bhomealarm.model.entity.SmsLog;

/**
 * Adapter per la visualizzazione dello storico dei messaggi SMS scambiati con la centrale.
 * <p>
 * Questo adapter gestisce una lista di {@link SmsLog} mostrando per ciascun messaggio
 * la direzione (inviato/ricevuto), il contenuto, il timestamp relativo e lo stato.
 * Utilizza il layout {@code item_log.xml} che presenta:
 * <ul>
 *     <li>Icona direzione: freccia verso l'alto (inviato) o verso il basso (ricevuto)</li>
 *     <li>Contenuto del messaggio SMS</li>
 *     <li>Timestamp in formato relativo (es. "5 min fa")</li>
 *     <li>Icona stato: successo (spunta), in attesa (orologio) o errore (X)</li>
 * </ul>
 * </p>
 * <p>
 * I colori delle icone variano in base allo stato:
 * <ul>
 *     <li><b>Inviato</b>: colore primario</li>
 *     <li><b>Ricevuto</b>: colore secondario</li>
 *     <li><b>Successo</b>: colore primario</li>
 *     <li><b>In attesa</b>: colore neutro</li>
 *     <li><b>Errore</b>: colore errore</li>
 * </ul>
 * </p>
 *
 * @see SmsLog
 * @see OnLogClickListener
 */
public class SmsLogAdapter extends ListAdapter<SmsLog, SmsLogAdapter.SmsLogViewHolder> {

    /**
     * Interfaccia listener per gestire gli eventi di click sui log SMS.
     * <p>
     * Implementare questa interfaccia per ricevere notifiche quando l'utente
     * tocca un elemento della lista, tipicamente per mostrare i dettagli completi
     * del messaggio SMS in un dialog o schermata dedicata.
     * </p>
     */
    public interface OnLogClickListener {
        /**
         * Chiamato quando un log SMS viene selezionato dalla lista.
         *
         * @param log il log SMS su cui e' stato effettuato il click
         */
        void onLogClick(SmsLog log);
    }

    private OnLogClickListener listener;

    private static final DiffUtil.ItemCallback<SmsLog> DIFF_CALLBACK = new DiffUtil.ItemCallback<SmsLog>() {
        @Override
        public boolean areItemsTheSame(@NonNull SmsLog oldItem, @NonNull SmsLog newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull SmsLog oldItem, @NonNull SmsLog newItem) {
            return oldItem.getMessage().equals(newItem.getMessage()) &&
                    oldItem.getStatus() == newItem.getStatus() &&
                    oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };

    public SmsLogAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnLogClickListener(OnLogClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SmsLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_log, parent, false);
        return new SmsLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsLogViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    /**
     * ViewHolder per la visualizzazione di un singolo log SMS.
     * <p>
     * Mostra le informazioni complete del messaggio: direzione, contenuto,
     * timestamp relativo e stato di invio/ricezione. Supporta il click
     * per visualizzare i dettagli completi.
     * </p>
     */
    class SmsLogViewHolder extends RecyclerView.ViewHolder {
        /** Icona per indicare la direzione del messaggio (inviato/ricevuto). */
        private final ImageView iconDirection;
        /** TextView per visualizzare il contenuto del messaggio SMS. */
        private final TextView textLogContent;
        /** TextView per visualizzare il timestamp in formato relativo. */
        private final TextView textLogTime;
        /** Icona per indicare lo stato del messaggio (successo/pending/errore). */
        private final ImageView iconStatus;

        /**
         * Costruisce un nuovo ViewHolder per l'elemento log SMS.
         * <p>
         * Configura il click listener per notificare la selezione del log.
         * </p>
         *
         * @param itemView la vista radice dell'elemento
         */
        SmsLogViewHolder(@NonNull View itemView) {
            super(itemView);
            iconDirection = itemView.findViewById(R.id.icon_direction);
            textLogContent = itemView.findViewById(R.id.text_log_content);
            textLogTime = itemView.findViewById(R.id.text_log_time);
            iconStatus = itemView.findViewById(R.id.icon_status);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLogClick(getItem(position));
                }
            });
        }

        /**
         * Associa i dati di un log SMS a questa vista.
         * <p>
         * Visualizza il contenuto del messaggio, il timestamp in formato relativo
         * (es. "5 min fa") e aggiorna le icone di direzione e stato:
         * <ul>
         *     <li><b>Direzione</b>: freccia su (blu) per inviati, freccia giu' (grigio) per ricevuti</li>
         *     <li><b>Stato successo</b>: icona spunta verde</li>
         *     <li><b>Stato pending</b>: icona orologio grigia</li>
         *     <li><b>Stato errore</b>: icona X rossa</li>
         * </ul>
         * </p>
         *
         * @param log il log SMS da visualizzare
         */
        void bind(SmsLog log) {
            textLogContent.setText(log.getMessage());

            // Format timestamp
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    log.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            textLogTime.setText(relativeTime);

            // Direction icon
            if (log.isOutgoing()) {
                iconDirection.setImageResource(R.drawable.ic_arrow_upward);
                iconDirection.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                        R.color.md_theme_light_primary));
            } else {
                iconDirection.setImageResource(R.drawable.ic_arrow_downward);
                iconDirection.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                        R.color.md_theme_light_secondary));
            }

            // Status icon
            if (log.isSuccessful()) {
                iconStatus.setImageResource(R.drawable.ic_check_circle);
                iconStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                        R.color.md_theme_light_primary));
            } else if (log.getStatus() == SmsLog.STATUS_PENDING) {
                iconStatus.setImageResource(R.drawable.ic_pending);
                iconStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                        R.color.md_theme_light_onSurfaceVariant));
            } else {
                iconStatus.setImageResource(R.drawable.ic_error);
                iconStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                        R.color.md_theme_light_error));
            }
        }
    }
}
