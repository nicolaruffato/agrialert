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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AlertsListFragment extends Fragment {

    private RecyclerView rvAlerts;
    private MaterialButton btnAlertsActive;
    private MaterialButton btnAlertsResolved;

    private AlertsAdapter adapter;
    AlertsViewModel avm;
    private final CompositeDisposable cd = new CompositeDisposable();

    public AlertsListFragment() {
        // costruttore vuoto richiesto
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alerts_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlerts = view.findViewById(R.id.rvAlerts);
        btnAlertsActive = view.findViewById(R.id.btnAlertsActive);
        btnAlertsResolved = view.findViewById(R.id.btnAlertsResolved);

        // RecyclerView
        rvAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        avm = a.alertsVM();

        adapter = new AlertsAdapter((alert, isResolved) -> {
            Log.e("AlertsListFragment", "onResolvedChanged:");

            // Aggiorno il model
            // Ricalcolo la lista da mostrare in base al toggle

            if (btnAlertsActive.isChecked()) {
                cd.add(avm.setAlertResolved(alert.id).subscribe(this::showActiveAlerts));
            } else {
                cd.add(avm.setAlertActive(alert.id).subscribe(this::showResolvedAlerts));
            }
        });

        rvAlerts.setAdapter(adapter);

        // default: tab "Attivi"
        showActiveAlerts();

        // toggle
        btnAlertsActive.setOnClickListener(v -> showActiveAlerts());
        btnAlertsResolved.setOnClickListener(v -> showResolvedAlerts());
    }

    // ------------------- FILTRI -------------------

    private void showActiveAlerts() {
        btnAlertsActive.setChecked(true);
        btnAlertsResolved.setChecked(false);

        cd.add(avm.getActiveAlerts().firstOrError().subscribe(alerts -> {
            List<AlertUiModel> active = new ArrayList<>();
            for(Alert alert : alerts) {
                String groupOrField = "";
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
            adapter.submitList(active);
        }));
    }

    private void showResolvedAlerts() {
        btnAlertsActive.setChecked(false);
        btnAlertsResolved.setChecked(true);

        cd.add(avm.getResolvedAlerts().firstOrError().subscribe(alerts -> {
            List<AlertUiModel> resolved = new ArrayList<>();
            for(Alert alert : alerts) {
                String groupOrField = "";
                if(alert.getFieldId() == 0) {
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
            adapter.submitList(resolved);
        }));
    }

    private String fromMsToTime(long ms) {
        Log.e("AlertsListFragment", "fromMsToTime:" + ms);
        long seconds = (ms / 1000) % 60;
        long minutes = (ms / (1000 * 60)) % 60;
        long hours = (ms / (1000 * 60 * 60)) % 24;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    // ------------------- MAPPING TIPO → ICONA -------------------

    /**
     * Restituisce l'icona corretta per la tipologia di alert.
     * ATTENZIONE: i nomi dei drawable devono esistere in res/drawable.
     * Esempio:
     *  - ic_alert_vento.xml
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

    private String formatDescriptionForList(String description, long durationMs) {
        String fallbackCondition = "Condizione meteo rilevata";
        String fallbackDuration = "Durata stimata: " + formatDuration(durationMs);

        if (description == null || description.trim().isEmpty()) {
            return fallbackCondition + "\n" + fallbackDuration;
        }

        String normalized = description.trim()
                .replace(" \u2022 ", "\n")
                .replace(" | ", "\n")
                .replace('\u2022', '\n');

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
