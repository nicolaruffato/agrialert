package com.agrialert.ui.fields.groups;

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
 * Adapter for displaying a list of field groups in a RecyclerView.
 * Handles group item clicks and dynamic rendering of alert icons for each group.
 */
public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    /**
     * Interface definition for a callback to be invoked when a group is clicked.
     */
    public interface OnGroupClickListener {
        /**
         * Called when a group item has been clicked.
         *
         * @param group The UI model of the group that was clicked.
         */
        void onGroupClick(GroupUiModel group);
    }

    /** The list of field groups to be displayed. */
    private final List<GroupUiModel> items = new ArrayList<>();
    
    /** The listener that receives click events. */
    private final OnGroupClickListener listener;

    /**
     * Default constructor with no click listener.
     */
    public GroupsAdapter() {
        this(null);
    }

    /**
     * Constructs a new GroupsAdapter with a click listener.
     *
     * @param listener The listener for group click events.
     */
    public GroupsAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the data set and refreshes the list.
     *
     * @param newItems The new list of groups to display.
     */
    public void submitList(List<GroupUiModel> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new {@link GroupViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new GroupViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        GroupUiModel group = items.get(position);
        holder.bind(group);

        holder.itemView.setOnClickListener(v -> {
            if (listener == null) return;
            int p = holder.getAdapterPosition();
            if (p == RecyclerView.NO_POSITION) return;
            listener.onGroupClick(items.get(p));
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of groups in the list.
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder for individual group items.
     */
    static class GroupViewHolder extends RecyclerView.ViewHolder {

        /** Icon representing the group. */
        private final ImageView imgGroupIcon;
        /** Title display for the group name. */
        private final TextView txtGroupName;
        /** Description of the group. */
        private final TextView txtGroupDescription;
        /** Container for dynamically adding alert icons associated with the group's fields. */
        private final LinearLayout layoutGroupAlertIcons;

        /**
         * Initializes the ViewHolder and finds its child views.
         *
         * @param itemView The view for a single group row.
         */
        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGroupIcon = itemView.findViewById(R.id.imgGroupIcon);
            txtGroupName = itemView.findViewById(R.id.txtGroupName);
            txtGroupDescription = itemView.findViewById(R.id.txtGroupDescription);
            layoutGroupAlertIcons = itemView.findViewById(R.id.layoutGroupIcons);
        }

        /**
         * Binds the group data to the views, including dynamic alert icon generation.
         *
         * @param group The group UI model containing data to display.
         */
        void bind(@NonNull GroupUiModel group) {
            imgGroupIcon.setImageResource(group.iconRes);
            txtGroupName.setText(group.name);
            txtGroupDescription.setText(group.description);

            layoutGroupAlertIcons.removeAllViews();
            if (group.icons != null) {
                int sizePx = dpToPx(18);
                int marginPx = dpToPx(4);

                for (int resId : group.icons) {
                    ImageView iv = new ImageView(itemView.getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizePx, sizePx);
                    lp.setMargins(marginPx, 0, 0, 0);
                    iv.setLayoutParams(lp);
                    iv.setImageResource(resId);
                    layoutGroupAlertIcons.addView(iv);
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
