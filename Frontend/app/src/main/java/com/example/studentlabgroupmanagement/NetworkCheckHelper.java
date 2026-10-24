package com.example.studentlabgroupmanagement;

import android.content.Context;
import android.widget.Toast;

import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkCheckHelper {

    public static void checkConnection(Context context) {
        RetrofitClient.getApiService().getAllGroups().enqueue(new Callback<List<LabGroupEntity>>() {
            @Override
            public void onResponse(Call<List<LabGroupEntity>> call, Response<List<LabGroupEntity>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Server connected successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LabGroupEntity>> call, Throwable t) {
                Toast.makeText(context, "Connection failed! Check IP/Wi-Fi", Toast.LENGTH_SHORT).show();
            }
        });
    }
}