package com.example.studentlabgroupmanagement.data.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Response;

public class DataSyncWorker extends Worker {

    private static final String TAG = "DataSyncWorker";

    public DataSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Full-app background sync worker started executing...");

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        ApiService apiService = RetrofitClient.getApiService();

        try {
            // 1. Sync Lab Groups
            List<LabGroupEntity> localGroups = db.labGroupDao().getAllGroupsSync();
            if (localGroups != null) {
                for (LabGroupEntity group : localGroups) {
                    try {
                        Response<LabGroupEntity> response = apiService.createGroup(group).execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully synced group: " + group.getGroupName());
                        } else if (response.code() == 409 || response.code() == 400) {
                            Log.d(TAG, "Group already exists on server: " + group.getGroupName());
                        } else {
                            Log.e(TAG, "Server rejected group " + group.getGroupName() + ", Code: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing group " + group.getGroupName() + ": " + e.getMessage());
                    }
                }
            }

            // 2. Sync Courses
            List<CourseEntity> localCourses = db.courseDao().getAllCoursesSync();
            if (localCourses != null) {
                for (CourseEntity course : localCourses) {
                    try {
                        Response<CourseEntity> response = apiService.createCourse(course).execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully synced course: " + course.getCourseName());
                        } else if (response.code() == 409 || response.code() == 400) {
                            Log.d(TAG, "Course already exists on server: " + course.getCourseName());
                        } else {
                            Log.e(TAG, "Server rejected course, Code: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing course: " + e.getMessage());
                    }
                }
            }

            // 3. Sync Students / Users
            List<StudentEntity> localStudents = db.studentDao().getAllStudents();
            if (localStudents != null) {
                for (StudentEntity student : localStudents) {
                    try {
                        Response<StudentEntity> response = apiService.createStudent(student).execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully synced student: " + student.getUsername());
                        } else if (response.code() == 409 || response.code() == 400) {
                            Log.d(TAG, "Student already exists on server: " + student.getUsername());
                        } else {
                            Log.e(TAG, "Server rejected student, Code: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing student: " + e.getMessage());
                    }
                }
            }

            // 4. Sync Submissions
            List<SubmissionEntity> localSubmissions = db.submissionDao().getAllSubmissionsSync();
            if (localSubmissions != null) {
                for (SubmissionEntity sub : localSubmissions) {
                    try {
                        Response<SubmissionEntity> response = apiService.createSubmission(sub).execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully synced submission ID: " + sub.getId());
                        } else if (response.code() == 409 || response.code() == 400) {
                            Log.d(TAG, "Submission already exists on server");
                        } else {
                            Log.e(TAG, "Server rejected submission, Code: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing submission: " + e.getMessage());
                    }
                }
            }

            // 5. Sync Labs
            List<LabEntity> localLabs = db.labDao().getAllLabsSync();
            if (localLabs != null) {
                for (LabEntity lab : localLabs) {
                    try {
                        Response<LabEntity> response = apiService.createLab(lab).execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully synced lab: " + lab.getTitle());
                        } else if (response.code() == 409 || response.code() == 400) {
                            Log.d(TAG, "Lab already exists on server: " + lab.getTitle());
                        } else {
                            Log.e(TAG, "Server rejected lab, Code: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing lab: " + e.getMessage());
                    }
                }
            }

            // 6. Sync Announcements
            List<AnnouncementEntity> localAnnouncements = db.announcementDao().getAllAnnouncementsSync();
            if (localAnnouncements != null) {
                for (AnnouncementEntity announcement : localAnnouncements) {
                    try {
                        Response<AnnouncementEntity> response = apiService.createAnnouncement(announcement).execute();
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully synced announcement ID: " + announcement.getId());
                        } else if (response.code() == 409 || response.code() == 400) {
                            Log.d(TAG, "Announcement already exists on server");
                        } else {
                            Log.e(TAG, "Server rejected announcement, Code: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing announcement: " + e.getMessage());
                    }
                }
            }

            Log.d(TAG, "Full-app background sync completed successfully.");
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Fatal error during full-app background sync: " + e.getMessage(), e);
            return Result.failure();
        }
    }
}