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

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AlertsListFragment extends Fragment {

    private RecyclerView rvAlerts;
    private MaterialButton btnAlertsActive;
    private MaterialButton btnAlertsResolved;

    private AlertsAdapter adapter;
    private final List<AlertUiModel> allAlerts = new ArrayList<>();

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


        // dati di esempio
        allAlerts.clear();
        allAlerts.addAll(createSampleAlerts());

        // default: tab "Attivi"
        showActiveAlerts();

        // toggle
        btnAlertsActive.setOnClickListener(v -> showActiveAlerts());
        btnAlertsResolved.setOnClickListener(v -> showResolvedAlerts());
    }

    // ------------------- FILTRI -------------------

    private void showActiveAlerts() {
        List<AlertUiModel> active = new ArrayList<>();
        for (AlertUiModel alert : allAlerts) {
            if (!alert.isResolved) {
                active.add(alert);
            }
        }
        adapter.submitList(active);

        btnAlertsActive.setChecked(true);
        btnAlertsResolved.setChecked(false);
    }

    private void showResolvedAlerts() {
        List<AlertUiModel> resolved = new ArrayList<>();
        for (AlertUiModel alert : allAlerts) {
            if (alert.isResolved) {
                resolved.add(alert);
            }
        }
        adapter.submitList(resolved);

        btnAlertsActive.setChecked(false);
        btnAlertsResolved.setChecked(true);
    }

    // ------------------- DATI DI ESEMPIO -------------------

    private List<AlertUiModel> createSampleAlerts() {
        List<AlertUiModel> list = new ArrayList<>();

        // ATTIVI
        list.add(new AlertUiModel(
                1L,
                "VENTO_FORTE",
                "Vento forte",
                "Vento > 50 km/h",
                "Via Verdirdi, 15 - Mestre (VE)",
                "Oggi",
                false, // non risolto
                getIconForType("VENTO_FORTE")
        ));

        list.add(new AlertUiModel(
                2L,
                "ONDATA_CALORE",
                "Ondata di calore",
                "Temperatura aria > 35 °C",
                "Via Verdirdi, 15 - Mestre (VE)",
                "Oggi",
                false,
                getIconForType("ONDATA_CALORE")
        ));

        list.add(new AlertUiModel(
                3L,
                "SCARSA_VENTILAZIONE",
                "Scarsa ventilazione",
                "Vento < 5 km/h, Umidità > 80%",
                "Via Verdirdi, 15 - Mestre (VE)",
                "Domani",
                false,
                getIconForType("SCARSA_VENTILAZIONE")
        ));

        // RISOLTO
        list.add(new AlertUiModel(
                4L,
                "GELO_BRINA",
                "Gelo / brina",
                "Temperatura minima < 0 °C",
                "Via Verdirdi, 15 - Mestre (VE)",
                "Tra 5 giorni",
                true, // già risolto
                getIconForType("GELO_BRINA")
        ));

        return list;
    }

    // ------------------- MAPPING TIPO → ICONA -------------------

    /**
     * Restituisce l'icona corretta per la tipologia di alert.
     * ATTENZIONE: i nomi dei drawable devono esistere in res/drawable.
     * Esempio:
     *  - ic_alert_vento.xml
     *  - ic_alert_calore.xml
     *  - ic_alert_ventilazione.xml
     *  - ic_alert_gelo.xml
     *  - ic_alert_pioggia.xml
     *  - ic_alert_temporale.xml
     *  - ic_alert_siccita.xml
     *  - ic_alert_umidita.xml
     *  - ic_alert_escursione.xml
     *  - ic_alert_incendio.xml
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
