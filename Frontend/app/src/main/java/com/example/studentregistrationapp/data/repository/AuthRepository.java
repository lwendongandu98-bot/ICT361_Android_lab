package com.example.studentregistrationapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studentregistrationapp.data.model.LoginRequest;
import com.example.studentregistrationapp.data.model.UserSession;

public class AuthRepository {

    private final MutableLiveData<UserSession> userSessionLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public LiveData<UserSession> getUserSession() {
        return userSessionLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    // Matches the loginUser method signature called by AuthViewModel
    public void loginUser(String username, String password, String role, AuthCallback callback) {
        if (username == null || password == null) {
            callback.onError("Invalid credentials.");
            return;
        }

        // TODO: Implement Volley network request to Node.js /api/auth/login endpoint here
        // For now, simulate successful response:
        UserSession session = new UserSession();
        userSessionLiveData.setValue(session);
        errorMessageLiveData.setValue(null);
        callback.onSuccess(role);
    }

    public void login(LoginRequest request) {
        if (request == null) {
            errorMessageLiveData.setValue("Invalid credentials.");
            return;
        }
        UserSession session = new UserSession();
        userSessionLiveData.setValue(session);
        errorMessageLiveData.setValue(null);
    }

    public void logout() {
        userSessionLiveData.setValue(null);
    }

    public interface AuthCallback {
        void onSuccess(String role);
        void onError(String error);
    }
}