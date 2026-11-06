package com.example.studentlabgroupmanagement.data.worker;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public final class SyncHelper {
    public static final String WORK_NAME = "AppWideDataSync";
    private SyncHelper() { }

    public static void triggerSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        WorkManager manager = WorkManager.getInstance(context.getApplicationContext());
        OneTimeWorkRequest recordsRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();
        OneTimeWorkRequest submissionsRequest = new OneTimeWorkRequest.Builder(DataSyncWorker.class)
                .setConstraints(constraints)
                .build();

        manager.enqueueUniqueWork(WORK_NAME + "_records", ExistingWorkPolicy.REPLACE, recordsRequest);
        manager.enqueueUniqueWork(WORK_NAME + "_submissions", ExistingWorkPolicy.REPLACE, submissionsRequest);
    }
}