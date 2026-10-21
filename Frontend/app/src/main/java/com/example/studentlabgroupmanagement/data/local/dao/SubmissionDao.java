package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;

import java.util.List;

@Dao
public interface SubmissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSubmission(SubmissionEntity submission);

    // Reactive LiveData query for UI ViewModels
    @Query("SELECT * FROM submissions")
    LiveData<List<SubmissionEntity>> getAllSubmissions();

    // Synchronous query for background workers and sync routines
    @Query("SELECT * FROM submissions")
    List<SubmissionEntity> getAllSubmissionsSync();

    @Query("SELECT * FROM submissions WHERE status = 'PENDING'")
    List<SubmissionEntity> getPendingSubmissions();

    @Query("SELECT * FROM submissions WHERE status = 'CHECKED'")
    List<SubmissionEntity> getCheckedSubmissions();

    // Updates status to CHECKED and sets the checked timestamp so date headers render properly
    @Query("UPDATE submissions SET status = 'CHECKED', checked_at = :checkedAt WHERE id = :submissionId")
    void markSubmissionAsChecked(int submissionId, String checkedAt);

    // Convenience overload for single-argument calls (auto-sets current datetime)
    @Query("UPDATE submissions SET status = 'CHECKED', checked_at = datetime('now') WHERE id = :submissionId")
    void markSubmissionAsChecked(int submissionId);
}