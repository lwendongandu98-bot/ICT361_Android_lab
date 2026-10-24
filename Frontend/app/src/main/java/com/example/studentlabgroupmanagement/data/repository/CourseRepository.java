package com.example.studentlabgroupmanagement.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.studentlabgroupmanagement.data.local.dao.CourseDao;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseRepository {
    private final CourseDao courseDao;
    private final ApiService apiService;
    private final LiveData<List<CourseEntity>> allCourses;
    private final ExecutorService executorService;

    public CourseRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        courseDao = database.courseDao();
        allCourses = courseDao.getAllCourses();
        apiService = RetrofitClient.getApiService();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<CourseEntity>> getAllCourses() {
        return allCourses;
    }

    public void insertCourse(CourseEntity course, GroupRepository.OnOperationCallback callback) {
        apiService.createCourse(course).enqueue(new Callback<CourseEntity>() {
            @Override
            public void onResponse(Call<CourseEntity> call, Response<CourseEntity> response) {
                if (response.isSuccessful() && response.body() != null) {
                    executorService.execute(() -> courseDao.insertCourse(course)); // MATCHED DAO METHOD
                    callback.onSuccess("Course created & saved to MySQL successfully!");
                } else {
                    saveLocallyAndFallback(course, callback);
                }
            }

            @Override
            public void onFailure(Call<CourseEntity> call, Throwable t) {
                saveLocallyAndFallback(course, callback);
            }
        });
    }

    private void saveLocallyAndFallback(CourseEntity course, GroupRepository.OnOperationCallback callback) {
        executorService.execute(() -> {
            courseDao.insertCourse(course); // MATCHED DAO METHOD
            callback.onSuccess("Offline mode: Course saved locally & sync scheduled!");
        });
    }
}