package com.agrialert.ui.fields.groups;

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

public class ConfirmDeleteGroupFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_delete_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmDelete);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelDelete);

        // Conferma eliminazione
        btnConfirm.setOnClickListener(v -> {
            // Per ora: solo il toast
            Toast.makeText(requireContext(),
                    "Gruppo eliminato",
                    Toast.LENGTH_SHORT).show();

            // Torna alla lista gruppi
            NavHostFragment.findNavController(ConfirmDeleteGroupFragment.this)
                    .popBackStack(R.id.fieldsListFragment,false);
        });

        // Annulla → torna a Visualizza gruppo
        btnCancel.setOnClickListener(v ->
                NavHostFragment.findNavController(ConfirmDeleteGroupFragment.this)
                        .popBackStack()
        );
    }
}
