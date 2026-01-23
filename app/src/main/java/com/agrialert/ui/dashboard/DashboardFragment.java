package com.agrialert.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import io.reactivex.rxjava3.core.Observable;
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
    /** Container for a vertical list of alert previews. */
    private LinearLayout layoutAlertPreview;
    /** Toggle group to switch between field and group previews. */
    private MaterialButtonToggleGroup toggleDash;
    /** Button within the toggle group for fields. */
    private MaterialButton btnDashFields;
    /** RecyclerView displaying a preview of fields or groups. */
    private RecyclerView rvDashboardPreview;
    /** Image to display when there's no fields */
    private ImageView noFieldsImage;
    /** Text to display when there's no fields */
    private TextView noFieldsText;

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

    /** Maximum number of groups or fields to display in the dashboard. */
    private final int MAX_PREVIEW = 3;
    List<GroupUiModel> uiGroups = new ArrayList<>();
    List<FieldUiModel> uiFields = new ArrayList<>();

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
        layoutAlertPreview = view.findViewById(R.id.layoutAlertPreview);
        TextView txtSeeAllAlerts = view.findViewById(R.id.txtSeeAllAlerts);
        noFieldsImage = view.findViewById(R.id.noFieldsImage);
        noFieldsText = view.findViewById(R.id.noFieldsText);


        // Navigation to Alerts List
        txtSeeAllAlerts.setOnClickListener(v -> {
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_nav);
            bottomNav.setSelectedItemId(R.id.alertsListFragment);
        });

        toggleDash = view.findViewById(R.id.toggleFieldsGroupsDash);
        btnDashFields = view.findViewById(R.id.btnDashFields);
        rvDashboardPreview = view.findViewById(R.id.rvDashboardPreview);
        rvDashboardPreview.setLayoutManager(new LinearLayoutManager(requireContext()));
        btnDashFields.setChecked(true);

        // Reuse existing adapters for fields and groups
        fieldsAdapter = new FieldsAdapter(this);
        groupsAdapter = new GroupsAdapter(this);

        MainActivity a = (MainActivity) requireActivity();
        cd.add(a.isBound().subscribe(isReady -> {
            vm = a.fieldsVM();
            avm = a.alertsVM();

            view.post(() -> {
                if (btnDashFields.isChecked()) {
                    showDashFields();
                } else {
                    showDashGroups();
                }
            });


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
        cd.add(avm.getActiveAlerts().subscribe(alerts -> {
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
                txtNoActiveAlerts.setVisibility(View.VISIBLE);
                layoutAlertPreview.setVisibility(View.GONE);
            } else {
                txtNoActiveAlerts.setVisibility(View.GONE);
                layoutAlertPreview.setVisibility(View.VISIBLE);
            }
        }));
    }

    /**
     * Formats a forecast time interval into a human-readable string.
     * <p>
     * The label uses relative terms like "Oggi" (Today) or "Domani" (Tomorrow) for the current
     * and following day, otherwise it uses the "dd/MM" format. It includes the start time
     * and, if a duration is provided, the end time.
     * </p>
     *
     * @param startMs    The start time of the forecast in milliseconds.
     * @param durationMs The duration of the forecast in milliseconds. If <= 0, only the start time is shown.
     * @return A formatted string representing the time range (e.g., "Oggi 14:30–16:00" or "12/05 22:00–Domani 02:00"),
     *         or an empty string if the start time is invalid.
     */
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
            return startLabel + "–" + timeFormat.format(endDate);
        }

        String endDayLabel = isSameDay(end, today)
                ? "Oggi"
                : (isSameDay(end, tomorrow) ? "Domani" : dateFormat.format(endDate));
        return startLabel + "–" + endDayLabel + " " + timeFormat.format(endDate);
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }


    /**
     * Shows a limited preview of fields in the dashboard RecyclerView.
     */
    private void showDashFields() {
        cd.add(io.reactivex.rxjava3.core.Observable.combineLatest(
            vm.getAllGroups().firstOrError().toObservable(),
            avm.getActiveAlerts().toObservable(),
            (groups, alerts) -> {
            // This function executes everytime that alerts change
            List<FieldUiModel> uiFields = new ArrayList<>();

            for (GroupWithFields group : groups) {
                for (Field field : group.getFields()) {
                    if (uiFields.size() == MAX_PREVIEW) break;
                    List<Integer> icons = new ArrayList<>();
                    for (Alert alert : alerts) {
                        if (icons.size() == 5) {
                            break;
                        }
                        if (alert.getFieldId() == field.getId()) {
                             icons.add(alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert);
                        }
                    }

                    uiFields.add(new FieldUiModel(
                            field.getId(),
                            field.getAddress(),
                            requireContext().getString(field.getCropType().getResourceId()),
                            field.getGroupName(),
                            field.getCropType().getImageResId(),
                            icons
                            )
                    );
                }
            }
            return uiFields;
            })
            .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
            .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
            .subscribe(uiFields -> {
                if (uiFields.isEmpty()) {
                    noFieldsImage.setVisibility(View.VISIBLE);
                    noFieldsText.setVisibility(View.VISIBLE);
                    rvDashboardPreview.setVisibility(View.GONE);
                } else {
                    noFieldsImage.setVisibility(View.GONE);
                    noFieldsText.setVisibility(View.GONE);
                    rvDashboardPreview.setVisibility(View.VISIBLE);
                }
                rvDashboardPreview.setAdapter(fieldsAdapter);
                fieldsAdapter.submitList(uiFields);
            }, throwable -> {
                Log.e("DashboardFragment", "Error rendering fields");
            })
        );
    }

    /**
     * Shows a limited preview of groups in the dashboard RecyclerView.
     */
    private void showDashGroups() {
        noFieldsImage.setVisibility(View.GONE);
        noFieldsText.setVisibility(View.GONE);
        rvDashboardPreview.setVisibility(View.VISIBLE);

        cd.add(io.reactivex.rxjava3.core.Observable.combineLatest(
                        vm.getAllGroups().firstOrError().toObservable(),
                        avm.getActiveAlerts().toObservable(),
                        (groups, alerts) -> {
                            // This function executes everytime that alerts change
                            List<GroupUiModel> uiGroups = new ArrayList<>();
                            for (GroupWithFields group : groups) {
                                if (uiGroups.size() == MAX_PREVIEW) break;
                                List<Integer> icons = new ArrayList<>();
                                for (Field field : group.getFields()) {
                                    for (Alert alert : alerts) {
                                        if (icons.size() == 5) {
                                            break;
                                        }
                                        if (alert.getFieldId() == field.getId()) {
                                            icons.add(alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert);
                                        }
                                    }
                                }
                                uiGroups.add(new GroupUiModel(
                                                0,
                                                group.getGroup().getName(),
                                                group.getGroup().getDescription(),
                                                R.drawable.ic_group_default,
                                                icons
                                        )
                                );
                            }
                            return uiGroups;
                        })
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(uiGroups -> {
                    rvDashboardPreview.setAdapter(groupsAdapter);
                    groupsAdapter.submitList(uiGroups);
                }, throwable -> {
                    Log.e("DashboardFragment", "Error rendering groups");
                })
        );
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
