package com.agrialert.ui.fields;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;


import com.agrialert.R;

import java.util.ArrayList;
import java.util.List;

public class FieldsAdapter extends RecyclerView.Adapter<FieldsAdapter.FieldViewHolder> {

    private final List<FieldUiModel> items = new ArrayList<>();

    public void submitList(List<FieldUiModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_field, parent, false);
        return new FieldViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        FieldUiModel field = items.get(position);
        holder.bind(field);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ---------------- VIEW HOLDER ----------------

    static class FieldViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgFieldIcon;
        private final TextView txtFieldAddress;
        private final TextView txtFieldCrop;
        private final TextView txtFieldGroup;
        private final LinearLayout layoutAlertIcons;

        FieldViewHolder(@NonNull View itemView) {
            super(itemView);

            imgFieldIcon = itemView.findViewById(R.id.imgFieldIcon);
            txtFieldAddress = itemView.findViewById(R.id.txtFieldAddress);
            txtFieldCrop = itemView.findViewById(R.id.txtFieldCrop);
            txtFieldGroup = itemView.findViewById(R.id.txtFieldGroup);
            layoutAlertIcons = itemView.findViewById(R.id.layoutAlertIcons);

            // CLICK sulla card: apri Visualizza Campo
            itemView.setOnClickListener(v -> {
                Navigation.findNavController(v)
                        .navigate(R.id.viewFieldFragment);
            });
        }


        void bind(FieldUiModel field) {
            Context context = itemView.getContext();

            // Testi dinamici
            txtFieldAddress.setText(field.address);
            txtFieldCrop.setText(field.cropType);

            if (field.groupName == null || field.groupName.isEmpty()) {
                txtFieldGroup.setText("Gruppo: nessun gruppo");
            } else {
                txtFieldGroup.setText("Gruppo: " + field.groupName);
            }

            // Icona principale in base alla coltura (già scelta nel model)
            imgFieldIcon.setImageResource(field.iconRes);

            // Icone alert/meteo (max 6)
            layoutAlertIcons.removeAllViews(); // pulisce quelle vecchie

            if (field.alertIcons != null && !field.alertIcons.isEmpty()) {
                int maxIcons = Math.min(6, field.alertIcons.size());

                for (int i = 0; i < maxIcons; i++) {
                    Integer iconResId = field.alertIcons.get(i);
                    if (iconResId == null) continue;

                    ImageView iconView = new ImageView(context);
                    LinearLayout.LayoutParams params =
                            new LinearLayout.LayoutParams(
                                    dpToPx(context, 20),
                                    dpToPx(context, 20)
                            );
                    if (i > 0) {
                        params.setMarginStart(dpToPx(context, 4));
                    }
                    iconView.setLayoutParams(params);
                    iconView.setImageResource(iconResId);

                    layoutAlertIcons.addView(iconView);
                }
            }
        }

        private int dpToPx(Context context, int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
