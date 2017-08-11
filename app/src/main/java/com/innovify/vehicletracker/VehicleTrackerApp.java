package com.innovify.vehicletracker;

import android.app.Application;

import net.ralphpina.permissionsmanager.PermissionsManager;

/**
 * Created by Akshay on 8/8/2017.
 */

public class VehicleTrackerApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PermissionsManager.init(this);
    }
}
