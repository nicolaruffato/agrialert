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

/**
 * Fragment for editing an existing field group.
 * Allows users to modify the group name, description, and manage the list of fields
 * associated with the group.
 */
public class EditGroupFragment extends Fragment {

    /** ImageView for the group's icon. */
    private ImageView imgGroupIcon;
    /** TextInputLayout for the group name input. */
    private TextInputLayout layoutGroupName;
    /** TextInputEditText for the group name input. */
    private TextInputEditText edtGroupName;
    /** TextInputEditText for the group description input. */
    private TextInputEditText edtDescription;
    /** RecyclerView displaying fields belonging to the group. */
    private RecyclerView rvFields;
    /** Button to save the modified group details. */
    private MaterialButton btnSaveGroup;
    /** Adapter for managing fields within the group. */
    private GroupFieldsAdapter fieldsAdapter;
    /** List of field UI models used by the adapter. */
    private List<GroupFieldUiModel> fields;
    /** ViewModel for field and group data management. */
    private FieldsViewModel vm;
    /** Container for managing RxJava disposables. */
    private final CompositeDisposable cd = new CompositeDisposable();

    /**
     * Inflates the fragment's layout.
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
        return inflater.inflate(R.layout.fragment_edit_group, container, false);
    }

    /**
     * Initializes UI components and loads the group data based on arguments.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Saved state if being reconstructed.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        imgGroupIcon   = view.findViewById(R.id.imgGroupIcon);
        layoutGroupName = view.findViewById(R.id.layoutGroupName);
        edtGroupName   = view.findViewById(R.id.edtGroupName);
        edtDescription = view.findViewById(R.id.edtDescription);
        rvFields       = view.findViewById(R.id.rvFields);
        btnSaveGroup   = view.findViewById(R.id.btnSaveGroup);

        Bundle args = getArguments();
        if (args == null) {
            Log.e("EditGroup", "Missing group: pass it as an argument to EditGroupFragment!");
            return;
        }
        String groupName = args.getString("groupName");

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        vm = a.fieldsVM();

        // Load group details and associated fields from database
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
                            requireContext().getString(f.getCropType().getResourceId()),
                            f.getGroupName(),
                            true
                        )
                );
            }

            rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));
            fieldsAdapter = new GroupFieldsAdapter(fields);
            rvFields.setAdapter(fieldsAdapter);

            // Save button listener
            btnSaveGroup.setOnClickListener(v -> onSaveGroup(g));
        }));
    }

    /**
     * Validates input and updates the group and its associated fields in the database.
     *
     * @param group The current group with its fields.
     */
    private void onSaveGroup(GroupWithFields group) {
        layoutGroupName.setError(null);

        String name = edtGroupName.getText() != null ? edtGroupName.getText().toString().trim() : "";
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            layoutGroupName.setError("*Required field");
            return;
        }

        // Check for group name uniqueness if name was changed
        cd.add(vm.getAllGroups().subscribe(groups -> {
            for (GroupWithFields g : groups) {
                if (!group.getGroup().getName().equals(name) && g.getGroup().getName().equals(name)) {
                    layoutGroupName.setError("Group name already exists");
                    return;
                }
            }
            
            group.getGroup().setDescription(description);

            // Update group and field assignments
            cd.add(vm.updateGroup(group.getGroup()).subscribe(() -> {
                List<Completable> comp = new ArrayList<>();

                for (GroupFieldUiModel f : fields) {
                    if(f.selected) {
                        comp.add(vm.getFieldById((int)f.id).flatMapCompletable(field -> {
                            field.setGroupName(name);
                            return vm.updateField(field);
                    }));
                    } else {
                        // Fields not selected are moved to Default group
                        comp.add(vm.getFieldById((int)f.id).flatMapCompletable(field -> {
                            field.setGroupName("Default");
                            return vm.updateField(field);
                        }));
                    }
                }

                Completable updateFields = Completable.mergeArray(comp.toArray(new Completable[0]));
                cd.add(updateFields.subscribe(() -> {
                    String msg = "Group \"" + name + "\" updated!";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();

                    // Go back to the previous screen
                    NavHostFragment.findNavController(EditGroupFragment.this)
                            .navigateUp();
                }));
            }));
        }));
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
