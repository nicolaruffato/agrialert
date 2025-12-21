package com.agrialert.ui.fields;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;

import java.util.ArrayList;
import java.util.List;

public class FieldsAdapter extends RecyclerView.Adapter<FieldsAdapter.FieldViewHolder> {

    public interface OnFieldClickListener {
        void onFieldClick(FieldUiModel field);
    }

    private final List<FieldUiModel> items = new ArrayList<>();
    private final OnFieldClickListener listener;

    //  Per liste dove NON serve click
    public FieldsAdapter() {
        this(null);
    }

    //  Per Dashboard (click → naviga)
    public FieldsAdapter(OnFieldClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FieldUiModel> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
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

        holder.itemView.setOnClickListener(v -> {
            if (listener == null) return;
            int p = holder.getAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;
            listener.onFieldClick(items.get(p));
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FieldViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgFieldIcon;
        private final TextView txtAddress;
        private final TextView txtCrop;
        private final TextView txtGroup;
        private final LinearLayout layoutAlertIcons;

        FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFieldIcon = itemView.findViewById(R.id.imgFieldIcon);
            txtAddress = itemView.findViewById(R.id.txtFieldAddress);
            txtCrop = itemView.findViewById(R.id.txtFieldCrop);
            txtGroup = itemView.findViewById(R.id.txtFieldGroup);
            layoutAlertIcons = itemView.findViewById(R.id.layoutAlertIcons);
        }

        void bind(@NonNull FieldUiModel field) {
            imgFieldIcon.setImageResource(field.iconRes);
            txtAddress.setText(field.address);
            txtCrop.setText(field.cropType);
            txtGroup.setText("Gruppo: " + (field.groupName == null ? "-" : field.groupName));

            // icone alert (a destra)
            layoutAlertIcons.removeAllViews();
            if (field.alertIcons != null) {
                int sizePx = dpToPx(18);
                int marginPx = dpToPx(4);

                for (int resId : field.alertIcons) {
                    ImageView iv = new ImageView(itemView.getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
                    lp.setMargins(marginPx, 0, 0, 0);
                    iv.setLayoutParams(lp);
                    iv.setImageResource(resId);
                    layoutAlertIcons.addView(iv);
                }
            }
        }

        private int dpToPx(int dp) {
            float density = itemView.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
