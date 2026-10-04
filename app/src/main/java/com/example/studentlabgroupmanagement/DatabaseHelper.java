package com.example.studentlabgroupmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LabManagement.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";

    private static final String COL_USERNAME = "username";
    private static final String COL_NAME = "name";
    private static final String COL_STUDENT_NUM = "student_num";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_PROGRAM = "program";
    private static final String COL_LAB_GROUP = "lab_group";
    private static final String COL_ROLE = "role";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_USERS_TABLE =
                "CREATE TABLE " + TABLE_USERS + "("
                        + COL_USERNAME + " TEXT PRIMARY KEY,"
                        + COL_NAME + " TEXT,"
                        + COL_STUDENT_NUM + " TEXT,"
                        + COL_EMAIL + " TEXT,"
                        + COL_PASSWORD + " TEXT,"
                        + COL_PROGRAM + " TEXT,"
                        + COL_LAB_GROUP + " TEXT,"
                        + COL_ROLE + " TEXT"
                        + ")";

        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {

        db.execSQL(
                "DROP TABLE IF EXISTS " + TABLE_USERS
        );

        onCreate(db);
    }

    public boolean insertUser(
            String username,
            String name,
            String studentNum,
            String email,
            String password,
            String program,
            String labGroup,
            String role) {

        SQLiteDatabase db =
                this.getWritableDatabase();

        ContentValues values =
                new ContentValues();

        values.put(COL_USERNAME, username);
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_NUM, studentNum);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        values.put(COL_PROGRAM, program);
        values.put(COL_LAB_GROUP, labGroup);
        values.put(COL_ROLE, role);

        long result =
                db.insert(TABLE_USERS,
                        null,
                        values);

        return result != -1;
    }

    public boolean checkUserLogin(
            String username,
            String password,
            String role) {

        SQLiteDatabase db =
                this.getReadableDatabase();

        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM " + TABLE_USERS
                                + " WHERE "
                                + COL_USERNAME + "=? AND "
                                + COL_PASSWORD + "=? AND "
                                + COL_ROLE + "=?",
                        new String[]{
                                username,
                                password,
                                role
                        });

        boolean exists =
                cursor.getCount() > 0;

        cursor.close();

        return exists;
    }

    public String getRecoveredPassword(
            String username,
            String email) {

        SQLiteDatabase db =
                this.getReadableDatabase();

        String recoveredPassword = null;

        Cursor cursor =
                db.rawQuery(
                        "SELECT "
                                + COL_PASSWORD
                                + " FROM "
                                + TABLE_USERS
                                + " WHERE "
                                + COL_USERNAME
                                + "=? AND "
                                + COL_EMAIL
                                + "=?",
                        new String[]{
                                username,
                                email
                        });

        if(cursor.moveToFirst()) {
            recoveredPassword =
                    cursor.getString(0);
        }

        cursor.close();

        return recoveredPassword;
    }
}