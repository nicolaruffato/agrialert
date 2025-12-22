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

public class AlertSettingsAdapter extends RecyclerView.Adapter<AlertSettingsAdapter.AlertViewHolder> {

    public interface OnDataChangedListener {
        void onAlertChanged(List<AlertSettingUiModel> items);
    }

    private List<AlertSettingUiModel> items;
    private final OnDataChangedListener listener;

    public AlertSettingsAdapter(List<AlertSettingUiModel> items,OnDataChangedListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alert_setting, parent, false);
        return new AlertViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertSettingUiModel item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class AlertViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView txtTitle, txtSubtitle;
        MaterialSwitch switchEnabled;
        View layoutThresholds;
        View layoutThreshold1, layoutThreshold2, dividerThresholds;
        TextView txtThresholdLabel1, txtThresholdValue1;
        TextView txtThresholdLabel2, txtThresholdValue2;
        MaterialButton btnMinus1, btnPlus1, btnMinus2, btnPlus2;


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

        void bind(AlertSettingUiModel item) {
            imgIcon.setImageResource(item.iconRes);
            txtTitle.setText(item.title);
            txtSubtitle.setText(item.description);

            switchEnabled.setOnCheckedChangeListener(null);
            switchEnabled.setChecked(item.enabled);

            // visibilità contenitore soglie
            if (item.enabled && (item.hasPrimaryThreshold || item.hasSecondaryThreshold)) {
                layoutThresholds.setVisibility(View.VISIBLE);
            } else {
                layoutThresholds.setVisibility(View.GONE);
            }

            // SOGLIA 1
            if (item.hasPrimaryThreshold) {
                layoutThreshold1.setVisibility(View.VISIBLE);
                txtThresholdLabel1.setText(item.primaryLabel);
                txtThresholdValue1.setText(item.primaryValue + item.primaryUnit);
            } else {
                layoutThreshold1.setVisibility(View.GONE);
            }

            // SOGLIA 2
            if (item.hasSecondaryThreshold) {
                dividerThresholds.setVisibility(View.VISIBLE);
                layoutThreshold2.setVisibility(View.VISIBLE);
                txtThresholdLabel2.setText(item.secondaryLabel);
                txtThresholdValue2.setText(item.secondaryValue + item.secondaryUnit);
            } else {
                dividerThresholds.setVisibility(View.GONE);
                layoutThreshold2.setVisibility(View.GONE);
            }

            // cambio stato switch
            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.enabled = isChecked;
                if (isChecked && (item.hasPrimaryThreshold || item.hasSecondaryThreshold)) {
                    layoutThresholds.setVisibility(View.VISIBLE);
                } else {
                    layoutThresholds.setVisibility(View.GONE);
                }
                if (listener != null) listener.onAlertChanged(items);
            });

            // pulsanti soglia 1
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

            // pulsanti soglia 2
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

