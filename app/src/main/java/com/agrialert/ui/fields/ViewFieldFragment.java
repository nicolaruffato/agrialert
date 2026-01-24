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
import com.agrialert.data_manager.Alert;
import com.agrialert.ui.alerts.AlertUiModel;
import com.agrialert.ui.alerts.AlertsAdapter;
import com.agrialert.viewmodel.AlertsViewModel;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Fragment that displays detailed information about a specific agricultural field.
 * It shows the field's address, crop type, associated group, and a list of active alerts.
 * Users can navigate to edit or delete the field from here.
 */
public class ViewFieldFragment extends Fragment {
    /** Tag for logging. */
    private static final String TAG = "ViewField";

    /** RecyclerView displaying the list of active alerts for this field. */
    private RecyclerView rvFieldAlerts;
    /** Image to display when there's no active alerts from the field. */
    private ImageView noAlertsListImage;
    /** Text to display when there's no active alerts from the field. */
    private TextView noAlertsListText;

    /** Adapter for the alerts RecyclerView. */
    private AlertsAdapter adapter;
    /** ViewModel for accessing alert data. */
    private AlertsViewModel avm;
    /** Container for managing RxJava disposables. */
    private final CompositeDisposable cd = new CompositeDisposable();

    /**
     * Required empty public constructor for Fragment instantiation.
     */
    public ViewFieldFragment() {
        // Required empty constructor
    }

    /**
     * Inflates the fragment layout.
     *
     * @param inflater           The LayoutInflater object.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Fragment's previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_field, container, false);
    }

    /**
     * Initializes UI components, listeners, and loads alert data for the field.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Fragment's previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        ImageView imgCrop = view.findViewById(R.id.imgCrop);
        TextView txtAddress = view.findViewById(R.id.txtAddress);
        TextView txtCrop = view.findViewById(R.id.txtCrop);
        TextView txtGroup = view.findViewById(R.id.txtGroup);
        rvFieldAlerts = view.findViewById(R.id.rvFieldAlerts);
        MaterialButton btnEditField = view.findViewById(R.id.btnEditField);
        MaterialButton btnDeleteField = view.findViewById(R.id.btnDeleteField);
        noAlertsListImage = view.findViewById(R.id.noAlertsListImage);
        noAlertsListText = view.findViewById(R.id.noAlertsListText);

        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "Missing field: pass it as an argument to ViewFieldFragment!");
            return;
        }
        FieldUiModel field = args.getParcelable("field");
        assert field != null;

        Bundle b = new Bundle();
        b.putInt("fieldId", (int)field.id);

        // Set up listeners
        btnEditField.setOnClickListener(v ->
                NavHostFragment.findNavController(ViewFieldFragment.this)
                        .navigate(R.id.action_viewFieldFragment_to_editFieldFragment, b));

        btnDeleteField.setOnClickListener(v ->
                NavHostFragment.findNavController(ViewFieldFragment.this)
                        .navigate(R.id.action_viewFieldFragment_to_confirmDeleteFieldFragment, b));

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        FieldsViewModel vm = a.fieldsVM();
        avm = a.alertsVM();

        cd.add(vm.getFieldById((int)field.id).subscribe(f -> {
            txtAddress.setText(f.getAddress());
            txtCrop.setText(requireContext().getString(f.getCropType().getResourceId()));
            txtGroup.setText(f.getGroupName());
            imgCrop.setImageResource(f.getCropType().getImageResId());
        }));

        // LIST OF ALERTS ASSOCIATED WITH THE FIELD
        rvFieldAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AlertsAdapter((alert, isResolved) -> {
            // Update the model
            alert.isResolved = isResolved;
            cd.add(avm.setAlertResolved(alert.id).subscribe());
        });
        rvFieldAlerts.setAdapter(adapter);

        getAlerts(field);
    }

    /**
     * Fetches active alerts for the given field and updates the RecyclerView adapter.
     *
     * @param field The field UI model to fetch alerts for.
     */
    private void getAlerts(FieldUiModel field) {
        cd.add(avm.getActiveAlertsFromField((int)field.id).subscribe(alerts -> {
            List<AlertUiModel> uiAlerts = new ArrayList<>();
            for(Alert alert : alerts) {
                uiAlerts.add(new AlertUiModel(
                        alert.getId(),
                        String.valueOf(alert.getTypeId()),
                        alert.getTitle(),
                        formatDescriptionForList(alert.getDescription(), alert.getDurationMs()),
                        "",
                        formatForecastLabel(alert.getForecastAt(), alert.getDurationMs()),
                        alert.isResolved(),
                        alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert
                ));
            }

            if (uiAlerts.isEmpty()) {
                rvFieldAlerts.setVisibility(View.GONE);
                noAlertsListImage.setVisibility(View.VISIBLE);
                noAlertsListText.setVisibility(View.VISIBLE);
            } else {
                noAlertsListImage.setVisibility(View.GONE);
                noAlertsListText.setVisibility(View.GONE);
                rvFieldAlerts.setVisibility(View.VISIBLE);
            }
            adapter.submitList(uiAlerts);
        }));
    }

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

    /**
     * Clears RxJava disposables when the fragment's view is destroyed.
     */
    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
