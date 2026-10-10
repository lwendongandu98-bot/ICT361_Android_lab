package com.yourpackage.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.yourpackage.data.local.entities.SubmissionEntity;
import java.util.List;

@Dao
public interface SubmissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSubmission(SubmissionEntity submission);

    @Query("SELECT * FROM submissions WHERE status = 'PENDING'")
    List<SubmissionEntity> getPendingSubmissions();

    @Query("UPDATE submissions SET status = 'CHECKED' WHERE id = :submissionId")
    void markAsChecked(int submissionId);
}