package com.agrialert.ui.fields;

import android.os.Bundle;

import io.reactivex.rxjava3.core.Completable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.Alert;
import com.agrialert.viewmodel.AlertsViewModel;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Fragment that displays a confirmation dialog before deleting an agricultural field.
 * Upon confirmation, it clears associated alerts and deletes the field from the database.
 */
public class ConfirmDeleteFieldFragment extends Fragment {

    /** Container for managing RxJava subscriptions. */
    private final CompositeDisposable cd = new CompositeDisposable();

    /**
     * Default constructor for ConfirmDeleteFieldFragment.
     */
    public ConfirmDeleteFieldFragment() { }

    /**
     * Inflates the layout for the confirmation dialog.
     *
     * @param inflater           The LayoutInflater object.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Fragment's previous saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_delete_field, container, false);
    }

    /**
     * Sets up the confirmation and cancellation buttons.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Fragment's previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnConfirm = view.findViewById(R.id.btnConfirmDeleteField);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancelDeleteField);

        Bundle args = getArguments();
        assert(args != null);
        int fieldId = args.getInt("fieldId");

        // Set up confirmation logic
        btnConfirm.setOnClickListener(v -> {
            MainActivity a = (MainActivity) requireActivity();
            if (!a.vmsReady()) return;
            FieldsViewModel vm = a.fieldsVM();

            // First, clear all alert types associated with the field
            cd.add(vm.updateAlertsToField(fieldId, new ArrayList<>()).subscribe(() -> {
                // Then, fetch and delete the field itself
                cd.add(vm.getFieldById(fieldId).subscribe(f -> cd.add(vm.deleteField(f).subscribe(() -> {
                   Toast.makeText(requireContext(),
                           "Campo eliminato",
                           Toast.LENGTH_SHORT).show();

                   // Return to the FIELDS list
                   NavHostFragment.findNavController(ConfirmDeleteFieldFragment.this)
                           .navigate(R.id.fieldsListFragment);
               }))));
            }));
        });

        // Set up cancellation logic
        btnCancel.setOnClickListener(v ->
                NavHostFragment.findNavController(ConfirmDeleteFieldFragment.this)
                        .popBackStack());
    }

    /**
     * Clears RxJava disposables when the fragment's view is destroyed.
     */
    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
