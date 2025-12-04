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

    private final List<AlertUiModel> alertList = new ArrayList<>();

    // Aggiorna la lista visibile
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
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    // ---------- VIEW HOLDER ----------
    static class AlertViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView txtTitle;
        TextView txtThreshold;
        TextView txtFieldAddress;
        TextView txtTime;
        TextView txtResolvedLabel;
        SwitchMaterial switchResolved;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgAlertIcon);
            txtTitle = itemView.findViewById(R.id.txtAlertTitle);
            txtThreshold = itemView.findViewById(R.id.txtAlertThreshold);
            txtFieldAddress = itemView.findViewById(R.id.txtAlertFieldAddress);
            txtTime = itemView.findViewById(R.id.txtAlertTimeLabel);
            switchResolved = itemView.findViewById(R.id.switchResolved);
            txtResolvedLabel = itemView.findViewById(R.id.txtResolvedLabel);
        }

        void bind(AlertUiModel item) {

            // Icona
            if (item.iconRes != 0) {
                imgIcon.setImageResource(item.iconRes);
            }

            // Testi
            txtTitle.setText(item.title);
            txtThreshold.setText(item.thresholdText);
            txtFieldAddress.setText(item.fieldAddress);
            txtTime.setText(item.timeLabel);

            // Switch Risolto
            switchResolved.setOnCheckedChangeListener(null);
            switchResolved.setChecked(item.isResolved);

            // Testo "Risolto"
            txtResolvedLabel.setText("Risolto");

            // Quando l’utente usa lo switch
            switchResolved.setOnCheckedChangeListener((buttonView, checked) -> {
                item.isResolved = checked;
            });
        }
    }
}
