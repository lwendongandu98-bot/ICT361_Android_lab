package com.example.studentlabgroupmanagement.data.remote;

import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @GET("api/groups")
    Call<List<LabGroupEntity>> getAllGroups();

    @POST("api/groups")
    Call<LabGroupEntity> createGroup(@Body LabGroupEntity group);

    @GET("api/students")
    Call<List<StudentEntity>> getAllStudents();
}