package com.agrialert.ui.fields.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.agrialert.ui.fields.FieldUiModel;
import com.agrialert.ui.fields.FieldsAdapter;
import com.agrialert.ui.fields.FieldsListFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class ViewGroupFragment extends Fragment {

    private ImageView imgGroupIcon;
    private TextView txtGroupName;
    private TextView txtGroupDescription;
    private RecyclerView rvGroupFields;
    private MaterialButton btnEditGroup;
    private MaterialButton btnDeleteGroup;
    private MaterialToolbar toolbar;

    public ViewGroupFragment() {
        // costruttore vuoto richiesto
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // findView
        imgGroupIcon = view.findViewById(R.id.imgGroupIcon);
        txtGroupName = view.findViewById(R.id.txtGroupName);
        txtGroupDescription = view.findViewById(R.id.txtGroupDescription);
        rvGroupFields = view.findViewById(R.id.rvGroupFields);
        btnEditGroup = view.findViewById(R.id.btnEditGroup);
        btnDeleteGroup = view.findViewById(R.id.btnDeleteGroup);


        // per ora dati finti
        txtGroupName.setText("Gruppo A");
        txtGroupDescription.setText("Descrizione di esempio del gruppo A.");
        imgGroupIcon.setImageResource(R.drawable.ic_group_default);

        // LISTA CAMPI DEL GRUPPO
        rvGroupFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        FieldsAdapter adapter = new FieldsAdapter();
        rvGroupFields.setAdapter(adapter);
        adapter.submitList(getSampleFields());

        // bottoni  / TODO bottone salva
        btnEditGroup.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.editGroupFragment)
        );

        btnDeleteGroup.setOnClickListener(v -> {
            NavHostFragment.findNavController(ViewGroupFragment.this)
                    .navigate(R.id.confirmDeleteGroupFragment);
        });



    }

    @Override
    public void onPause(){
        super.onPause();
        FieldsListFragment.forceGroupsTab=true;
    }

    

    private List<FieldUiModel> getSampleFields() {
        return Arrays.asList(
                new FieldUiModel(
                        1L,
                        "Via Verdiridi, 15 - Mestre (VE)",
                        "Ortaggi",
                        "Gruppo: Prova",
                        R.drawable.ic_ortaggi,
                        Arrays.asList(
                                R.drawable.ic_alert_vento,
                                R.drawable.ic_alert_calore
                        )
                ),
                new FieldUiModel(
                        2L,
                        "Via Giallo, 15 - Mestre (VE)",
                        "Leguminose",
                        "Gruppo: Prova",
                        R.drawable.ic_leguminose,
                        Arrays.asList(
                                R.drawable.ic_alert_gelo
                        )
                ),
                new FieldUiModel(
                        3L,
                        "Via Torino, 154 - Martellago (VE)",
                        "Cereali",
                        "Gruppo: Prova",
                        R.drawable.ic_cereali,
                        Arrays.asList(
                                R.drawable.ic_alert_siccita
                        )
                )
        );
    }
}
