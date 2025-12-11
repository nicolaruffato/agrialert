package com.agrialert.ui.fields;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
        MaterialButton btnSave = view.findViewById(R.id.btnSetAlerts);

        // Dati FINTI di esempio (poi verranno dal DB)
        edtAddress.setText("Via Verdirdi, 15 - Mestre (VE)");
        ddlCrop.setText("Ortaggi", false);
        ddlGroup.setText("Gruppo A", false);
        btnSave.setText("Salva campo");

        btnSave.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Campo aggiornato (finto, niente database ancora)",
                    Toast.LENGTH_SHORT).show();

            // Torna alla schermata Visualizza campo
            NavHostFragment.findNavController(EditFieldFragment.this)
                    .popBackStack();
        });
    }
}
