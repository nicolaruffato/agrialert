package com.agrialert.ui.fields.groups;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

public class AddGroupFragment extends Fragment {

    private ImageView imgGroupIcon;
    private TextInputLayout layoutGroupName;
    private TextInputEditText edtGroupName, edtDescription;
    private RecyclerView rvFields;
    private MaterialButton btnSaveGroup;

    private GroupFieldsAdapter adapter;
    private List<GroupFieldUiModel> fields = new ArrayList<>();

    public AddGroupFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgGroupIcon = view.findViewById(R.id.imgGroupIcon);
        layoutGroupName = view.findViewById(R.id.layoutGroupName);
        edtGroupName = view.findViewById(R.id.edtGroupName);
        edtDescription = view.findViewById(R.id.edtDescription);
        rvFields = view.findViewById(R.id.rvFields);
        btnSaveGroup = view.findViewById(R.id.btnSaveGroup);

        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        fields = createSampleFieldsForGroup();
        adapter = new GroupFieldsAdapter(fields);
        rvFields.setAdapter(adapter);

        imgGroupIcon.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Selezione icona (da implementare)", Toast.LENGTH_SHORT).show()
        );

        btnSaveGroup.setOnClickListener(v -> onSaveGroup());
    }

    private void onSaveGroup() {
        layoutGroupName.setError(null);

        String name = edtGroupName.getText() != null ? edtGroupName.getText().toString().trim() : "";
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            layoutGroupName.setError("*Campo obbligatorio");
            return;
        }

        int selectedCount = 0;
        for (GroupFieldUiModel f : fields) {
            if (f.selected) selectedCount++;
        }

        String msg = "Gruppo \"" + name + "\" salvato con " + selectedCount + " campi";
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();

        // Torna alla schermata precedente (lista gruppi)
        NavHostFragment.findNavController(AddGroupFragment.this)
                .navigateUp();
    }


    // Per ora dati finti, in futuro arriveranno dal database
    private List<GroupFieldUiModel> createSampleFieldsForGroup() {
        List<GroupFieldUiModel> list = new ArrayList<>();
        list.add(new GroupFieldUiModel(
                1,
                R.drawable.ic_ortaggi,
                "Via Verdirdi, 15 - Mestre (VE)",
                "Ortaggi",
                "Prova",
                true
        ));
        list.add(new GroupFieldUiModel(
                2,
                R.drawable.ic_cereali,
                "Via Giallo, 15 - Mestre (VE)",
                "Cereali",
                "Prova",
                false
        ));
        list.add(new GroupFieldUiModel(
                3,
                R.drawable.ic_frutteti,
                "Via Torino, 154 - Martellago (VE)",
                "Frutteti",
                "Prova",
                false
        ));
        // aggiungi quanti campi demo vuoi
        return list;
    }
}
