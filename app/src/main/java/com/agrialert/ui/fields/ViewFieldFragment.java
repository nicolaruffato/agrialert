package com.agrialert.ui.fields;

import android.os.Bundle;
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

import com.agrialert.R;
import com.agrialert.ui.alerts.AlertUiModel;
import com.agrialert.ui.alerts.AlertsAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewFieldFragment extends Fragment {

    private ImageView imgFieldIcon;
    private TextView txtFieldAddress;
    private TextView txtFieldCrop;
    private TextView txtFieldGroup;
    private RecyclerView rvFieldAlerts;

    private AlertsAdapter alertsAdapter;

    public ViewFieldFragment() {
        // costruttore vuoto richiesto
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

        // header
        imgFieldIcon = view.findViewById(R.id.imgFieldIcon);
        txtFieldAddress = view.findViewById(R.id.txtFieldAddress);
        txtFieldCrop = view.findViewById(R.id.txtFieldCrop);
        txtFieldGroup = view.findViewById(R.id.txtFieldGroup);

        // lista alert
        rvFieldAlerts = view.findViewById(R.id.rvFieldAlerts);
        rvFieldAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));

        // adapter degli alert (qui non ci serve aggiornare le tab, quindi listener vuoto)
        alertsAdapter = new AlertsAdapter((alert, isResolved) -> {
            alert.isResolved = isResolved;
        });
        rvFieldAlerts.setAdapter(alertsAdapter);

        // per ora dati FINTI per provare la UI
        bindFakeFieldData();
    }

    private void bindFakeFieldData() {
        // dati finti header
        imgFieldIcon.setImageResource(R.drawable.ic_ortaggi); // cambia con la tua icona se serve
        txtFieldAddress.setText("Via Verdirdi, 15 - Mestre (VE)");
        txtFieldCrop.setText("Ortaggi");
        txtFieldGroup.setText("Gruppo: Prova");

        // dati finti alert del campo
        List<AlertUiModel> alerts = new ArrayList<>();
        alerts.add(new AlertUiModel(
                1L,
                "VENTO_FORTE",
                "Vento forte",
                "Vento > 50 km/h",
                "Via Verdirdi, 15 - Mestre (VE)",
                "Oggi",
                false,
                R.drawable.ic_alert_vento
        ));
        alerts.add(new AlertUiModel(
                2L,
                "GELO_BRINA",
                "Gelo / brina",
                "Temperatura < 0 °C",
                "Via Verdirdi, 15 - Mestre (VE)",
                "Domani",
                false,
                R.drawable.ic_alert_gelo
        ));

        alertsAdapter.submitList(alerts);
    }
}

