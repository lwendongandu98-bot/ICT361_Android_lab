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
}
