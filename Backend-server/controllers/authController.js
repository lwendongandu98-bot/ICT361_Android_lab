const db = require('../config/db');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const crypto = require('crypto');

// Register or sync Student
exports.registerStudent = async (req, res) => {
    let full_name = req.body.full_name || req.body.fullName || req.body.name;
    const username = req.body.username;
    let student_number = req.body.student_number || req.body.studentNumber || req.body.studentNum;
    const email = req.body.email;
    const password = req.body.password;
    const program = req.body.program || req.body.selectedProgram;
    let groupParam = req.body.lab_group_id || req.body.labGroupId || req.body.selectedGroup || req.body.labGroup;

    if (!username || !password || !student_number || !email) {
        console.error('Validation failed: Missing fields for student registration:', req.body);
        return res.status(400).json({ message: 'Required fields are missing' });
    }

    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        let resolvedGroupId = null;

        // Resolve group name to database ID safely
        if (groupParam && groupParam !== "Select Lab Group" && groupParam !== "No lab groups available") {
            if (!isNaN(groupParam)) {
                resolvedGroupId = parseInt(groupParam);
            } else {
                const [groupRows] = await db.execute('SELECT id FROM lab_groups WHERE group_name = ?', [groupParam]);
                if (groupRows.length > 0) {
                    resolvedGroupId = groupRows[0].id;
                }
            }
        }

        // Check if student already exists by username or student_number
        const [existingStudents] = await db.execute(
            'SELECT id FROM students WHERE username = ? OR student_number = ?',
            [username, student_number]
        );

        let result;
        if (existingStudents.length > 0) {
            // Update existing record explicitly
            const studentId = existingStudents[0].id;
            [result] = await db.execute(
                `UPDATE students
                 SET full_name = ?, email = ?, password = ?, program = ?, lab_group_id = ?
                 WHERE id = ?`,
                [full_name || '', email, hashedPassword, program || '', resolvedGroupId, studentId]
            );
            console.log(`Updated existing student record for username: ${username}`);
        } else {
            // Insert new record explicitly
            [result] = await db.execute(
                `INSERT INTO students (full_name, username, student_number, email, password, program, lab_group_id)
                 VALUES (?, ?, ?, ?, ?, ?, ?)`,
                [full_name || '', username, student_number, email, hashedPassword, program || '', resolvedGroupId]
            );
            console.log(`Inserted new student record into MySQL for username: ${username}`);
        }

        res.status(201).json({
            message: 'Student registered/synced successfully',
            studentId: result.insertId || existingStudents[0]?.id || null
        });
    } catch (error) {
        console.error('CRITICAL ERROR registering student in MySQL:', error);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};

// Register or sync Lecturer
exports.registerLecturer = async (req, res) => {
    const full_name = req.body.full_name || req.body.fullName || req.body.name;
    const employee_number = req.body.employee_number || req.body.employeeNumber;
    const department = req.body.department;
    const email = req.body.email;
    const phone_number = req.body.phone_number || req.body.phoneNumber;
    const password = req.body.password;

    if (!employee_number || !password || !email) {
        return res.status(400).json({ message: 'Required fields are missing' });
    }

    try {
        const hashedPassword = await bcrypt.hash(password, 10);

        const [result] = await db.execute(
            `INSERT INTO lecturers (full_name, employee_number, department, email, phone_number, password)
             VALUES (?, ?, ?, ?, ?, ?)
             ON DUPLICATE KEY UPDATE department = VALUES(department), phone_number = VALUES(phone_number)`,
            [full_name || '', employee_number, department || '', email, phone_number || '', hashedPassword]
        );

        res.status(201).json({
            message: 'Lecturer registered/synced successfully',
            lecturerId: result.insertId || null
        });
    } catch (error) {
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(409).json({ message: 'Employee number or email already exists' });
        }
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};

// Login Account (Checks both Students and Lecturers with diagnostic logs)
exports.login = async (req, res) => {
    console.log("========== LOGIN ATTEMPT RECEIVED ==========");
    console.log("Full req.body received from Android:", req.body);

    const rawUsername = req.body.username || req.body.email || req.body.student_number || req.body.studentNumber || req.body.employee_number;
    const password = req.body.password;

    console.log("Extracted rawUsername/identifier:", rawUsername);
    console.log("Password provided length:", password ? password.length : 0);

    if (!rawUsername || !password) {
        console.error("Login validation failed: Missing username or password");
        return res.status(400).json({ message: 'Username/Student Number and password are required' });
    }

    const username = String(rawUsername).trim();

    try {
        let user = null;
        let role = '';

        // Flexible student lookup across username, student_number, email, or full_name with trimming
        const [studentRows] = await db.execute(
            `SELECT * FROM students
             WHERE TRIM(username) = ?
                OR TRIM(student_number) = ?
                OR TRIM(email) = ?
                OR TRIM(full_name) = ?`,
            [username, username, username, username]
        );

        console.log("Student rows found in DB:", studentRows.length);

        if (studentRows.length > 0) {
            user = studentRows[0];
            role = 'Student';
        } else {
            // Flexible lecturer lookup
            const [lecturerRows] = await db.execute(
                `SELECT * FROM lecturers
                 WHERE TRIM(employee_number) = ?
                    OR TRIM(email) = ?
                    OR TRIM(full_name) = ?`,
                [username, username, username]
            );

            console.log("Lecturer rows found in DB:", lecturerRows.length);

            if (lecturerRows.length > 0) {
                user = lecturerRows[0];
                role = 'Lecturer';
            }
        }

        if (!user) {
            console.log("Login failed: User not found for identifier:", username);
            return res.status(401).json({ message: 'Invalid credentials or user does not exist' });
        }

        console.log("User found in database with ID:", user.id, "Role:", role);

        // Android's offline-first queue support for SHA-256 vs direct bcrypt match
        const sha256Password = crypto.createHash('sha256').update(password, 'utf8').digest('hex');
        const directMatch = await bcrypt.compare(password, user.password);
        const queuedHashMatch = directMatch ? false : await bcrypt.compare(sha256Password, user.password);
        const isMatch = directMatch || queuedHashMatch;

        console.log("Password check results -> Direct match:", directMatch, "Queued hash match:", queuedHashMatch);

        if (!isMatch) {
            console.log("Login failed: Password mismatch for user:", username);
            return res.status(401).json({ message: 'Invalid credentials' });
        }

        const secret = process.env.JWT_SECRET || 'ict361_default_jwt_secret';
        const userId = user.id;

        const token = jwt.sign(
            { id: userId, username: user.username || user.employee_number, role },
            secret,
            { expiresIn: '1d' }
        );

        console.log("Login successful for user:", username);

        res.json({
            message: 'Login successful',
            token,
            user: {
                id: userId,
                name: user.full_name || user.username,
                role
            }
        });
    } catch (error) {
        console.error("Login server error:", error);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};