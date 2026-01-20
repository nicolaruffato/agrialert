package com.agrialert.ui.alerts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

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
    /** Image to display when there's no alerts visible */
    private ImageView noAlertsImage;
    /** Text to display when there's no alerts visible */
    private TextView noAlertsText;

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
        noAlertsImage = view.findViewById(R.id.noAlertsImage);
        noAlertsText = view.findViewById(R.id.noAlertsText);

        // Configure RecyclerView
        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        avm = a.alertsVM();

        // Initialize adapter with a listener for resolution state changes
        adapter = new AlertsAdapter((alert, isResolved) -> {
            // Update the model and refresh the list based on the current toggle state
            if (btnAlertsActive.isChecked()) {
                cd.add(avm.setAlertResolved(alert.id).subscribe());
            } else {
                cd.add(avm.setAlertActive(alert.id).subscribe());
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

        cd.add(avm.getActiveAlerts().subscribe(alerts -> {
            if(btnAlertsActive.isChecked()) {
                List<AlertUiModel> active = new ArrayList<>();
                for(Alert alert : alerts) {
                    String groupOrField;
                    if(alert.getFieldId() == 0) {
                        groupOrField = "Gruppo: " + alert.getGroupName();
                    } else {
                        groupOrField = "Campo: " + alert.getFieldAddress();
                    }
                    active.add(new AlertUiModel(
                            alert.getId(),
                            String.valueOf(alert.getTypeId()),
                            alert.getTitle(),
                            formatDescriptionForList(alert.getDescription(), alert.getDurationMs()),
                            groupOrField,
                            formatForecastLabel(alert.getForecastAt(), alert.getDurationMs()),
                            alert.isResolved(),
                            alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert
                    ));
                }
                if(active.isEmpty()) {
                   noAlertsText.setText(R.string.no_active_alerts);
                   noAlertsImage.setVisibility(View.VISIBLE);
                   noAlertsText.setVisibility(View.VISIBLE);
                   rvAlerts.setVisibility(View.GONE);
                } else {
                    noAlertsImage.setVisibility(View.GONE);
                    noAlertsText.setVisibility(View.GONE);
                    rvAlerts.setVisibility(View.VISIBLE);
                }
                adapter.submitList(active);
            }
        }));
    }

    /**
     * Updates the UI to show only resolved alerts.
     * Subscribes to the ViewModel to get resolved alerts and updates the adapter.
     */
    private void showResolvedAlerts() {
        btnAlertsActive.setChecked(false);
        btnAlertsResolved.setChecked(true);

        cd.add(avm.getResolvedAlerts().subscribe(alerts -> {
            if(btnAlertsResolved.isChecked()) {
                List<AlertUiModel> resolved = new ArrayList<>();
                for (Alert alert : alerts) {
                    String groupOrField;
                    if (alert.getFieldId() == 0) {
                        groupOrField = "Gruppo: " + alert.getGroupName();
                    } else {
                        groupOrField = "Campo: " + alert.getFieldAddress();
                    }
                    resolved.add(new AlertUiModel(
                            alert.getId(),
                            String.valueOf(alert.getTypeId()),
                            alert.getTitle(),
                            formatDescriptionForList(alert.getDescription(), alert.getDurationMs()),
                            groupOrField,
                            formatForecastLabel(alert.getForecastAt(), alert.getDurationMs()),
                            alert.isResolved(),
                            alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert
                    ));
                }
                if (resolved.isEmpty()) {
                    noAlertsText.setText(R.string.no_resolved_alerts);
                    noAlertsImage.setVisibility(View.VISIBLE);
                    noAlertsText.setVisibility(View.VISIBLE);
                    rvAlerts.setVisibility(View.GONE);
                } else {
                    noAlertsImage.setVisibility(View.GONE);
                    noAlertsText.setVisibility(View.GONE);
                    rvAlerts.setVisibility(View.VISIBLE);
                }
                adapter.submitList(resolved);
            }
        }));
    }

    /**
     * Gets alert duration from description.
     *
     * @param description description of the alert
     * @param durationMs duration in milliseconds.
     * @return A formatted time string.
     */
    private String formatDescriptionForList(String description, long durationMs) {
        String fallbackCondition = "Condizione meteo rilevata";
        String fallbackDuration = "Durata stimata: " + formatDuration(durationMs);

        if (description == null || description.trim().isEmpty()) {
            return fallbackCondition + "\n" + fallbackDuration;
        }

        String normalized = description.trim()
                .replace(" • ", "\n")
                .replace(" | ", "\n")
                .replace('•', '\n');

        String condition = "";
        String durationLine = "";
        for (String raw : normalized.split("\n")) {
            if (raw == null) {
                continue;
            }
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (condition.isEmpty()) {
                condition = line;
            }
            if (durationLine.isEmpty() && line.toLowerCase(Locale.ROOT).contains("durata")) {
                durationLine = line;
            }
        }

        if (condition.isEmpty()) {
            condition = fallbackCondition;
        }
        if (durationLine.isEmpty()) {
            durationLine = fallbackDuration;
        }

        return condition + "\n" + durationLine;
    }

    /**
     * Formats the duration into a string.
     *
     * @param durationMs duration in milliseconds.
     * @return A formatted time string.
     */
    private String formatDuration(long durationMs) {
        if (durationMs <= 0L) {
            return "n/d";
        }
        long hourMs = 3_600_000L;
        long hours = Math.max(1L, durationMs / hourMs);
        if (hours < 24L) {
            return hours + "h";
        }
        long days = hours / 24L;
        long remHours = hours % 24L;
        if (remHours == 0L) {
            return days + "g";
        }
        return days + "g " + remHours + "h";
    }

    /**
     * Formats the forecast label into a string.
     *
     * @param startMs start of alert event in milliseconds
     * @param durationMs duration in milliseconds.
     * @return A formatted time string.
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

        String endDayLabel = isSameDay(end, today)
                ? "Oggi"
                : (isSameDay(end, tomorrow) ? "Domani" : dateFormat.format(endDate));
        String endLabel = endDayLabel + " " + timeFormat.format(endDate);

        return startLabel + "\n" + endLabel;
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }
}
