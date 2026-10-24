package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studentlabgroupmanagement.data.local.entity.OperationEntity;
import java.util.List;

@Dao
public interface OperationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOperation(OperationEntity operation);

    @Query("SELECT * FROM operation_queue ORDER BY timestamp ASC")
    List<OperationEntity> getAllPendingOperations();

    @Delete
    void deleteOperation(OperationEntity operation);
}