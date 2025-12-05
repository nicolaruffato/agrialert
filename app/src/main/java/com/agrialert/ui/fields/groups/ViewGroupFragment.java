package com.agrialert.ui.fields.groups;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.agrialert.ui.fields.FieldUiModel;
import com.agrialert.ui.fields.FieldsAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewGroupFragment extends Fragment {

    private ImageView imgGroupIcon;
    private TextView txtGroupName;
    private TextView txtGroupDescription;
    private LinearLayout layoutGroupIcons;
    private RecyclerView rvGroupFields;

    private FieldsAdapter fieldsAdapter;

    public ViewGroupFragment() {
        // costruttore vuoto richiesto
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgGroupIcon = view.findViewById(R.id.imgGroupIcon);
        txtGroupName = view.findViewById(R.id.txtGroupName);
        txtGroupDescription = view.findViewById(R.id.txtGroupDescription);
        layoutGroupIcons = view.findViewById(R.id.layoutGroupIcons);
        rvGroupFields = view.findViewById(R.id.rvGroupFields);

        rvGroupFields.setLayoutManager(new LinearLayoutManager(requireContext()));

        // riuso il FieldsAdapter; qui il click può aprire ancora Visualizza Campo
        fieldsAdapter = new FieldsAdapter(field -> {
            // per ora lasciamo vuoto, o potresti navigare a ViewFieldFragment se vuoi
            // NavHostFragment.findNavController(ViewGroupFragment.this)
            //      .navigate(R.id.viewFieldFragment);
        });
        rvGroupFields.setAdapter(fieldsAdapter);

        bindFakeGroupData();
    }

    private void bindFakeGroupData() {
        // GRUPPO finto
        GroupUiModel group = createFakeGroup();

        imgGroupIcon.setImageResource(group.iconRes);
        txtGroupName.setText(group.name);
        txtGroupDescription.setText(group.description);

        // icone alert del gruppo
        layoutGroupIcons.removeAllViews();
        Context ctx = requireContext();
        if (group.icons != null && !group.icons.isEmpty()) {
            for (int i = 0; i < group.icons.size(); i++) {
                Integer iconRes = group.icons.get(i);
                if (iconRes == null) continue;

                ImageView iv = new ImageView(ctx);
                int size = dpToPx(ctx, 22);
                LinearLayout.LayoutParams params =
                        new LinearLayout.LayoutParams(size, size);
                if (i > 0) {
                    params.setMarginStart(dpToPx(ctx, 4));
                }
                iv.setLayoutParams(params);
                iv.setImageResource(iconRes);
                layoutGroupIcons.addView(iv);
            }
        }

        // CAMPI finti del gruppo
        fieldsAdapter.submitList(createFakeGroupFields());
    }

    private GroupUiModel createFakeGroup() {
        return new GroupUiModel(
                1,
                "Gruppo A",
                "Campi in zona Mestre con ortaggi.",
                R.drawable.ic_group_default,
                Arrays.asList(
                        R.drawable.ic_alert_vento,
                        R.drawable.ic_alert_gelo,
                        R.drawable.ic_alert_calore
                )
        );
    }

    private List<FieldUiModel> createFakeGroupFields() {
        List<FieldUiModel> list = new ArrayList<>();

        int iconOrtaggi = R.drawable.ic_ortaggi;

        list.add(new FieldUiModel(
                1,
                "Via Verdirdi, 15 - Mestre (VE)",
                "Ortaggi",
                "Gruppo A",
                iconOrtaggi,
                Arrays.asList(R.drawable.ic_alert_vento, R.drawable.ic_alert_calore)
        ));
        list.add(new FieldUiModel(
                2,
                "Via Giallo, 10 - Mestre (VE)",
                "Ortaggi",
                "Gruppo A",
                iconOrtaggi,
                Arrays.asList(R.drawable.ic_alert_gelo)
        ));

        return list;
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
