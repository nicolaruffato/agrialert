package com.agrialert.ui.fields.groups;

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

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClicked(GroupUiModel group);
    }

    private final List<GroupUiModel> items = new ArrayList<>();
    private final OnGroupClickListener listener;

    public GroupsAdapter(OnGroupClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<GroupUiModel> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group, parent, false);
        return new GroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {

        ImageView imgGroupIcon;
        TextView txtGroupName;
        TextView txtGroupDescription;
        LinearLayout layoutIcons;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGroupIcon = itemView.findViewById(R.id.imgGroupIcon);
            txtGroupName = itemView.findViewById(R.id.txtGroupName);
            txtGroupDescription = itemView.findViewById(R.id.txtGroupDescription);
            layoutIcons = itemView.findViewById(R.id.layoutGroupIcons);
        }

        void bind(GroupUiModel item, OnGroupClickListener listener) {
            Context ctx = itemView.getContext();

            imgGroupIcon.setImageResource(item.iconRes);
            txtGroupName.setText(item.name);
            txtGroupDescription.setText(item.description);

            layoutIcons.removeAllViews();
            if (item.icons != null && !item.icons.isEmpty()) {
                for (int i = 0; i < item.icons.size(); i++) {
                    Integer iconRes = item.icons.get(i);
                    if (iconRes == null) continue;

                    ImageView iv = new ImageView(ctx);
                    int size = dpToPx(ctx, 18);
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

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGroupClicked(item);
                }
            });
        }

        private int dpToPx(Context context, int dp) {
            float density = context.getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
