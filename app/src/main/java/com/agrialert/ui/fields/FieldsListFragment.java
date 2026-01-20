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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.agrialert.ui.fields.groups.GroupUiModel;
import com.agrialert.ui.fields.groups.GroupsAdapter;
import com.agrialert.MainActivity;
import com.agrialert.viewmodel.FieldsViewModel;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.data_manager.Field;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

import com.google.android.material.button.MaterialButton;

import androidx.navigation.fragment.NavHostFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FieldsListFragment extends Fragment
        implements GroupsAdapter.OnGroupClickListener, FieldsAdapter.OnFieldClickListener  {


    //quando true , al prossimo ritorno forziamo il tab "Gruppi"
    public static boolean forceGroupsTab = false;
    private MaterialButton btnFields;
    private MaterialButton btnFieldGroups;
    private MaterialButton btnAddField;
    private RecyclerView rvFields;
    private ImageView emptyImage;
    private TextView emptyText;

    // FALSE = Campi, TRUE = Gruppi di campi
    private boolean showingGroups = false;

    private FieldsAdapter fieldsAdapter;
    private GroupsAdapter groupsAdapter;
    private FieldsViewModel vm;
    private final CompositeDisposable cd = new CompositeDisposable();


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
        emptyImage = view.findViewById(R.id.emptyImage);
        emptyText = view.findViewById(R.id.emptyText);

        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));

        // adapter (usano submitList(...) per i dati)
        fieldsAdapter = new FieldsAdapter(this);
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

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        vm = a.fieldsVM();

        // Handle user from addField -> fieldList without setting alerts
        vm.isFieldPending = false;

        // schermata iniziale: Campi

        btnFieldGroups.setChecked(false);
        btnFields.setChecked(true);

        view.post(() -> {
           if(btnFields.isChecked()){
               showFields();
           } else {
               showGroups();
           }
        });
    }

    @Override
    public void onFieldClick(FieldUiModel field) {
        Bundle b = new Bundle();
        b.putParcelable("field", field);

        NavHostFragment.findNavController(this)
                .navigate(R.id.viewFieldFragment, b);
    }


    @Override
    public void onGroupClick(GroupUiModel group){
        // TODO: Passare solo nome
        Bundle b = new Bundle();
        b.putParcelable("group", group);
        NavHostFragment.findNavController(this)
                .navigate(R.id.viewGroupFragment, b);
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

        cd.clear();
        cd.add(
                vm.getAllGroups().subscribe(
                        groups -> {
                            List<FieldUiModel> uiList = new java.util.ArrayList<>();

                            for (GroupWithFields g : groups) {
                                if (g.getFields() == null) continue;

                                for (Field f : g.getFields()) {
                                    uiList.add(
                                            new FieldUiModel(
                                                    f.getId(),
                                                    f.getAddress(),
                                                    f.getLatitude(),
                                                    f.getLongitude(),
                                                    requireContext().getString(f.getCropType().getResourceId()),
                                                    f.getGroupName(),
                                                    f.getCropType().getImageResId(),
                                                    Collections.emptyList() // icone alert → da fare
                                            )
                                    );
                                }
                            }

                            if (uiList.isEmpty()) {
                               rvFields.setVisibility(View.GONE);
                               emptyImage.setVisibility(View.VISIBLE);
                               emptyText.setVisibility(View.VISIBLE);
                            } else {
                                rvFields.setVisibility(View.VISIBLE);
                                emptyImage.setVisibility(View.GONE);
                                emptyText.setVisibility(View.GONE);
                            }
                            fieldsAdapter.submitList(uiList);
                        },
                        err -> {
                            android.util.Log.e("FieldsListFragment", "Errore DB", err);
                        }
                )
        );
    }

    private void showGroups() {
        emptyImage.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        rvFields.setVisibility(View.VISIBLE);
        showingGroups = true;

        btnFields.setChecked(false);
        btnFieldGroups.setChecked(true);
        btnAddField.setText("Aggiungi un nuovo gruppo");

        rvFields.setAdapter(groupsAdapter);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;

        vm = a.fieldsVM();
        cd.add(vm.getAllGroups().subscribe(
                groups -> {
                    List<GroupUiModel> uiList = new java.util.ArrayList<>();

                    for (GroupWithFields g : groups) {
                        uiList.add(new GroupUiModel(
                                0, // TODO: remove
                                g.getGroup().getName(),
                                g.getGroup().getDescription(),
                                R.drawable.ic_group_default,
                                Collections.emptyList() // TODO: list alert for group
                        ));
                    }

                    groupsAdapter.submitList(uiList);
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        cd.clear();
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
