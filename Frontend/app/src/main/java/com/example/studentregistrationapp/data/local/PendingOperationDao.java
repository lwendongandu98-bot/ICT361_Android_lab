package com.example.studentregistrationapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PendingOperationDao {

    @Insert
    void insert(PendingOperation operation);

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    List<PendingOperation> getAllOperations();

    @Query("DELETE FROM pending_operations WHERE operationId = :operationId")
    void deleteById(String operationId);
}