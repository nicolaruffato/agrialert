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

/**
 * Fragment responsible for creating a new field group.
 * It allows the user to specify a group name, description, and select which existing fields
 * should belong to this new group.
 */
public class AddGroupFragment extends Fragment {

    /** Icon representing the group. */
    private ImageView imgGroupIcon;
    /** Layout container for the group name input. */
    private TextInputLayout layoutGroupName;
    /** Edit text for entering the group name. */
    private TextInputEditText edtGroupName;
    /** Edit text for entering the group description. */
    private TextInputEditText edtDescription;
    /** RecyclerView displaying the list of available fields to add to the group. */
    private RecyclerView rvFields;
    /** Button to save the new group and update the associated fields. */
    private MaterialButton btnSaveGroup;

    /** Adapter for managing field selection in the group creation process. */
    private GroupFieldsAdapter adapter;
    /** List of field UI models used by the adapter. */
    private List<GroupFieldUiModel> fields = new ArrayList<>();
    /** Container for managing RxJava disposables. */
    private final CompositeDisposable cd = new CompositeDisposable();
    /** ViewModel for field and group management. */
    private FieldsViewModel vm;

    /**
     * Default empty constructor.
     */
    public AddGroupFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_group, container, false);
    }

    /**
     * Initializes UI components and loads existing fields into the selection list.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Saved state if being reconstructed.
     */
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

        // Load all existing fields and categorize them as available for group assignment
        cd.add(
                vm.getAllGroups().subscribe(
                        groups -> {
                            fields.clear();
                            for (GroupWithFields g : groups) {
                                if (g.getFields() == null) continue;

                                for (Field f : g.getFields()) {
                                    fields.add(
                                            new GroupFieldUiModel(
                                                    f.getId(),
                                                    f.getCropType().getImageResId(),
                                                    f.getAddress(),
                                                    requireContext().getString(f.getCropType().getResourceId()),
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
                            android.util.Log.e("AddGroupFragment", "Database error", err);
                        }
                )
        );

        imgGroupIcon.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Icon selection (to be implemented)", Toast.LENGTH_SHORT).show()
        );

        btnSaveGroup.setOnClickListener(v -> onSaveGroup());
    }

    /**
     * Validates the input and saves the new group to the database.
     * Also updates the group assignment for any selected fields.
     */
    private void onSaveGroup() {
        layoutGroupName.setError(null);

        String name = edtGroupName.getText() != null ? edtGroupName.getText().toString().trim() : "";
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            layoutGroupName.setError("*Required field");
            return;
        }

        // Check if group name already exists
        cd.add(vm.getAllGroups().firstOrError().subscribe(groups -> {
            for (GroupWithFields g : groups) {
                if (g.getGroup().getName().equalsIgnoreCase(name)) {
                    layoutGroupName.setError("Group name already exists");
                    return;
                }
            }

            // Insert new group
            cd.add(vm.insertGroup(new FieldsGroup(name, description)).subscribe(() -> {
                List<Completable> comp = new ArrayList<>();
                // Prepare updates for selected fields
                for (GroupFieldUiModel f : fields) {
                    if (f.selected) {
                        comp.add(vm.getFieldById((int) f.id).flatMapCompletable(field -> {
                            field.setGroupName(name);
                            return vm.updateField(field);
                        }));
                    }
                }

                // Execute all field updates and navigate back upon completion
                Completable updateFields = Completable.mergeArray(comp.toArray(new Completable[0]));
                cd.add(updateFields.subscribe(() -> {
                    String msg = "Group \"" + name + "\" saved!";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();

                    // Go back to the previous screen (groups list)
                    NavHostFragment.findNavController(AddGroupFragment.this)
                            .navigateUp();
                }));
            }));

        }));
    }

    /**
     * Clears RxJava disposables when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
