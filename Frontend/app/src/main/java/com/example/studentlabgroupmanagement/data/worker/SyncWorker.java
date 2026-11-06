package com.example.studentlabgroupmanagement.data.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.dao.OperationDao;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.OperationEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.remote.ApiService;
import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Response;

public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        OperationDao operationDao = db.operationDao();
        ApiService apiService = RetrofitClient.getApiService();
        Gson gson = new Gson();
        List<OperationEntity> pendingOps = operationDao.getAllPendingOperations();

        if (pendingOps == null || pendingOps.isEmpty()) return Result.success();

        try {
            for (OperationEntity op : pendingOps) {
                String type = op.getOperationType();
                if (type == null || op.getPayloadJson() == null) {
                    operationDao.deleteOperation(op);
                    continue;
                }

                boolean successful = false;
                switch (type) {
                    case "REGISTER_USER":
                    case "CREATE_USER": {
                        StudentEntity student = gson.fromJson(op.getPayloadJson(), StudentEntity.class);
                        Response<StudentEntity> response = apiService.createStudent(student).execute();
                        successful = response.isSuccessful() || response.code() == 409;
                        break;
                    }
                    case "CREATE_GROUP":
                    case "REGISTER_GROUP": {
                        LabGroupEntity group = gson.fromJson(op.getPayloadJson(), LabGroupEntity.class);
                        Response<LabGroupEntity> response = apiService.createGroup(group).execute();
                        successful = response.isSuccessful() || response.code() == 409;
                        break;
                    }
                    default:
                        // Unknown operations should not remain queued forever.
                        Log.w(TAG, "Discarding unsupported queued operation: " + type);
                        successful = true;
                        break;
                }

                if (successful) {
                    operationDao.deleteOperation(op);
                } else {
                    Log.w(TAG, "Sync failed for operation " + op.getOperationId() + "; it will retry.");
                    return Result.retry();
                }
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Could not sync queued operations", e);
            return Result.retry();
        }
    }
}