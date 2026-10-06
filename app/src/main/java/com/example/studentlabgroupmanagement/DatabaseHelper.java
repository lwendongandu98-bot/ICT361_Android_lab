package com.example.studentlabgroupmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LabManagement.db";
    // Incremented version to 5 to ensure fresh database schema creation with new operational tables
    private static final int DATABASE_VERSION = 5;

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
        // Core users table for Authentication
        String CREATE_USERS_TABLE =
                "CREATE TABLE " + TABLE_USERS + "("
                        + COL_USERNAME + " TEXT PRIMARY KEY,"
                        + COL_NAME + " TEXT,"
                        + COL_STUDENT_NUM + " TEXT,"
                        + COL_EMAIL + " TEXT,"
                        + COL_PASSWORD + " TEXT,"
                        + COL_PROGRAM + " TEXT,"
                        + COL_LAB_GROUP + " TEXT DEFAULT 'Unassigned',"
                        + COL_ROLE + " TEXT"
                        + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Lab Groups Table
        db.execSQL("CREATE TABLE lab_groups (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "group_name TEXT UNIQUE, " +
                "description TEXT)");

        // Labs Table
        db.execSQL("CREATE TABLE labs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lab_title TEXT, " +
                "lab_description TEXT, " +
                "due_date TEXT)");

        // Assignments Table
        db.execSQL("CREATE TABLE assignments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "description TEXT, " +
                "max_marks INTEGER, " +
                "deadline TEXT)");

        // Announcements Table
        db.execSQL("CREATE TABLE announcements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "message TEXT, " +
                "date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        // Submissions Table
        db.execSQL("CREATE TABLE submissions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "assignment_id INTEGER, " +
                "student_number TEXT, " +
                "submission_text TEXT, " +
                "grade TEXT DEFAULT 'Not Graded')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS lab_groups");
        db.execSQL("DROP TABLE IF EXISTS labs");
        db.execSQL("DROP TABLE IF EXISTS assignments");
        db.execSQL("DROP TABLE IF EXISTS announcements");
        db.execSQL("DROP TABLE IF EXISTS submissions");
        onCreate(db);
    }

    // --- EXISTING AUTHENTICATION METHODS ---

    public boolean insertUser(
            String username,
            String name,
            String studentNum,
            String email,
            String password,
            String program,
            String labGroup,
            String role) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_USERNAME, username);
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_NUM, studentNum);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        values.put(COL_PROGRAM, program);
        values.put(COL_LAB_GROUP, (labGroup == null || labGroup.isEmpty()) ? "Unassigned" : labGroup);
        values.put(COL_ROLE, role);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUserLogin(String username, String password, String role) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_USERS
                            + " WHERE " + COL_USERNAME + "=? AND "
                            + COL_PASSWORD + "=? AND "
                            + COL_ROLE + "=?",
                    new String[]{username, password, role});

            if (cursor != null && cursor.moveToFirst()) {
                exists = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return exists;
    }

    public String getRecoveredPassword(String username, String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String recoveredPassword = "";

        try {
            cursor = db.rawQuery(
                    "SELECT " + COL_PASSWORD + " FROM " + TABLE_USERS
                            + " WHERE " + COL_USERNAME + "=? AND " + COL_EMAIL + "=?",
                    new String[]{username, email});

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(COL_PASSWORD);
                if (index != -1) {
                    recoveredPassword = cursor.getString(index);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return recoveredPassword;
    }

    // --- NEW OPERATIONAL MANAGEMENT QUERIES ---

    // Validates if group allocations have hit the 15 student max capacity limit rule
    public boolean checkGroupCapacity(String groupName) {
        if (groupName == null || groupName.equals("Unassigned") || groupName.isEmpty()) return true;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_LAB_GROUP + " = ? AND " + COL_ROLE + " = 'Student'", new String[]{groupName});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count < 15;
    }

    // Filters down student users profiles depending on search string input, program streams or group filters
    public Cursor getFilteredStudents(String searchQuery, String programFilter, String groupFilter) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder query = new StringBuilder("SELECT username AS id, " + COL_NAME + " AS student_name, " + COL_STUDENT_NUM + " AS student_number, " + COL_PROGRAM + " AS program_of_study, " + COL_LAB_GROUP + " AS lab_group FROM " + TABLE_USERS + " WHERE " + COL_ROLE + " = 'Student'");
        ArrayList<String> args = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            query.append(" AND (" + COL_NAME + " LIKE ? OR " + COL_STUDENT_NUM + " LIKE ?)");
            args.add("%" + searchQuery + "%");
            args.add("%" + searchQuery + "%");
        }

        if (programFilter != null && !programFilter.equals("All Programs")) {
            query.append(" AND " + COL_PROGRAM + " = ?");
            args.add(programFilter);
        }

        if (groupFilter != null && !groupFilter.equals("All Groups")) {
            query.append(" AND " + COL_LAB_GROUP + " = ?");
            args.add(groupFilter);
        }

        return db.rawQuery(query.toString(), args.toArray(new String[0]));
    }

    // Handles deletions safely from primary registration listings
    public boolean deleteStudent(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USERS, COL_USERNAME + " = ?", new String[]{username});
        return result > 0;
    }
}
