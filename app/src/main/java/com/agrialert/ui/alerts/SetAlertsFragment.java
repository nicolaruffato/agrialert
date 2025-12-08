
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
import androidx.navigation.fragment.NavHostFragment;
import android.widget.Toast;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SetAlertsFragment extends Fragment {

    private RecyclerView rvAlertSettings;
    private MaterialButton btnSaveField;
    private AlertSettingsAdapter adapter;
    private List<AlertSettingUiModel> items = new ArrayList<>();

    public SetAlertsFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set_alerts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvAlertSettings = view.findViewById(R.id.rvAlertSettings);
        btnSaveField = view.findViewById(R.id.btnSaveField);

        rvAlertSettings.setLayoutManager(new LinearLayoutManager(requireContext()));
        items = createSampleAlerts();

        adapter = new AlertSettingsAdapter(items, updatedItems -> {
            // qui in futuro potrai salvare in ViewModel, ecc.
        });

        rvAlertSettings.setAdapter(adapter);

        btnSaveField.setOnClickListener(v -> {
            // Conta quanti alert sono attivi
            int activeCount = 0;
            for (AlertSettingUiModel item : items) {
                if (item.enabled) activeCount++;
            }

            // Messaggio di conferma
            String msg = "Campo salvato con " + activeCount + " alert attivi";
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();

            // Torna alla lista Campi
            NavHostFragment.findNavController(SetAlertsFragment.this)
                    .popBackStack(R.id.fieldsListFragment, false);
        });

    }

    private List<AlertSettingUiModel> createSampleAlerts() {
        List<AlertSettingUiModel> list = new ArrayList<>();

        // 1) Ondata di calore: temperatura + ore
        list.add(new AlertSettingUiModel(
                1,
                R.drawable.ic_alert_calore,
                "Ondata di calore",
                "Temperature elevate che possono causare stress termico",
                false,
                true, "Temperatura massima", 26, "°",
                true, "Per oltre", 24, " h"
        ));

        // 2) Gelo / Brina: temperatura + ore
        list.add(new AlertSettingUiModel(
                2,
                R.drawable.ic_alert_gelo,
                "Gelo / Brina",
                "Rischio di danni da gelo su colture sensibili",
                false,
                true, "Temperatura minima", 0, "°",
                true, "Per oltre", 6, " h"
        ));

        // 3) Pioggia intensa: mm + ore
        list.add(new AlertSettingUiModel(
                3,
                R.drawable.ic_alert_pioggia,
                "Pioggia intensa",
                "Precipitazioni elevate che possono provocare ristagni o erosione",
                false,
                true, "Pioggia maggiore di", 30, " mm",
                true, "In meno di", 3, " h"
        ));

        // 4) Vento forte: solo velocità
        list.add(new AlertSettingUiModel(
                4,
                R.drawable.ic_alert_vento,
                "Vento Forte",
                "Raffiche che possono piegare o danneggiare le piante",
                false,
                true, "Velocità vento maggiore di", 50, " km/h",
                false, "", 0, ""
        ));

        // 5) Temporale / Grandine: 2 soglie
        list.add(new AlertSettingUiModel(
                5,
                R.drawable.ic_alert_temporale,
                "Temporale / Grandine",
                "Eventi violenti con rischio di danni ai raccolti",
                false,
                true, "Probabilità temporale maggiore di", 60, "%",
                true, "Probabilità grandine maggiore di", 40, "%"
        ));

        // 6) Siccità prolungata
        list.add(new AlertSettingUiModel(
                6,
                R.drawable.ic_alert_siccita,
                "Siccità prolungata",
                "Carenza idrica dovuta a mancanza di piogge",
                false,
                true, "Giorni senza pioggia", 5, " giorni",
                false, "", 0, ""
        ));

        // 7) Umidità elevata
        list.add(new AlertSettingUiModel(
                7,
                R.drawable.ic_alert_umidita,
                "Umidità elevata",
                "Rischio malattie fungine dovute a eccesso di umidità",
                false,
                true, "Umidità maggiore di", 80, "%",
                false, "", 0, ""
        ));

        // 8) Escursione termica
        list.add(new AlertSettingUiModel(
                8,
                R.drawable.ic_alert_escursione,
                "Escursione termica elevata",
                "Rischio stress termico tra giorno e notte",
                false,
                true, "Escursione maggiore di", 12, "°",
                false, "", 0, ""
        ));

        // 9) Rischio incendio: temperatura + umidità
        list.add(new AlertSettingUiModel(
                9,
                R.drawable.ic_alert_incendio,
                "Rischio incendio",
                "Condizioni di vento secco e terreno arido",
                false,
                true, "Temperatura maggiore di", 30, "°",
                true, "Umidità minore di", 30, "%"
        ));

        // 10) Scarsa ventilazione
        list.add(new AlertSettingUiModel(
                10,
                R.drawable.ic_alert_ventilazione,
                "Scarsa ventilazione",
                "Stagnazione dell'aria con rischio muffe",
                false,
                true, "Velocità vento minore di", 5, " km/h",
                false, "", 0, ""
        ));

        return list;
    }


}
