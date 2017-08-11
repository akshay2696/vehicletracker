package com.innovify.vehicletracker.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.innovify.vehicletracker.interfaces.LocationCallbacks;
import com.innovify.vehicletracker.models.LocationModel;
import com.innovify.vehicletracker.utils.Constants;
import com.innovify.vehicletracker.utils.DBHelper;
import com.innovify.vehicletracker.utils.SessionManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;


/**
 * Created by Akshay Panchal on 11-Aug-2017
 */
public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final int FREQUENCY_DEFAULT = 1000;
    private final int FREQUENCY_THIRTY_SECONDS = 30 * 1000;
    private final int FREQUENCY_ONE_MINUTES = 60 * 1000;
    private final int FREQUENCY_TWO_MINUTES = 2 * 60 * 1000;
    private final int FREQUENCY_FIVE_MINUTES = 5 * 60 * 1000;

    Location location; // location
    private static String TAG = "LOCATION SERVICE";
    private final IBinder binder = new MyBinder();
    // Registered callbacks
    private LocationCallbacks locationCallbacks;
    GoogleApiClient mGoogleApiClient;
    SessionManager sessionManager;
    DBHelper dbHelper;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.e(TAG, "OnStartcommand");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            updateLocationRequestFrequency(FREQUENCY_DEFAULT);
        } else {
            setUpLocationService();
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        sessionManager = new SessionManager(getApplicationContext());
        dbHelper = new DBHelper(getApplicationContext());
        Log.e(TAG, "OnCreate");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public void setCallbacks(LocationCallbacks callbacks) {
        locationCallbacks = callbacks;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "OnDestory");
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }


    private void setUpLocationService() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "GoogleClient onConnected");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        updateLocationRequestFrequency(FREQUENCY_DEFAULT);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleClient onSuspended");
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.i(TAG, " Locationchanged " + location.toString());
            this.location = location;
            changeFrequencyAndSaveData();
        }

    }


    private void changeFrequencyAndSaveData() {
        String stringToWrite = "";
        int currentFrequency;
        int nextFrequency;
        long currentTime = location.getTime();
        float currentSpeed = location.getSpeed();
        float lastRecordedSpeed = sessionManager.getDataByKey(Constants.PREF_LAST_RECORDED_SPEED, 0f);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss.SS");
        String strDate = sdf.format(currentTime);
        System.out.println("Current date in String Format: " + strDate);

        if (currentSpeed < 30) {
            if (lastRecordedSpeed >= 80) {
                updateLocationRequestFrequency(FREQUENCY_ONE_MINUTES);
                currentFrequency = 60;
                nextFrequency = 30;
            } else if (lastRecordedSpeed >= 60 && lastRecordedSpeed < 80) {
                updateLocationRequestFrequency(FREQUENCY_TWO_MINUTES);
                currentFrequency = 120;
                nextFrequency = 60;
            } else {
                updateLocationRequestFrequency(FREQUENCY_FIVE_MINUTES);
                currentFrequency = 300;
                nextFrequency = 120;
            }
        } else if (currentSpeed >= 30 && currentSpeed < 60) {
            updateLocationRequestFrequency(FREQUENCY_TWO_MINUTES);
            currentFrequency = 120;
            nextFrequency = 60;
        } else if (currentSpeed >= 60 && currentSpeed < 80) {
            updateLocationRequestFrequency(FREQUENCY_ONE_MINUTES);
            currentFrequency = 60;
            nextFrequency = 30;
        } else {
            updateLocationRequestFrequency(FREQUENCY_THIRTY_SECONDS);
            currentFrequency = 30;
            nextFrequency = 30;
        }

        stringToWrite += strDate + " " + location.getLatitude() + " " + location.getLongitude() + " " + currentFrequency + " " + nextFrequency;


        LocationModel model = new LocationModel();
        model.setTimeAndDate(strDate);
        model.setLatitude(location.getLatitude());
        model.setLongitude(location.getLongitude());
        model.setCurrentFrequency(currentFrequency);
        model.setNextFrequency(nextFrequency);
        dbHelper.addLocationData(model);

        Log.e("String to write : ", stringToWrite);
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/locations");
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "locationData.txt");
        try {
            FileOutputStream f = new FileOutputStream(file, true);
            PrintWriter pw = new PrintWriter(f);
            pw.println(stringToWrite);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "File not found. Did you");
        } catch (IOException e) {
            e.printStackTrace();
        }
        sessionManager.storeDataByKey(Constants.PREF_LAST_RECORDED_SPEED, currentSpeed);
        sessionManager.storeDataByKey(Constants.PREF_STRING_TO_WRITE, stringToWrite);
        if (locationCallbacks != null)
            locationCallbacks.onLocationChanged(location);

    }


    @SuppressWarnings("MissingPermission")
    private void updateLocationRequestFrequency(final int timeInMilli) {
        Log.e("Ping time", "modified");

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        final LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(timeInMilli);
        mLocationRequest.setFastestInterval(timeInMilli);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mGoogleApiClient.isConnected()) {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, LocationService.this);
                }
            }
        }, timeInMilli);


    }

}
