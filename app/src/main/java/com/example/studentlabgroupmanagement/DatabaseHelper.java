package com.example.studentlabgroupmanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LabManagement.db";
    private static final int DATABASE_VERSION = 11;

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
                        + COL_LAB_GROUP + " TEXT DEFAULT 'Unassigned',"
                        + COL_ROLE + " TEXT"
                        + ")";
        db.execSQL(CREATE_USERS_TABLE);

        db.execSQL("CREATE TABLE lab_groups (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "group_name TEXT UNIQUE, " +
                "description TEXT)");

        db.execSQL("CREATE TABLE courses (" +
                "course_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "course_code TEXT, " +
                "course_name TEXT)");

        db.execSQL("CREATE TABLE labs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lab_title TEXT, " +
                "lab_description TEXT, " +
                "due_date TEXT, " +
                "file_path TEXT, " +
                "status TEXT DEFAULT 'DRAFT', " +
                "date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE assignments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "description TEXT, " +
                "max_marks INTEGER, " +
                "deadline TEXT)");

        db.execSQL("CREATE TABLE announcements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "message TEXT, " +
                "date_posted TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE TABLE submissions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "lab_id INTEGER, " +
                "student_number TEXT, " +
                "file_path TEXT, " +
                "submitted_at TEXT, " +
                "grade TEXT DEFAULT 'Not Graded', " +
                "status TEXT DEFAULT 'PENDING', " +
                "checked_at TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS lab_groups");
        db.execSQL("DROP TABLE IF EXISTS courses");
        db.execSQL("DROP TABLE IF EXISTS labs");
        db.execSQL("DROP TABLE IF EXISTS assignments");
        db.execSQL("DROP TABLE IF EXISTS announcements");
        db.execSQL("DROP TABLE IF EXISTS submissions");
        onCreate(db);
    }

    // --- COURSE MANAGEMENT METHODS ---

    public boolean insertCourse(String code, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("course_code", code);
        values.put("course_name", name);
        long result = db.insert("courses", null, values);
        return result != -1;
    }

    public List<String> getAllCourses() {
        List<String> courseList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM courses ORDER BY course_name ASC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex("course_name");
                int codeIdx = cursor.getColumnIndex("course_code");
                do {
                    String name = (nameIdx != -1 && !cursor.isNull(nameIdx)) ? cursor.getString(nameIdx) : "";
                    String code = (codeIdx != -1 && !cursor.isNull(codeIdx)) ? cursor.getString(codeIdx) : "";

                    if (!code.isEmpty() && !name.isEmpty()) {
                        courseList.add(code + " - " + name);
                    } else if (!name.isEmpty()) {
                        courseList.add(name);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return courseList;
    }

    // --- AUTHENTICATION & PROFILE METHODS ---

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
                            + " WHERE " + COL_USERNAME + "=? COLLATE NOCASE AND "
                            + COL_PASSWORD + "=? AND "
                            + COL_ROLE + "=? COLLATE NOCASE",
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

    /**
     * Retrieves full user profile information (Name, Program) for Dashboard/Header.
     */
    public Cursor getUserProfile(String userIdentifier) {
        if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
            return null;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COL_NAME + " AS student_name, " + COL_PROGRAM + " AS program " +
                        "FROM " + TABLE_USERS + " " +
                        "WHERE " + COL_USERNAME + " = ? OR " + COL_STUDENT_NUM + " = ?",
                new String[]{userIdentifier, userIdentifier});
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

    // --- GROUP MANAGEMENT & REGISTRATION QUERIES ---

    /**
     * Retrieves all lab groups directly from the 'lab_groups' table.
     */
    public List<String> getAllGroups() {
        List<String> groupList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT group_name FROM lab_groups ORDER BY group_name ASC", null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int colIndex = cursor.getColumnIndex("group_name");
                do {
                    if (colIndex != -1 && !cursor.isNull(colIndex)) {
                        groupList.add(cursor.getString(colIndex));
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return groupList;
    }

    /**
     * Retrieves lecturer-created lab groups from 'lab_groups' that have fewer than 15 members.
     * Full groups (>= 15 members) are filtered out automatically.
     */
    public List<String> getAvailableLabGroups() {
        List<String> availableGroups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT lg.group_name " +
                "FROM lab_groups lg " +
                "LEFT JOIN " + TABLE_USERS + " u ON lg.group_name = u." + COL_LAB_GROUP + " AND u." + COL_ROLE + " = 'Student' " +
                "GROUP BY lg.group_name " +
                "HAVING COUNT(u." + COL_USERNAME + ") < 15 " +
                "ORDER BY lg.group_name ASC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int colIndex = cursor.getColumnIndex("group_name");
                if (colIndex != -1) {
                    availableGroups.add(cursor.getString(colIndex));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return availableGroups;
    }

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

    /**
     * Gets the lab group assigned to a specific student username or student number.
     */
    public String getStudentGroup(String userIdentifier) {
        if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
            return "Unassigned";
        }

        SQLiteDatabase db = this.getReadableDatabase();
        String group = "Unassigned";
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_LAB_GROUP + " FROM " + TABLE_USERS +
                        " WHERE " + COL_USERNAME + " = ? OR " + COL_STUDENT_NUM + " = ?",
                new String[]{userIdentifier, userIdentifier});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(COL_LAB_GROUP);
                if (idx != -1 && !cursor.isNull(idx)) {
                    group = cursor.getString(idx);
                }
            }
            cursor.close();
        }
        return group;
    }

    /**
     * Retrieves all student members belonging to a specific lab group.
     */
    public Cursor getGroupMembers(String groupName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_NAME + " AS student_name, "
                + COL_STUDENT_NUM + " AS student_number, "
                + COL_EMAIL + " AS contact "
                + "FROM " + TABLE_USERS
                + " WHERE " + COL_LAB_GROUP + " = ? AND " + COL_ROLE + " = 'Student' "
                + "ORDER BY " + COL_NAME + " ASC";

        return db.rawQuery(query, new String[]{groupName});
    }

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

    public boolean deleteStudent(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_USERS, COL_USERNAME + " = ?", new String[]{username});
        return result > 0;
    }

    // --- LAB MANAGEMENT METHODS ---

    public boolean saveLab(
            String title,
            String description,
            String dueDate,
            String filePath,
            String status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("lab_title", title);
        values.put("lab_description", description);
        values.put("due_date", dueDate);
        values.put("file_path", filePath);
        values.put("status", status);

        long result = db.insert("labs", null, values);
        return result != -1;
    }

    public Cursor getAllLabs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM labs ORDER BY id DESC", null);
    }

    public Cursor getDraftLabs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM labs WHERE status='DRAFT' ORDER BY id DESC", null);
    }

    public Cursor getPublishedLabs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM labs WHERE status='PUBLISHED' ORDER BY id DESC", null);
    }

    public boolean publishLab(int labId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "PUBLISHED");
        int result = db.update("labs", values, "id = ?", new String[]{String.valueOf(labId)});
        return result > 0;
    }

    public boolean deleteLab(int labId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("labs", "id = ?", new String[]{String.valueOf(labId)});
        return result > 0;
    }

    public boolean updateLab(int labId, String title, String description, String dueDate, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lab_title", title);
        values.put("lab_description", description);
        values.put("due_date", dueDate);
        values.put("file_path", filePath);

        int result = db.update("labs", values, "id = ?", new String[]{String.valueOf(labId)});
        return result > 0;
    }

    // --- SUBMISSION METHODS ---

    public boolean saveSubmission(int labId, String studentNumber, String filePath, String submittedAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("lab_id", labId);
        values.put("student_number", studentNumber);
        values.put("file_path", filePath);
        values.put("submitted_at", submittedAt);
        values.put("status", "PENDING");

        long result = db.insert("submissions", null, values);
        return result != -1;
    }

    public Cursor getAllSubmissions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM submissions ORDER BY id DESC", null);
    }

    public Cursor getPendingSubmissions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM submissions WHERE status = 'PENDING' OR status IS NULL ORDER BY id DESC", null);
    }

    public Cursor getCheckedSubmissions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM submissions WHERE status = 'CHECKED' ORDER BY checked_at DESC, id DESC", null);
    }

    public boolean markSubmissionAsChecked(int submissionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", "CHECKED");

        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        values.put("checked_at", currentDateTime);

        int result = db.update("submissions", values, "id = ?", new String[]{String.valueOf(submissionId)});
        return result > 0;
    }

    public Cursor getSubmissionsByStudent(String studentNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM submissions WHERE student_number = ? ORDER BY id DESC", new String[]{studentNumber});
    }
}