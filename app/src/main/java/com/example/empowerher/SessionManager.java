package com.example.empowerher;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String SESSION_PREFS = "SessionPrefs";
    private static final String KEY_SESSION_COOKIE = "sessionCookie";
    private static final String KEY_USER_NAME = "userName";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSessionCookie(String cookie) {
        editor.putString(KEY_SESSION_COOKIE, cookie).apply();
    }

    public String getSessionCookie() {
        return sharedPreferences.getString(KEY_SESSION_COOKIE, "");
    }

    public void saveUserName(String userName) {
        editor.putString(KEY_USER_NAME, userName).apply();
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public boolean isLoggedIn() {
        return !getSessionCookie().isEmpty();
    }

    public void logoutUser() {
        editor.clear().apply(); // This clears all data, including the session cookie and user name
    }
}
