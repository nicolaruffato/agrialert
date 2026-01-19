package com.agrialert.ui.alerts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

/**
 * Adapter for configuring alert settings in a RecyclerView.
 * This adapter manages a list of {@link AlertSettingUiModel} and provides UI for
 * toggling alerts and adjusting their threshold values.
 */
public class AlertSettingsAdapter extends RecyclerView.Adapter<AlertSettingsAdapter.AlertViewHolder> {

    /**
     * Interface for listening to data changes within the adapter.
     */
    public interface OnDataChangedListener {
        /**
         * Called whenever an alert setting (enabled state or threshold) is modified.
         *
         * @param items The updated list of alert settings.
         */
        void onAlertChanged(List<AlertSettingUiModel> items);
    }

    /**
     * The list of alert settings to display.
     */
    private List<AlertSettingUiModel> items;

    /**
     * Listener for data change events.
     */
    private final OnDataChangedListener listener;

    /**
     * Constructs a new AlertSettingsAdapter.
     *
     * @param items    The initial list of alert settings.
     * @param listener The callback to notify when data changes.
     */
    public AlertSettingsAdapter(List<AlertSettingUiModel> items, OnDataChangedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link AlertViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new AlertViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert_setting, parent, false);
        return new AlertViewHolder(v);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link AlertViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertSettingUiModel item = items.get(position);
        holder.bind(item);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * ViewHolder class for individual alert setting items.
     */
    class AlertViewHolder extends RecyclerView.ViewHolder {

        /** Icon representing the alert type. */
        ImageView imgIcon;
        /** Title and subtitle/description of the alert. */
        TextView txtTitle, txtSubtitle;
        /** Switch to enable or disable the alert. */
        MaterialSwitch switchEnabled;
        /** Container for threshold adjustment UI. */
        View layoutThresholds;
        /** Individual threshold layouts and their divider. */
        View layoutThreshold1, layoutThreshold2, dividerThresholds;
        /** Labels and value displays for thresholds. */
        TextView txtThresholdLabel1, txtThresholdValue1;
        /** Labels and value displays for thresholds. */
        TextView txtThresholdLabel2, txtThresholdValue2;
        /** Buttons to increment or decrement threshold values. */
        MaterialButton btnMinus1, btnPlus1, btnMinus2, btnPlus2;


        /**
         * Initializes the ViewHolder and its view references.
         *
         * @param itemView The view representing a single alert setting row.
         */
        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSubtitle = itemView.findViewById(R.id.txtSubtitle);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
            layoutThresholds = itemView.findViewById(R.id.layoutThresholds);
            layoutThreshold1 = itemView.findViewById(R.id.layoutThreshold1);
            layoutThreshold2 = itemView.findViewById(R.id.layoutThreshold2);
            dividerThresholds = itemView.findViewById(R.id.dividerThresholds);

            txtThresholdLabel1 = itemView.findViewById(R.id.txtThresholdLabel1);
            txtThresholdValue1 = itemView.findViewById(R.id.txtThresholdValue1);
            btnMinus1 = itemView.findViewById(R.id.btnMinus1);
            btnPlus1 = itemView.findViewById(R.id.btnPlus1);

            txtThresholdLabel2 = itemView.findViewById(R.id.txtThresholdLabel2);
            txtThresholdValue2 = itemView.findViewById(R.id.txtThresholdValue2);
            btnMinus2 = itemView.findViewById(R.id.btnMinus2);
            btnPlus2 = itemView.findViewById(R.id.btnPlus2);

        }

        /**
         * Binds the given {@link AlertSettingUiModel} to the ViewHolder views,
         * updating UI state and registering user interaction callbacks.
         *
         * @param item The UI model containing the alert settings.
         */
        void bind(AlertSettingUiModel item) {
            imgIcon.setImageResource(item.iconRes);
            txtTitle.setText(item.title);
            txtSubtitle.setText(item.description);

            // Temporarily remove listener to prevent unwanted triggers during data binding
            switchEnabled.setOnCheckedChangeListener(null);
            switchEnabled.setChecked(item.enabled);

            // Update threshold container visibility based on current state
            if (item.enabled && (item.hasPrimaryThreshold || item.hasSecondaryThreshold)) {
                layoutThresholds.setVisibility(View.VISIBLE);
            } else {
                layoutThresholds.setVisibility(View.GONE);
            }

            // Bind Primary Threshold (Threshold 1)
            if (item.hasPrimaryThreshold) {
                layoutThreshold1.setVisibility(View.VISIBLE);
                txtThresholdLabel1.setText(item.primaryLabel);
                txtThresholdValue1.setText(item.primaryValue + item.primaryUnit);
            } else {
                layoutThreshold1.setVisibility(View.GONE);
            }

            // Bind Secondary Threshold (Threshold 2)
            if (item.hasSecondaryThreshold) {
                dividerThresholds.setVisibility(View.VISIBLE);
                layoutThreshold2.setVisibility(View.VISIBLE);
                txtThresholdLabel2.setText(item.secondaryLabel);
                txtThresholdValue2.setText(item.secondaryValue + item.secondaryUnit);
            } else {
                dividerThresholds.setVisibility(View.GONE);
                layoutThreshold2.setVisibility(View.GONE);
            }

            // Handle switch state changes
            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.enabled = isChecked;
                if (isChecked && (item.hasPrimaryThreshold || item.hasSecondaryThreshold)) {
                    layoutThresholds.setVisibility(View.VISIBLE);
                } else {
                    layoutThresholds.setVisibility(View.GONE);
                }
                if (listener != null) listener.onAlertChanged(items);
            });

            // Set up Primary Threshold adjustment buttons
            if (item.hasPrimaryThreshold) {
                btnMinus1.setOnClickListener(v -> {
                    item.primaryValue = Math.max(0, item.primaryValue - 1);
                    txtThresholdValue1.setText(item.primaryValue + item.primaryUnit);
                    if (listener != null) listener.onAlertChanged(items);
                });
                btnPlus1.setOnClickListener(v -> {
                    item.primaryValue = item.primaryValue + 1;
                    txtThresholdValue1.setText(item.primaryValue + item.primaryUnit);
                    if (listener != null) listener.onAlertChanged(items);
                });
            } else {
                btnMinus1.setOnClickListener(null);
                btnPlus1.setOnClickListener(null);
            }

            // Set up Secondary Threshold adjustment buttons
            if (item.hasSecondaryThreshold) {
                btnMinus2.setOnClickListener(v -> {
                    item.secondaryValue = Math.max(0, item.secondaryValue - 1);
                    txtThresholdValue2.setText(item.secondaryValue + item.secondaryUnit);
                    if (listener != null) listener.onAlertChanged(items);
                });
                btnPlus2.setOnClickListener(v -> {
                    item.secondaryValue = item.secondaryValue + 1;
                    txtThresholdValue2.setText(item.secondaryValue + item.secondaryUnit);
                    if (listener != null) listener.onAlertChanged(items);
                });
            } else {
                btnMinus2.setOnClickListener(null);
                btnPlus2.setOnClickListener(null);
            }
        }

    }
}
