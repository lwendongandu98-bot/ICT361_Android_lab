package com.example.studentlabgroupmanagement.data.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.dao.OperationDao;
import com.example.studentlabgroupmanagement.data.local.entity.OperationEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity; // Used for user payloads
// Import your Retrofit client and API interface classes:
// import com.example.studentlabgroupmanagement.data.remote.ApiService;
// import com.example.studentlabgroupmanagement.data.remote.RetrofitClient;
// import com.google.gson.Gson;

import java.util.List;
import retrofit2.Response;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        OperationDao operationDao = db.operationDao();

        List<OperationEntity> pendingOps = operationDao.getAllPendingOperations();
        if (pendingOps.isEmpty()) {
            return Result.success();
        }

        // Initialize Retrofit API service and Gson
        // ApiService apiService = RetrofitClient.getInstance().getApi();
        // Gson gson = new Gson();

        try {
            for (OperationEntity op : pendingOps) {
                boolean isSuccessful = false;
                String type = op.getOperationType();

                if (type == null) {
                    operationDao.deleteOperation(op);
                    continue;
                }

                switch (type) {
                    case "REGISTER_USER":
                    case "CREATE_USER":
                        // Deserialize payload into StudentEntity (or your user representation)
                        // StudentEntity student = gson.fromJson(op.getPayloadJson(), StudentEntity.class);
                        // Response<StudentEntity> userResponse = apiService.createStudent(student).execute();
                        // isSuccessful = userResponse.isSuccessful();
                        break;

                    case "CREATE_GROUP":
                    case "REGISTER_GROUP":
                        // LabGroupEntity group = gson.fromJson(op.getPayloadJson(), LabGroupEntity.class);
                        // Response<LabGroupEntity> groupResponse = apiService.createGroup(group).execute();
                        // isSuccessful = groupResponse.isSuccessful();
                        break;

                    default:
                        isSuccessful = true;
                        break;
                }

                if (isSuccessful) {
                    operationDao.deleteOperation(op);
                } else {
                    return Result.retry();
                }
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}