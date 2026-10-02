package com.example.studentlabgroupmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // 1. Increment the database version when updating the schema layout
    private static final String DATABASE_NAME = "LabManagement.db";
    private static final int DATABASE_VERSION = 2;

    // Table and Columns names
    private static final String TABLE_STUDENTS = "students";
    private static final String COL_USERNAME = "username";
    private static final String COL_NAME = "name";
    private static final String COL_STUDENT_NUM = "student_num";
    private static final String COL_EMAIL = "email"; // Added Email recovery column tracking reference
    private static final String COL_PASSWORD = "password";
    private static final String COL_PROGRAM = "program";
    private static final String COL_LAB_GROUP = "lab_group";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table structure containing all fields
        String CREATE_STUDENTS_TABLE = "CREATE TABLE " + TABLE_STUDENTS + "("
                + COL_USERNAME + " TEXT PRIMARY KEY,"
                + COL_NAME + " TEXT,"
                + COL_STUDENT_NUM + " TEXT,"
                + COL_EMAIL + " TEXT," // Injected database structural initialization parameter tracking column entry
                + COL_PASSWORD + " TEXT,"
                + COL_PROGRAM + " TEXT,"
                + COL_LAB_GROUP + " TEXT" + ")";
        db.execSQL(CREATE_STUDENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drops old data schemas and recreates them fresh upon configuration increments
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        onCreate(db);
    }

    // 2. Updated insert method signature containing 7 arguments to resolve the compilation error
    public boolean insertStudent(String username, String name, String studentNum, String email, String password, String program, String labGroup) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_NAME, name);
        contentValues.put(COL_STUDENT_NUM, studentNum);
        contentValues.put(COL_EMAIL, email); // Store the email string value parameter
        contentValues.put(COL_PASSWORD, password);
        contentValues.put(COL_PROGRAM, program);
        contentValues.put(COL_LAB_GROUP, labGroup);

        long result = db.insert(TABLE_STUDENTS, null, contentValues);
        return result != -1; // Returns true if insertion succeeded, false if username already exists
    }

    // Existing login check utility logic block module
    public boolean checkUserLogin(String username, String password, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Adjust column verification constraints depending on validation rules if needed
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STUDENTS + " WHERE "
                + COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Added: Security method to look up a forgotten password based on username and verification email
    public String getRecoveredPassword(String username, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String recoveredPassword = null;

        // Query fields matching BOTH unique input keys safely
        Cursor cursor = db.rawQuery("SELECT " + COL_PASSWORD + " FROM " + TABLE_STUDENTS
                + " WHERE " + COL_USERNAME + "=? AND " + COL_EMAIL + "=?", new String[]{username, email});

        if (cursor.moveToFirst()) {
            recoveredPassword = cursor.getString(0); // Retrieve match string data from password column offset
        }
        cursor.close();
        return recoveredPassword;
    }
}
