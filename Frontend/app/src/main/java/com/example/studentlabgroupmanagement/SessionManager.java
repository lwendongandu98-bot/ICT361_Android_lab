package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_STUDENT_NUMBER = "student_number";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Save comprehensive user session info on successful login
    public void createLoginSession(String username, String studentNumber, String email, String fullName, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_STUDENT_NUMBER, studentNumber);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    // Check login status
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Retrieve active logged-in username
    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    // Retrieve active student number (critical for submissions foreign keys)
    public String getStudentNumber() {
        return pref.getString(KEY_STUDENT_NUMBER, "");
    }

    // Retrieve active email
    public String getEmail() {
        return pref.getString(KEY_EMAIL, "");
    }

    // Retrieve active full name
    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, "");
    }

    // Retrieve active logged-in user role
    public String getUserRole() {
        return pref.getString(KEY_ROLE, "");
    }

    // Wipe session details on Logout action
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}