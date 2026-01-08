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

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.FieldsGroup;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AddGroupFragment extends Fragment {

    private ImageView imgGroupIcon;
    private TextInputLayout layoutGroupName;
    private TextInputEditText edtGroupName, edtDescription;
    private RecyclerView rvFields;
    private MaterialButton btnSaveGroup;

    private GroupFieldsAdapter adapter;
    private List<GroupFieldUiModel> fields = new ArrayList<>();
    CompositeDisposable cd = new CompositeDisposable();
    FieldsViewModel vm;

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

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        vm = a.fieldsVM();

        cd.add(
                vm.getAllGroups().subscribe(
                        groups -> {
                            for (GroupWithFields g : groups) {
                                if (g.getFields() == null) continue;

                                for (Field f : g.getFields()) {
                                    fields.add(
                                            new GroupFieldUiModel(
                                                    f.getId(),
                                                    f.getCropType().getImageResId(),
                                                    f.getAddress(),
                                                    requireContext().getString(f.getCropType().getResourceId()), // TODO: call displayName?
                                                    f.getGroupName(),
                                                    false
                                            )
                                    );
                                }
                            }
                            adapter = new GroupFieldsAdapter(fields);
                            rvFields.setAdapter(adapter);
                        },
                        err -> {
                            android.util.Log.e("FieldsListFragment", "Errore DB", err);
                        }
                )
        );

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

        cd.add(vm.getAllGroups().firstOrError().subscribe(groups -> {
            for (GroupWithFields g : groups) {
                if (g.getGroup().getName().equals(name)) {
                    layoutGroupName.setError("Nome già esistente");
                    return;
                }
            }

            cd.add(vm.insertGroup(new FieldsGroup(name, description)).subscribe(() -> {
                List<Completable> comp = new ArrayList<>();
                for (GroupFieldUiModel f : fields) {
                    if (f.selected) {
                        comp.add(vm.getFieldById((int) f.id).flatMapCompletable(field -> {
                            field.setGroupName(name);
                            return vm.updateField(field);
                        }));
                    }
                }
                Completable updateFields = Completable.mergeArray(comp.toArray(new Completable[0]));
                cd.add(updateFields.subscribe(() -> {
                    String msg = "Gruppo \"" + name + "\" salvato!";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();

                    // Torna alla schermata precedente (lista gruppi)
                    NavHostFragment.findNavController(AddGroupFragment.this)
                            .navigateUp();
                }));
            }));

        }));
    }

    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
