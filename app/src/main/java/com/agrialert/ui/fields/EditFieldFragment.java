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

/**
 * Fragment for editing an existing agricultural field.
 * Users can update the field's address, crop type, and group assignment,
 * or navigate to alert settings and field deletion.
 */
public class EditFieldFragment extends Fragment {

    /** Layout for the address input field. */
    private TextInputLayout tilAddress;
    /** EditText for entering or displaying the field's physical address. */
    private TextInputEditText edtAddress;
    /** Dropdown for selecting the crop type planted in the field. */
    private AutoCompleteTextView ddlCrop;
    /** Dropdown for selecting or changing the field's assigned group. */
    private AutoCompleteTextView ddlGroup;
    /** Manager for handling point annotations (markers) on the map. */
    private PointAnnotationManager pointAnnotationManager;

    /** Map view for selecting or displaying the field location. */
    private MapView mapView;
    /** Flag to prevent recursive search triggers when address is set from map interaction. */
    private boolean isFromMap = false;

    /** Runnable for delayed geocoding searches to avoid excessive API calls. */
    private Runnable searchRunnable = null;
    /** Handler for managing UI delayed tasks. */
    private final Handler handler = new Handler(Looper.getMainLooper());

    /** Container for managing RxJava subscriptions and disposables. */
    private final CompositeDisposable cd = new CompositeDisposable();

    /**
     * Default constructor for EditFieldFragment.
     */
    public EditFieldFragment() { }

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_field, container, false);
    }

    /**
     * Initializes UI components, loads existing field data, and sets up Mapbox and button listeners.
     *
     * @param view               The inflated view.
     * @param savedInstanceState Fragment's previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        edtAddress = view.findViewById(R.id.inputAddress);
        ddlCrop = view.findViewById(R.id.dropCropType);
        ddlGroup = view.findViewById(R.id.dropGroup);
        tilAddress = view.findViewById(R.id.tilAddress);
        MaterialButton btnEditAlerts = view.findViewById(R.id.btnEditAlerts);
        mapView = view.findViewById(R.id.mapView);

        Bundle args = getArguments();
        if (args == null) {
            Log.e("EditField", "Missing field: pass it as an argument to EditFieldFragment!");
            return;
        }

        int fieldId = args.getInt("fieldId");

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        FieldsViewModel vm = a.fieldsVM();

        // Load existing field data
        cd.add(vm.getFieldById(fieldId).subscribe(f -> {
            edtAddress.setText(f.getAddress());
            ddlCrop.setText(requireContext().getString(f.getCropType().getResourceId()), false);
            ddlGroup.setText(f.getGroupName(), false);

            // Configure Mapbox
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

            // Handle edit alerts button click
            btnEditAlerts.setOnClickListener(v -> {
                if (!validateForm()) return;

                String address = edtAddress.getText().toString().trim();
                String selectedCropName = ddlCrop.getText().toString().trim();
                CropType selectedCrop = CropType.getFromName(selectedCropName, requireContext());
                String groupName = ddlGroup.getText().toString().trim();
                if (groupName.isEmpty()) groupName = "Default";

                f.setAddress(address);
                f.setGroupName(groupName);
                f.setCropType(selectedCrop);

                // Update field in database and navigate to alert settings
                cd.add(vm.updateField(f).subscribe(() -> {
                    Bundle b = new Bundle();
                    b.putInt("fieldId", f.getId());
                    NavHostFragment.findNavController(EditFieldFragment.this)
                            .navigate(R.id.action_editField_to_setAlerts, b);
                }));
            });
        }));
    }

    /**
     * Adds a marker to the map at the specified point or updates the existing one.
     * Updates the field object with the new geographic coordinates.
     *
     * @param point The geographic coordinates for the marker.
     * @param f     The field object to update.
     */
    private void addOrUpdateMarker(Point point, Field f) {
        // Remove previous markers to ensure only one is present on the map
        pointAnnotationManager.deleteAll();

        // Create bitmap for the marker icon
        Bitmap markerBitmap = bitmapFromDrawableRes(getContext(), R.drawable.ic_location_pin);
        if (markerBitmap == null) {
            Log.e("EditFieldFragment", "Unable to create marker bitmap");
            return;
        }

        // Define marker options
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(markerBitmap);

        // Create and add the marker to the map
        pointAnnotationManager.create(pointAnnotationOptions);

        // Save the coordinates of the clicked point
        f.setLatitude(point.latitude());
        f.setLongitude(point.longitude());
    }

    /**
     * Converts a drawable resource into a Bitmap.
     *
     * @param context    The context.
     * @param resourceId The resource ID of the drawable.
     * @return The resulting Bitmap, or null if conversion fails.
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
     * Configures dropdown menus for crop types and groups, and sets up the address search listener.
     *
     * @param f The field object whose data is used to initialize the UI.
     */
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

        // Initialize crop type dropdown
        CropType[] cropTypes = CropType.values();
        List<String> cropTypeNames = new ArrayList<>();
        for (CropType crop : cropTypes) {
            cropTypeNames.add(requireContext().getString(crop.getResourceId()));
        }
        ArrayAdapter<String> cropAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, cropTypeNames);
        ddlCrop.setAdapter(cropAdapter);

        // Load and initialize group dropdown
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

    /**
     * Validates the form data before updating.
     *
     * @return True if the form is valid, false otherwise.
     */
    private boolean validateForm() {
        boolean ok = true;
        String address = textOrEmpty(edtAddress);
        if (TextUtils.isEmpty(address) || tilAddress.getError() != null) {
            tilAddress.setError("Devi inserire un indirizzo valido!");
            ok = false;
        }
        return ok;
    }

    /**
     * Helper method to safely extract text from an EditText.
     *
     * @param edit The EditText view.
     * @return The trimmed text or an empty string if null.
     */
    private String textOrEmpty(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    /**
     * Clears RxJava disposables and removes pending search callbacks when the fragment is destroyed.
     */
    @Override
    public void onDestroyView() {
        cd.clear();
        handler.removeCallbacks(searchRunnable);
        super.onDestroyView();
    }
}
