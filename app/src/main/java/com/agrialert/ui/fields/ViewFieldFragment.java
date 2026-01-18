package com.agrialert.ui.fields;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.alert_manager.repo.AlertRepository;
import com.agrialert.data_manager.Alert;
import com.agrialert.ui.alerts.AlertUiModel;
import com.agrialert.ui.alerts.AlertsAdapter;
import com.agrialert.viewmodel.AlertsViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ViewFieldFragment extends Fragment {
    private static final String TAG = "ViewField";
    private ImageView imgCrop;
    private TextView txtAddress;
    private TextView txtCrop;
    private TextView txtGroup;
    private RecyclerView rvFieldAlerts;

    private MaterialButton btnEditField;
    private MaterialButton btnDeleteField;
    private AlertsAdapter adapter;
    private AlertsViewModel avm;
    private CompositeDisposable cd = new CompositeDisposable();

    public ViewFieldFragment() {
        // costruttore vuoto richiesto dal Fragment
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_field, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // BIND VIEW
        imgCrop = view.findViewById(R.id.imgCrop);
        txtAddress = view.findViewById(R.id.txtAddress);
        txtCrop = view.findViewById(R.id.txtCrop);
        txtGroup = view.findViewById(R.id.txtGroup);
        rvFieldAlerts = view.findViewById(R.id.rvFieldAlerts);
        btnEditField = view.findViewById(R.id.btnEditField);
        btnDeleteField = view.findViewById(R.id.btnDeleteField);

        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "field mancante: passalo come arg a ViewFieldFragment!");
            return;
        }
        FieldUiModel field = args.getParcelable("field");

        Bundle b = new Bundle();
        b.putInt("fieldId", (int)field.id);

        //LISTENER
        btnEditField.setOnClickListener(v ->
                NavHostFragment.findNavController(ViewFieldFragment.this)
                        .navigate(R.id.action_viewFieldFragment_to_editFieldFragment, b)); // Try to use args

        btnDeleteField.setOnClickListener(v ->
                NavHostFragment.findNavController(ViewFieldFragment.this)
                        .navigate(R.id.action_viewFieldFragment_to_confirmDeleteFieldFragment, b));
        ;
        txtAddress.setText(field.address);
        txtCrop.setText(field.crop);
        txtGroup.setText(field.groupName);
        imgCrop.setImageResource(field.iconRes);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        avm = a.alertsVM();

        // LISTA ALERT COLLEGATI AL CAMPO
        rvFieldAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AlertsAdapter((alert, isResolved) -> {
            // aggiorno il model
            alert.isResolved = isResolved;
            avm.setAlertResolved(alert.id);
        });
        rvFieldAlerts.setAdapter(adapter);

        getAlerts(field);
    }

    private void getAlerts(FieldUiModel field) {
        cd.add(avm.getActiveAlertsFromField((int)field.id).subscribe(alerts -> {
            List<AlertUiModel>  uiAlerts = new ArrayList<>();
            for(Alert alert : alerts) {
                String groupOrField;
                if(alert.getFieldId() == 0) {
                    groupOrField = "Gruppo: " + alert.getGroupName();
                } else {
                    groupOrField = "Campo: " + alert.getFieldAddress();
                }
                uiAlerts.add(new AlertUiModel(
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

            adapter.submitList(uiAlerts);
        }));
    };

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
