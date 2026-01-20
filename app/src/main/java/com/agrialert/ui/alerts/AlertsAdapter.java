package com.agrialert.ui.alerts;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying alerts in a RecyclerView.
 * Manages a list of {@link AlertUiModel} and handles user interactions.
 */
public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    /**
     * Interface for handling changes in an alert's resolution state.
     */
    public interface OnResolvedChangeListener {
        /**
         * Called when the resolution status of an alert is changed.
         *
         * @param alert      The alert model that was modified.
         * @param isResolved The new resolution state.
         */
        void onResolvedChanged(AlertUiModel alert, boolean isResolved);
    }

    /**
     * The list of alerts to be displayed in the RecyclerView.
     */
    private final List<AlertUiModel> alertList = new ArrayList<>();

    /**
     * The listener notified when an alert's resolution status changes.
     */
    private final OnResolvedChangeListener listener;

    /**
     * Constructor for the adapter.
     *
     * @param listener The listener to handle alert state changes.
     */
    public AlertsAdapter(OnResolvedChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the list of alerts displayed by the adapter.
     *
     * @param newList The new list of alerts.
     */
    public void submitList(List<AlertUiModel> newList) {
        alertList.clear();
        if (newList != null) {
            alertList.addAll(newList);
        }
        notifyDataSetChanged();
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
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
        AlertUiModel item = alertList.get(position);
        holder.bind(item, listener);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return alertList.size();
    }

    /**
     * ViewHolder for individual alert items.
     * Holds references to the views for each item in the list.
     */
    static class AlertViewHolder extends RecyclerView.ViewHolder {

        /**
         * Icon representing the type of alert.
         */
        ImageView imgIcon;

        /**
         * Title of the alert.
         */
        TextView txtTitle;

        /**
         * Text describing the threshold that triggered the alert.
         */
        TextView txtThreshold;

        /**
         * Alert duration
         */
        TextView txtDuration;
        /**
         * Address or location of the field related to the alert.
         */
        TextView txtFieldAddress;

        /**
         * View displaying the time of the alert.
         */
        TextView txtTime;

        /**
         * Label showing the alert time information.
         */
        TextView txtAlertTimeLabel;

        /**
         * Switch to mark the alert as resolved or unresolved.
         */
        SwitchMaterial switchResolved;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The view representing an individual alert item.
         */
        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgAlertIcon);
            txtTitle = itemView.findViewById(R.id.txtAlertTitle);
            txtThreshold = itemView.findViewById(R.id.txtAlertThreshold);
            txtDuration = itemView.findViewById(R.id.txtAlertDuration);
            txtFieldAddress = itemView.findViewById(R.id.txtAlertFieldAddress);
            txtTime = itemView.findViewById(R.id.txtAlertTimeLabel);
            switchResolved = itemView.findViewById(R.id.switchResolved);
        }

        /**
         * Binds an {@link AlertUiModel} to the ViewHolder views and sets up user interactions.
         *
         * @param item     The alert data to display.
         * @param listener The listener to handle switch state changes.
         */
        void bind(AlertUiModel item, OnResolvedChangeListener listener) {

            imgIcon.setImageResource(item.iconRes != 0 ? item.iconRes : R.drawable.ic_alert);

            txtTitle.setText(item.title);

            String description = item.thresholdText != null ? item.thresholdText : "";
            String firstLine = description;
            String secondLine = "";
            int newline = description.indexOf('\n');
            if (newline >= 0) {
                firstLine = description.substring(0, newline).trim();
                secondLine = description.substring(newline + 1).trim();
            }
            firstLine = firstLine.replace('\n', ' ').replace('\r', ' ').trim();
            secondLine = secondLine.replace('\n', ' ').replace('\r', ' ').trim();

            txtThreshold.setText(firstLine);
            if (secondLine.isEmpty()) {
                txtDuration.setVisibility(View.GONE);
            } else {
                txtDuration.setVisibility(View.VISIBLE);
                txtDuration.setText(secondLine);
            }
            txtFieldAddress.setText(item.fieldAddress);
            if (item.timeLabel == null || item.timeLabel.isEmpty()) {
                txtTime.setVisibility(View.GONE);
            } else {
                txtTime.setVisibility(View.VISIBLE);
                txtTime.setText(item.timeLabel);
            }

            // Temporarily remove listener to avoid triggering it while setting initial state
            switchResolved.setOnCheckedChangeListener(null);
            switchResolved.setChecked(item.isResolved);

            switchResolved.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onResolvedChanged(item, isChecked);
                }
            });
        }
    }
}
