package com.agrialert.ui.fields.groups;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrialert.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupsListFragment extends Fragment {

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

        rvGroups.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GroupsAdapter();
        rvGroups.setAdapter(adapter);

        adapter.submitList(createSampleGroups());
    }

    private List<GroupUiModel> createSampleGroups() {
        return Arrays.asList(
                new GroupUiModel(
                        1,
                        "Gruppo A",
                        "Descrizione del gruppo A",
                        R.drawable.ic_group_default,
                        Arrays.asList(
                                R.drawable.ic_alert_vento,
                                R.drawable.ic_alert_calore,
                                R.drawable.ic_alert_gelo
                        )
                ),
                new GroupUiModel(
                        2,
                        "Gruppo B",
                        "Descrizione del gruppo B",
                        R.drawable.ic_group_default,
                        Arrays.asList(
                                R.drawable.ic_alert_pioggia,
                                R.drawable.ic_alert_temporale
                        )
                )
        );
    }

}
