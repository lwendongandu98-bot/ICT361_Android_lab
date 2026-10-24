package com.example.studentlabgroupmanagement.data.remote;

import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LecturerEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    // --- Lab Groups ---
    @GET("api/groups")
    Call<List<LabGroupEntity>> getAllGroups();

    @POST("api/groups")
    Call<LabGroupEntity> createGroup(@Body LabGroupEntity group);

    // --- Students / Users ---
    @GET("api/students")
    Call<List<StudentEntity>> getAllStudents();

    @POST("api/students")
    Call<StudentEntity> createStudent(@Body StudentEntity student);

    // --- Courses ---
    @GET("api/courses")
    Call<List<CourseEntity>> getAllCourses();

    @POST("api/courses")
    Call<CourseEntity> createCourse(@Body CourseEntity course);

    // --- Submissions ---
    @GET("api/submissions")
    Call<List<SubmissionEntity>> getAllSubmissions();

    @POST("api/submissions")
    Call<SubmissionEntity> createSubmission(@Body SubmissionEntity submission);

    // --- Labs (Lecturer Actions) ---
    @GET("api/labs")
    Call<List<LabEntity>> getAllLabs();

    @POST("api/labs")
    Call<LabEntity> createLab(@Body LabEntity lab);

    // --- Announcements (Lecturer Actions) ---
    @GET("api/announcements")
    Call<List<AnnouncementEntity>> getAllAnnouncements();

    @POST("api/announcements")
    Call<AnnouncementEntity> createAnnouncement(@Body AnnouncementEntity announcement);

    // --- Lecturers / Profiles ---
    @GET("api/lecturers/{username}")
    Call<StudentEntity> getLecturerProfile(@Path("username") String username);

    @POST("api/lecturers/update")
    Call<StudentEntity> updateLecturerProfile(@Body StudentEntity lecturer);

    // --- Lecturer Registration ---
    @POST("api/lecturers")
    Call<Void> registerLecturer(@Body LecturerEntity lecturer);
}