package com.innovify.vehicletracker.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.innovify.vehicletracker.models.LocationModel;

/**
 * Created by Akshay.Panchal on 11-Aug-17.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "vehicletracker";
    public static final String TABLE_LOCATION = "location";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DATE_AND_TIME = "date_and_time";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_CURRENT_FREQUENCY = "current_frequency";
    public static final String COLUMN_NEXT_FREQUENCY = "next_frequency";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableTripList = "CREATE TABLE IF NOT EXISTS  " + TABLE_LOCATION + " "
                + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DATE_AND_TIME + " TEXT, "
                + COLUMN_LATITUDE + " TEXT, "
                + COLUMN_LONGITUDE + " TEXT, "
                + COLUMN_CURRENT_FREQUENCY + " INTEGER, "
                + COLUMN_NEXT_FREQUENCY + " INTEGER" + " )";
        db.execSQL(createTableTripList);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        onCreate(db);
    }

    public void addLocationData(LocationModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE_AND_TIME, model.getTimeAndDate());
        values.put(COLUMN_LATITUDE, model.getLatitude());
        values.put(COLUMN_LONGITUDE, model.getLongitude());
        values.put(COLUMN_CURRENT_FREQUENCY, model.getCurrentFrequency());
        values.put(COLUMN_NEXT_FREQUENCY, model.getNextFrequency());
        int id = (int) db.insert(TABLE_LOCATION, null, values);
        Log.d("DATABASE", "ADDED LOCATION DATA WITH ID = " + id);
        db.close();
    }
}
