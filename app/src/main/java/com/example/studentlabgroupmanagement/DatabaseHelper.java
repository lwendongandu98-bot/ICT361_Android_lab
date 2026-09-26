package com.example.studentlabgroupmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LabManagement.db";
    private static final int DATABASE_VERSION = 1;

    // --- TABLE STUDENTS ---
    private static final String TABLE_STUDENTS = "students";
    private static final String COL_STU_USER = "username";
    private static final String COL_STU_NAME = "name";
    private static final String COL_STU_NUM = "student_number";
    private static final String COL_STU_PASS = "password";
    private static final String COL_STU_PROG = "program";
    private static final String COL_STU_GROUP = "lab_group";

    // --- TABLE LECTURERS ---
    private static final String TABLE_LECTURERS = "lecturers";
    private static final String COL_LEC_USER = "username";
    private static final String COL_LEC_NAME = "name";
    private static final String COL_LEC_ID = "lecturer_id";
    private static final String COL_LEC_PASS = "password";
    private static final String COL_LEC_PROG = "program";

    // --- TABLE GROUPS ---
    private static final String TABLE_GROUPS = "lab_groups";
    private static final String COL_GRP_NAME = "group_name";
    private static final String COL_GRP_MAX = "max_members";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Students Table
        String createStudentTable = "CREATE TABLE " + TABLE_STUDENTS + " (" +
                COL_STU_USER + " TEXT PRIMARY KEY, " +
                COL_STU_NAME + " TEXT, " +
                COL_STU_NUM + " TEXT, " +
                COL_STU_PASS + " TEXT, " +
                COL_STU_PROG + " TEXT, " +
                COL_STU_GROUP + " TEXT)";
        db.execSQL(createStudentTable);

        // Create Lecturers Table
        String createLecturerTable = "CREATE TABLE " + TABLE_LECTURERS + " (" +
                COL_LEC_USER + " TEXT PRIMARY KEY, " +
                COL_LEC_NAME + " TEXT, " +
                COL_LEC_ID + " TEXT, " +
                COL_LEC_PASS + " TEXT, " +
                COL_LEC_PROG + " TEXT)";
        db.execSQL(createLecturerTable);

        // Create Lab Groups Table
        String createGroupTable = "CREATE TABLE " + TABLE_GROUPS + " (" +
                COL_GRP_NAME + " TEXT PRIMARY KEY, " +
                COL_GRP_MAX + " INTEGER)";
        db.execSQL(createGroupTable);

        // Seed initial default groups as seen on layout 11 (Max 15 rules)
        db.execSQL("INSERT INTO " + TABLE_GROUPS + " VALUES ('Group 1', 15)");
        db.execSQL("INSERT INTO " + TABLE_GROUPS + " VALUES ('Group 2', 15)");
        db.execSQL("INSERT INTO " + TABLE_GROUPS + " VALUES ('Group 3', 15)");
        db.execSQL("INSERT INTO " + TABLE_GROUPS + " VALUES ('Group 4', 15)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LECTURERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
        onCreate(db);
    }

    // 1. Register a Student
    public boolean insertStudent(String username, String name, String stuNum, String password, String program, String labGroup) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (checkUserExists(username, "Student")) return false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_STU_USER, username);
        contentValues.put(COL_STU_NAME, name);
        contentValues.put(COL_STU_NUM, stuNum);
        contentValues.put(COL_STU_PASS, password);
        contentValues.put(COL_STU_PROG, program);
        contentValues.put(COL_STU_GROUP, labGroup);

        long result = db.insert(TABLE_STUDENTS, null, contentValues);
        return result != -1;
    }

    // 2. Register a Lecturer
    public boolean insertLecturer(String username, String name, String lecId, String password, String program) {
        SQLiteDatabase db = this.getWritableDatabase();
        if (checkUserExists(username, "Lecturer")) return false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_LEC_USER, username);
        contentValues.put(COL_LEC_NAME, name);
        contentValues.put(COL_LEC_ID, lecId);
        contentValues.put(COL_LEC_PASS, password);
        contentValues.put(COL_LEC_PROG, program);

        long result = db.insert(TABLE_LECTURERS, null, contentValues);
        return result != -1;
    }

    // 3. Helper to prevent duplicates
    public boolean checkUserExists(String username, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        String table = role.equalsIgnoreCase("Student") ? TABLE_STUDENTS : TABLE_LECTURERS;
        String query = "SELECT * FROM " + table + " WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // 4. Validate Credentials during Login Phase
    public boolean checkUserLogin(String username, String password, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        String table = role.equalsIgnoreCase("Student") ? TABLE_STUDENTS : TABLE_LECTURERS;
        String query = "SELECT * FROM " + table + " WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});
        boolean matched = cursor.getCount() > 0;
        cursor.close();
        return matched;
    }
}

