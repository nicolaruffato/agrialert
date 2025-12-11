package com.agrialert.ui.fields;

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

import androidx.navigation.fragment.NavHostFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FieldsListFragment extends Fragment {


    //quando true , al prossimo ritorno forziamo il tab "Gruppi"
    public static boolean forceGroupsTab = false;
    private MaterialButton btnFields;
    private MaterialButton btnFieldGroups;
    private MaterialButton btnAddField;
    private RecyclerView rvFields;

    // FALSE = Campi, TRUE = Gruppi di campi
    private boolean showingGroups = false;

    private FieldsAdapter fieldsAdapter;
    private GroupsAdapter groupsAdapter;

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

        // view binding “manuale”
        btnFields = view.findViewById(R.id.btnFields);
        btnFieldGroups = view.findViewById(R.id.btnFieldGroups);
        btnAddField = view.findViewById(R.id.btnAddField);
        rvFields = view.findViewById(R.id.rvFields);

        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));

        // adapter (usano submitList(...) per i dati)
        fieldsAdapter = new FieldsAdapter();
        rvFields.setAdapter(fieldsAdapter);
        groupsAdapter = new GroupsAdapter(this);

        // toggle Campi / Gruppi di campi
        btnFields.setOnClickListener(v -> showFields());
        btnFieldGroups.setOnClickListener(v -> showGroups());

        // bottone in basso: decide in base alla tab attiva
        btnAddField.setOnClickListener(v -> {
            if (showingGroups) {
                // siamo nella tab "Gruppi di campi"
                NavHostFragment.findNavController(FieldsListFragment.this)
                        .navigate(R.id.addGroupFragment);
            } else {
                // siamo nella tab "Campi"
                NavHostFragment.findNavController(FieldsListFragment.this)
                        .navigate(R.id.addFieldFragment);
            }
        });

        // schermata iniziale: Campi
        showFields();
    }


    @Override
    public void onResume(){
       super.onResume();
       //se toogle "Gruppi" è selezionato mostra lista gruppi
        if (forceGroupsTab){
            showGroups();
            forceGroupsTab = false;
        }
    }

    // -------------------- UI helper --------------------

    private void showFields() {
        showingGroups = false;

        btnFields.setChecked(true);
        btnFieldGroups.setChecked(false);
        btnAddField.setText("Aggiungi un nuovo campo");

        rvFields.setAdapter(fieldsAdapter);
        fieldsAdapter.submitList(createSampleFields());
    }

    private void showGroups() {
        showingGroups = true;

        btnFields.setChecked(false);
        btnFieldGroups.setChecked(true);
        btnAddField.setText("Aggiungi un nuovo gruppo");

        rvFields.setAdapter(groupsAdapter);
        groupsAdapter.submitList(createSampleGroups());
    }

    // ------------------- DATI DI ESEMPIO CAMPi -------------------

    private List<FieldUiModel> createSampleFields() {
        return Arrays.asList(
                new FieldUiModel(
                        1L,
                        "Via Verdirdi, 15 - Mestre (VE)",
                        "Ortaggi",
                        "Gruppo A",
                        R.drawable.ic_ortaggi,
                        Arrays.asList(
                                R.drawable.ic_alert_vento,
                                R.drawable.ic_alert_calore,
                                R.drawable.ic_alert_gelo
                        )
                ),
                new FieldUiModel(
                        2L,
                        "Via Giallo, 15 - Mestre (VE)",
                        "Cereali",
                        "Gruppo A",
                        R.drawable.ic_cereali,
                        Arrays.asList(
                                R.drawable.ic_alert_calore,
                                R.drawable.ic_alert_temporale
                        )
                ),
                new FieldUiModel(
                        3L,
                        "Via Torino, 154 - Martellago (VE)",
                        "Frutteti",
                        "Gruppo B",
                        R.drawable.ic_frutteti,
                        Arrays.asList(
                                R.drawable.ic_alert_vento
                        )
                )
        );
    }


    // ------------------- DATI DI ESEMPIO GRUPPI -------------------

    private List<GroupUiModel> createSampleGroups() {
        return Arrays.asList(
                new GroupUiModel(
                        1L,
                        "Gruppo A",
                        "Descrizione del Gruppo A",
                        R.drawable.ic_group_default,
                        Arrays.asList(
                                R.drawable.ic_alert_vento,
                                R.drawable.ic_alert_calore
                        )
                ),
                new GroupUiModel(
                        2L,
                        "Gruppo B",
                        "Descrizione del Gruppo B",
                        R.drawable.ic_group_default,
                        Arrays.asList(
                                R.drawable.ic_alert_gelo
                        )
                )
        );
    }

}
