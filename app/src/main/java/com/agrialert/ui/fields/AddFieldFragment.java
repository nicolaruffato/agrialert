package com.agrialert.ui.fields;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.agrialert.MainActivity;
import com.agrialert.R;
import com.agrialert.data_manager.CropType;
import com.agrialert.data_manager.Field;
import com.agrialert.data_manager.GroupWithFields;
import com.agrialert.viewmodel.FieldsViewModel;
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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AddFieldFragment extends Fragment implements OnMapReadyCallback {

    private static final int REQ_LOCATION = 1001;
    private final CompositeDisposable cd = new CompositeDisposable();

    // UI
    private TextInputLayout tilAddress, tilCropType, tilGroup;
    private double selectedLat=0;
    private double selectedLng=0;
    private TextInputEditText inputAddress;
    private AutoCompleteTextView dropCropType, dropGroup;
    private MaterialButton btnSetAlerts;
    private View mapContainer;
    private MaterialButton btnSaveField;
    private int savedFieldId = -1;

    // MAPPA
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private Marker marker;
    private FusedLocationProviderClient fusedLocationClient;

    private FieldsViewModel vm;


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
        btnSaveField = view.findViewById(R.id.btnSaveField);
        btnSetAlerts = view.findViewById(R.id.btnSetAlerts);

        mapContainer = view.findViewById(R.id.mapContainer);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        btnSetAlerts.setEnabled(false);

        MainActivity a = (MainActivity) requireActivity();
        if (!a.vmsReady()) return;
        vm = a.fieldsVM();

        if(vm.isFieldPending) {
            btnSetAlerts.setEnabled(true);
            btnSaveField.setEnabled(false);
        }

        setupDropdowns();
        setupListeners();
    }

    // ----------------------------------------------------
    // DROPDOWN
    // ----------------------------------------------------
    private void setupDropdowns() {

        // CropType Dropdown
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
        dropCropType.setAdapter(cropAdapter);

        cd.add(vm.getAllGroups().subscribe(groups -> {
            List<String> groupNames = new ArrayList<>();
            for(GroupWithFields group : groups) {
               groupNames.add(group.getGroup().getName());
            }

            Context c = getContext();
            if (c != null) {
                Log.e("AddField", "Il contest è:");
                Log.e("AddField", getContext().toString());
            } else {
                Log.e("AddField", "Il Context è null!");
            }

            ArrayAdapter<String> groupAdapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_list_item_1,
                    groupNames
            );
            dropGroup.setAdapter(groupAdapter);

            dropGroup.setOnItemClickListener((parent, view, position, id) -> {
                String selected = groupNames.get(position);
                // TODO: Capire meglio come fare
                if ("Inserisci nuovo gruppo".equals(selected)) {
                    Toast.makeText(requireContext(),
                            "Qui apriremo 'Inserisci nuovo gruppo'",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }));

    }

    // ----------------------------------------------------
    // LISTENER
    // ----------------------------------------------------
    private void setupListeners() {

        // icona di localizzazione nell'input indirizzo
        tilAddress.setEndIconOnClickListener(v -> onLocationIconClicked());

        // bottoni
        btnSaveField.setOnClickListener(v -> {

            MainActivity a = (MainActivity) requireActivity();
            if (!a.vmsReady()) return;
            FieldsViewModel vm = a.fieldsVM();

            if (!validateForm()) return;

            // usa i tuoi veri input (già presenti nel file)
            String address = inputAddress.getText().toString().trim();

            String selectedCropName = dropCropType.getText().toString().trim();
            CropType selectedCrop = CropType.valueOf(selectedCropName);

            String groupName = dropGroup.getText().toString().trim();
            if (groupName.isEmpty()) groupName = "default";

            // TODO: call API convertion method from address to coordinates
            /*
            Pair<Double, Double> coords = new Pair<>(0.0, 0.0);
            try {
                 coords = ApiManager.getCoordinatesFromAddress(address);
            } catch (IOException e) {
                Log.e("AddField", "Errore nella chiamata API");
                throw new RuntimeException(e);
            }
            */
            double latitude = selectedLat;
            double longitude = selectedLng;

            Field field = new Field(address, latitude, longitude, groupName, selectedCrop);

            // 1) insert
            cd.add(vm.insertField(field)
                    // 2) rileggi gruppo e trova fieldId
                    .andThen(vm.getGroupByName(groupName).firstOrError())
                    .subscribe(groupWithFields -> {
                        vm.isFieldPending = true;

                        int id = -1;
                        for (Field f : groupWithFields.getFields()) {
                            if (address.equals(f.getAddress())
                                    && f.getLatitude() == latitude
                                    && f.getLongitude() == longitude) {
                                id = f.getId();
                                break;
                            }
                        }

                        if (id <= 0) {
                            Toast.makeText(requireContext(), "Salvato ma ID non trovato", Toast.LENGTH_LONG).show();
                            return;
                        }

                        savedFieldId = id;
                        Toast.makeText(requireContext(), "Campo salvato", Toast.LENGTH_SHORT).show();

                        vm.isFieldPending = true;
                        btnSetAlerts.setEnabled(true);
                        btnSaveField.setEnabled(false); // opzionale: impedisce doppio insert
                    }, err -> {
                        android.util.Log.e("AddField","Errore Salvataggio Campo",err);
                        Toast.makeText(requireContext(), "Errore : "+err.getMessage(), Toast.LENGTH_LONG).show();
                    }));
        });

        btnSetAlerts.setOnClickListener(v -> {
            if (savedFieldId <= 0) {
                Toast.makeText(requireContext(), "Prima salva il campo", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle b = new Bundle();
            b.putInt("fieldId", savedFieldId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.setAlertsFragment, b);
        });


    }

    // TODO: fix map
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

    @Override
    public void onDestroyView() {
        cd.clear();
        super.onDestroyView();
    }
}
