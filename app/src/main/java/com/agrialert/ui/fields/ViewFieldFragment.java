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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Fragment that displays detailed information about a specific agricultural field.
 * It shows the field's address, crop type, associated group, and a list of active alerts.
 * Users can navigate to edit or delete the field from here.
 */
public class ViewFieldFragment extends Fragment {
    /** Tag for logging. */
    private static final String TAG = "ViewField";

    /** ImageView displaying the icon for the field's crop type. */
    private ImageView imgCrop;
    /** TextView for the field's address. */
    private TextView txtAddress;
    /** TextView for the field's crop type name. */
    private TextView txtCrop;
    /** TextView for the name of the group the field belongs to. */
    private TextView txtGroup;
    /** RecyclerView displaying the list of active alerts for this field. */
    private RecyclerView rvFieldAlerts;

    /** Button to navigate to the field editing screen. */
    private MaterialButton btnEditField;
    /** Button to navigate to the field deletion confirmation screen. */
    private MaterialButton btnDeleteField;
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
        imgCrop = view.findViewById(R.id.imgCrop);
        txtAddress = view.findViewById(R.id.txtAddress);
        txtCrop = view.findViewById(R.id.txtCrop);
        txtGroup = view.findViewById(R.id.txtGroup);
        rvFieldAlerts = view.findViewById(R.id.rvFieldAlerts);
        btnEditField = view.findViewById(R.id.btnEditField);
        btnDeleteField = view.findViewById(R.id.btnDeleteField);

        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "Missing field: pass it as an argument to ViewFieldFragment!");
            return;
        }
        FieldUiModel field = args.getParcelable("field");
        if (field == null) return;

        Bundle b = new Bundle();
        b.putInt("fieldId", (int)field.id);

        // Set up listeners
        btnEditField.setOnClickListener(v ->
                NavHostFragment.findNavController(ViewFieldFragment.this)
                        .navigate(R.id.action_viewFieldFragment_to_editFieldFragment, b));

        btnDeleteField.setOnClickListener(v ->
                NavHostFragment.findNavController(ViewFieldFragment.this)
                        .navigate(R.id.action_viewFieldFragment_to_confirmDeleteFieldFragment, b));

        txtAddress.setText(field.address);
        txtCrop.setText(field.crop);
        txtGroup.setText(field.groupName);
        imgCrop.setImageResource(field.iconRes);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        avm = a.alertsVM();

        // LIST OF ALERTS ASSOCIATED WITH THE FIELD
        rvFieldAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AlertsAdapter((alert, isResolved) -> {
            // Update the model
            alert.isResolved = isResolved;
            avm.setAlertResolved(alert.id);
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
                String groupOrField = "";
                if(alert.getFieldId() == 0) {
                    groupOrField = "Group: " + alert.getGroupName();
                } else {
                    groupOrField = "Field: " + alert.getFieldAddress();
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
    }

    /**
     * Maps an alert type ID to its corresponding drawable resource.
     *
     * @param typeId The ID representing the alert type.
     * @return The resource ID of the icon.
     */
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

    /**
     * Clears RxJava disposables when the fragment's view is destroyed.
     */
    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
