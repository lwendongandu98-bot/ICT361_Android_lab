package com.example.studentlabgroupmanagement.data.worker;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class SyncHelper {

    public static final String WORK_NAME = "AppWideDataSync";

    public static void triggerSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(DataSyncWorker.class) //[cite: 6]
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE, // Forces the queue to refresh and run immediately
                syncRequest
        );
    }
}