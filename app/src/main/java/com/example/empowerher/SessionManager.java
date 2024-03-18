package com.example.empowerher;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String SESSION_PREFS = "SessionPrefs";
    private static final String KEY_SESSION_COOKIE = "sessionCookie";
    private static final String KEY_USER_NAME = "userName"; // Key for user name in SharedPreferences

    private final SharedPreferences sharedPreferences;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
    }

    public void saveSessionCookie(String cookie) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SESSION_COOKIE, cookie);
        editor.apply();
    }

    public String getSessionCookie() {
        return sharedPreferences.getString(KEY_SESSION_COOKIE, null);
    }

    // Method to save the user's name
    public void saveUserName(String userName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    // Method to retrieve the user's name
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    // Include other methods you might have in your SessionManager...
}
