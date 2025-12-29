package com.agrialert.ui.fields;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.CropType;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;

public class EditFieldFragment extends Fragment {

    private TextInputLayout tilAddress, tilCropType, tilGroup;
    TextInputEditText edtAddress;
    AutoCompleteTextView ddlCrop;
    AutoCompleteTextView ddlGroup;

    public EditFieldFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_field, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        edtAddress = view.findViewById(R.id.inputAddress);
        ddlCrop = view.findViewById(R.id.dropCropType);
        ddlGroup = view.findViewById(R.id.dropGroup);
        tilAddress = view.findViewById(R.id.tilAddress);
        tilCropType = view.findViewById(R.id.tilCropType);
        tilGroup = view.findViewById(R.id.tilGroup);
        MaterialButton btnEditAlerts = view.findViewById(R.id.btnEditAlerts);
        MaterialButton btnDeleteField = view.findViewById(R.id.btnDeleteField);

        setupDropdowns(ddlCrop, ddlGroup);

        Bundle args = getArguments();
        if (args == null) {
            Log.e("EditField", "field mancante: passalo come arg a EditFieldFragment!");
            return;
        }
        FieldUiModel uiField = args.getParcelable("field");

        btnEditAlerts.setOnClickListener(v -> {
            // Save field changes and go to set alerts
            MainActivity a = (MainActivity) requireActivity();
            if (!a.vmsReady()) return;
            FieldsViewModel vm = a.fieldsVM();

            if (!validateForm()) return;

            String address = edtAddress.getText().toString().trim();

            String selectedCropName = ddlCrop.getText().toString().trim();
            CropType selectedCrop = CropType.valueOf(selectedCropName);

            String groupName = ddlGroup.getText().toString().trim();
            if (groupName.isEmpty()) groupName = "default";


            Field field = new Field(address, uiField.latitude, uiField.longitude, groupName, selectedCrop);

            // TODO: Subscribe, then navigate to set alerts?
            vm.updateField(field);

            NavHostFragment.findNavController(EditFieldFragment.this)
                    .navigate(R.id.action_editField_to_setAlerts);
        });

        btnDeleteField.setOnClickListener(v -> {
            NavHostFragment.findNavController(EditFieldFragment.this)
                    .navigate(R.id.action_editField_to_confirmDeleteField);
        });

        edtAddress.setText(uiField.address);
        ddlCrop.setText(uiField.crop, false);
        ddlGroup.setText(uiField.groupName, false);

    }

    private void setupDropdowns(AutoCompleteTextView ddlCrop, AutoCompleteTextView ddlGroup) {

        CropType[] cropTypes = CropType.values();

        List<String> cropTypeNames = new ArrayList<>();
        for (CropType crop : cropTypes) {
            cropTypeNames.add(crop.name());
        }

        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                cropTypeNames
        );
        ddlCrop.setAdapter(cropAdapter);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        FieldsViewModel vm = a.fieldsVM();

        Disposable test = vm.getAllGroups().subscribe(groups -> {
            List<String> groupNames = new ArrayList<>();
            for(GroupWithFields group : groups) {
                groupNames.add(group.getGroup().getName());
            }

            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    groupNames
            );
            ddlGroup.setAdapter(groupAdapter);
        });
    }

    private boolean validateForm() {
        boolean ok = true;

        String address = textOrEmpty(edtAddress);
        String crop = textOrEmpty(ddlCrop);
        String group = textOrEmpty(ddlGroup);

        if (TextUtils.isEmpty(address)) {
            tilAddress.setError("*Campo obbligatorio");
            ok = false;
        } else {
            tilAddress.setError(null);
        }

        if (TextUtils.isEmpty(crop)) {
            tilCropType.setError("*Campo obbligatorio");
            ok = false;
        } else {
            tilCropType.setError(null);
        }

        return ok;
    }

    private String textOrEmpty(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }
    private String textOrEmpty(AutoCompleteTextView edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }
}
