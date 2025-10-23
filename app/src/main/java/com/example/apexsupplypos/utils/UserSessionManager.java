package com.example.apexsupplypos.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class UserSessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_TYPE = "userType"; // "admin" or "user"
    private static final String TAG = "ApexPOS_Session";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;

    public static final String USER_TYPE_ADMIN = "admin";
    public static final String USER_TYPE_USER = "user";

    public UserSessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String username, String userType) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_TYPE, userType);
        editor.commit(); // Use commit for immediate save
        Log.d(TAG, "Login session created for user: " + username + " as " + userType);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getUserType() {
        return pref.getString(KEY_USER_TYPE, null);
    }

    public void logoutUser() {
        editor.clear();
        editor.commit(); // Use commit for immediate save
        Log.d(TAG, "User logged out. Session cleared.");
    }
}
