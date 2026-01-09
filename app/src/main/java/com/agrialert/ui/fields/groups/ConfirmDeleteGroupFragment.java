package com.agrialert.ui.fields.groups;

import android.os.Bundle;
import android.util.Log;
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
import com.agrialert.data_manager.Field;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ConfirmDeleteGroupFragment extends Fragment {

    CompositeDisposable cd = new CompositeDisposable();

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

        Bundle args = getArguments();
        assert(args != null);
        String groupName = args.getString("groupName");

        // Conferma eliminazione
        btnConfirm.setOnClickListener(v -> {
            MainActivity a = (MainActivity) requireActivity();
            if (!a.vmsReady()) return;
            FieldsViewModel vm = a.fieldsVM();

            cd.add(vm.getGroupByName(groupName).subscribe(g -> {
                // Before deleting the group, move all the fields in it to the default group
                List<Completable> comp = new ArrayList<>();
                for(Field f : g.getFields()) {
                    f.setGroupName("Default");
                    comp.add(vm.updateField(f));
                }

                Completable moveFields = Completable.mergeArray(comp.toArray(new Completable[0]));
                cd.add(moveFields.subscribe(() -> {
                    cd.add(vm.deleteGroup(g.getGroup()).subscribe(() -> {
                        Toast.makeText(requireContext(),
                                "Gruppo eliminato",
                                Toast.LENGTH_SHORT).show();

                        // Torna alla lista gruppi
                        NavHostFragment.findNavController(ConfirmDeleteGroupFragment.this)
                                .popBackStack(R.id.fieldsListFragment,false);
                    }, err -> {
                        Log.e("DELETE", "Non sono riuscito a eliminare il gruppo", err);
                    }));
                }));
            }, err -> {
                Log.e("DELETE", "Il Gruppo che vuoi eliminare non è stato trovato!");
            }));
        });

        // Annulla → torna a Visualizza gruppo
        btnCancel.setOnClickListener(v ->
                NavHostFragment.findNavController(ConfirmDeleteGroupFragment.this)
                        .popBackStack()
        );
    }

    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
