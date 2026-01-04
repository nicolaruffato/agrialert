package com.agrialert.ui.fields.groups;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class EditGroupFragment extends Fragment {

    private ImageView imgGroupIcon;
    private TextInputLayout layoutGroupName;
    private TextInputEditText edtGroupName;
    private TextInputEditText edtDescription;
    private RecyclerView rvFields;
    private MaterialButton btnSaveGroup;
    private GroupFieldsAdapter fieldsAdapter;
    private List<GroupFieldUiModel> fields;
    private FieldsViewModel vm;
    private final CompositeDisposable cd = new CompositeDisposable();

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
        layoutGroupName = view.findViewById(R.id.layoutGroupName);
        edtGroupName   = view.findViewById(R.id.edtGroupName);
        edtDescription = view.findViewById(R.id.edtDescription);
        rvFields       = view.findViewById(R.id.rvFields);
        btnSaveGroup   = view.findViewById(R.id.btnSaveGroup);

        Bundle args = getArguments();
        if (args == null) {
            Log.e("EditGroup", "Gruppo mancante: passalo come arg a EditGroupFragment!");
            return;
        }
        String groupName = args.getString("groupName");

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        vm = a.fieldsVM();

        cd.add(vm.getGroupByName(groupName).subscribe(g -> {
            edtGroupName.setText(g.getGroup().getName());
            edtDescription.setText(g.getGroup().getDescription());
            imgGroupIcon.setImageResource(R.drawable.ic_group_default);

            fields = new ArrayList<>();
            for(Field f : g.getFields()) {
                fields.add(new GroupFieldUiModel(
                            f.getId(),
                            f.getCropType().getImageResId(),
                            f.getAddress(),
                            requireContext().getString(f.getCropType().getResourceId()), // TODO: call displayName?
                            f.getGroupName(),
                            true
                        )
                );
            }

            rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));
            fieldsAdapter = new GroupFieldsAdapter(fields);
            rvFields.setAdapter(fieldsAdapter);

            // --- CLICK SU "SALVA GRUPPO" ---
            btnSaveGroup.setOnClickListener(v -> onSaveGroup(g));
        }));
    }

    private void onSaveGroup(GroupWithFields group) {
        layoutGroupName.setError(null);

        String name = edtGroupName.getText() != null ? edtGroupName.getText().toString().trim() : "";
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            layoutGroupName.setError("*Campo obbligatorio");
            return;
        }

        cd.add(vm.getAllGroups().subscribe(groups -> {
            for (GroupWithFields g : groups) {
                if (!group.getGroup().getName().equals(name) && g.getGroup().getName().equals(name)) {
                    layoutGroupName.setError("Nome già esistente");
                    return;
                }
            }
            // TODO: e' possibile modificare il nome del gruppo?
            //group.getGroup().setName(name);
            group.getGroup().setDescription(description);

            cd.add(vm.updateGroup(group.getGroup()).subscribe(() -> {
                List<Completable> comp = new ArrayList<>();

                for (GroupFieldUiModel f : fields) {
                    if(f.selected) {
                        comp.add(vm.getFieldById((int)f.id).flatMapCompletable(field -> {
                            field.setGroupName(name);
                            return vm.updateField(field);
                    }));
                    } else {
                        // Groups not selected are moved to Default
                        comp.add(vm.getFieldById((int)f.id).flatMapCompletable(field -> {
                            field.setGroupName("Default");
                            return vm.updateField(field);
                        }));
                    }
                }

                Completable updateFields = Completable.mergeArray(comp.toArray(new Completable[0]));
                cd.add(updateFields.subscribe(() -> {
                    String msg = "Gruppo \"" + name + "\" modificato con ";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();

                    // Torna alla schermata precedente (lista gruppi)
                    NavHostFragment.findNavController(EditGroupFragment.this)
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
