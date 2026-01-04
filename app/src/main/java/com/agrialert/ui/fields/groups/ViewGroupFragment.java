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
import io.reactivex.rxjava3.disposables.Disposable;

public class ViewGroupFragment extends Fragment {

    private ImageView imgGroupIcon;
    private TextView txtGroupName;
    private TextView txtGroupDescription;
    private RecyclerView rvGroupFields;
    private MaterialButton btnEditGroup;
    private MaterialButton btnDeleteGroup;
    private final CompositeDisposable cd = new CompositeDisposable();

    public ViewGroupFragment() {
        // costruttore vuoto richiesto
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // findView
        imgGroupIcon = view.findViewById(R.id.imgGroupIcon);
        txtGroupName = view.findViewById(R.id.txtGroupName);
        txtGroupDescription = view.findViewById(R.id.txtGroupDescription);
        rvGroupFields = view.findViewById(R.id.rvGroupFields);
        btnEditGroup = view.findViewById(R.id.btnEditGroup);
        btnDeleteGroup = view.findViewById(R.id.btnDeleteGroup);

        Bundle args = getArguments();
        if (args == null) {
            Log.e("ViewGroup", "Gruppo mancante: passalo come arg a ViewFieldFragment!");
            return;
        }

        GroupUiModel group = args.getParcelable("group");

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        FieldsViewModel vm = a.fieldsVM();

        cd.add(vm.getGroupByName(group.name).subscribe(g -> {
            txtGroupName.setText(g.getGroup().getName());
            txtGroupDescription.setText(g.getGroup().getDescription());
            imgGroupIcon.setImageResource(R.drawable.ic_group_default);
        }));

        // LISTA CAMPI DEL GRUPPO
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
            adapter.submitList(fields);
        }));

        // Bottoni
        if(group.name.equals("Default")) {
            btnDeleteGroup.setEnabled(false);
            btnEditGroup.setEnabled(false);
        }

        Bundle b = new Bundle();
        b.putString("groupName", group.name);

        btnEditGroup.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.editGroupFragment, b));
        btnDeleteGroup.setOnClickListener(v -> NavHostFragment.findNavController(ViewGroupFragment.this).navigate(R.id.confirmDeleteGroupFragment, b));
    }

    @Override
    public void onPause() {
        super.onPause();
        FieldsListFragment.forceGroupsTab = true;
    }

    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
