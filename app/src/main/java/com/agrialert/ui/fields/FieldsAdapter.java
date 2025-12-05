package com.agrialert.ui.fields;

import android.content.Context;
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
        void onFieldClicked(FieldUiModel field);
    }

    private final List<FieldUiModel> items = new ArrayList<>();
    private final OnFieldClickListener listener;

    public FieldsAdapter(OnFieldClickListener listener) {
        this.listener = listener;
    }

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
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_field, parent, false);
        return new FieldViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FieldViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView txtAddress, txtCrop, txtGroup;
        LinearLayout layoutIcons;

        public FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgFieldIcon);
            txtAddress = itemView.findViewById(R.id.txtFieldAddress);
            txtCrop = itemView.findViewById(R.id.txtFieldCrop);
            txtGroup = itemView.findViewById(R.id.txtFieldGroup);
            layoutIcons = itemView.findViewById(R.id.layoutFieldIcons);
        }

        void bind(FieldUiModel item, OnFieldClickListener listener) {
            Context ctx = itemView.getContext();

            imgIcon.setImageResource(item.iconRes);
            txtAddress.setText(item.address);
            txtCrop.setText(item.crop);
            txtGroup.setText(item.groupName);

            // icone alert associate al campo
            layoutIcons.removeAllViews();
            if (item.icons != null && !item.icons.isEmpty()) {
                for (int i = 0; i < item.icons.size(); i++) {
                    Integer iconRes = item.icons.get(i);
                    if (iconRes == null) continue;

                    ImageView iv = new ImageView(ctx);
                    int size = dpToPx(ctx, 20);
                    LinearLayout.LayoutParams params =
                            new LinearLayout.LayoutParams(size, size);
                    if (i > 0) {
                        params.setMarginStart(dpToPx(ctx, 4));
                    }
                    iv.setLayoutParams(params);
                    iv.setImageResource(iconRes);
                    layoutIcons.addView(iv);
                }
            }

            // click sulla card
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFieldClicked(item);
                }
            });
        }

        private int dpToPx(Context context, int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
