package com.example.studentlabgroupmanagement.data.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.studentlabgroupmanagement.data.local.AppDatabase;
import com.example.studentlabgroupmanagement.data.local.dao.OperationDao;
import com.example.studentlabgroupmanagement.data.local.entity.OperationEntity;
import java.util.List;

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

        try {
            for (OperationEntity op : pendingOps) {
                // TODO: Dispatch mutation to your Node.js/Express API via Retrofit
                // Once successful:
                operationDao.deleteOperation(op);
            }
            return Result.success();
        } catch (Exception e) {
            // Trigger exponential backoff retry on network failure
            return Result.retry();
        }
    }
}