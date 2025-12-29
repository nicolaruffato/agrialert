package com.agrialert.ui.fields.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public class GroupsListFragment extends Fragment implements GroupsAdapter.OnGroupClickListener {

    private RecyclerView rvGroups;
    private MaterialButton btnAddGroup;
    private GroupsAdapter adapter;

    public GroupsListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fields_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvGroups = view.findViewById(R.id.rvGroups);
        btnAddGroup = view.findViewById(R.id.btnAddGroup);
        /*btnAddGroup.setOnClickListener(v ->
                NavHostFragment.findNavController(GroupsListFragment.this)
                        .navigate(R.id.addGroupFragment)
        );*/
        rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GroupsAdapter( this);
        rvGroups.setAdapter(adapter);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        FieldsViewModel vm = a.fieldsVM();

        Disposable test = vm.getAllGroups().subscribe(groups -> {
            List<GroupUiModel> uiGroups = new ArrayList<>();
            for(GroupWithFields group : groups) {
                uiGroups.add(new GroupUiModel(
                   0,
                   group.getGroup().getName(),
                   group.getGroup().getDescription(),
                   R.drawable.ic_group_default,
                   Collections.emptyList()
                ));
            }
            adapter.submitList(uiGroups);
        });
    }

    public void onGroupClick(GroupUiModel group){
        Bundle b = new Bundle();
        b.putParcelable("group", group);
        NavHostFragment.findNavController(this)
                .navigate(R.id.viewGroupFragment, b);
    }
}
