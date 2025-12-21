package com.agrialert.ui.fields;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class EditFieldFragment extends Fragment {

    public EditFieldFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_field, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText edtAddress = view.findViewById(R.id.inputAddress);
        AutoCompleteTextView ddlCrop = view.findViewById(R.id.dropCropType);
        AutoCompleteTextView ddlGroup = view.findViewById(R.id.dropGroup);
        MaterialButton btnEditAlerts = view.findViewById(R.id.btnEditAlerts);
        MaterialButton btnDeleteField = view.findViewById(R.id.btnDeleteField);

        setupDropdowns(ddlCrop, ddlGroup);

        btnEditAlerts.setOnClickListener(v -> {
            NavHostFragment.findNavController(EditFieldFragment.this)
                    .navigate(R.id.action_editField_to_setAlerts);
        });

        btnDeleteField.setOnClickListener(v -> {
            NavHostFragment.findNavController(EditFieldFragment.this)
                    .navigate(R.id.action_editField_to_confirmDeleteField);
        });

        // Dati FINTI di esempio (poi verranno dal DB)
        edtAddress.setText("Via Verdirdi, 15 - Mestre (VE)");
        ddlCrop.setText("Ortaggi", false);
        ddlGroup.setText("Gruppo A", false);

    }

    private void setupDropdowns(AutoCompleteTextView ddlCrop, AutoCompleteTextView ddlGroup) {

        ArrayAdapter<CharSequence> cropAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.crop_types,
                android.R.layout.simple_list_item_1
        );
        ddlCrop.setAdapter(cropAdapter);

        List<String> groupNames = getUserGroups(); // finti per ora
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                groupNames
        );
        ddlGroup.setAdapter(groupAdapter);
    }

    private List<String> getUserGroups() {
        // TODO: sostituire con i gruppi reali salvati dall'utente
        return Arrays.asList(
                "Gruppo A",
                "Gruppo B",
                "Inserisci nuovo gruppo"
        );
    }
}
