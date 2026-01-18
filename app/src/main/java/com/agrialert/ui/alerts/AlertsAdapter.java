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
        TextView txtFieldAddress;
        TextView txtTime;
        TextView txtAlertTimeLabel;
        SwitchMaterial switchResolved;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgAlertIcon);
            txtTitle = itemView.findViewById(R.id.txtAlertTitle);
            txtThreshold = itemView.findViewById(R.id.txtAlertThreshold);
            txtFieldAddress = itemView.findViewById(R.id.txtAlertFieldAddress);
            txtTime = itemView.findViewById(R.id.txtAlertTimeLabel);
            switchResolved = itemView.findViewById(R.id.switchResolved);
            txtAlertTimeLabel = itemView.findViewById(R.id.txtAlertTimeLabel);
        }

        void bind(AlertUiModel item, OnResolvedChangeListener listener) {

            if (item.iconRes != 0) {
                imgIcon.setImageResource(item.iconRes);
            }

            txtTitle.setText(item.title);
            txtThreshold.setText(item.thresholdText);
            txtFieldAddress.setText(item.fieldAddress);
            // Previsto per 08/01 18:00
            String[] time = item.timeLabel.split(" ");
            txtAlertTimeLabel.setText(time[2]);

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

