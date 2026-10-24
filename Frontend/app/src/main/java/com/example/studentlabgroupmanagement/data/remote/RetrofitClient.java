package com.example.studentlabgroupmanagement.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    // Android Emulator reaches the development computer through 10.0.2.2.
    // For a physical phone, replace this with the computer's LAN IP and keep
    // both devices connected to the same Wi-Fi network.
    private static final String BASE_URL = "http://10.247.33.225:3000/";
    private static volatile Retrofit retrofit;

    private RetrofitClient() { }

    public static ApiService getApiService() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit.create(ApiService.class);
    }
}
