const db = require('../config/db');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

// Register Student
exports.registerStudent = async (req, res) => {
    const { full_name, username, student_number, email, password, program, lab_group_id } = req.body;

    if (!username || !password || !student_number || !email) {
        return res.status(400).json({ message: 'Required fields are missing' });
    }

    try {
        const hashedPassword = await bcrypt.hash(password, 10);

        const [result] = await db.execute(
            `INSERT INTO students (full_name, username, student_number, email, password, program, lab_group_id)
             VALUES (?, ?, ?, ?, ?, ?, ?)`,
            [full_name, username, student_number, email, hashedPassword, program, lab_group_id || null]
        );

        res.status(201).json({
            message: 'Student registered successfully',
            studentId: result.insertId
        });
    } catch (error) {
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(400).json({ message: 'Username, student number, or email already exists' });
        }
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};

// Register Lecturer
exports.registerLecturer = async (req, res) => {
    const { full_name, employee_number, department, email, phone_number, password } = req.body;

    if (!employee_number || !password || !email) {
        return res.status(400).json({ message: 'Required fields are missing' });
    }

    try {
        const hashedPassword = await bcrypt.hash(password, 10);

        const [result] = await db.execute(
            `INSERT INTO lecturers (full_name, employee_number, department, email, phone_number, password)
             VALUES (?, ?, ?, ?, ?, ?)`,
            [full_name, employee_number, department, email, phone_number, hashedPassword]
        );

        res.status(201).json({
            message: 'Lecturer registered successfully',
            lecturerId: result.insertId
        });
    } catch (error) {
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(400).json({ message: 'Employee number or email already exists' });
        }
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};

// Login Account (Checks both Students and Lecturers)
exports.login = async (req, res) => {
    const { username, password } = req.body; // Can be student username or lecturer employee_number

    if (!username || !password) {
        return res.status(400).json({ message: 'Username/Employee number and password are required' });
    }

    try {
        let user = null;
        let role = '';

        // Check in Students table first
        const [studentRows] = await db.execute(
            'SELECT * FROM students WHERE username = ? OR student_number = ?',
            [username, username]
        );

        if (studentRows.length > 0) {
            user = studentRows[0];
            role = 'Student';
        } else {
            // Check in Lecturers table
            const [lecturerRows] = await db.execute(
                'SELECT * FROM lecturers WHERE employee_number = ? OR email = ?',
                [username, username]
            );

            if (lecturerRows.length > 0) {
                user = lecturerRows[0];
                role = 'Lecturer';
            }
        }

        if (!user) {
            return res.status(401).json({ message: 'Invalid credentials' });
        }

        const isMatch = await bcrypt.compare(password, user.password);

        if (!isMatch) {
            return res.status(401).json({ message: 'Invalid credentials' });
        }

        const secret = process.env.JWT_SECRET || 'ict361_default_jwt_secret';
        const userId = user.id;

        const token = jwt.sign(
            { id: userId, username: user.username || user.employee_number, role },
            secret,
            { expiresIn: '1d' }
        );

        res.json({
            message: 'Login successful',
            token,
            user: {
                id: userId,
                name: user.full_name,
                role
            }
        });
    } catch (error) {
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};