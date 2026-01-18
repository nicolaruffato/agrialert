package com.agrialert.ui.fields;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

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
import static com.mapbox.maps.plugin.annotation.AnnotationsUtils.getAnnotations;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AddFieldFragment extends Fragment {

    private final CompositeDisposable cd = new CompositeDisposable();

    // UI
    private TextInputLayout tilAddress;
    private TextInputLayout tilCrop;
    private TextInputEditText inputAddress;
    private AutoCompleteTextView dropCropType, dropGroup;
    private MaterialButton btnSaveField, btnSetAlerts;
    private int savedFieldId = -1;

    // Mapbox
    private MapView mapView;
    private PointAnnotationManager pointAnnotationManager;

    private FieldsViewModel vm;
    private double selectedLat = 0;
    private double selectedLng = 0;
    private boolean isFromMap = false;
    private Runnable searchRunnable = null;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public AddFieldFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_field, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- View Binding ---
        tilAddress = view.findViewById(R.id.tilAddress);
        inputAddress = view.findViewById(R.id.inputAddress);
        dropCropType = view.findViewById(R.id.dropCropType);
        dropGroup = view.findViewById(R.id.dropGroup);
        btnSaveField = view.findViewById(R.id.btnSaveField);
        btnSetAlerts = view.findViewById(R.id.btnSetAlerts);
        mapView = view.findViewById(R.id.mapView);
        tilCrop = view.findViewById(R.id.tilCropType);

        btnSetAlerts.setEnabled(false);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        vm = a.fieldsVM();

        if (vm.isFieldPending) {
            btnSetAlerts.setEnabled(true);
            btnSaveField.setEnabled(false);
        }

        view.post(this::setupDropdowns);
        setupListeners();

        mapView.getMapboxMap().loadStyle(Style.MAPBOX_STREETS, style -> {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(Point.fromLngLat(12.5, 42.5))
                    .zoom(5.0)
                    .build());

            AnnotationPlugin annotationPlugin = getAnnotations(mapView);
            pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, null);

            GesturesPlugin gesturesPlugin = getGestures(mapView);
            gesturesPlugin.addOnMapClickListener(point -> {
                cd.add(ApiManager.getAddressFromCoordinates(point.latitude(), point.longitude()).subscribe(address -> {
                    isFromMap = true;
                    inputAddress.setText(address);
                    if(address.isEmpty()) {
                        // inputAddress not found by API call
                        tilAddress.setError("Il punto selezionato non ha un indirizzo valido!");
                        pointAnnotationManager.deleteAll();
                    } else {
                        addOrUpdateMarker(point);
                    }
                }, throwable -> {
                    Toast.makeText(requireContext(), "Nessuna connessione a Internet!", Toast.LENGTH_LONG).show();
                    tilAddress.setError("Non riesco ad ottenere il punto selezionato");
                    pointAnnotationManager.deleteAll();
                }));
                return true;
            });
        });

    }

    private void addOrUpdateMarker(Point point) {
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
        selectedLat = point.latitude();
        selectedLng = point.longitude();
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


    // --- Dropdowns ---
    private void setupDropdowns() {
        inputAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(!isFromMap) {
                    searchRunnable = () -> cd.add(ApiManager.getCoordinatesFromAddress(inputAddress.getText().toString().trim()).subscribe(coords -> {
                        if (coords.first == null) {
                            tilAddress.setError("L'indirizzo non è stato trovato!");
                            pointAnnotationManager.deleteAll();
                        } else {
                            tilAddress.setError(null);
                            tilAddress.setErrorEnabled(false);
                            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                                    .center(Point.fromLngLat((double) coords.first, (double) coords.second))
                                    .zoom(12.0)
                                    .build());
                            addOrUpdateMarker(Point.fromLngLat((double) coords.first, (double) coords.second));
                        }
                    }, throwable -> {
                        Toast.makeText(requireContext(), "Nessuna connessione a Internet!", Toast.LENGTH_LONG).show();
                        tilAddress.setError("Non riesco ad ottenere l'indirizzo");
                        pointAnnotationManager.deleteAll();
                    }));
                    handler.postDelayed(searchRunnable, 2000);
                }
                isFromMap = false;
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
                tilAddress.setError(null);
                tilAddress.setErrorEnabled(false);
            }
        });

        // CropType Dropdown
        CropType[] cropTypes = CropType.values();
        List<String> cropTypeNames = new ArrayList<>();
        for (CropType crop : cropTypes) {
            cropTypeNames.add(requireContext().getString(crop.getResourceId()));
        }
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, cropTypeNames);
        dropCropType.setAdapter(cropAdapter);
        dropCropType.setOnItemClickListener((parent, view, position, id) -> {
            tilCrop.setError(null);
            tilCrop.setErrorEnabled(false);
        });

        // Group Dropdown
        cd.add(vm.getAllGroups().subscribe(groups -> {
            List<String> groupNames = new ArrayList<>();
            for (GroupWithFields group : groups) {
                groupNames.add(group.getGroup().getName());
            }
            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, groupNames);
            dropGroup.setAdapter(groupAdapter);
        }));
    }

    // --- Listeners ---
    private void setupListeners() {
        btnSaveField.setOnClickListener(v -> {
            if (!validateForm()) return;

            String address = inputAddress.getText().toString().trim();
            String selectedCropName = dropCropType.getText().toString().trim();
            CropType selectedCrop = CropType.getFromName(selectedCropName, requireContext());
            String groupName = dropGroup.getText().toString().trim();
            if (groupName.isEmpty()) groupName = "Default";

            Field field = new Field(address, selectedLat, selectedLng, groupName, selectedCrop);

            cd.add(vm.insertField(field)
                    .andThen(vm.getGroupByName(groupName))
                    .subscribe(groupWithFields -> {
                        int id = -1;
                        for (Field f : groupWithFields.getFields()) {
                            if (address.equals(f.getAddress()) && f.getLatitude() == selectedLat && f.getLongitude() == selectedLng) {
                                id = f.getId();
                                break;
                            }
                        }
                        if (id <= 0) {
                            Toast.makeText(requireContext(), "Il salvataggio non e' andato a buon fine", Toast.LENGTH_LONG).show();
                            return;
                        }
                        savedFieldId = id;
                        Toast.makeText(requireContext(), "Campo Salvato", Toast.LENGTH_SHORT).show();
                        btnSetAlerts.setEnabled(true);
                        btnSaveField.setEnabled(false);
                    }, err -> {
                        Log.e("AddField", "Error Saving Field", err);
                        Toast.makeText(requireContext(), "Error: " + err.getMessage(), Toast.LENGTH_LONG).show();
                    }));
        });

        btnSetAlerts.setOnClickListener(v -> {
            vm.isFieldPending = true;
            Bundle b = new Bundle();
            b.putInt("fieldId", savedFieldId);
            NavHostFragment.findNavController(this).navigate(R.id.setAlertsFragment, b);
        });
    }

    // --- Validation & Helpers ---
    private boolean validateForm() {
        boolean ok = true;
        if (TextUtils.isEmpty(inputAddress.getText()) || tilAddress.getError() != null) {
            tilAddress.setError("Devi inserire un indirizzo valido!");
            ok = false;
        }
        if (TextUtils.isEmpty(dropCropType.getText())) {
            tilCrop.setError("Devi selezionare una coltivazione!");
            ok = false;
        }
        return ok;
    }

    @Override
    public void onDestroyView() {
        cd.clear();
        handler.removeCallbacks(searchRunnable);
        super.onDestroyView();
    }
}