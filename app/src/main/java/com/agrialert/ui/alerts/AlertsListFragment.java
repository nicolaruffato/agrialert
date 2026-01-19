package com.agrialert.ui.alerts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.Alert;
import com.agrialert.viewmodel.AlertsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Fragment that displays a list of alerts, categorized into "Active" and "Resolved".
 * It allows users to view alert details and toggle their resolution status.
 */
public class AlertsListFragment extends Fragment {

    /** RecyclerView to display the list of alerts. */
    private RecyclerView rvAlerts;
    /** Button to switch to the active alerts tab. */
    private MaterialButton btnAlertsActive;
    /** Button to switch to the resolved alerts tab. */
    private MaterialButton btnAlertsResolved;

    /** Adapter for managing the alert items in the RecyclerView. */
    private AlertsAdapter adapter;
    /** ViewModel for accessing alert data and business logic. */
    private AlertsViewModel avm;
    /** Container for RxJava disposables to handle cleanup. */
    private final CompositeDisposable cd = new CompositeDisposable();

    /**
     * Default empty constructor required for fragment instantiation.
     */
    public AlertsListFragment() {
        // Required empty constructor
    }

    /**
     * Inflates the fragment layout.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts_list, container, false);
    }

    /**
     * Initializes views and data after the fragment's view has been created.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlerts = view.findViewById(R.id.rvAlerts);
        btnAlertsActive = view.findViewById(R.id.btnFields);
        btnAlertsResolved = view.findViewById(R.id.btnFieldGroups);

        // Configure RecyclerView
        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        avm = a.alertsVM();

        // Initialize adapter with a listener for resolution state changes
        adapter = new AlertsAdapter((alert, isResolved) -> {
            // Update the model and refresh the list based on the current toggle state
            if (btnAlertsActive.isChecked()) {
                cd.add(avm.setAlertResolved(alert.id).subscribe(this::showActiveAlerts));
            } else {
                cd.add(avm.setAlertActive(alert.id).subscribe(this::showResolvedAlerts));
            }
        });

        rvAlerts.setAdapter(adapter);

        // Default to active alerts tab
        showActiveAlerts();

        // Set up tab toggling
        btnAlertsActive.setOnClickListener(v -> showActiveAlerts());
        btnAlertsResolved.setOnClickListener(v -> showResolvedAlerts());
    }

    /**
     * Clears RxJava disposables when the fragment's view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cd.clear();
    }

    // ------------------- FILTERS -------------------

    /**
     * Updates the UI to show only active alerts.
     * Subscribes to the ViewModel to get active alerts and updates the adapter.
     */
    private void showActiveAlerts() {
        btnAlertsActive.setChecked(true);
        btnAlertsResolved.setChecked(false);

        cd.add(avm.getActiveAlerts().firstOrError().subscribe(alerts -> {
            List<AlertUiModel> active = new ArrayList<>();
            for(Alert alert : alerts) {
                String groupOrField = "";
                if(alert.getFieldId() == 0) {
                    groupOrField = "Group: " + alert.getGroupName();
                } else {
                    groupOrField = "Field: " + alert.getFieldAddress();
                }
                String[] descAndForecast = alert.getDescription().split(" - ");
                active.add(new AlertUiModel(
                        alert.getId(),
                        String.valueOf(alert.getTypeId()),
                        alert.getTitle(),
                        descAndForecast[0],
                        groupOrField,
                        descAndForecast[1],
                        alert.isResolved(),
                        getIconForType(alert.getTypeId())
                ));
            }
            adapter.submitList(active);
        }));
    }

    /**
     * Updates the UI to show only resolved alerts.
     * Subscribes to the ViewModel to get resolved alerts and updates the adapter.
     */
    private void showResolvedAlerts() {
        btnAlertsActive.setChecked(false);
        btnAlertsResolved.setChecked(true);

        cd.add(avm.getResolvedAlerts().firstOrError().subscribe(alerts -> {
            List<AlertUiModel> resolved = new ArrayList<>();
            for(Alert alert : alerts) {
                String groupOrField = "";
                if(alert.getFieldId() == 0) {
                    groupOrField = "Group: " + alert.getGroupName();
                } else {
                    groupOrField = "Field: " + alert.getFieldAddress();
                }
                String[] descAndForecast = alert.getDescription().split(" - ");
                resolved.add(new AlertUiModel(
                        alert.getId(),
                        String.valueOf(alert.getTypeId()),
                        alert.getTitle(),
                        descAndForecast[0],
                        groupOrField,
                        descAndForecast[1],
                        alert.isResolved(),
                        getIconForType(alert.getTypeId())
                ));
            }
            adapter.submitList(resolved);
        }));
    }

    /**
     * Converts a duration in milliseconds to a formatted time string (HH:mm:ss).
     *
     * @param ms The duration in milliseconds.
     * @return A formatted time string.
     */
    private String fromMsToTime(long ms) {
        Log.e("AlertsListFragment", "fromMsToTime:" + ms);
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000 * 60)) % 60;
        long hours = (ms / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // ------------------- TYPE → ICON MAPPING -------------------

    /**
     * Returns the appropriate drawable resource ID for a given alert type.
     *
     * @param typeId The ID of the alert type.
     * @return The resource ID of the icon.
     */
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
}
