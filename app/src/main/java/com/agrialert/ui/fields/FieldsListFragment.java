package com.agrialert.ui.fields;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.agrialert.ui.fields.groups.GroupUiModel;
import com.agrialert.ui.fields.groups.GroupsAdapter;
import com.agrialert.MainActivity;
import com.agrialert.viewmodel.FieldsViewModel;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.data_manager.Field;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

import com.google.android.material.button.MaterialButton;

import androidx.navigation.fragment.NavHostFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fragment that displays a list of agricultural fields or field groups.
 * Users can switch between the two views, view details for individual items,
 * and navigate to screens for adding new fields or groups.
 */
public class FieldsListFragment extends Fragment
        implements GroupsAdapter.OnGroupClickListener, FieldsAdapter.OnFieldClickListener  {

    /** When true, the "Groups" tab is forcibly selected upon the next resume. */
    public static boolean forceGroupsTab = false;

    /** Button to switch to the fields view. */
    private MaterialButton btnFields;
    /** Button to switch to the field groups view. */
    private MaterialButton btnFieldGroups;
    /** Button to add a new field or group, depending on the current tab. */
    private MaterialButton btnAddField;
    /** RecyclerView for displaying either fields or groups. */
    private RecyclerView rvFields;
    /** Image to display when there's no field */
    private ImageView emptyImage;
    /** Text to display when there's no field */
    private TextView emptyText;

    /** Current view state: FALSE = Fields, TRUE = Field Groups. */
    private boolean showingGroups = false;

    /** Adapter for the fields list. */
    private FieldsAdapter fieldsAdapter;
    /** Adapter for the field groups list. */
    private GroupsAdapter groupsAdapter;
    /** ViewModel for field and group data management. */
    private FieldsViewModel vm;
    /** Container for managing RxJava disposables. */
    private final CompositeDisposable cd = new CompositeDisposable();

    /**
     * Default empty constructor.
     */
    public FieldsListFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fields_list, container, false);
    }

    /**
     * Initializes UI components, listeners, and sets the initial view state.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Fragment's previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Manual view binding
        btnFields = view.findViewById(R.id.btnFields);
        btnFieldGroups = view.findViewById(R.id.btnFieldGroups);
        btnAddField = view.findViewById(R.id.btnAddField);
        rvFields = view.findViewById(R.id.rvFields);
        emptyImage = view.findViewById(R.id.emptyImage);
        emptyText = view.findViewById(R.id.emptyText);

        rvFields.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize adapters
        fieldsAdapter = new FieldsAdapter(this);
        rvFields.setAdapter(fieldsAdapter);
        groupsAdapter = new GroupsAdapter(this);

        // Set up toggle between Fields and Groups
        btnFields.setOnClickListener(v -> showFields());
        btnFieldGroups.setOnClickListener(v -> showGroups());

        // Set up "Add" button logic based on the active tab
        btnAddField.setOnClickListener(v -> {
            if (showingGroups) {
                // Tab: Field Groups
                NavHostFragment.findNavController(FieldsListFragment.this)
                        .navigate(R.id.addGroupFragment);
            } else {
                // Tab: Fields
                NavHostFragment.findNavController(FieldsListFragment.this)
                        .navigate(R.id.addFieldFragment);
            }
        });

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        vm = a.fieldsVM();

        // Handle navigation fallback from field creation process
        vm.isFieldPending = false;

        // Default initial view: Fields

        btnFieldGroups.setChecked(false);
        btnFields.setChecked(true);

        view.post(() -> {
           if(btnFields.isChecked()){
               showFields();
           } else {
               showGroups();
           }
        });
    }

    /**
     * Navigates to the field details screen.
     *
     * @param field The UI model of the field that was clicked.
     */
    @Override
    public void onFieldClick(FieldUiModel field) {
        Bundle b = new Bundle();
        b.putParcelable("field", field);

        NavHostFragment.findNavController(this)
                .navigate(R.id.viewFieldFragment, b);
    }

    /**
     * Navigates to the group details screen.
     *
     * @param group The UI model of the group that was clicked.
     */
    @Override
    public void onGroupClick(GroupUiModel group){
        Bundle b = new Bundle();
        b.putParcelable("group", group);
        NavHostFragment.findNavController(this)
                .navigate(R.id.viewGroupFragment, b);
    }

    /**
     * Checks if the groups tab should be forced on resume.
     */
    @Override
    public void onResume(){
       super.onResume();
       // If "Groups" toggle was marked for forcing, show groups list
        if (forceGroupsTab){
            showGroups();
            forceGroupsTab = false;
        }
    }

    // -------------------- UI Helpers --------------------

    /**
     * Configures the UI to display the list of individual fields.
     */
    private void showFields() {
        showingGroups = false;

        btnFields.setChecked(true);
        btnFieldGroups.setChecked(false);
        btnAddField.setText("Aggiungi un nuovo campo");

        rvFields.setAdapter(fieldsAdapter);

        cd.clear();
        cd.add(
                vm.getAllGroups().subscribe(
                        groups -> {
                            List<FieldUiModel> uiList = new java.util.ArrayList<>();

                            for (GroupWithFields g : groups) {
                                if (g.getFields() == null) continue;

                                for (Field f : g.getFields()) {
                                    uiList.add(
                                            new FieldUiModel(
                                                    f.getId(),
                                                    f.getAddress(),
                                                    f.getLatitude(),
                                                    f.getLongitude(),
                                                    requireContext().getString(f.getCropType().getResourceId()),
                                                    f.getGroupName(),
                                                    f.getCropType().getImageResId(),
                                                    Collections.emptyList() // alert icons → TODO
                                            )
                                    );
                                }
                            }

                            if (uiList.isEmpty()) {
                               rvFields.setVisibility(View.GONE);
                               emptyImage.setVisibility(View.VISIBLE);
                               emptyText.setVisibility(View.VISIBLE);
                            } else {
                                rvFields.setVisibility(View.VISIBLE);
                                emptyImage.setVisibility(View.GONE);
                                emptyText.setVisibility(View.GONE);
                            }
                            fieldsAdapter.submitList(uiList);
                        },
                        err -> {
                            android.util.Log.e("FieldsListFragment", "Database Error", err);
                        }
                )
        );
    }

    /**
     * Configures the UI to display the list of field groups.
     */
    private void showGroups() {
        emptyImage.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        rvFields.setVisibility(View.VISIBLE);
        showingGroups = true;

        btnFields.setChecked(false);
        btnFieldGroups.setChecked(true);
        btnAddField.setText("Aggiungi un nuovo gruppo");

        rvFields.setAdapter(groupsAdapter);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;

        vm = a.fieldsVM();
        cd.add(vm.getAllGroups().subscribe(
                groups -> {
                    List<GroupUiModel> uiList = new java.util.ArrayList<>();

                    for (GroupWithFields g : groups) {
                        uiList.add(new GroupUiModel(
                                0, // TODO: remove
                                g.getGroup().getName(),
                                g.getGroup().getDescription(),
                                R.drawable.ic_group_default,
                                Collections.emptyList() // TODO: fetch alert list for group
                        ));
                    }

                    groupsAdapter.submitList(uiList);
                }));
    }

    /**
     * Clears disposables when the fragment is stopped.
     */
    @Override
    public void onStop() {
        super.onStop();
        cd.clear();
    }
}
