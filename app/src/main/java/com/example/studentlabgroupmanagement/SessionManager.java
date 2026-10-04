package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Save user info on successful login
    public void createLoginSession(String username, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.commit();
    }

    // Retrieve active logged in username
    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    // Retrieve active logged in user role
    public String getUserRole() {
        return pref.getString(KEY_ROLE, null);
    }

    // Wipe session details on Logout action
    public void logoutUser() {
        editor.clear();
        editor.commit();
    }
}
