package com.agrialert.ui.fields.groups;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.Field;
import com.agrialert.ui.fields.FieldUiModel;
import com.agrialert.ui.fields.FieldsAdapter;
import com.agrialert.ui.fields.FieldsListFragment;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Fragment for viewing the details of a specific field group.
 * Displays the group name, description, and the list of fields associated with it.
 * Provides options to edit or delete the group (if it's not the Default group).
 */
public class ViewGroupFragment extends Fragment {

    /** ImageView for the group's icon. */
    private ImageView imgGroupIcon;
    /** TextView for the group name. */
    private TextView txtGroupName;
    /** TextView for the group description. */
    private TextView txtGroupDescription;
    /** RecyclerView for listing fields within the group. */
    private RecyclerView rvGroupFields;
    private ImageView noFieldsGroupImage;
    private TextView noFieldsGroupText;
    private final CompositeDisposable cd = new CompositeDisposable();

    /**
     * Default empty constructor.
     */
    public ViewGroupFragment() {
        // required empty constructor
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           The LayoutInflater object.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Fragment's previous saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_group, container, false);
    }

    /**
     * Initializes UI components and loads group data and its associated fields.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Saved state if being reconstructed.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // findView
        imgGroupIcon = view.findViewById(R.id.imgGroupIcon);
        txtGroupName = view.findViewById(R.id.txtGroupName);
        txtGroupDescription = view.findViewById(R.id.txtGroupDescription);
        rvGroupFields = view.findViewById(R.id.rvGroupFields);
        MaterialButton btnEditGroup = view.findViewById(R.id.btnEditGroup);
        MaterialButton btnDeleteGroup = view.findViewById(R.id.btnDeleteGroup);
        noFieldsGroupImage = view.findViewById(R.id.noFieldsGroupImage);
        noFieldsGroupText = view.findViewById(R.id.noFieldsGroupText);

        Bundle args = getArguments();
        if (args == null) {
            Log.e("ViewGroup", "Missing group: pass it as an argument to ViewGroupFragment!");
            return;
        }

        GroupUiModel group = args.getParcelable("group");

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        FieldsViewModel vm = a.fieldsVM();

        // Load group details
        cd.add(vm.getGroupByName(group.name).subscribe(g -> {
            txtGroupName.setText(g.getGroup().getName());
            txtGroupDescription.setText(g.getGroup().getDescription());
            imgGroupIcon.setImageResource(R.drawable.ic_group_default);
        }));

        // LIST OF FIELDS IN THE GROUP
        rvGroupFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        FieldsAdapter adapter = new FieldsAdapter();
        rvGroupFields.setAdapter(adapter);

        cd.add(vm.getGroupByName(group.name).subscribe(g -> {
            List<FieldUiModel> fields = new ArrayList<>();
            for (Field f : g.getFields()) {
                fields.add(new FieldUiModel(
                        f.getId(),
                        f.getAddress(),
                        requireContext().getString(f.getCropType().getResourceId()),
                        f.getGroupName(),
                        f.getCropType().getImageResId(),
                        Collections.emptyList()
                ));
            }
            if (fields.isEmpty()) {
               rvGroupFields.setVisibility(View.GONE);
               noFieldsGroupImage.setVisibility(View.VISIBLE);
               noFieldsGroupText.setVisibility(View.VISIBLE);
            } else {
               noFieldsGroupImage.setVisibility(View.GONE);
               noFieldsGroupText.setVisibility(View.GONE);
               rvGroupFields.setVisibility(View.VISIBLE);
            }
            adapter.submitList(fields);
        }));

        // Buttons configuration
        // The Default group cannot be edited or deleted
        if(group.name.equals("Default")) {
            btnDeleteGroup.setEnabled(false);
            btnEditGroup.setEnabled(false);
        }

        Bundle b = new Bundle();
        b.putString("groupName", group.name);

        btnEditGroup.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.editGroupFragment, b));
        btnDeleteGroup.setOnClickListener(v -> NavHostFragment.findNavController(ViewGroupFragment.this).navigate(R.id.confirmDeleteGroupFragment, b));
    }

    /**
     * Ensures that when returning to the fields list, the groups tab is selected.
     */
    @Override
    public void onPause() {
        super.onPause();
        FieldsListFragment.forceGroupsTab = true;
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
