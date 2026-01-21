package com.agrialert.ui.fields;

import android.Manifest;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.mapbox.common.location.DeviceLocationProvider;
import com.mapbox.common.location.LocationService;
import com.mapbox.common.location.LocationServiceFactory;
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

/**
 * Fragment for adding a new agricultural field to the system.
 * It allows users to specify an address (via manual input or map interaction),
 * select a crop type, and assign the field to a group.
 */
public class AddFieldFragment extends Fragment {

    /** Container for managing RxJava subscriptions. */
    private final CompositeDisposable cd = new CompositeDisposable();

    // --- UI Components ---
    /** Layout container for the address input. */
    private TextInputLayout tilAddress;
    /** Layout container for the crop type dropdown. */
    private TextInputLayout tilCrop;
    /** EditText for entering the field's physical address. */
    private TextInputEditText inputAddress;
    /** Dropdown for selecting the crop type planted in the field. */
    private AutoCompleteTextView dropCropType;
    /** Dropdown for selecting the field's group. */
    private AutoCompleteTextView dropGroup;
    /** Button to save the field information. */
    private MaterialButton btnSaveField;
    /** Button to proceed to alert configuration for the field. */
    private MaterialButton btnSetAlerts;
    /** Stores the ID of the field after it has been saved. */
    private int savedFieldId = -1;

    // --- Mapbox Components ---
    /** Map view for selecting the field location visually. */
    private MapView mapView;
    /** Manager for handling point annotations (markers) on the map. */
    private PointAnnotationManager pointAnnotationManager;

    /** ViewModel for field-related data operations. */
    private FieldsViewModel vm;
    /** The latitude of the selected location. */
    private double selectedLat = 0;
    /** The longitude of the selected location. */
    private double selectedLng = 0;
    /** Flag indicating if the current address was set via a map interaction. */
    private boolean isFromMap = false;
    /** Runnable for delayed geocoding searches based on text input. */
    private Runnable searchRunnable = null;
    /** Handler for managing UI-related delayed tasks. */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * Default constructor for AddFieldFragment.
     */
    public AddFieldFragment() {}

    /**
     * Inflates the layout for the fragment.
     *
     * @param inflater           The LayoutInflater object.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Fragment's previous saved state.
     * @return The View for the fragment's UI.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_field, container, false);
    }

    /**
     * Initializes UI components, listeners, and Mapbox functionality.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Fragment's previous saved state.
     */
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

        /*
        ActivityResultLauncher<String[]> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    boolean fineLocationStatus = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                    boolean coarseLocationStatus = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                    if (fineLocationStatus || coarseLocationStatus) {
                        LocationService locationService = LocationServiceFactory.getOrCreate();

                        DeviceLocationProvider locationProvider = locationService.getDeviceLocationProvider(null).getValue();
                        if(locationProvider != null) {
                            locationProvider.getLastLocation(location -> {
                                if(location != null) {
                                    mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                                            .center(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                                            .zoom(5.0)
                                            .build());
                                }
                            });
                        }
                    }
                });

        requestPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        */

        // --- Mapbox Setup ---
        mapView.getMapboxMap().loadStyle(Style.MAPBOX_STREETS, style -> {
            AnnotationPlugin annotationPlugin = getAnnotations(mapView);
            pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, null);

            LocationService locationService = LocationServiceFactory.getOrCreate();

            DeviceLocationProvider locationProvider = locationService.getDeviceLocationProvider(null).getValue();
            if(locationProvider != null) {
                locationProvider.getLastLocation(location -> {
                    if(location != null) {
                        mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                                .center(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                                .zoom(12.0)
                                .build());
                    }
                });
            }

            GesturesPlugin gesturesPlugin = getGestures(mapView);
            gesturesPlugin.addOnMapClickListener(point -> {
                cd.add(ApiManager.getAddressFromCoordinates(point.latitude(), point.longitude()).subscribe(address -> {
                    isFromMap = true;
                    inputAddress.setText(address);
                    if(address.isEmpty()) {
                        tilAddress.setError("The selected point does not have a valid address!");
                        pointAnnotationManager.deleteAll();
                    } else {
                        addOrUpdateMarker(point);
                    }
                }, throwable -> {
                    Toast.makeText(requireContext(), "No internet connection!", Toast.LENGTH_LONG).show();
                    tilAddress.setError("Unable to get the selected point");
                    pointAnnotationManager.deleteAll();
                }));
                return true;
            });
        });

    }

    /**
     * Adds a marker to the map at the specified point or updates the existing one.
     *
     * @param point The geographic coordinates for the marker.
     */
    private void addOrUpdateMarker(Point point) {
        // Remove previous markers to ensure only one is present
        pointAnnotationManager.deleteAll();

        // Create bitmap for the marker icon
        Bitmap markerBitmap = bitmapFromDrawableRes(getContext(), R.drawable.ic_location_pin);
        if (markerBitmap == null) {
            Log.e("AddFieldFragment", "Unable to create marker bitmap");
            return;
        }

        // Define marker options
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(markerBitmap);

        // Create and add the marker
        pointAnnotationManager.create(pointAnnotationOptions);

        // Store selected coordinates
        selectedLat = point.latitude();
        selectedLng = point.longitude();
    }

    private void loadCurrentLocation() {
        LocationService locationService = LocationServiceFactory.getOrCreate();

        DeviceLocationProvider locationProvider = locationService.getDeviceLocationProvider(null).getValue();
        if(locationProvider != null) {
            locationProvider.getLastLocation(expected -> {
                if(expected != null) {
                    cd.add(ApiManager.getAddressFromCoordinates(expected.getLatitude(), expected.getLongitude()).subscribe(address -> {
                        inputAddress.setText(address);
                        if(address.isEmpty()) {
                            tilAddress.setError("La tua posizione non ha un indirizzo valido");
                            pointAnnotationManager.deleteAll();
                        } else {
                            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                                    .center(Point.fromLngLat(expected.getLongitude(), expected.getLatitude()))
                                    .zoom(12.0)
                                    .build());
                            addOrUpdateMarker(Point.fromLngLat(expected.getLongitude(), expected.getLatitude()));
                        }
                    }, throwable -> {
                        Toast.makeText(requireContext(), "Nessuna connessione ad internet!", Toast.LENGTH_LONG).show();
                        tilAddress.setError("Non riesco ad ottenere l'indirizzo");
                        pointAnnotationManager.deleteAll();
                    }));
                } else {
                    tilAddress.setError("Non riesco ad ottenere la tua posizione");
                }
            });
        } else {
            Log.e("AddFieldFragment", "Unable to get location provider");
        }

    }


    /**
     * Converts a drawable resource into a Bitmap.
     *
     * @param context    The context.
     * @param resourceId The resource ID of the drawable.
     * @return The resulting Bitmap, or null if the conversion fails.
     */
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


    /**
     * Configures dropdown menus for crop types and field groups, and sets up address search logic.
     */
    private void setupDropdowns() {
        inputAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if(!isFromMap) {
                    searchRunnable = () -> cd.add(ApiManager.getCoordinatesFromAddress(inputAddress.getText().toString().trim()).subscribe(coords -> {
                        if (coords.first == null) {
                            tilAddress.setError("Address not found!");
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
                        Toast.makeText(requireContext(), "No internet connection!", Toast.LENGTH_LONG).show();
                        tilAddress.setError("Unable to get the address");
                        pointAnnotationManager.deleteAll();
                    }));
                    handler.postDelayed(searchRunnable, 500);
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

    /**
     * Sets up click listeners for the save and alert configuration buttons.
     */
    private void setupListeners() {
        tilAddress.setEndIconOnClickListener(v -> loadCurrentLocation());

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
                            Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_LONG).show();
                            return;
                        }
                        savedFieldId = id;
                        Toast.makeText(requireContext(), "Field saved", Toast.LENGTH_SHORT).show();
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

    /**
     * Validates the form data before saving.
     *
     * @return True if the form is valid, false otherwise.
     */
    private boolean validateForm() {
        boolean ok = true;
        if (TextUtils.isEmpty(inputAddress.getText()) || tilAddress.getError() != null) {
            tilAddress.setError("You must enter a valid address!");
            ok = false;
        }
        if (TextUtils.isEmpty(dropCropType.getText())) {
            tilCrop.setError("You must select a crop!");
            ok = false;
        }
        return ok;
    }

    /**
     * Clears RxJava disposables and removes pending search callbacks when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        cd.clear();
        handler.removeCallbacks(searchRunnable);
        super.onDestroyView();
    }
}
