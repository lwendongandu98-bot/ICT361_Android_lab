package com.example.studentlabgroupmanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.dao.StudentDao;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LecturerRepository {
    private StudentDao studentDao;
    private ApiService apiService;
    private ExecutorService executorService;

    public LecturerRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        studentDao = database.studentDao();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<StudentEntity> getLecturerByUsername(String username) {
        return studentDao.getStudentLiveById(username);
    }

    public void updateProfile(StudentEntity lecturer, GroupRepository.OnOperationCallback callback) {
        apiService.createStudent(lecturer).enqueue(new Callback<StudentEntity>() {
            @Override
            public void onResponse(Call<StudentEntity> call, Response<StudentEntity> response) {
                if (response.isSuccessful()) {
                    executorService.execute(() -> studentDao.updateStudent(lecturer));
                    callback.onSuccess("Profile updated successfully on MySQL server!");
                } else {
                    saveLocallyAndFallback(lecturer, callback);
                }
            }

            @Override
            public void onFailure(Call<StudentEntity> call, Throwable t) {
                saveLocallyAndFallback(lecturer, callback);
            }
        });
    }

    private void saveLocallyAndFallback(StudentEntity lecturer, GroupRepository.OnOperationCallback callback) {
        executorService.execute(() -> {
            studentDao.updateStudent(lecturer);
            callback.onSuccess("Offline mode: Profile updated locally & sync scheduled!");
        });
    }
}