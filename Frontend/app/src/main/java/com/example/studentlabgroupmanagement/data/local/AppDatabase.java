package com.example.studentlabgroupmanagement.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.studentlabgroupmanagement.data.local.entity.StudentEntity;
import com.example.studentlabgroupmanagement.data.local.entity.CourseEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabGroupEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LabEntity;
import com.example.studentlabgroupmanagement.data.local.entity.SubmissionEntity;
import com.example.studentlabgroupmanagement.data.local.entity.OperationEntity;
import com.example.studentlabgroupmanagement.data.local.entity.AnnouncementEntity;
import com.example.studentlabgroupmanagement.data.local.entity.LecturerEntity;

import com.example.studentlabgroupmanagement.data.local.dao.StudentDao;
import com.example.studentlabgroupmanagement.data.local.dao.CourseDao;
import com.example.studentlabgroupmanagement.data.local.dao.LabGroupDao;
import com.example.studentlabgroupmanagement.data.local.dao.LabDao;
import com.example.studentlabgroupmanagement.data.local.dao.SubmissionDao;
import com.example.studentlabgroupmanagement.data.local.dao.OperationDao;
import com.example.studentlabgroupmanagement.data.local.dao.AnnouncementDao;
import com.example.studentlabgroupmanagement.data.local.dao.LecturerDao;

@Database(entities = {
        StudentEntity.class,
        CourseEntity.class,
        LabGroupEntity.class,
        LabEntity.class,
        SubmissionEntity.class,
        OperationEntity.class,
        AnnouncementEntity.class,
        LecturerEntity.class
}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract StudentDao studentDao();
    public abstract CourseDao courseDao();
    public abstract LabGroupDao labGroupDao();
    public abstract LabDao labDao();
    public abstract SubmissionDao submissionDao();
    public abstract OperationDao operationDao();
    public abstract AnnouncementDao announcementDao();
    public abstract LecturerDao lecturerDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "student_lab_room.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}