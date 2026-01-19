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
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Fragment that displays a high-level summary of the application status.
 * It shows active alerts, a preview of fields and groups, and provides quick
 * navigation to more detailed views.
 */
public class DashboardFragment extends Fragment implements FieldsAdapter.OnFieldClickListener, GroupsAdapter.OnGroupClickListener {

    /**
     * Initializes the fragment with the dashboard layout.
     */
    public DashboardFragment() {
        super(R.layout.fragment_dashboard);
    }

    /** View displaying the number of active alerts. */
    private TextView txtAlertCount;
    /** View shown when there are no active alerts. */
    private TextView txtNoActiveAlerts;
    /** View used as a shortcut to the full alerts list. */
    private TextView txtSeeAllAlerts;
    /** Container for a vertical list of alert previews. */
    private LinearLayout layoutAlertPreview;
    /** Toggle group to switch between field and group previews. */
    private MaterialButtonToggleGroup toggleDash;
    /** Button within the toggle group for fields. */
    private MaterialButton btnDashFields;
    /** Button within the toggle group for groups. */
    private MaterialButton btnDashGroups;
    /** RecyclerView displaying a preview of fields or groups. */
    private RecyclerView rvDashboardPreview;

    /** Adapter for displaying a limited set of fields. */
    private FieldsAdapter fieldsAdapter;
    /** Adapter for displaying a limited set of groups. */
    private GroupsAdapter groupsAdapter;
    /** Container for RxJava disposables. */
    private final CompositeDisposable cd = new CompositeDisposable();
    /** ViewModel for field-related data. */
    private FieldsViewModel vm;
    /** ViewModel for alert-related data. */
    private AlertsViewModel avm;

    /**
     * Initializes views, adapters, and data observers after the view is created.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Saved state if being reconstructed.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtAlertCount = view.findViewById(R.id.txtAlertCount);
        txtNoActiveAlerts = view.findViewById(R.id.txtNoActiveAlerts);
        txtSeeAllAlerts = view.findViewById(R.id.txtSeeAllAlerts);
        layoutAlertPreview = view.findViewById(R.id.layoutAlertPreview);

        // Navigation to Alerts List
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

        // Reuse existing adapters for fields and groups
        fieldsAdapter = new FieldsAdapter(this);
        groupsAdapter = new GroupsAdapter(this);

        MainActivity a = (MainActivity) requireActivity();
        cd.add(a.isBound().subscribe(isReady -> {
            vm = a.fieldsVM();
            avm = a.alertsVM();

            // Default selection: Fields
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

    /**
     * Navigates to the field details screen when a field is clicked.
     *
     * @param field The field model that was clicked.
     */
    @Override
    public void onFieldClick(FieldUiModel field) {
        Bundle b = new Bundle();
        b.putParcelable("field", field);
        NavHostFragment.findNavController(this).navigate(R.id.viewFieldFragment, b);
    }

    /**
     * Navigates to the group details screen when a group is clicked.
     *
     * @param group The group model that was clicked.
     */
    @Override
    public void onGroupClick(GroupUiModel group) {
        Bundle b = new Bundle();
        b.putParcelable("group", group);
        NavHostFragment.findNavController(this).navigate(R.id.viewGroupFragment, b);
    }

    /**
     * Fetches and renders the top active alerts in the preview area.
     */
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
                    TextView txt = row.findViewById(R.id.txtAlertText);

                    String alertName = alert.getTitle();
                    String[] descAndTime = alert.getDescription().split(" - ");
                    String[] time = descAndTime[1].split(" ");

                    // Format: "Alert Title on Date/Time"
                    txt.setText(alertName + " on " + time[2]);
                    img.setImageResource(getIconForType(alert.getTypeId()));

                    layoutAlertPreview.addView(row);
                }
            }

            txtAlertCount.setText(total + " Active Alerts");

            if (total == 0) {
                txtNoActiveAlerts.setVisibility(View.VISIBLE);
                layoutAlertPreview.setVisibility(View.GONE);
            } else {
                txtNoActiveAlerts.setVisibility(View.GONE);
                layoutAlertPreview.setVisibility(View.VISIBLE);
            }
        }));
    }

    /**
     * Maps an alert type ID to its corresponding drawable resource.
     *
     * @param typeId The ID representing the alert type.
     * @return The resource ID of the icon.
     */
    private int getIconForType(int typeId) {
        switch (typeId) {
            case 1: return R.drawable.ic_alert_vento;
            case 2: return R.drawable.ic_alert_calore;
            case 3: return R.drawable.ic_alert_ventilazione;
            case 4: return R.drawable.ic_alert_gelo;
            case 5: return R.drawable.ic_alert_pioggia;
            case 6: return R.drawable.ic_alert_temporale;
            case 7: return R.drawable.ic_alert_siccita;
            case 8: return R.drawable.ic_alert_umidita;
            case 9: return R.drawable.ic_alert_escursione;
            case 10: return R.drawable.ic_alert_incendio;
            default: return R.drawable.ic_alert;
        }
    }

    /**
     * Shows a limited preview of fields in the dashboard RecyclerView.
     */
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

    /**
     * Shows a limited preview of groups in the dashboard RecyclerView.
     */
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

    /**
     * Clears RxJava disposables when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
