package com.agrialert;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.agrialert.viewmodel.FieldsViewModel;
import com.agrialert.viewmodel.AlertsViewModel;
import com.agrialert.alert_manager.AlertManagerInitializer;
import com.agrialert.data_manager.DataManager;
import com.agrialert.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.mapbox.common.MapboxOptions;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Main Activity of the application.
 * Manages the connection to the DataManager service, initializes ViewModels,
 * and sets up the primary navigation components (Toolbar, Bottom Navigation).
 */
public class MainActivity extends AppCompatActivity {
    /** Navigation view for switching between top-level destinations. */
    private BottomNavigationView bottomNav;
    /** View binding for the activity layout. */
    private ActivityMainBinding binding;
    /** Configuration for the App Bar, defining top-level navigation destinations. */
    private AppBarConfiguration appBarConfiguration;
    /** Instance of the DataManager service. */
    private DataManager dataManager;
    /** Flag indicating whether the activity is bound to the DataManager service. */
    private boolean mBound = false;
    private static final int REQ_STARTUP_PERMISSIONS = 1001;
    /** Request code for notification permission. */
    private static final int REQ_NOTIFICATIONS = 1001;
    /** ViewModel for field-related operations. */
    private FieldsViewModel fieldsVM;
    /** ViewModel for alert-related operations. */
    private AlertsViewModel alertsVM;

    /**
     * Gets the current FieldsViewModel.
     * @return The FieldsViewModel instance.
     */
    public FieldsViewModel fieldsVM() { return fieldsVM; }

    /**
     * Gets the current AlertsViewModel.
     * @return The AlertsViewModel instance.
     */
    public AlertsViewModel alertsVM() { return alertsVM; }

    /** Subject emitting the binding state of the DataManager service. */
    private final BehaviorSubject<Boolean> isBoundSubject = BehaviorSubject.create();

    /**
     * Checks if all ViewModels and the DataManager are initialized and ready.
     * @return True if ready, false otherwise.
     */
    public boolean vmsReady() {
        return mBound && dataManager != null && fieldsVM != null && alertsVM != null;
    }

    /**
     * Returns a Single that completes when the activity is bound to the DataManager.
     * @return A Single emitting true when bound.
     */
    public Single<Boolean> isBound() {
        return isBoundSubject.filter(bound -> bound).firstOrError();
    }

    /**
     * Connection object for the DataManager service.
     */
    private ServiceConnection connection = new ServiceConnection() {
        /**
         * Called when a connection with the service has been established.
         * Initializes ViewModels once the service is available.
         */
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            DataManager.LocalBinder binder = (DataManager.LocalBinder) service;
            dataManager = binder.getService();
            mBound = true;
            Toast.makeText(MainActivity.this, "DataManager Bound", Toast.LENGTH_SHORT).show();
            fieldsVM = new FieldsViewModel(dataManager);
            alertsVM = new AlertsViewModel(dataManager);
            isBoundSubject.onNext(true);
        }

        /**
         * Called when the service has crashed or been killed.
         */
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Toast.makeText(MainActivity.this, "Not Bound to DataManager", Toast.LENGTH_SHORT).show();
        }
    };


    /**
     * Initializes the activity, sets up navigation components, and starts background services.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize background alert management
        AlertManagerInitializer.init(getApplicationContext());
        requestStartupPermissionsIfNeeded();

        // VIEW BINDING
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TOOLBAR (HEADER)
        MaterialToolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);

        // NAVCONTROLLER from NAVHOST
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        bottomNav = binding.bottomNav;
        //NavigationUI.setupWithNavController(bottomNav, navController);
        bottomNav.setOnItemSelectedListener(item -> {
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setRestoreState(false)
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), false, false)
                    .build();

            navController.navigate(item.getItemId(), null, options);
            return true;
        });

        // TOP LEVEL DESTINATIONS (no back arrow shown)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardFragment,
                R.id.alertsListFragment,
                R.id.fieldsListFragment
        ).build();

        // Link toolbar to navController (title + back)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();

            // Controlla se la destinazione è una di quelle presenti nella BottomNav
            if (id == R.id.dashboardFragment ||
                    id == R.id.alertsListFragment ||
                    id == R.id.fieldsListFragment) {

                // Forza la selezione dell'elemento corretto nella BottomNav
                bottomNav.getMenu().findItem(id).setChecked(true);
            }
        });

        // Set Mapbox API key
        MapboxOptions.setAccessToken(BuildConfig.MAPBOX_API_KEY);
    }

    /**
     * Binds the activity to the DataManager service when it starts.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to DataManager
        Intent intent = new Intent(this, DataManager.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Lifecycle callback called when the activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Do NOT unbind here: activity is still in use (navigation)
    }

    /**
     * Unbinds the DataManager service and cleans up resources.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(connection);
            mBound = false;
        }
    }

    /**
     * Requests notification permission if the device is running Android 13 or higher.
     */
    private void requestStartupPermissionsIfNeeded() {
        ArrayList<String> toRequest = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            toRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        boolean fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!fineGranted && !coarseGranted) {
            toRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            toRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (toRequest.isEmpty()) {
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                toRequest.toArray(new String[0]),
                REQ_STARTUP_PERMISSIONS
        );
    }

    /**
     * Callback for the result from requesting permissions.
     *
     * @param requestCode The request code passed in {@link #requestPermissions}.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STARTUP_PERMISSIONS) {
            // no-op: alcune funzionalità si disabilitano automaticamente se negate
        }
    }

    /**
     * Handles navigation when the up button in the toolbar is pressed.
     *
     * @return true if navigation was successful.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}
