package com.agrialert.ui.fields;

import android.os.Bundle;

import io.reactivex.rxjava3.core.Completable;
import kotlin.Pair;
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
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.Threshold;
import com.agrialert.viewmodel.AlertsViewModel;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ConfirmDeleteFieldFragment extends Fragment {
    private final CompositeDisposable cd = new CompositeDisposable();

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

        Bundle args = getArguments();
        assert(args != null);
        int fieldId = args.getInt("fieldId");

        btnConfirm.setOnClickListener(v -> {
            MainActivity a = (MainActivity) requireActivity();
            if (!a.vmsReady()) return;
            FieldsViewModel vm = a.fieldsVM();
            AlertsViewModel avm = a.alertsVM();


            cd.add(vm.updateAlertsToField(fieldId, new ArrayList<Pair<Integer, Threshold>>()).subscribe(() -> {
                cd.add(vm.getFieldById(fieldId).subscribe(f -> {
                    cd.add(avm.getAlertsFromField(fieldId).subscribe(alerts -> {
                        // before deleting field, delete alerts
                        List<Completable> comp = new ArrayList<>();
                        for(Alert alert : alerts) {
                            comp.add(avm.deleteAlert((int)alert.getId()));
                        }

                        Completable deleteAlerts = Completable.mergeArray(comp.toArray(new Completable[0]));
                        cd.add(deleteAlerts.subscribe(() -> {
                            cd.add(vm.deleteField(f).subscribe(() -> {
                                Toast.makeText(requireContext(),
                                        "Campo eliminato",
                                        Toast.LENGTH_SHORT).show();

                                // Torna alla lista CAMPI
                                NavHostFragment.findNavController(ConfirmDeleteFieldFragment.this)
                                        .navigate(R.id.fieldsListFragment);
                            }));
                        }));
                    }));
                }));
            }));
        });

        btnCancel.setOnClickListener(v ->
                NavHostFragment.findNavController(ConfirmDeleteFieldFragment.this)
                        .popBackStack());
    }

    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
