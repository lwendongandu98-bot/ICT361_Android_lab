const db = require('../config/db');
const bcrypt = require('bcryptjs'); // Import bcrypt for secure hashing

exports.registerLecturer = async (req, res) => {
    console.log("==> Incoming Lecturer Registration Payload:", req.body);

    const { employee_number, full_name, department, email, phone_number, password } = req.body;

    if (!employee_number || !full_name || !password) {
        return res.status(400).json({ message: 'Employee number, full name, and password are required' });
    }

    try {
        // 1. Generate a salt and hash the password securely
        const salt = await bcrypt.genSalt(10);
        const hashedPassword = await bcrypt.hash(password, salt);

        // 2. Insert the HASHED password into MySQL instead of plaintext
        const [result] = await db.execute(
            `INSERT INTO lecturers (employee_number, full_name, department, email, phone_number, password)
             VALUES (?, ?, ?, ?, ?, ?)`,
            [employee_number, full_name, department, email, phone_number, hashedPassword]
        );

        res.status(201).json({ message: 'Lecturer registered successfully', id: result.insertId });
    } catch (error) {
        console.error("==> Database Error during Lecturer Reg:", error.message);
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(409).json({ message: 'Lecturer already exists' });
        }
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};

// Return a lecturer profile using the field names expected by the Android profile UI.
exports.getLecturerProfile = async (req, res) => {
    const employeeNumber = req.params.employeeNumber;
    try {
        const [rows] = await db.execute(
            `SELECT employee_number AS username, full_name AS name, email,
                    department AS program, phone_number AS studentNum, 'Lecturer' AS role
             FROM lecturers WHERE employee_number = ? OR email = ?`,
            [employeeNumber, employeeNumber]
        );
        if (!rows.length) return res.status(404).json({ message: 'Lecturer not found' });
        return res.json(rows[0]);
    } catch (error) {
        console.error('Error fetching lecturer profile:', error);
        return res.status(500).json({ message: 'Database error' });
    }
};

// Update only profile fields; never replace the stored password from a profile form.
exports.updateLecturerProfile = async (req, res) => {
    const employeeNumber = req.body.username || req.body.employee_number || req.body.employeeNumber;
    const fullName = req.body.name || req.body.full_name || req.body.fullName;
    const email = req.body.email;
    const department = req.body.program || req.body.department;
    const phoneNumber = req.body.studentNum || req.body.phone_number || req.body.phoneNumber;
    if (!employeeNumber || !fullName || !email) {
        return res.status(400).json({ message: 'Employee number, name, and email are required' });
    }
    try {
        const [result] = await db.execute(
            `UPDATE lecturers SET full_name = ?, email = ?, department = ?, phone_number = ?
             WHERE employee_number = ?`,
            [fullName, email, department || '', phoneNumber || '', employeeNumber]
        );
        if (!result.affectedRows) return res.status(404).json({ message: 'Lecturer not found' });
        return res.json({
            username: employeeNumber, name: fullName, email,
            program: department || '', studentNum: phoneNumber || '', role: 'Lecturer'
        });
    } catch (error) {
        console.error('Error updating lecturer profile:', error);
        if (error.code === 'ER_DUP_ENTRY') return res.status(409).json({ message: 'Email already exists' });
        return res.status(500).json({ message: 'Database error' });
    }
};
