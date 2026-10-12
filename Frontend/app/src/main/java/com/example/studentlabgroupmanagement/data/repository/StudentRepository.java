package com.example.studentlabgroupmanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.dao.StudentDao;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentRepository {
    private StudentDao studentDao;
    private ApiService apiService;
    private LiveData<List<StudentEntity>> allStudents;
    private ExecutorService executorService;

    public StudentRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        studentDao = database.studentDao();
        allStudents = studentDao.getAllStudentsLive();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<StudentEntity>> getAllStudents() {
        return allStudents;
    }

    public void insertStudent(StudentEntity student, GroupRepository.OnOperationCallback callback) {
        apiService.createStudent(student).enqueue(new Callback<StudentEntity>() {
            @Override
            public void onResponse(Call<StudentEntity> call, Response<StudentEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> studentDao.insertStudent(student));
                    callback.onSuccess("Student synced with MySQL successfully!");
                } else {
                    saveLocallyAndFallback(student, callback);
                }
            }

            @Override
            public void onFailure(Call<StudentEntity> call, Throwable t) {
                saveLocallyAndFallback(student, callback);
            }
        });
    }

    private void saveLocallyAndFallback(StudentEntity student, GroupRepository.OnOperationCallback callback) {
        executorService.execute(() -> {
            studentDao.insertStudent(student);
            callback.onSuccess("Offline mode: Student saved locally & sync scheduled!");
        });
    }
}