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

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private ActivityMainBinding binding;
    private AppBarConfiguration appBarConfiguration;
    private DataManager dataManager;
    private boolean mBound = false;
    private static final int REQ_NOTIFICATIONS = 1001;
    private FieldsViewModel fieldsVM;
    private AlertsViewModel alertsVM;
    public FieldsViewModel fieldsVM() { return fieldsVM; }
    public AlertsViewModel alertsVM() { return alertsVM; }

    public boolean vmsReady() {
        return mBound && dataManager != null && fieldsVM != null && alertsVM != null;
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

            dataManager.insertGroup(new FieldsGroup("default","default")).subscribe(
                    ()->{},
                    e->{}
            );

            // 1) Ondata di calore
            dataManager.insertAlertType(
                    new AlertType(
                            "Ondata di calore",
                            "Temperature elevate che possono causare stress termico",
                            26.0
                    )
            ).subscribe();

            // 2) Gelo / Brina
            dataManager.insertAlertType(
                    new AlertType(
                            "Gelo / Brina",
                            "Rischio di danni da gelo su colture sensibili",
                            0.0
                    )
            ).subscribe();

            // 3) Pioggia intensa
            dataManager.insertAlertType(
                    new AlertType(
                            "Pioggia intensa",
                            "Precipitazioni elevate che possono provocare ristagno o erosione",
                            30.0
                    )
            ).subscribe();

            // 4) Vento forte
            dataManager.insertAlertType(
                    new AlertType(
                            "Vento forte",
                            "Raffiche che possono piegare o danneggiare le piante",
                            50.0
                    )
            ).subscribe();

            // 5) Temporale / Grandine
            dataManager.insertAlertType(
                    new AlertType(
                            "Temporale / Grandine",
                            "Eventi violenti con rischio di danni ai raccolti",
                            60.0
                    )
            ).subscribe();

            // 6) Siccità prolungata
            dataManager.insertAlertType(
                    new AlertType(
                            "Siccità prolungata",
                            "Carenza idrica dovuta a mancanza di piogge",
                            5.0
                    )
            ).subscribe();

            // 7) Umidità elevata
            dataManager.insertAlertType(
                    new AlertType(
                            "Umidità elevata",
                            "Rischio di malattie fungine dovute a eccesso di umidità",
                            80.0
                    )
            ).subscribe();

            // 8) Escursione termica elevata
            dataManager.insertAlertType(
                    new AlertType(
                            "Escursione termica elevata",
                            "Rischio di stress termico tra giorno e notte",
                            12.0
                    )
            ).subscribe();

            // 9) Rischio incendio
            dataManager.insertAlertType(
                    new AlertType(
                            "Rischio incendio",
                            "Condizioni di vento secco e terreno arido",
                            30.0
                    )
            ).subscribe();

            // 10) Scarsa ventilazione
            dataManager.insertAlertType(
                    new AlertType(
                            "Scarsa ventilazione",
                            "Stagnazione dell'aria con rischio muffe",
                            5.0
                    )
            ).subscribe();



            /*dataManager.insertField(new Field("test", 2d, 2d, "Default", CropType.CEREALS)).subscribe(
                    () -> {},
                    error -> Log.d("mytag", "error: " + error)
            );
            dataManager.getFieldById(1).subscribe(
                    field -> Log.d("mytag", "field: " + field.toString()),
                    error -> Log.d("mytag", "error: " + error)
            );*/
            /*dataManager.insertGroup(new FieldsGroup("default", "default")).subscribe(
                    () -> {},
                    error -> Log.d("mytag", "error: " + error)
            );
            dataManager.insertField(new Field("test", 2d, 2d, "default")).subscribe(
                    () -> {},
                    error -> Log.d("mytag", "error: " + error)
            );
            dataManager.addAlertType(new AlertType("test", "test", 0)).subscribe();
            dataManager.addAlertToField(1, 1, 31d).subscribe();

            dataManager.getGroupByName("default").subscribe(defaultGroup -> {
                Field test = defaultGroup.fields.get(0);
                test.setAddress("bla vfsdfas");
                dataManager.updateField(test).subscribe();
            });

            dataManager.getAllGroups().subscribe(groups -> {
                for (GroupWithFields group : groups) {
                    Log.d("mytag", group.toString());
                }
            });

            dataManager.getActivatedAlertsFromField(1).subscribe(alerts -> {
                for (AlertWithThreshold alert : alerts.getAlerts()) {
                    Log.d("mytag", alert.toString());
                }
            });*/

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
        requestNotificationPermissionIfNeeded();

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

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQ_NOTIFICATIONS
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATIONS) {
            // no-op: AlertNotificationManager logga e continua anche se negato
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
