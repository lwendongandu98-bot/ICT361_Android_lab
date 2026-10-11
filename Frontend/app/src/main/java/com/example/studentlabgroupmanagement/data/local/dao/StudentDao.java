package com.example.studentlabgroupmanagement.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import java.util.List;

@Dao
public interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStudent(StudentEntity student);

    @Update
    void updateStudent(StudentEntity student);

    @Query("SELECT * FROM users WHERE username = :username")
    StudentEntity getStudentById(String username);

    @Query("SELECT * FROM users")
    LiveData<List<StudentEntity>> getAllStudentsLive();

    @Query("SELECT * FROM users")
    List<StudentEntity> getAllStudents();

    @Query("SELECT * FROM users WHERE labGroup = :groupName")
    List<StudentEntity> getStudentsByLabGroup(String groupName);

    @Query("SELECT COUNT(*) FROM users WHERE labGroup = :groupName AND role = 'Student'")
    int getStudentCountByGroup(String groupName);

    @Query("UPDATE users SET labGroup = :groupName WHERE username = :username")
    void updateStudentGroup(String username, String groupName);

    @Query("DELETE FROM users WHERE username = :username")
    void deleteUserByUsername(String username);

    @Query("UPDATE users SET labGroup = :newName WHERE labGroup = :oldName")
    void updateGroupMembersLabGroup(String oldName, String newName);
}