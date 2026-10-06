package com.example.studentregistrationapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthViewModel extends ViewModel {

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> authErrorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthenticated = new MutableLiveData<>(false);
    private final MutableLiveData<String> userRole = new MutableLiveData<>();

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getAuthErrorMessage() {
        return authErrorMessage;
    }

    public LiveData<Boolean> getIsAuthenticated() {
        return isAuthenticated;
    }

    public LiveData<String> getUserRole() {
        return userRole;
    }

    public void login(String username, String password, String role) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            authErrorMessage.setValue("Please fill in all credentials.");
            return;
        }

        isLoading.setValue(true);

        // Mock authentication logic until integrated with feature/auth-backend
        if (password.length() >= 4) {
            userRole.setValue(role);
            isAuthenticated.setValue(true);
            authErrorMessage.setValue(null);
        } else {
            authErrorMessage.setValue("Invalid credentials provided.");
            isAuthenticated.setValue(false);
        }

        isLoading.setValue(false);
    }

    public void logout() {
        isAuthenticated.setValue(false);
        userRole.setValue(null);
    }
}