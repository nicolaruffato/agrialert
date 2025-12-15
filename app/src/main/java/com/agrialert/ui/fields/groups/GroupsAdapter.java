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

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(GroupUiModel group);
    }

    private final List<GroupUiModel> items = new ArrayList<>();
    private final OnGroupClickListener listener;

    public GroupsAdapter() {
        this(null);
    }

    public GroupsAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<GroupUiModel> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgGroupIcon;
        private final TextView txtGroupName;
        private final TextView txtGroupDescription;
        private final LinearLayout layoutGroupAlertIcons;

        GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGroupIcon = itemView.findViewById(R.id.imgGroupIcon);
            txtGroupName = itemView.findViewById(R.id.txtGroupName);
            txtGroupDescription = itemView.findViewById(R.id.txtGroupDescription);
            layoutGroupAlertIcons = itemView.findViewById(R.id.layoutGroupIcons);
        }

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

        private int dpToPx(int dp) {
            float density = itemView.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
