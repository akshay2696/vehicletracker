package com.innovify.vehicletracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.innovify.vehicletracker.R;


public class SessionManager {
    private SharedPreferences pref;
    public static final String KEY_TOKEN = "Token";

    public SessionManager(Context context) {
        String PREF_NAME = context.getResources().getString(R.string.app_name);
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    /**
     * Getting value for key from shared Preferences
     *
     * @param key          key for which we need to get Value
     * @param defaultValue default value to be returned if key is not exits
     * @return It will return value of key if exist and defaultValue otherwise
     */
    public String getValueFromKey(String key, String defaultValue) {
        if (pref.contains(key)) {
            return pref.getString(key, defaultValue);
        } else {
            return defaultValue;
        }
    }


    public String getDataByKey(String Key) {
        return getDataByKey(Key, "");
    }

    public Boolean getDataByKey(String Key, boolean DefaultValue) {
        if (pref.contains(Key)) {
            return pref.getBoolean(Key, DefaultValue);
        } else {
            return DefaultValue;
        }
    }

    public String getDataByKey(String Key, String DefaultValue) {
        String returnValue;
        if (pref.contains(Key)) {
            returnValue = pref.getString(Key, DefaultValue);
        } else {
            returnValue = DefaultValue;
        }
        return returnValue;
    }

    public int getDataByKey(String Key, int DefaultValue) {
        if (pref.contains(Key)) {
            return pref.getInt(Key, DefaultValue);
        } else {
            return DefaultValue;
        }
    }

    public float getDataByKey(String Key, float DefaultValue) {
        if (pref.contains(Key)) {
            return pref.getFloat(Key, DefaultValue);
        } else {
            return DefaultValue;
        }
    }

    public void storeDataByKey(String key, float Value) {
        pref.edit().putFloat(key, Value).commit();
    }

    public void storeDataByKey(String key, int Value) {
        pref.edit().putInt(key, Value).commit();
    }

    public void storeDataByKey(String key, String Value) {
        pref.edit().putString(key, Value).commit();
    }

    public void storeDataByKey(String key, boolean Value) {
        pref.edit().putBoolean(key, Value).commit();
    }

    public void clearDataByKey(String key) {
        pref.edit().remove(key);
    }

    public void clearSession() {
        pref.edit().clear().commit();
    }
}
