package com.innovify.vehicletracker.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.innovify.vehicletracker.R;
import com.innovify.vehicletracker.databinding.ActivityMainBinding;
import com.innovify.vehicletracker.interfaces.LocationCallbacks;
import com.innovify.vehicletracker.services.LocationService;
import com.innovify.vehicletracker.utils.Constants;
import com.innovify.vehicletracker.utils.SessionManager;

import net.ralphpina.permissionsmanager.PermissionsManager;
import net.ralphpina.permissionsmanager.PermissionsResult;

import rx.functions.Action1;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, LocationCallbacks {

    private GoogleMap mMap;
    private LocationService locationService;
    private boolean bound = false;
    Intent serviceIntent;
    ActivityMainBinding mBinder;
    boolean isLocationTrackingEnabled = true;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_main);
        sessionManager = new SessionManager(this);
        isLocationTrackingEnabled = sessionManager.getDataByKey(Constants.PREF_SHOULD_TRACK_LOCATION, true);
        setUpToolbar();
        setUpViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (PermissionsManager.get().isStorageGranted()) {

        } else if (PermissionsManager.get().neverAskForStorage(this)) {
            Toast.makeText(this, "Please grant storage permission from settings", Toast.LENGTH_SHORT).show();
        } else {
            PermissionsManager.get().requestStoragePermission().subscribe(new Action1<PermissionsResult>() {
                @Override
                public void call(PermissionsResult permissionsResult) {

                }
            });
        }
    }

    private void setUpToolbar() {

    }

    private void setUpViews() {
        mBinder.toolbar.toggle.setChecked(isLocationTrackingEnabled);
        mBinder.toolbar.toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sessionManager.storeDataByKey(Constants.PREF_SHOULD_TRACK_LOCATION, true);
                    startLocationService();
                } else {
                    sessionManager.storeDataByKey(Constants.PREF_SHOULD_TRACK_LOCATION, false);
                    stopLocationService();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bound) {
            locationService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isLocationTrackingEnabled) {
            startLocationService();
            mBinder.tvData.setText(sessionManager.getDataByKey(Constants.PREF_STRING_TO_WRITE, ""));
        }
    }

    private void startLocationService() {
        if (serviceIntent == null) {
            serviceIntent = new Intent(getApplicationContext(), LocationService.class);
            if (PermissionsManager.get().isLocationGranted()) {
                startService(serviceIntent);
                bindLocationService();
            } else if (PermissionsManager.get().neverAskForLocation(this)) {
                Toast.makeText(this, "Please grant location permission from settings", Toast.LENGTH_SHORT).show();
            } else {
                PermissionsManager.get().requestLocationPermission().subscribe(new Action1<PermissionsResult>() {
                    @Override
                    public void call(PermissionsResult permissionsResult) {
                        if (permissionsResult.isGranted()) {
                            startService(serviceIntent);
                            bindLocationService();
                        }
                    }
                });
            }
        } else {
            bindLocationService();
        }
    }

    private void stopLocationService() {
        if (bound) {
            locationService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
        stopService(serviceIntent);
        serviceIntent = null;
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            locationService.setCallbacks(null); // unregister
            unbindService(serviceConnection);
            bound = false;
        }
    }

    private void bindLocationService() {
        if (PermissionsManager.get().isLocationGranted()) {
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else if (PermissionsManager.get().neverAskForLocation(this)) {
            Toast.makeText(this, "Please grant location permission from settings", Toast.LENGTH_SHORT).show();
        } else {
            PermissionsManager.get().requestLocationPermission().subscribe(new Action1<PermissionsResult>() {
                @Override
                public void call(PermissionsResult permissionsResult) {
                    if (permissionsResult.isGranted()) {
                        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                    }
                }
            });
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.MyBinder binder = (LocationService.MyBinder) service;
            locationService = binder.getService();
            bound = true;
            locationService.setCallbacks(MainActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mMap != null) {
            mMap.clear();
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14f));
        }
        if (mBinder != null)
            mBinder.tvData.setText(sessionManager.getDataByKey(Constants.PREF_STRING_TO_WRITE, ""));
    }
}
