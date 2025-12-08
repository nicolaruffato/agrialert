package com.agrialert.ui.fields;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agrialert.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

public class AddFieldFragment extends Fragment implements OnMapReadyCallback {

    private static final int REQ_LOCATION = 1001;

    // UI
    private TextInputLayout tilAddress, tilCropType, tilGroup;
    private TextInputEditText inputAddress;
    private AutoCompleteTextView dropCropType, dropGroup;
    private MaterialButton btnSetAlerts;
    private View mapContainer;

    // MAPPA
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private Marker marker;
    private FusedLocationProviderClient fusedLocationClient;

    public AddFieldFragment() {
        // costruttore vuoto richiesto da Fragment
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_field, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ----- COLLEGAMENTO VIEW -----
        tilAddress = view.findViewById(R.id.tilAddress);
        tilCropType = view.findViewById(R.id.tilCropType);
        tilGroup = view.findViewById(R.id.tilGroup);

        inputAddress = view.findViewById(R.id.inputAddress);
        dropCropType = view.findViewById(R.id.dropCropType);
        dropGroup = view.findViewById(R.id.dropGroup);
        btnSetAlerts = view.findViewById(R.id.btnSetAlerts);

        mapContainer = view.findViewById(R.id.mapContainer);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupDropdowns();
        setupListeners();
    }

    // ----------------------------------------------------
    // DROPDOWN
    // ----------------------------------------------------
    private void setupDropdowns() {
        // Tipologia campo da arrays.xml
        ArrayAdapter<CharSequence> cropAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.crop_types,
                android.R.layout.simple_list_item_1
        );
        dropCropType.setAdapter(cropAdapter);

        // Gruppi finti (in futuro dal DB utente)
        List<String> groupNames = getUserGroups();
        ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                groupNames
        );
        dropGroup.setAdapter(groupAdapter);

        dropGroup.setOnItemClickListener((parent, view, position, id) -> {
            String selected = groupNames.get(position);
            if ("Inserisci nuovo gruppo".equals(selected)) {
                Toast.makeText(requireContext(),
                        "Qui apriremo 'Inserisci nuovo gruppo'",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getUserGroups() {
        // TODO: sostituire con i gruppi reali salvati dall'utente
        return Arrays.asList(
                "Gruppo A",
                "Gruppo B",
                "Inserisci nuovo gruppo"
        );
    }

    // ----------------------------------------------------
    // LISTENER
    // ----------------------------------------------------
    private void setupListeners() {
        // icona di localizzazione nell'input indirizzo
        tilAddress.setEndIconOnClickListener(v -> onLocationIconClicked());

        // bottone "Imposta alert"
        btnSetAlerts.setOnClickListener(v -> {
            if (validateForm()) {
                NavHostFragment.findNavController(AddFieldFragment.this)
                        .navigate(R.id.setAlertsFragment);
            }
        });

    }

    private void onLocationIconClicked() {
        // mostra il frame mappa se nascosto
        if (mapContainer.getVisibility() != View.VISIBLE) {
            mapContainer.setVisibility(View.VISIBLE);
        }

        // inizializza la mappa se necessario
        if (mapFragment == null) {
            mapFragment = (SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapContainer);

            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance();
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mapContainer, mapFragment)
                        .commitNow();
            }

            mapFragment.getMapAsync(this);
        } else if (googleMap != null) {
            // se la mappa c'è già, ricentriamo
            moveCameraToUser();
        }
    }

    // ----------------------------------------------------
    // VALIDAZIONE
    // ----------------------------------------------------
    private boolean validateForm() {
        boolean ok = true;

        String address = textOrEmpty(inputAddress);
        String crop = textOrEmpty(dropCropType);
        String group = textOrEmpty(dropGroup);

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

        if (TextUtils.isEmpty(group)) {
            tilGroup.setError("*Campo obbligatorio");
            ok = false;
        } else {
            tilGroup.setError(null);
        }

        return ok;
    }

    private String textOrEmpty(TextInputEditText edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    private String textOrEmpty(AutoCompleteTextView edit) {
        return edit.getText() != null ? edit.getText().toString().trim() : "";
    }

    // ----------------------------------------------------
    // MAPPA
    // ----------------------------------------------------
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // marker trascinabile
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(@NonNull Marker marker) {}
            @Override public void onMarkerDrag(@NonNull Marker marker) {}
            @Override public void onMarkerDragEnd(@NonNull Marker marker) {
                // qui potresti salvare le coordinate finali del campo
            }
        });

        moveCameraToUser();
    }

    private void moveCameraToUser() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    LatLng target;
                    if (location != null) {
                        target = new LatLng(location.getLatitude(), location.getLongitude());
                    } else {
                        // fallback: centro Italia
                        target = new LatLng(42.5, 12.5);
                    }

                    if (googleMap == null) return;

                    if (marker == null) {
                        marker = googleMap.addMarker(new MarkerOptions()
                                .position(target)
                                .draggable(true));
                    } else {
                        marker.setPosition(target);
                    }

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 14f));
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_LOCATION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            moveCameraToUser();
        }
    }
}
