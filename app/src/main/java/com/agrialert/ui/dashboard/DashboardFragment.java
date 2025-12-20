package com.agrialert.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.agrialert.ui.fields.FieldUiModel;
import com.agrialert.ui.fields.FieldsAdapter;
import com.agrialert.ui.fields.groups.GroupUiModel;
import com.agrialert.ui.fields.groups.GroupsAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DashboardFragment extends Fragment {

    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    private TextView txtAlertCount, txtNoActiveAlerts, txtSeeAllAlerts;
    private LinearLayout layoutAlertPreview;
    private MaterialButtonToggleGroup toggleDash;
    private MaterialButton btnDashFields, btnDashGroups;
    private RecyclerView rvDashboardPreview;

    // riuso adapter già esistenti
    private FieldsAdapter fieldsAdapter;
    private GroupsAdapter groupsAdapter;


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtAlertCount = view.findViewById(R.id.txtAlertCount);
        txtNoActiveAlerts = view.findViewById(R.id.txtNoActiveAlerts);
        txtSeeAllAlerts = view.findViewById(R.id.txtSeeAllAlerts);
        layoutAlertPreview = view.findViewById(R.id.layoutAlertPreview);
        TextView txtSeeAllAlerts = view.findViewById(R.id.txtSeeAllAlerts);


        txtSeeAllAlerts.setOnClickListener(v -> {
            BottomNavigationView bottomNav =
                    (BottomNavigationView) requireActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.alertsListFragment);
        });

        toggleDash = view.findViewById(R.id.toggleFieldsGroupsDash);
        btnDashFields = view.findViewById(R.id.btnDashFields);
        btnDashGroups = view.findViewById(R.id.btnDashGroups);
        rvDashboardPreview = view.findViewById(R.id.rvDashboardPreview);

        rvDashboardPreview.setLayoutManager(new LinearLayoutManager(requireContext()));

// IMPORTANTISSIMO: qui usi gli adapter che hai già (quelli della lista)
        fieldsAdapter = new FieldsAdapter(field -> {
            // preview click -> visualizza campo (per ora senza id veri)
            NavHostFragment.findNavController(this).navigate(R.id.viewFieldFragment);
        });

        groupsAdapter = new GroupsAdapter(group -> {
            // preview click -> visualizza gruppo
            NavHostFragment.findNavController(this).navigate(R.id.viewGroupFragment);
        });

        btnDashFields.setChecked(true);
        showDashFields();

        toggleDash.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            if (checkedId == R.id.btnDashFields) {
                showDashFields();
            } else if (checkedId == R.id.btnDashGroups) {
                showDashGroups();
            }
        });


        List<String> activeAlerts = getSampleActiveAlerts(); // per ora finto
        renderActiveAlerts(activeAlerts);
    }

    private void renderActiveAlerts(List<String> activeAlerts) {
        int total = (activeAlerts == null) ? 0 : activeAlerts.size();

        txtAlertCount.setText(total + " Alert Attivi");
        layoutAlertPreview.removeAllViews();

        // Nessun alert
        if (total == 0) {
            txtNoActiveAlerts.setVisibility(View.VISIBLE);
            layoutAlertPreview.setVisibility(View.GONE);
            return;
        }

        // Ci sono alert
        txtNoActiveAlerts.setVisibility(View.GONE);
        layoutAlertPreview.setVisibility(View.VISIBLE);

        int maxPreview = Math.min(3, total);
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 0; i < maxPreview; i++) {
            View row = inflater.inflate(R.layout.item_alert_preview, layoutAlertPreview, false);

            ImageView img = row.findViewById(R.id.imgAlertIcon);
            TextView txt = row.findViewById(R.id.txtAlertText);

            String alertName = activeAlerts.get(i);

            txt.setText(alertName + " • Oggi");
            img.setImageResource(getAlertIcon(alertName));

            layoutAlertPreview.addView(row);
        }
    }

    private int getAlertIcon(String alertName) {
        if (alertName == null) return R.drawable.ic_alert;

        String a = alertName.toLowerCase();

        if (a.contains("caldo"))
            return R.drawable.ic_alert_calore;

        if (a.contains("gelo") || a.contains("brina"))
            return R.drawable.ic_alert_gelo;

        if (a.contains("vento"))
            return R.drawable.ic_alert_vento;

        if (a.contains("scarsa ventilazione"))
            return R.drawable.ic_alert_ventilazione;

        if (a.contains("pioggia"))
            return R.drawable.ic_alert_pioggia;

        if (a.contains("temporale"))
            return R.drawable.ic_alert_temporale;

        if (a.contains("siccità"))
            return R.drawable.ic_alert_siccita;

        if (a.contains("umidità"))
            return R.drawable.ic_alert_umidita;

        if (a.contains("rischio incendio"))
            return R.drawable.ic_alert_incendio;

        if (a.contains("escursione termica"))
            return R.drawable.ic_alert_escursione;

        // FALLBACK
        return R.drawable.ic_alert;
    }

    private void showDashFields() {
        rvDashboardPreview.setAdapter(fieldsAdapter);
        fieldsAdapter.submitList(getSampleFieldsPreview()); // 3 elementi
    }

    private void showDashGroups() {
        rvDashboardPreview.setAdapter(groupsAdapter);
        groupsAdapter.submitList(getSampleGroupsPreview()); // 3 elementi
    }


    private List<FieldUiModel> getSampleFieldsPreview() {
        List<FieldUiModel> list = new ArrayList<>();

        list.add(new FieldUiModel(
                1L,
                "Via Verdirdi, 15 - Mestre (VE)",
                "Ortaggi",
                "Gruppo A",
                R.drawable.ic_ortaggi,
                Arrays.asList(
                        R.drawable.ic_alert_vento,
                        R.drawable.ic_alert_calore,
                        R.drawable.ic_alert_ventilazione
                )
        ));

        list.add(new FieldUiModel(
                2L,
                "Via Giallo, 10 - Mestre (VE)",
                "Cereali",
                "Gruppo B",
                R.drawable.ic_cereali,
                Arrays.asList(
                        R.drawable.ic_alert_gelo,
                        R.drawable.ic_alert_pioggia
                )
        ));

        list.add(new FieldUiModel(
                3L,
                "Via Torino, 154 - Martellago (VE)",
                "Leguminose",
                "Gruppo Prova",
                R.drawable.ic_leguminose,
                Arrays.asList(
                        R.drawable.ic_alert_temporale
                )
        ));

        return list;
    }


    private List<GroupUiModel> getSampleGroupsPreview() {
        List<GroupUiModel> list = new ArrayList<>();

        list.add(new GroupUiModel(
                1L,
                "Gruppo A",
                "Descrizione",
                R.drawable.ic_group_default,
                Arrays.asList(R.drawable.ic_alert_vento, R.drawable.ic_alert_calore, R.drawable.ic_alert_ventilazione)
        ));

        list.add(new GroupUiModel(
                2L,
                "Gruppo B",
                "Descrizione",
                R.drawable.ic_group_default,
                Arrays.asList(R.drawable.ic_alert_gelo, R.drawable.ic_alert_pioggia)
        ));

        list.add(new GroupUiModel(
                3L,
                "Gruppo Prova",
                "Descrizione",
                R.drawable.ic_group_default,
                Arrays.asList(R.drawable.ic_alert_pioggia)
        ));

        return list;
    }


    private List<String> getSampleActiveAlerts() {
        List<String> list = new ArrayList<>();
        // per test "0 alert" lascia vuoto
        list.add("Caldo estremo");
        list.add("Gelo/Brina");
        list.add("Scarsa ventilazione");
        return list;
    }
}
