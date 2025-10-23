package com.example.apexsupplypos.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class SharedPrefsManager {
    private static final String PREF_NAME = "ApexSupplyPrefs";
    private static final String TAG = "ApexPOS_SharedPrefs";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveData(String key, String value) {
        editor.putString(key, value);
        editor.apply();
        Log.d(TAG, "Saved data for key: " + key);
    }

    public String getData(String key) {
        String data = sharedPreferences.getString(key, null); // Return null if key not found
        Log.d(TAG, "Retrieved data for key: " + key + (data != null ? " (found)" : " (not found)"));
        return data;
    }

    public void clearData() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "All SharedPreferences data cleared.");
    }

    public boolean contains(String key) {
        return sharedPreferences.contains(key);
    }

    public void removeData(String key) {
        editor.remove(key);
        editor.apply();
        Log.d(TAG, "Removed data for key: " + key);
    }

    public boolean exportPrefsToJson(Context context, String filename) {
        // This is a simplified example. For real app, use Storage Access Framework
        // and handle permissions properly.
        String json = new Gson().toJson(sharedPreferences.getAll());
        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            File file = new File(externalDir, filename);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
                Log.d(TAG, "Preferences exported to: " + file.getAbsolutePath());
                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    public boolean importPrefsFromJson(Context context, String filename) {
        // This is a simplified example. For real app, use Storage Access Framework
        // and handle permissions properly.
        try {
            File externalDir = context.getExternalFilesDir(null);
            if (externalDir != null) {
                File file = new File(externalDir, filename);
                if (file.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        String json = stringBuilder.toString();
                        // This will overwrite existing data. Handle merging carefully in a real app.
                        editor.clear(); // Clear existing
                        Type type = new TypeToken<Map<String, ?>>(){}.getType();
                        Map<String, ?> map = new Gson().fromJson(json, type);
                        for (Map.Entry<String, ?> entry : map.entrySet()) {
                            Object value = entry.getValue();
                            if (value instanceof String) {
                                editor.putString(entry.getKey(), (String) value);
                            } else if (value instanceof Integer) {
                                editor.putInt(entry.getKey(), (Integer) value);
                            } // ... handle other types
                        }
                        editor.apply();
                        Log.d(TAG, "Preferences imported from: " + file.getAbsolutePath());
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error importing preferences from JSON: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
