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
import com.agrialert.data_manager.Alert;
import com.agrialert.ui.fields.groups.GroupUiModel;
import com.agrialert.ui.fields.groups.GroupsAdapter;
import com.agrialert.MainActivity;
import com.agrialert.viewmodel.AlertsViewModel;
import com.agrialert.viewmodel.FieldsViewModel;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.data_manager.Field;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

import com.google.android.material.button.MaterialButton;

import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
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
    /** ViewModel for alerts data management. */
    private AlertsViewModel avm;
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
        avm = a.alertsVM();

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

        cd.add(io.reactivex.rxjava3.core.Observable.combineLatest(
                        vm.getAllGroups().firstOrError().toObservable(),
                        avm.getActiveAlerts().toObservable(),
                        (groups, alerts) -> {
                            // This function executes everytime that alerts change
                            List<FieldUiModel> uiFields = new ArrayList<>();

                            for (GroupWithFields group : groups) {
                                for (Field field : group.getFields()) {
                                    List<Integer> icons = new ArrayList<>();
                                    for (Alert alert : alerts) {
                                        if (icons.size() == 5) {
                                            break;
                                        }
                                        if (alert.getFieldId() == field.getId()) {
                                            icons.add(alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert);
                                        }
                                    }

                                    uiFields.add(new FieldUiModel(
                                                    field.getId(),
                                                    field.getAddress(),
                                                    requireContext().getString(field.getCropType().getResourceId()),
                                                    field.getGroupName(),
                                                    field.getCropType().getImageResId(),
                                                    icons
                                            )
                                    );
                                }
                            }
                            return uiFields;
                        })
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(uiFields -> {
                    if (uiFields.isEmpty()) {
                        emptyImage.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.VISIBLE);
                        rvFields.setVisibility(View.GONE);
                    } else {
                        emptyImage.setVisibility(View.GONE);
                        emptyText.setVisibility(View.GONE);
                        rvFields.setVisibility(View.VISIBLE);
                    }
                    fieldsAdapter.submitList(uiFields);
                }, throwable -> {
                    Log.e("FieldsListFragment", "Database Error");
                })
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

        cd.add(io.reactivex.rxjava3.core.Observable.combineLatest(
                        vm.getAllGroups().firstOrError().toObservable(),
                        avm.getActiveAlerts().toObservable(),
                        (groups, alerts) -> {
                            // This function executes everytime that alerts change
                            List<GroupUiModel> uiGroups = new ArrayList<>();
                            for (GroupWithFields group : groups) {
                                List<Integer> icons = new ArrayList<>();
                                for (Field field : group.getFields()) {
                                    for (Alert alert : alerts) {
                                        if (icons.size() == 5) {
                                            break;
                                        }
                                        if (alert.getFieldId() == field.getId()) {
                                            icons.add(alert.getIconRes() != 0 ? alert.getIconRes() : R.drawable.ic_alert);
                                        }
                                    }
                                }
                                uiGroups.add(new GroupUiModel(
                                                0,
                                                group.getGroup().getName(),
                                                group.getGroup().getDescription(),
                                                R.drawable.ic_group_default,
                                                icons
                                        )
                                );
                            }
                            return uiGroups;
                        })
                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(uiFields -> {
                    groupsAdapter.submitList(uiFields);
                }, throwable -> {
                    Log.e("FieldsListFragment", "Database Error");
                })
        );
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
