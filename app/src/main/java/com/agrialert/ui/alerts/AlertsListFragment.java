package com.agrialert.ui.alerts;

import android.os.Bundle;
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

        adapter = new AlertsAdapter((alert, isResolved) -> {
            // aggiorno il model
            alert.isResolved = isResolved;

            // ricalcolo la lista da mostrare in base al toggle
            if (btnAlertsActive.isChecked()) {
                showActiveAlerts();
            } else {
                showResolvedAlerts();
            }
        });

        rvAlerts.setAdapter(adapter);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;

        avm = a.alertsVM();

        // default: tab "Attivi"
        showActiveAlerts();

        // toggle
        btnAlertsActive.setOnClickListener(v -> showActiveAlerts());
        btnAlertsResolved.setOnClickListener(v -> showResolvedAlerts());

    }

    // ------------------- FILTRI -------------------

    private void showActiveAlerts() {
        cd.add(avm.getActiveAlerts().subscribe(alerts -> {
            List<AlertUiModel> active = new ArrayList<>();
            for(Alert alert : alerts) {
                active.add(new AlertUiModel(
                        alert.getId(),
                        String.valueOf(alert.getTypeId()),
                        alert.getTitle(),
                        "Threshold",
                        alert.getFieldAddress(),
                        String.valueOf(alert.getForecastAt()), // TODO: mettere data calcolando ms?
                        alert.isResolved(),
                        getIconForType(String.valueOf(alert.getTypeId()))
                ));
            }

            adapter.submitList(active);
        }));

        btnAlertsActive.setChecked(true);
        btnAlertsResolved.setChecked(false);
    }

    private void showResolvedAlerts() {
        cd.add(avm.getResolvedAlerts().subscribe(alerts -> {
            List<AlertUiModel> resolved = new ArrayList<>();
            for(Alert alert : alerts) {
                resolved.add(new AlertUiModel(
                        alert.getId(),
                        String.valueOf(alert.getTypeId()),
                        alert.getTitle(),
                        "Threshold",
                        alert.getFieldAddress(),
                        String.valueOf(alert.getForecastAt()), // TODO: mettere data calcolando ms?
                        alert.isResolved(),
                        getIconForType(String.valueOf(alert.getTypeId()))
                ));
            }

            adapter.submitList(resolved);
        }));

        btnAlertsActive.setChecked(false);
        btnAlertsResolved.setChecked(true);
    }

    // ------------------- MAPPING TIPO → ICONA -------------------

    /**
     * Restituisce l'icona corretta per la tipologia di alert.
     * ATTENZIONE: i nomi dei drawable devono esistere in res/drawable.
     * Esempio:
     *  - ic_alert_vento.xml
     */
    private int getIconForType(String typeId) {
        if (typeId == null) return R.drawable.ic_alert; // icona generica

        switch (typeId) {
            case "VENTO_FORTE":
                return R.drawable.ic_alert_vento;

            case "ONDATA_CALORE":
                return R.drawable.ic_alert_calore;

            case "SCARSA_VENTILAZIONE":
                return R.drawable.ic_alert_ventilazione;

            case "GELO_BRINA":
                return R.drawable.ic_alert_gelo;

            case "PIOGGIA_INTENSA":
                return R.drawable.ic_alert_pioggia;

            case "TEMPORALE_GRANDINE":
                return R.drawable.ic_alert_temporale;

            case "SICCITA":
                return R.drawable.ic_alert_siccita;

            case "UMIDITA_ELEVATA":
                return R.drawable.ic_alert_umidita;

            case "ESCURSIONE_TERMICA":
                return R.drawable.ic_alert_escursione;

            case "RISCHIO_INCENDIO":
                return R.drawable.ic_alert_incendio;

            default:
                return R.drawable.ic_alert; // fallback
        }
    }
}
