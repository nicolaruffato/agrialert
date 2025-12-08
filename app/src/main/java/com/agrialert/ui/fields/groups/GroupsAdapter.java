package com.agrialert.ui.fields.groups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter per la lista dei gruppi nella schermata "Campi / Gruppi di campi".
 *
 * Layout riga: res/layout/item_group.xml
 * ID usati:
 *  - imgGroupIcon          -> icona grande del gruppo
 *  - txtGroupName          -> nome gruppo
 *  - txtGroupDescription   -> descrizione gruppo
 *  - layoutGroupAlertIcons -> fila con icone degli alert del gruppo (max 6)
 *
 * Campi attesi in GroupUiModel:
 *  public long id;
 *  public String name;
 *  public String description;
 *  public int iconRes;          // icona principale del gruppo
 *  public List<Integer> icons;  // icone dei tipi di alert associati
 */
public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupViewHolder> {



    private final List<GroupUiModel> items = new ArrayList<>();
    private final Fragment fragment;
    public GroupsAdapter( Fragment fragment) {
        this.fragment = fragment;
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
        GroupUiModel item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Rimpiazza l’elenco dei gruppi con nuovi dati (es. quelli che arriveranno dal DB).
     */
    public void submitList(@NonNull List<GroupUiModel> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    // =========================================
    //              VIEW HOLDER
    // =========================================
    class GroupViewHolder extends RecyclerView.ViewHolder {

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

            // CLICK SULLA CARD -> apre "Visualizza gruppo"
            itemView.setOnClickListener(v ->
                    NavHostFragment.findNavController(fragment)
                            .navigate(R.id.viewGroupFragment)
            );
        }

        void bind(@NonNull GroupUiModel item) {
            // Nome e descrizione
            txtGroupName.setText(item.name);
            txtGroupDescription.setText(item.description);

            // Icona grande del gruppo
            imgGroupIcon.setImageResource(item.iconRes);

            // icone degli alert associati (max 6)
            layoutGroupAlertIcons.removeAllViews();
            if (item.icons != null && !item.icons.isEmpty()) {
                final int maxIcons = Math.min(item.icons.size(), 6);
                for (int i = 0; i < maxIcons; i++) {
                    Integer iconRes = item.icons.get(i);
                    if (iconRes == null) continue;

                    ImageView iv = new ImageView(itemView.getContext());
                    // misura in dp ~20 come negli altri adapter
                    int sizePx = dpToPx(20);
                    LinearLayout.LayoutParams params =
                            new LinearLayout.LayoutParams(sizePx, sizePx);

                    // un po’ di spazio a sinistra dalle icone successive
                    if (i > 0) {
                        params.setMarginStart(dpToPx(4));
                    }

                    iv.setLayoutParams(params);
                    iv.setImageResource(iconRes);
                    layoutGroupAlertIcons.addView(iv);
                }
            }
        }

        private int dpToPx(int dp) {
            float density = itemView.getResources().getDisplayMetrics().density;
            return (int) (dp * density + 0.5f);
        }
    }
}
