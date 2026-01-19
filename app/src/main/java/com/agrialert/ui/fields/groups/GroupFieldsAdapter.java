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

/**
 * Adapter for displaying and selecting fields within a group selection list.
 * It manages a list of {@link GroupFieldUiModel} items and handles the selection state via switches.
 */
public class GroupFieldsAdapter extends RecyclerView.Adapter<GroupFieldsAdapter.FieldViewHolder> {

    /** The list of field UI models to be displayed. */
    private final List<GroupFieldUiModel> items;

    /**
     * Constructs a new GroupFieldsAdapter.
     *
     * @param items The initial list of fields to display.
     */
    public GroupFieldsAdapter(List<GroupFieldUiModel> items) {
        this.items = items;
    }

    /**
     * Updates the data set and refreshes the adapter.
     *
     * @param newItems The new list of fields.
     */
    public void submitList(List<GroupFieldUiModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link FieldViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new FieldViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_field, parent, false);
        return new FieldViewHolder(v);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull FieldViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of fields in the list.
     */
    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * ViewHolder for individual field items in the group selection list.
     */
    class FieldViewHolder extends RecyclerView.ViewHolder {

        /** Icon representing the crop type of the field. */
        ImageView imgIcon;
        /** Displays the field address, crop type, and current group name. */
        TextView txtAddress, txtCrop, txtGroup;
        /** Switch to select or deselect the field for group assignment. */
        MaterialSwitch switchSelected;

        /**
         * Initializes the ViewHolder and its view references.
         *
         * @param itemView The view representing a single field row.
         */
        FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtCrop = itemView.findViewById(R.id.txtCrop);
            txtGroup = itemView.findViewById(R.id.txtGroup);
            switchSelected = itemView.findViewById(R.id.switchSelected);
        }

        /**
         * Binds the field data to the view components.
         *
         * @param item The UI model containing the field data.
         */
        void bind(GroupFieldUiModel item) {
            imgIcon.setImageResource(item.iconRes);
            txtAddress.setText(item.address);
            txtCrop.setText(item.crop);
            txtGroup.setText("Group: " + item.groupName);

            // Temporarily disable listener to prevent triggering during data binding
            switchSelected.setOnCheckedChangeListener(null);
            switchSelected.setChecked(item.selected);

            switchSelected.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.selected = isChecked;
            });
        }
    }
}
