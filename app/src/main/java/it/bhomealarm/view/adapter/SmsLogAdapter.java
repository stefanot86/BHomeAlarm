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
 * Adapter per la lista log SMS.
 */
public class SmsLogAdapter extends ListAdapter<SmsLog, SmsLogAdapter.SmsLogViewHolder> {

    public interface OnLogClickListener {
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

    class SmsLogViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconDirection;
        private final TextView textLogContent;
        private final TextView textLogTime;
        private final ImageView iconStatus;

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
