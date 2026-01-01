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

import com.agrialert.MainActivity;
import com.agrialert.R;
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

public class SetAlertsFragment extends Fragment {

    private static final String TAG = "SetAlerts";

    private final CompositeDisposable cd = new CompositeDisposable();

    private androidx.recyclerview.widget.RecyclerView rvAlertSettings;
    private MaterialButton btnSaveField;

    // LISTA STABILE (mai riassegnare!)
    private final List<AlertSettingUiModel> items = new ArrayList<>();
    private AlertSettingsAdapter adapter;

    private int fieldId = -1;

    public SetAlertsFragment() {
        super(R.layout.fragment_set_alerts);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlertSettings = view.findViewById(R.id.rvAlertSettings);
        btnSaveField = view.findViewById(R.id.btnSaveField);

        // 0) prendo fieldId
        Bundle args = getArguments();
        if (args != null) fieldId = args.getInt("fieldId", -1);

        // TODO: remove
        if (fieldId == -1) {
            // fallback: se vuoi prenderlo dal FieldsViewModel (se ce l’hai)
            MainActivity a0 = (MainActivity) requireActivity();
            if (a0.vmsReady()) {
                FieldsViewModel fvm0 = a0.fieldsVM();
                // <-- se hai un metodo tipo getCurrentFieldId(), mettilo qui:
                // fieldId = fvm0.getCurrentFieldId();
            }
        }

        if (fieldId == -1) {
            Log.e(TAG, "fieldId mancante: passalo come arg a SetAlertsFragment!");
            btnSaveField.setEnabled(false);
            return;
        }

        // 1) recycler
        rvAlertSettings.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 2) adapter + listener (QUI si fixano lista che sparisce e toggle)
        adapter = new AlertSettingsAdapter(items,updatedItems->{

        });

        rvAlertSettings.setAdapter(adapter);

        // 3) carico alert types dal DB e costruisco la UI list
        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;

        AlertsViewModel avm = a.alertsVM();

        cd.add(
                avm.getAllAlertTypes()
                        .subscribe(
                                alertTypes -> {
                                    // costruisci items da DB (ID veri!)

                                    items.clear();
                                    items.addAll(mapAlertTypesToUi(alertTypes));
                                    cd.add(avm.getActivatedAlertsFromField(fieldId).subscribe(activeList -> {
                                        List<AlertWithThreshold> active = activeList.getAlerts();
                                        for (AlertSettingUiModel asUi : items) {
                                            for (AlertWithThreshold activeAlert : active) {
                                                if (asUi.getId() == activeAlert.getAlertType().getId()) {
                                                    asUi.enabled = true;
                                                    asUi.hasPrimaryThreshold = true;
                                                    asUi.primaryValue = activeAlert.getThreshold().getThreshold1().intValue();
                                                    if(activeAlert.getThreshold().getThreshold2() != null) {
                                                        asUi.hasSecondaryThreshold = true;
                                                        asUi.secondaryValue = activeAlert.getThreshold().getThreshold2().intValue();
                                                    }
                                                }
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                    }));
                                },
                                err -> Log.e(TAG, "Errore getAllAlertTypes", err)
                        )
        );
        // TODO: check for alerts with 2 threshold
        // 4) salva relazioni alert <-> field
        btnSaveField.setOnClickListener(v -> {
            MainActivity a2 = (MainActivity) requireActivity();
            if (!a2.vmsReady()) return;
            FieldsViewModel vm = a2.fieldsVM();

            Log.d(TAG, "=== UI STATE | fieldId=" + fieldId + " ===");

            for (AlertSettingUiModel m : items) {
                Log.d(TAG,
                        "UI -> id=" + m.id +
                                " | name=" + m.title +
                                " | enabled=" + m.enabled +
                                " | primary=" + m.primaryValue +
                                (m.hasSecondaryThreshold ? " | secondary=" + m.secondaryValue : "")
                );
            }

            List<Pair<Integer, Threshold>> selected = new ArrayList<>();

            for (AlertSettingUiModel m : items) {
                if (m != null && m.enabled) {
                    selected.add(new Pair<>(m.getId(), new Threshold((double)m.primaryValue, (double)m.secondaryValue)));
                }
            }

            Log.d(TAG, "=== SEND TO DB | fieldId=" + fieldId + " | count=" + selected.size() + " ===");
            for (Pair<Integer, Threshold> p : selected) {
                Log.d(TAG, "SEND -> alertTypeId=" + p.getFirst() + "  threshold=" + p.getSecond());
            }

            cd.add(
                    vm.updateAlertsToField(fieldId, selected)
                            .andThen(vm.getActivatedAlertsFromField(fieldId).firstOrError())
                            .subscribe(
                                    activated -> {

                                        Log.d(TAG, "=== DB DOPO IL SAVE | fieldId=" + fieldId + " ===");
                                        Log.d(TAG, "DB alerts size = " + activated.getAlerts().size());

                                        for (AlertWithThreshold b : activated.getAlerts()) {
                                            Log.d(TAG,
                                                    "DB -> alertId=" + b.getAlertType().getId() +
                                                            " name=" + b.getAlertType().getName() +
                                                            " threshold=" + b.getThreshold()
                                            );
                                        }

                                        Toast.makeText(requireContext(),
                                                "Alert salvati",
                                                Toast.LENGTH_SHORT).show();

                                        NavHostFragment.findNavController(this)
                                                .popBackStack(R.id.fieldsListFragment, false);

                                    },
                                    err -> {
                                        Log.e(TAG, "Errore save/read DB", err);
                                        Toast.makeText(requireContext(),
                                                "Errore salvataggio",
                                                Toast.LENGTH_LONG).show();
                                    }
                            )
            );

        });
    }

    // Mappa AlertType(DB) -> AlertSettingUiModel(UI)
    private List<AlertSettingUiModel> mapAlertTypesToUi(List<AlertType> alertTypes) {
        List<AlertSettingUiModel> out = new ArrayList<>();
        if (alertTypes == null) return out;

        for (AlertType t : alertTypes) {
            if (t == null) continue;

            AlertMeta meta = metaFor(t.getName()); // <-- qui metti icona/label/unit/2a soglia dal sample

            int primary = (t.getDefaultThreshold().getThreshold1() == null) ? meta.primaryDefault : (int) Math.round(t.getDefaultThreshold().getThreshold1());
            int secondary = (t.getDefaultThreshold().getThreshold2() == null) ? meta.secondaryDefault : (int) Math.round(t.getDefaultThreshold().getThreshold2());

            AlertSettingUiModel ui = new AlertSettingUiModel(
                    (long) t.getId(),                 // id
                    meta.iconRes,                     // iconRes (DAL SAMPLE)
                    t.getName(),                      // title
                    t.getDescription(),               // description
                    false,                            // enabled default (all’inizio OFF)
                    true,                             // hasPrimaryThreshold
                    meta.primaryLabel,                // primaryLabel (DAL SAMPLE)
                    primary,                          // primaryValue
                    meta.primaryUnit,                 // primaryUnit
                    meta.hasSecondary,                // hasSecondaryThreshold
                    meta.secondaryLabel,              // secondaryLabel
                    secondary,                        // secondaryValue
                    meta.secondaryUnit                // secondaryUnit
            );

            out.add(ui);
        }

        return out;
    }

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

    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
