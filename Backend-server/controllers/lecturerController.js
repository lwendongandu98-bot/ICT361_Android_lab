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