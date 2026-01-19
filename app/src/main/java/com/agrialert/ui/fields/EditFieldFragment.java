package com.agrialert.ui.fields;

import static com.mapbox.maps.plugin.annotation.AnnotationsUtils.getAnnotations;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;

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
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class EditFieldFragment extends Fragment {

    private TextInputLayout tilAddress;
    TextInputEditText edtAddress;
    AutoCompleteTextView ddlCrop;
    AutoCompleteTextView ddlGroup;
    PointAnnotationManager pointAnnotationManager;

    MapView mapView;
    private boolean isFromMap = false;

    private Runnable searchRunnable = null;
    private final Handler handler = new Handler(Looper.getMainLooper());

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
        MaterialButton btnEditAlerts = view.findViewById(R.id.btnEditAlerts);
        mapView = view.findViewById(R.id.mapView);

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

            mapView.getMapboxMap().loadStyle(Style.MAPBOX_STREETS, style -> {
                mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                        .center(Point.fromLngLat(f.getLongitude(), f.getLatitude()))
                        .zoom(12.0)
                        .build());

                AnnotationPlugin annotationPlugin = getAnnotations(mapView);
                pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, null);

                addOrUpdateMarker(Point.fromLngLat(f.getLongitude(), f.getLatitude()), f);

                GesturesPlugin gesturesPlugin = getGestures(mapView);
                gesturesPlugin.addOnMapClickListener(point -> {
                    cd.add(ApiManager.getAddressFromCoordinates(point.latitude(), point.longitude()).subscribe(address -> {
                        isFromMap = true;
                        edtAddress.setText(address);
                        if(address.isEmpty()) {
                            // inputAddress not found by API call
                            tilAddress.setError("Il punto selezionato non ha un indirizzo valido!");
                            pointAnnotationManager.deleteAll();
                        } else {
                            addOrUpdateMarker(point, f);
                        }
                    }, throwable -> {
                        Toast.makeText(requireContext(), "Nessuna connessione a Internet!", Toast.LENGTH_LONG).show();
                        tilAddress.setError("Non riesco ad ottenere il punto selezionato");
                        pointAnnotationManager.deleteAll();
                    }));
                    return true;
                });
            });

            setupDropdowns(f);

            btnEditAlerts.setOnClickListener(v -> {
                // Save field changes and go to set alerts
                if (!validateForm()) return;

                String address = edtAddress.getText().toString().trim();

                String selectedCropName = ddlCrop.getText().toString().trim();
                CropType selectedCrop = CropType.getFromName(selectedCropName, requireContext());

                String groupName = ddlGroup.getText().toString().trim();
                if (groupName.isEmpty()) groupName = "Default";

                f.setAddress(address);
                f.setGroupName(groupName);
                f.setCropType(selectedCrop);
                cd.add(vm.updateField(f).subscribe(() -> {
                    Bundle b = new Bundle();
                    b.putInt("fieldId", f.getId());

                    NavHostFragment.findNavController(EditFieldFragment.this)
                            .navigate(R.id.action_editField_to_setAlerts, b);
                }));
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

    private void setupDropdowns(Field f) {
        edtAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(!isFromMap) {
                    searchRunnable = () -> cd.add(ApiManager.getCoordinatesFromAddress(edtAddress.getText().toString().trim()).subscribe(coords -> {
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
                            addOrUpdateMarker(Point.fromLngLat((double) coords.first, (double) coords.second), f);
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

        CropType[] cropTypes = CropType.values();

        List<String> cropTypeNames = new ArrayList<>();
        for (CropType crop : cropTypes) {
            cropTypeNames.add(requireContext().getString(crop.getResourceId()));
        }

        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, cropTypeNames);
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

        if (TextUtils.isEmpty(address) || tilAddress.getError() != null) {
            tilAddress.setError("Devi inserire un indirizzo valido!");
            ok = false;
        }

        return ok;
    }

    private String textOrEmpty(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        cd.clear();
        handler.removeCallbacks(searchRunnable);
        super.onDestroyView();
    }
}
