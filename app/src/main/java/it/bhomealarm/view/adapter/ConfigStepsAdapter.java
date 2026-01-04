package it.bhomealarm.view.adapter;

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

import com.google.android.material.progressindicator.CircularProgressIndicator;

import it.bhomealarm.R;
import it.bhomealarm.controller.viewmodel.ConfigurationViewModel.ConfigStep;
import it.bhomealarm.controller.viewmodel.ConfigurationViewModel.StepStatus;

/**
 * Adapter per la lista degli step di configurazione.
 */
public class ConfigStepsAdapter extends ListAdapter<ConfigStep, ConfigStepsAdapter.ConfigStepViewHolder> {

    private static final DiffUtil.ItemCallback<ConfigStep> DIFF_CALLBACK = new DiffUtil.ItemCallback<ConfigStep>() {
        @Override
        public boolean areItemsTheSame(@NonNull ConfigStep oldItem, @NonNull ConfigStep newItem) {
            return oldItem.stepNumber == newItem.stepNumber;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ConfigStep oldItem, @NonNull ConfigStep newItem) {
            return oldItem.status == newItem.status &&
                    (oldItem.message == null ? newItem.message == null : oldItem.message.equals(newItem.message));
        }
    };

    public ConfigStepsAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ConfigStepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_config_step, parent, false);
        return new ConfigStepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfigStepViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ConfigStepViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconStatus;
        private final TextView textStepName;
        private final TextView textStepStatus;
        private final CircularProgressIndicator progressStep;

        ConfigStepViewHolder(@NonNull View itemView) {
            super(itemView);
            iconStatus = itemView.findViewById(R.id.icon_status);
            textStepName = itemView.findViewById(R.id.text_step_name);
            textStepStatus = itemView.findViewById(R.id.text_step_status);
            progressStep = itemView.findViewById(R.id.progress_step);
        }

        void bind(ConfigStep step) {
            textStepName.setText(step.name);

            switch (step.status) {
                case PENDING:
                    iconStatus.setImageResource(R.drawable.ic_pending);
                    iconStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                            R.color.md_theme_light_onSurfaceVariant));
                    iconStatus.setVisibility(View.VISIBLE);
                    progressStep.setVisibility(View.GONE);
                    textStepStatus.setVisibility(View.GONE);
                    break;

                case IN_PROGRESS:
                    iconStatus.setVisibility(View.GONE);
                    progressStep.setVisibility(View.VISIBLE);
                    textStepStatus.setVisibility(View.VISIBLE);
                    textStepStatus.setText(step.message != null ? step.message : "In corso...");
                    textStepStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),
                            R.color.md_theme_light_primary));
                    break;

                case COMPLETED:
                    iconStatus.setImageResource(R.drawable.ic_check_circle);
                    iconStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                            R.color.md_theme_light_primary));
                    iconStatus.setVisibility(View.VISIBLE);
                    progressStep.setVisibility(View.GONE);
                    textStepStatus.setVisibility(View.VISIBLE);
                    textStepStatus.setText("Completato");
                    textStepStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),
                            R.color.md_theme_light_primary));
                    break;

                case ERROR:
                    iconStatus.setImageResource(R.drawable.ic_error);
                    iconStatus.setColorFilter(ContextCompat.getColor(itemView.getContext(),
                            R.color.md_theme_light_error));
                    iconStatus.setVisibility(View.VISIBLE);
                    progressStep.setVisibility(View.GONE);
                    textStepStatus.setVisibility(View.VISIBLE);
                    textStepStatus.setText(step.message != null ? step.message : "Errore");
                    textStepStatus.setTextColor(ContextCompat.getColor(itemView.getContext(),
                            R.color.md_theme_light_error));
                    break;
            }
        }
    }
}
