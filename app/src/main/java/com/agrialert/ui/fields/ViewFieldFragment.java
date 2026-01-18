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

import java.util.ArrayList;
import java.util.List;

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
                String groupOrField = "";
                if(alert.getFieldId() == 0) {
                    groupOrField = "Gruppo: " + alert.getGroupName();
                } else {
                    groupOrField = "Campo: " + alert.getFieldAddress();
                }
                String[] descAndForecast = alert.getDescription().split(" - ");
                uiAlerts.add(new AlertUiModel(
                        alert.getId(),
                        String.valueOf(alert.getTypeId()),
                        alert.getTitle(),
                        descAndForecast[0],
                        groupOrField,
                        descAndForecast[1],
                        alert.isResolved(),
                        getIconForType(alert.getTypeId())
                ));
            }

            adapter.submitList(uiAlerts);
        }));
    };

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
}
