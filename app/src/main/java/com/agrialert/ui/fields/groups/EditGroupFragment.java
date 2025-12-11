package com.agrialert.ui.fields.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class EditGroupFragment extends Fragment {

    private ImageView imgGroupIcon;
    private TextInputEditText edtGroupName;
    private TextInputEditText edtDescription;
    private RecyclerView rvFields;
    private MaterialButton btnSaveGroup;

    private GroupFieldsAdapter fieldsAdapter;
    private List<GroupFieldUiModel> fields;

    //  QUI gonfiamo il layout
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- TROVA LE VIEW ---
        imgGroupIcon   = view.findViewById(R.id.imgGroupIcon);
        edtGroupName   = view.findViewById(R.id.edtGroupName);
        edtDescription = view.findViewById(R.id.edtDescription);
        rvFields       = view.findViewById(R.id.rvFields);
        btnSaveGroup   = view.findViewById(R.id.btnSaveGroup);

        // --- DATI FINTI DEL GRUPPO (per ora) ---
        edtGroupName.setText("Gruppo A");
        edtDescription.setText("Descrizione finta del gruppo A...");
        imgGroupIcon.setImageResource(R.drawable.ic_group_default);

        // --- LISTA CAMPI DEL GRUPPO (FINTA) ---
        fields = createSampleFieldsForEdit();

        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        fieldsAdapter = new GroupFieldsAdapter(fields);
        rvFields.setAdapter(fieldsAdapter);

        // --- CLICK SU "SALVA GRUPPO" ---
        btnSaveGroup.setOnClickListener(v -> onSaveGroup());
    }

    private List<GroupFieldUiModel> createSampleFieldsForEdit() {
        List<GroupFieldUiModel> list = new ArrayList<>();

        list.add(new GroupFieldUiModel(
                1L,
                R.drawable.ic_ortaggi,
                "Via Verdirrì, 15 - Mestre (VE)",
                "Ortaggi",
                "Gruppo A",
                true
        ));

        list.add(new GroupFieldUiModel(
                2L,
                R.drawable.ic_cereali,
                "Via Giallo, 10 - Mestre (VE)",
                "Cereali",
                "Gruppo B",
                false
        ));

        list.add(new GroupFieldUiModel(
                3L,
                R.drawable.ic_frutteti,
                "Via Torino, 154 - Mestre (VE)",
                "Frutteti",
                "Gruppo C",
                false
        ));

        return list;
    }

    private void onSaveGroup() {
        // ---TOAST DI CONFERMA ---
        Toast.makeText(requireContext(),
                "Gruppo salvato (finto, per ora)",
                Toast.LENGTH_SHORT).show();
        // ---TORNA A VISUALIZZA GRUPPO---
        NavHostFragment.findNavController(EditGroupFragment.this)
                .navigate(R.id.viewGroupFragment);
    }
}
