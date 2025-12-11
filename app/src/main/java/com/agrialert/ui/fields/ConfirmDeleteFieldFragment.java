package com.agrialert.ui.fields;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;

public class ConfirmDeleteFieldFragment extends Fragment {

    public ConfirmDeleteFieldFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_delete_field, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmDeleteField);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelDeleteField);

        btnConfirm.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Campo eliminato (finto, niente database ancora)",
                    Toast.LENGTH_SHORT).show();

            // Torna alla lista CAMPI
            NavHostFragment.findNavController(ConfirmDeleteFieldFragment.this)
                    .popBackStack(R.id.fieldsListFragment, false);
        });

        btnCancel.setOnClickListener(v ->
                NavHostFragment.findNavController(ConfirmDeleteFieldFragment.this)
                        .popBackStack());
    }
}
