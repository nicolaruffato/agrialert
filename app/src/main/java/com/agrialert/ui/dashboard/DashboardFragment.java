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

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.Alert;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.ui.fields.FieldUiModel;
import com.agrialert.ui.fields.FieldsAdapter;
import com.agrialert.ui.fields.groups.GroupUiModel;
import com.agrialert.ui.fields.groups.GroupsAdapter;
import com.agrialert.viewmodel.AlertsViewModel;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class DashboardFragment extends Fragment implements FieldsAdapter.OnFieldClickListener, GroupsAdapter.OnGroupClickListener {

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
    private CompositeDisposable cd = new CompositeDisposable();
    FieldsViewModel vm;
    AlertsViewModel avm;


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
        fieldsAdapter = new FieldsAdapter(this);
        groupsAdapter = new GroupsAdapter(this);

        MainActivity a = (MainActivity) requireActivity();
        cd.add(a.isBound().subscribe(isReady -> {
            vm = a.fieldsVM();
            avm = a.alertsVM();

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

            renderActiveAlerts();
        }));
    }

    @Override
    public void onFieldClick(FieldUiModel field) {
        Bundle b = new Bundle();
        b.putParcelable("field", field);
        NavHostFragment.findNavController(this).navigate(R.id.viewFieldFragment, b);
    }

    @Override
    public void onGroupClick(GroupUiModel group) {
        Bundle b = new Bundle();
        b.putParcelable("group", group);
        NavHostFragment.findNavController(this).navigate(R.id.viewGroupFragment, b);
    }

    private void renderActiveAlerts() {
        cd.add(avm.getActiveAlerts().firstOrError().subscribe(alerts -> {
            int total = 0;
            int maxPreview = 3;
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            layoutAlertPreview.removeAllViews();

            for(Alert alert : alerts) {
                total++;
                if(total <= maxPreview) {
                    View row = inflater.inflate(R.layout.item_alert_preview, layoutAlertPreview, false);

                    ImageView img = row.findViewById(R.id.imgAlertIcon);
                    TextView txtTitle = row.findViewById(R.id.txtAlertTitle);
                    TextView txtTime = row.findViewById(R.id.txtAlertTime);

                    String alertName = alert.getTitle();
                    String whenLabel = formatForecastLabel(alert.getForecastAt(), alert.getDurationMs());
                    txtTitle.setText(alertName);
                    if (whenLabel.isEmpty()) {
                        txtTime.setVisibility(View.GONE);
                    } else {
                        txtTime.setVisibility(View.VISIBLE);
                        txtTime.setText(whenLabel);
                    }
                    img.setImageResource(alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert);

                    layoutAlertPreview.addView(row);
                }
            }

            txtAlertCount.setText(total + " Alert Attivi");

            if (total == 0) {
                // Nessun alert
                txtNoActiveAlerts.setVisibility(View.VISIBLE);
                layoutAlertPreview.setVisibility(View.GONE);
            } else {
                // Ci sono alert
                txtNoActiveAlerts.setVisibility(View.GONE);
                layoutAlertPreview.setVisibility(View.VISIBLE);
            }
        }));
    }

    private String formatForecastLabel(long startMs, long durationMs) {
        if (startMs <= 0L) {
            return "";
        }

        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startMs);
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Date startDate = start.getTime();

        String startDayLabel = isSameDay(start, today)
                ? "Oggi"
                : (isSameDay(start, tomorrow) ? "Domani" : dateFormat.format(startDate));
        String startLabel = startDayLabel + " " + timeFormat.format(startDate);

        if (durationMs <= 0L) {
            return startLabel;
        }

        long endMs = startMs + durationMs;
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(endMs);
        Date endDate = end.getTime();

        if (isSameDay(start, end)) {
            return startLabel + "\u2013" + timeFormat.format(endDate);
        }

        String endDayLabel = isSameDay(end, today)
                ? "Oggi"
                : (isSameDay(end, tomorrow) ? "Domani" : dateFormat.format(endDate));
        return startLabel + "\u2013" + endDayLabel + " " + timeFormat.format(endDate);
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private int getIconForType(int typeId) {
        switch (typeId) {
            case 1:
                return R.drawable.ic_alert_vento;

            case 2:
                return R.drawable.ic_alert_calore;

            case 3:
                return R.drawable.ic_alert_ventilazione;

            case 4:
                return R.drawable.ic_alert_gelo;

            case 5:
                return R.drawable.ic_alert_pioggia;

            case 6:
                return R.drawable.ic_alert_temporale;

            case 7:
                return R.drawable.ic_alert_siccita;

            case 8:
                return R.drawable.ic_alert_umidita;

            case 9:
                return R.drawable.ic_alert_escursione;

            case 10:
                return R.drawable.ic_alert_incendio;

            default:
                return R.drawable.ic_alert; // fallback
        }
    }

    private void showDashFields() {
        cd.add(vm.getAllGroups().firstOrError().subscribe(groups -> {
            List<FieldUiModel> uiFields = new ArrayList<>();
            int count = 0;
            for(GroupWithFields group : groups) {
                if (count >= 3) break;
                for (Field field : group.getFields()) {
                    if (count >= 3) break;
                    uiFields.add(new FieldUiModel(
                            field.getId(),
                            field.getAddress(),
                            getContext().getString(field.getCropType().getResourceId()),
                            field.getGroupName(),
                            field.getCropType().getImageResId(),
                            Collections.emptyList())
                    );
                    count++;
                }
            }

            rvDashboardPreview.setAdapter(fieldsAdapter);
            fieldsAdapter.submitList(uiFields);
        }));
    }

    private void showDashGroups() {
        cd.add(vm.getAllGroups().firstOrError().subscribe(groups -> {
            List<GroupUiModel> uiGroups = new ArrayList<>();
            int count = 0;
            for(GroupWithFields group : groups) {
                if (count >= 3) break;
                uiGroups.add(new GroupUiModel(
                        0,
                        group.getGroup().getName(),
                        group.getGroup().getDescription(),
                        R.drawable.ic_group_default,
                        Collections.emptyList()
                ));
                count++;
            }
            rvDashboardPreview.setAdapter(groupsAdapter);
            groupsAdapter.submitList(uiGroups);
        }));
    }

    private List<String> getSampleActiveAlerts() {
        List<String> list = new ArrayList<>();
        // per test "0 alert" lascia vuoto
        list.add("Caldo estremo");
        list.add("Gelo/Brina");
        list.add("Scarsa ventilazione");
        return list;
    }

    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
