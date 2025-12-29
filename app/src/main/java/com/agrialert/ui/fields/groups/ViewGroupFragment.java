package com.agrialert.ui.fields.groups;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.disposables.Disposable;

public class ViewGroupFragment extends Fragment {

    private ImageView imgGroupIcon;
    private TextView txtGroupName;
    private TextView txtGroupDescription;
    private RecyclerView rvGroupFields;
    private MaterialButton btnEditGroup;
    private MaterialButton btnDeleteGroup;
    private MaterialToolbar toolbar;

    public ViewGroupFragment() {
        // costruttore vuoto richiesto
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
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
            Log.e("ViewGroup", "Grouppo mancante: passalo come arg a ViewFieldFragment!");
            return;
        }
        GroupUiModel group = args.getParcelable("group");


        // per ora dati finti
        txtGroupName.setText(group.name);
        txtGroupDescription.setText(group.description);
        imgGroupIcon.setImageResource(group.iconRes);

        // LISTA CAMPI DEL GRUPPO
        rvGroupFields.setLayoutManager(new LinearLayoutManager(requireContext()));
        FieldsAdapter adapter = new FieldsAdapter();
        rvGroupFields.setAdapter(adapter);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        FieldsViewModel vm = a.fieldsVM();
        Disposable test = vm.getGroupByName(group.name).subscribe(g -> {
          List<FieldUiModel> fields = new ArrayList<>();
          for(Field f : g.getFields()){
             fields.add(new FieldUiModel(
                 f.getId(),
                 f.getAddress(),
                 f.getCropType().name(), // TODO: call displayName?
                 f.getGroupName(),
                 f.getCropType().getImageResId(),
                 Collections.emptyList() // icone alert → da fare
             ));
          }

            adapter.submitList(fields);
        });


        // bottoni  / TODO bottone salva
        btnEditGroup.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.editGroupFragment)
        );

        btnDeleteGroup.setOnClickListener(v -> {
            NavHostFragment.findNavController(ViewGroupFragment.this)
                    .navigate(R.id.confirmDeleteGroupFragment);
        });

    }

    @Override
    public void onPause(){
        super.onPause();
        FieldsListFragment.forceGroupsTab=true;
    }
}
