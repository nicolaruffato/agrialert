package com.agrialert.ui.fields;
import com.agrialert.ui.fields.groups.GroupUiModel;
import com.agrialert.ui.fields.groups.GroupsAdapter;

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
import com.agrialert.ui.fields.groups.GroupUiModel;
import com.agrialert.ui.fields.groups.GroupsAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldsListFragment extends Fragment {

    private RecyclerView rvFields;
    private MaterialButton btnFields;
    private MaterialButton btnFieldGroups;
    private MaterialButton btnAddField;

    // Adapter e dati per Campi
    private FieldsAdapter fieldsAdapter;
    private List<FieldUiModel> fieldsList = new ArrayList<>();

    // Adapter e dati per Gruppi
    private GroupsAdapter groupsAdapter;
    private List<GroupUiModel> groupsList = new ArrayList<>();

    public FieldsListFragment() {
        // costruttore vuoto richiesto
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fields_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFields = view.findViewById(R.id.rvFields);
        btnFields = view.findViewById(R.id.btnFields);
        btnFieldGroups = view.findViewById(R.id.btnFieldGroups);
        btnAddField = view.findViewById(R.id.btnAddField);

        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Adapter campi
        fieldsAdapter = new FieldsAdapter();
        fieldsList = createSampleFields();

        // Adapter gruppi
        groupsAdapter = new GroupsAdapter();
        groupsList = createSampleGroups();

        // Stato iniziale: CAMPI
        showFields();

        // Toggle CAMPI
        btnFields.setOnClickListener(v -> showFields());

        // Toggle GRUPPI
        btnFieldGroups.setOnClickListener(v -> showGroups());
    }

    // ------------------- MOSTRA CAMPi -------------------

    private void showFields() {
        btnFields.setChecked(true);
        btnFieldGroups.setChecked(false);

        rvFields.setAdapter(fieldsAdapter);
        fieldsAdapter.submitList(fieldsList);

        btnAddField.setText("Aggiungi un nuovo campo");
    }

    // ------------------- MOSTRA GRUPPI -------------------

    private void showGroups() {
        btnFields.setChecked(false);
        btnFieldGroups.setChecked(true);

        rvFields.setAdapter(groupsAdapter);
        groupsAdapter.submitList(groupsList);

        btnAddField.setText("Aggiungi un nuovo gruppo");
    }

    // ------------------- DATI DI ESEMPIO CAMPi -------------------

    private List<FieldUiModel> createSampleFields() {
        List<FieldUiModel> list = new ArrayList<>();

        int iconOrtaggi = R.drawable.ic_ortaggi;
        int iconCereali = R.drawable.ic_cereali;
        int iconFrutteti = R.drawable.ic_frutteti;

        list.add(new FieldUiModel(
                1,
                "Via Verdirdi, 15 - Mestre (VE)",
                "Ortaggi",
                "Gruppo: Prova",
                iconOrtaggi,
                Arrays.asList(R.drawable.ic_alert_vento, R.drawable.ic_alert_pioggia)
        ));

        list.add(new FieldUiModel(
                2,
                "Via Giallo, 10 - Mestre (VE)",
                "Cereali",
                "Gruppo: Zona A",
                iconCereali,
                Arrays.asList(R.drawable.ic_alert_calore)
        ));

        list.add(new FieldUiModel(
                3,
                "Via Blu, 5 - Rovigo (RO)",
                "Frutteti",
                "Gruppo: Zona B",
                iconFrutteti,
                Arrays.asList(R.drawable.ic_alert_gelo, R.drawable.ic_alert_temporale)
        ));

        return list;
    }

    // ------------------- DATI DI ESEMPIO GRUPPI -------------------

    private List<GroupUiModel> createSampleGroups() {
        List<GroupUiModel> list = new ArrayList<>();

        // icona grande del gruppo (puoi mettere una tua)
        int groupIcon = R.drawable.ic_group_default;

        list.add(new GroupUiModel(
                1,
                "Gruppo A",
                "Campi in zona Mestre con ortaggi",
                groupIcon,
                Arrays.asList(
                        R.drawable.ic_alert_vento,
                        R.drawable.ic_alert_gelo,
                        R.drawable.ic_alert_calore
                )
        ));

        list.add(new GroupUiModel(
                2,
                "Gruppo B",
                "Campi in zona Rovigo con frutteti",
                groupIcon,
                Arrays.asList(
                        R.drawable.ic_alert_pioggia,
                        R.drawable.ic_alert_temporale
                )
        ));

        return list;
    }
}

