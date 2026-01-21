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
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.agrialert.data_manager.AlertType;
import com.agrialert.data_manager.FieldsGroup;
import com.agrialert.viewmodel.FieldsViewModel;
import com.agrialert.viewmodel.AlertsViewModel;
import com.agrialert.alert_manager.AlertManagerInitializer;
import com.agrialert.data_manager.CropType;
import com.agrialert.data_manager.DataManager;
import com.agrialert.data_manager.Field;
import com.agrialert.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.mapbox.common.MapboxOptions;

import java.util.ArrayList;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private DataManager dataManager;
    private boolean mBound = false;
    private static final int REQ_STARTUP_PERMISSIONS = 1001;
    private FieldsViewModel fieldsVM;
    private AlertsViewModel alertsVM;
    public FieldsViewModel fieldsVM() { return fieldsVM; }
    public AlertsViewModel alertsVM() { return alertsVM; }
    private final BehaviorSubject<Boolean> isBoundSubject = BehaviorSubject.create();

    public boolean vmsReady() {
        return mBound && dataManager != null && fieldsVM != null && alertsVM != null;
    }

    public Single<Boolean> isBound() {
        return isBoundSubject.filter(bound -> bound).firstOrError();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance.
            DataManager.LocalBinder binder = (DataManager.LocalBinder) service;
            dataManager = binder.getService();
            mBound = true;
            Toast.makeText(MainActivity.this, "DataManger Bound", Toast.LENGTH_SHORT).show();
            fieldsVM = new FieldsViewModel(dataManager);
            alertsVM = new AlertsViewModel(dataManager);
            isBoundSubject.onNext(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Toast.makeText(MainActivity.this, "Not Bound to DataManager", Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertManagerInitializer.init(getApplicationContext());
        requestStartupPermissionsIfNeeded();

        // VIEW BINDING
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // TOOLBAR (HEADER)
        MaterialToolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);

        // NAVCONTROLLER dal NAVHOST
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
        bottomNav = binding.bottomNav;
        NavigationUI.setupWithNavController(bottomNav, navController);

        // DESTINAZIONI "TOP LEVEL" (non mostrano freccia indietro)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dashboardFragment,
                R.id.alertsListFragment,
                R.id.fieldsListFragment
        ).build();

        // Collega toolbar al navController (titolo + back)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // BOTTOM NAV
        BottomNavigationView bottomNav = binding.bottomNav;
        NavigationUI.setupWithNavController(bottomNav, navController);

        MapboxOptions.setAccessToken(BuildConfig.MAPBOX_API_KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to DataManager
        Intent intent = new Intent(this, DataManager.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // NON fare unbind qui: l'Activity è ancora in uso (navigation)
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(connection);
            mBound = false;
        }
    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STARTUP_PERMISSIONS) {
            // no-op: alcune funzionalità si disabilitano automaticamente se negate
        }
    }

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
