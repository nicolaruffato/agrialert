package com.agrialert.ui.alerts;

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

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertViewHolder> {

    public interface OnResolvedChangeListener {
        void onResolvedChanged(AlertUiModel alert, boolean isResolved);
    }

    private final List<AlertUiModel> alertList = new ArrayList<>();
    private final OnResolvedChangeListener listener;

    public AlertsAdapter(OnResolvedChangeListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AlertUiModel> newList) {
        alertList.clear();
        if (newList != null) {
            alertList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertUiModel item = alertList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView txtTitle;
        TextView txtThreshold;
        TextView txtDuration;
        TextView txtFieldAddress;
        TextView txtTime;
        TextView txtResolvedLabel;
        SwitchMaterial switchResolved;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgAlertIcon);
            txtTitle = itemView.findViewById(R.id.txtAlertTitle);
            txtThreshold = itemView.findViewById(R.id.txtAlertThreshold);
            txtDuration = itemView.findViewById(R.id.txtAlertDuration);
            txtFieldAddress = itemView.findViewById(R.id.txtAlertFieldAddress);
            txtTime = itemView.findViewById(R.id.txtAlertTimeLabel);
            switchResolved = itemView.findViewById(R.id.switchResolved);
            //txtResolvedLabel = itemView.findViewById(R.id.txtResolvedLabel);
        }

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
            //txtResolvedLabel.setText(time[1]);

            // evito che il listener scatti quando faccio setChecked
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

