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

/**
 * Adapter for displaying a list of agricultural fields in a RecyclerView.
 * Handles field item clicks and dynamic rendering of associated alert icons.
 */
public class FieldsAdapter extends RecyclerView.Adapter<FieldsAdapter.FieldViewHolder> {

    /**
     * Interface definition for a callback to be invoked when a field is clicked.
     */
    public interface OnFieldClickListener {
        /**
         * Called when a field item has been clicked.
         *
         * @param field The UI model of the field that was clicked.
         */
        void onFieldClick(FieldUiModel field);
    }

    /** The list of fields to be displayed. */
    private final List<FieldUiModel> items = new ArrayList<>();
    
    /** The listener that receives click events. */
    private final OnFieldClickListener listener;

    /**
     * Default constructor for lists where click interaction is not required.
     */
    public FieldsAdapter() {
        this(null);
    }

    /**
     * Constructor used when field click interactions need to be handled (e.g., in Dashboard).
     *
     * @param listener The listener for field click events.
     */
    public FieldsAdapter(OnFieldClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the data set and refreshes the adapter.
     *
     * @param newItems The new list of fields to display.
     */
    public void submitList(List<FieldUiModel> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link FieldViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new FieldViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public FieldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_field, parent, false);
        return new FieldViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
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

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of fields in the list.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for individual field items.
     */
    static class FieldViewHolder extends RecyclerView.ViewHolder {

        /** Icon representing the crop type or status of the field. */
        private final ImageView imgFieldIcon;
        /** Displays the address of the field. */
        private final TextView txtAddress;
        /** Displays the crop type of the field. */
        private final TextView txtCrop;
        /** Displays the name of the group the field belongs to. */
        private final TextView txtGroup;
        /** Container for dynamically adding alert icons associated with the field. */
        private final LinearLayout layoutAlertIcons;

        /**
         * Initializes the ViewHolder and finds its child views.
         *
         * @param itemView The view for a single field row.
         */
        FieldViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFieldIcon = itemView.findViewById(R.id.imgFieldIcon);
            txtAddress = itemView.findViewById(R.id.txtFieldAddress);
            txtCrop = itemView.findViewById(R.id.txtFieldCrop);
            txtGroup = itemView.findViewById(R.id.txtFieldGroup);
            layoutAlertIcons = itemView.findViewById(R.id.layoutAlertIcons);
        }

        /**
         * Binds the field data to the views, including dynamic alert icon generation.
         *
         * @param field The field UI model containing data to display.
         */
        void bind(@NonNull FieldUiModel field) {
            imgFieldIcon.setImageResource(field.iconRes);
            txtAddress.setText(field.address);
            txtCrop.setText(field.crop);
            txtGroup.setText("Group: " + (field.groupName == null ? "-" : field.groupName));

            // Dynamic alert icons (on the right)
            layoutAlertIcons.removeAllViews();
            if (field.icons != null) {
                int sizePx = dpToPx(18);
                int marginPx = dpToPx(4);

                for (int resId : field.icons) {
                    ImageView iv = new ImageView(itemView.getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
                    lp.setMargins(marginPx, 0, 0, 0);
                    iv.setLayoutParams(lp);
                    iv.setImageResource(resId);
                    layoutAlertIcons.addView(iv);
                }
            }
        }

        /**
         * Converts dp units to pixels based on screen density.
         *
         * @param dp The value in density-independent pixels.
         * @return The value in physical pixels.
         */
        private int dpToPx(int dp) {
            float density = itemView.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
