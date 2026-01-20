package com.agrialert.ui.alerts;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.alert_manager.AlertManagerInitializer;
import com.agrialert.data_manager.AlertType;
import com.agrialert.data_manager.AlertWithThreshold;
import com.agrialert.data_manager.Threshold;
import com.agrialert.viewmodel.AlertsViewModel;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import kotlin.Pair;

/**
 * Fragment for configuring alert settings for a specific agricultural field.
 * Allows users to enable/disable different alert types and set custom threshold values.
 */
public class SetAlertsFragment extends Fragment {

    /** Tag for logging. */
    private static final String TAG = "SetAlerts";

    /** Container for managing RxJava subscriptions. */
    private final CompositeDisposable cd = new CompositeDisposable();

    /**
     * Stable list of UI models representing alert settings.
     * This list is populated from the database and used by the adapter.
     */
    private final List<AlertSettingUiModel> items = new ArrayList<>();

    /** Adapter for the alert settings RecyclerView. */
    private AlertSettingsAdapter adapter;

    /** The ID of the field for which alerts are being configured. */
    private int fieldId = -1;

    /**
     * Initializes the fragment with the layout resource.
     */
    public SetAlertsFragment() {
        super(R.layout.fragment_set_alerts);
    }

    /**
     * Called after the view has been created. Initializes components, retrieves arguments,
     * and sets up data loading and UI interaction logic.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Saved state if the fragment is being reconstructed.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvAlertSettings = view.findViewById(R.id.rvAlertSettings);
        MaterialButton btnSaveField = view.findViewById(R.id.btnSaveField);

        // 0) Retrieve fieldId from arguments
        Bundle args = getArguments();
        if (args != null) fieldId = args.getInt("fieldId", -1);

        if (fieldId == -1) {
            Log.e(TAG, "Missing fieldId: pass it as an argument to SetAlertsFragment!");
            btnSaveField.setEnabled(false);
            return;
        }

        // 1) Set up RecyclerView
        rvAlertSettings.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 2) Initialize adapter with an empty listener (changes are handled via the shared 'items' list)
        adapter = new AlertSettingsAdapter(items, updatedItems -> {
            // Data is updated in-place within the 'items' list
        });

        rvAlertSettings.setAdapter(adapter);

        // 3) Load alert types from database and build the UI list
        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;

        AlertsViewModel avm = a.alertsVM();

        cd.add(
                avm.getAllAlertTypes()
                        .subscribe(
                                alertTypes -> {
                                    // Map all possible alert types to UI models
                                    items.clear();
                                    items.addAll(mapAlertTypesToUi(alertTypes));

                                    // Fetch currently activated alerts for this specific field
                                    cd.add(avm.getActivatedAlertsFromField(fieldId).subscribe(activeList -> {
                                        List<AlertWithThreshold> active = activeList.getAlerts();
                                        for (AlertSettingUiModel asUi : items) {
                                            for (AlertWithThreshold activeAlert : active) {
                                                if (asUi.getId() == activeAlert.getAlertType().getId()) {
                                                    // Synchronize UI state with database values
                                                    asUi.enabled = true;
                                                    asUi.hasPrimaryThreshold = true;
                                                    asUi.primaryValue = activeAlert.getThreshold().getThreshold1().intValue();
                                                    if (activeAlert.getThreshold().getThreshold2() != null) {
                                                        asUi.hasSecondaryThreshold = true;
                                                        asUi.secondaryValue = activeAlert.getThreshold().getThreshold2().intValue();
                                                    }
                                                }
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                    }));
                                },
                                err -> Log.e(TAG, "Error fetching all alert types", err)
                        )
        );

        // 4) Save alert configurations for the current field
        btnSaveField.setOnClickListener(v -> {
            MainActivity a2 = (MainActivity) requireActivity();
            if (!a2.vmsReady()) return;
            FieldsViewModel vm = a2.fieldsVM();

            Log.d(TAG, "=== Saving UI State | fieldId=" + fieldId + " ===");

            List<Pair<Integer, Threshold>> selected = new ArrayList<>();

            for (AlertSettingUiModel m : items) {
                if (m != null && m.enabled) {
                    selected.add(new Pair<>(
                            m.getId(),
                            new Threshold((double) m.primaryValue, m.hasSecondaryThreshold ? (double) m.secondaryValue : null)
                    ));
                }
            }

            cd.add(
                    vm.updateAlertsToField(fieldId, selected)
                            .andThen(vm.getActivatedAlertsFromField(fieldId).firstOrError())
                            .subscribe(
                                    activated -> {
                                        Toast.makeText(requireContext(),
                                                "Alert salvati",
                                                Toast.LENGTH_SHORT).show();

                                        AlertManagerInitializer.triggerImmediateSync(requireContext());
                                        NavHostFragment.findNavController(this)
                                                .navigate(R.id.fieldsListFragment);

                                    },
                                    err -> {
                                        Log.e(TAG, "Error saving to DB", err);
                                        Toast.makeText(requireContext(),
                                                "Errore salvataggio",
                                                Toast.LENGTH_LONG).show();
                                    }
                            )
            );
        });
    }

    /**
     * Maps the database AlertType entities to UI models used by the adapter.
     *
     * @param alertTypes List of alert types from the database.
     * @return List of UI models for the settings screen.
     */
    private List<AlertSettingUiModel> mapAlertTypesToUi(List<AlertType> alertTypes) {
        List<AlertSettingUiModel> out = new ArrayList<>();
        if (alertTypes == null) return out;

        for (AlertType t : alertTypes) {
            if (t == null) continue;

            // Retrieve metadata (icons, labels, units) based on alert name
            AlertMeta meta = metaFor(t.getName());

            int primary = (t.getDefaultThreshold().getThreshold1() == null) ? meta.primaryDefault : (int) Math.round(t.getDefaultThreshold().getThreshold1());
            int secondary = (t.getDefaultThreshold().getThreshold2() == null) ? meta.secondaryDefault : (int) Math.round(t.getDefaultThreshold().getThreshold2());

            AlertSettingUiModel ui = new AlertSettingUiModel(
                    t.getId(),
                    meta.iconRes,
                    t.getName(),
                    t.getDescription(),
                    false, // disabled by default until matched with active alerts
                    true,
                    meta.primaryLabel,
                    primary,
                    meta.primaryUnit,
                    meta.hasSecondary,
                    meta.secondaryLabel,
                    secondary,
                    meta.secondaryUnit
            );

            out.add(ui);
        }

        return out;
    }

    /**
     * Helper class to store metadata (icons, labels, and defaults) for different alert types.
     */
    private static class AlertMeta {
        int iconRes;
        String primaryLabel;
        String primaryUnit;
        int primaryDefault;

        boolean hasSecondary;
        String secondaryLabel;
        String secondaryUnit;
        int secondaryDefault;

        AlertMeta(int iconRes, String pLabel, String pUnit, int pDef,
                  boolean has2, String sLabel, String sUnit, int sDef) {
            this.iconRes = iconRes;
            this.primaryLabel = pLabel;
            this.primaryUnit = pUnit;
            this.primaryDefault = pDef;
            this.hasSecondary = has2;
            this.secondaryLabel = sLabel;
            this.secondaryUnit = sUnit;
            this.secondaryDefault = sDef;
        }
    }

    /**
     * Provides metadata for an alert based on its name.
     *
     * @param name The name of the alert type.
     * @return An {@link AlertMeta} object containing UI-specific metadata.
     */
    private AlertMeta metaFor(String name) {
        if (name == null) name = "";

        switch (name) {
            case "Ondata di calore":
                return new AlertMeta(
                        /*icon*/ R.drawable.ic_alert_calore,
                        "Temperatura massima", "°C", 35,
                        true, "Per oltre", "h", 24
                );

            case "Gelo / Brina":
                return new AlertMeta(
                        R.drawable.ic_alert_gelo,
                        "Temperatura minima", "°C", 0,
                        true, "Per oltre", "h", 6
                );

            case "Pioggia intensa":
                return new AlertMeta(
                        R.drawable.ic_alert_pioggia,
                        "Pioggia maggiore di", "mm", 30,
                        true, "In meno di", "h", 3
                );

            case "Vento forte":
                return new AlertMeta(
                        R.drawable.ic_alert_vento,
                        "Velocità vento maggiore di", "km/h", 50,
                        false, "", "", 0
                );

            case "Temporale / Grandine":
                return new AlertMeta(
                        R.drawable.ic_alert_temporale,
                        "Probabilità temporale maggiore di", "%", 60,
                        true, "Probabilità grandine maggiore di", "%", 40
                );

            case "Siccità prolungata":
                return new AlertMeta(
                        R.drawable.ic_alert_siccita,
                        "Giorni senza pioggia", "giorni", 5,
                        false, "", "", 0
                );

            case "Umidità elevata":
                return new AlertMeta(
                        R.drawable.ic_alert_umidita,
                        "Umidità maggiore di", "%", 80,
                        false, "", "", 0
                );

            case "Escursione termica elevata":
                return new AlertMeta(
                        R.drawable.ic_alert_escursione,
                        "Escursione maggiore di", "°C", 12,
                        false, "", "", 0
                );

            case "Rischio incendio":
                return new AlertMeta(
                        R.drawable.ic_alert_incendio,
                        "Temperatura maggiore di", "°C", 30,
                        true, "Umidità minore di", "%", 30
                );

            case "Scarsa ventilazione":
                return new AlertMeta(
                        R.drawable.ic_alert_ventilazione,
                        "Velocità vento minore di", "km/h", 5,
                        false, "", "", 0
                );

            default:
                return new AlertMeta(
                        R.drawable.ic_group_default,  // fallback
                        "Soglia", "", 0,
                        false, "", "", 0
                );
        }
    }

    /**
     * Clears subscriptions to avoid memory leaks when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
