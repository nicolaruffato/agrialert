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
import com.agrialert.ui.alerts.AlertUiModel;
import com.agrialert.ui.alerts.AlertsAdapter;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public class ViewFieldFragment extends Fragment {
    private static final String TAG = "ViewField";
    private ImageView imgCrop;
    private TextView txtAddress;
    private TextView txtCrop;
    private TextView txtGroup;
    private RecyclerView rvFieldAlerts;

    private MaterialButton btnEditField;
    private MaterialButton btnDeleteField;

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
        b.putParcelable("field", field);


        // TODO: send whole field to edit
        //LISTENER
        btnEditField.setOnClickListener(v ->
                NavHostFragment.findNavController(ViewFieldFragment.this)
                        .navigate(R.id.action_viewFieldFragment_to_editFieldFragment, b));

        btnDeleteField.setOnClickListener(v ->
                NavHostFragment.findNavController(ViewFieldFragment.this)
                        .navigate(R.id.action_viewFieldFragment_to_confirmDeleteFieldFragment, b));
        ;
        txtAddress.setText(field.address);
        txtCrop.setText(field.crop);
        txtGroup.setText(field.groupName);
        imgCrop.setImageResource(field.iconRes);

        // LISTA ALERT COLLEGATI AL CAMPO
        rvFieldAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        AlertsAdapter adapter = new AlertsAdapter((alert, isResolved) -> {
            // aggiorno il model
            alert.isResolved = isResolved;


        });
        rvFieldAlerts.setAdapter(adapter);

        adapter.submitList(getSampleAlerts());
    }

    // ------- DATI DI ESEMPIO ALERT -------

    private List<AlertUiModel> getSampleAlerts() {
        List<AlertUiModel> list = new ArrayList<>();

        // ALERT 1 – Vento forte
        list.add(new AlertUiModel(
                1L,                                   // id
                "VENTO_FORTE",                        // typeId
                "Vento forte",                        // title
                "Vento > 50 km/h",                    // thresholdText
                "Via Verdirdi, 15 - Mestre (VE)",     // fieldAddress
                "Oggi",                               // timeLabel
                false,                                // isResolved
                R.drawable.ic_alert_vento             // iconRes
        ));

        // ALERT 2 – Ondata di calore
        list.add(new AlertUiModel(
                2L,
                "ONDATA_CALORE",
                "Ondata di calore",
                "Temperatura aria > 35 °C",
                "Via Verdirdi, 15 - Mestre (VE)",
                "Oggi",
                true,
                R.drawable.ic_alert_calore   // se non ce l’hai usa ic_alert_vento
        ));

        // ALERT 3 – Scarsa ventilazione
        list.add(new AlertUiModel(
                3L,
                "SCARSA_VENTILAZIONE",
                "Scarsa ventilazione",
                "Vento < 5 km/h, Umidità > 80%",
                "Via Verdirdi, 15 - Mestre (VE)",
                "Domani",
                false,
                R.drawable.ic_alert_ventilazione   // oppure ic_alert_vento
        ));

        return list;
    }
}
