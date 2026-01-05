package com.agrialert.ui.fields;

import static com.mapbox.maps.plugin.annotation.AnnotationsUtils.getAnnotations;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.api.ApiManager;
import com.agrialert.data_manager.CropType;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.viewmodel.FieldsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class EditFieldFragment extends Fragment {

    private TextInputLayout tilAddress, tilCropType, tilGroup;
    TextInputEditText edtAddress;
    AutoCompleteTextView ddlCrop;
    AutoCompleteTextView ddlGroup;
    PointAnnotationManager pointAnnotationManager;

    MapView mapView;
    private boolean coordsFromMap;

    private final CompositeDisposable cd = new CompositeDisposable();

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
        mapView = view.findViewById(R.id.mapView);
        coordsFromMap = false;

        Bundle args = getArguments();
        if (args == null) {
            Log.e("EditField", "field mancante: passalo come arg a EditFieldFragment!");
            return;
        }

        int fieldId = args.getInt("fieldId");

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        FieldsViewModel vm = a.fieldsVM();



        cd.add(vm.getFieldById(fieldId).subscribe(f -> {
            edtAddress.setText(f.getAddress());
            ddlCrop.setText(requireContext().getString(f.getCropType().getResourceId()), false);
            ddlGroup.setText(f.getGroupName(), false);

            mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .center(Point.fromLngLat(12.5, 42.5))
                        .zoom(5.0)
                        .build());

                AnnotationPlugin annotationPlugin = getAnnotations(mapView);
                pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, null);

                GesturesPlugin gesturesPlugin = getGestures(mapView);
                gesturesPlugin.addOnMapClickListener(point -> {
                    addOrUpdateMarker(point, f);
                    return true;
                });
            });

            setupDropdowns();

            btnEditAlerts.setOnClickListener(v -> {
                // Save field changes and go to set alerts
                if (!validateForm()) return;

                String address = edtAddress.getText().toString().trim();

                String selectedCropName = ddlCrop.getText().toString().trim();
                CropType selectedCrop = CropType.getFromName(selectedCropName, requireContext());

                String groupName = ddlGroup.getText().toString().trim();
                if (groupName.isEmpty()) groupName = "Default";

                if (!coordsFromMap) {
                    try {
                        String finalGroupName = groupName;
                        ApiManager.getCoordinatesFromAddress(address).subscribe(coords -> {
                            f.setLatitude(coords.first);
                            f.setLongitude(coords.second);
                            f.setAddress(address);
                            // TODO: change lat and lon with API
                            f.setGroupName(finalGroupName);
                            f.setCropType(selectedCrop);
                            cd.add(vm.updateField(f).subscribe(
                                    () -> {
                                        Bundle b = new Bundle();
                                        b.putInt("fieldId", (int)f.getId());

                                        NavHostFragment.findNavController(EditFieldFragment.this)
                                                .navigate(R.id.action_editField_to_setAlerts, b);
                                    }
                            ));
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String finalGroupName = groupName;
                    f.setAddress(address);
                    // TODO: change lat and lon with API
                    f.setGroupName(finalGroupName);
                    f.setCropType(selectedCrop);
                    cd.add(vm.updateField(f).subscribe(
                            () -> {
                                Bundle b = new Bundle();
                                b.putInt("fieldId", (int)f.getId());

                                NavHostFragment.findNavController(EditFieldFragment.this)
                                        .navigate(R.id.action_editField_to_setAlerts, b);
                            }
                    ));
                }
            });

            btnDeleteField.setOnClickListener(v -> {
                Bundle b = new Bundle();
                b.putInt("fieldId", (int)f.getId());

                NavHostFragment.findNavController(EditFieldFragment.this)
                        .navigate(R.id.action_editField_to_confirmDeleteField, b);
            });
        }));
    }

    private void addOrUpdateMarker(Point point, Field f) {
        // Rimuovi eventuali marker precedenti per averne solo uno sulla mappa
        pointAnnotationManager.deleteAll();

        // Crea il bitmap per l'icona del marker
        Bitmap markerBitmap = bitmapFromDrawableRes(getContext(), R.drawable.ic_location_pin);
        if (markerBitmap == null) {
            Log.e("AddFieldFragment", "Impossibile creare il bitmap per il marker");
            return;
        }

        // Definisci le opzioni per il nuovo marker
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(markerBitmap);

        // Crea e aggiungi il marker alla mappa
        pointAnnotationManager.create(pointAnnotationOptions);

        // Salva le coordinate del punto cliccato
        f.setLatitude(point.latitude());
        f.setLongitude(point.longitude());
        try {
            cd.add(ApiManager.getAddressFromCoordinates(point.latitude(), point.longitude()).subscribe(address -> {
                edtAddress.setText(address);
            }));
            coordsFromMap = true;
        } catch (IOException e) {
            Log.e("EditFieldFragment", "Error getting address", e);
            throw new RuntimeException(e);
        }
    }

    // Metodo helper per convertire un drawable in un Bitmap
    private Bitmap bitmapFromDrawableRes(Context context, @DrawableRes int resourceId) {
        if (context == null) return null;
        Drawable drawable = AppCompatResources.getDrawable(context, resourceId);
        if (drawable == null) return null;

        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private void setupDropdowns() {
        CropType[] cropTypes = CropType.values();

        List<String> cropTypeNames = new ArrayList<>();
        for (CropType crop : cropTypes) {
            cropTypeNames.add(requireContext().getString(crop.getResourceId()));
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

        cd.add(vm.getAllGroups().subscribe(groups -> {
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
        }));
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

    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
