package com.agrialert.ui.fields.groups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

public class GroupFieldsAdapter extends RecyclerView.Adapter<GroupFieldsAdapter.FieldViewHolder> {

    private final List<GroupFieldUiModel> items;

    public GroupFieldsAdapter(List<GroupFieldUiModel> items) {
        this.items = items;
    }

    public void submitList(List<GroupFieldUiModel> newItems) {
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
                .inflate(R.layout.item_group_field, parent, false);
        return new FieldViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    class FieldViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView txtAddress, txtCrop, txtGroup;
        MaterialSwitch switchSelected;

        FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtCrop = itemView.findViewById(R.id.txtCrop);
            txtGroup = itemView.findViewById(R.id.txtGroup);
            switchSelected = itemView.findViewById(R.id.switchSelected);
        }

        void bind(GroupFieldUiModel item) {
            imgIcon.setImageResource(item.iconRes);
            txtAddress.setText(item.address);
            txtCrop.setText(item.crop);
            txtGroup.setText("Gruppo: " + item.groupName);

            switchSelected.setOnCheckedChangeListener(null);
            switchSelected.setChecked(item.selected);

            switchSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.selected = isChecked;
            });
        }
    }
}

