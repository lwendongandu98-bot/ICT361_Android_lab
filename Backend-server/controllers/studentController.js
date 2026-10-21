const db = require('../config/db');
const bcrypt = require('bcryptjs'); // Standardized to bcryptjs for consistency

// Create or sync a Student (Safely handles duplicates without crashing background sync)
exports.createStudent = async (req, res) => {
    console.log("========== STUDENT SYNC REQUEST ==========");
    console.log(req.body);

    const full_name = req.body.full_name || req.body.fullName || req.body.name;
    const username = req.body.username;
    const student_number = req.body.student_number || req.body.studentNumber || req.body.studentNum;
    const email = req.body.email;
    const password = req.body.password;
    const program = req.body.program;
    const lab_group_id = req.body.lab_group_id || req.body.labGroupId || req.body.groupId;
    const role = req.body.role || 'Student';

    if (!username || !password || !student_number || !email) {
        return res.status(400).json({
            message: 'Required fields are missing'
        });
    }

    try {
        const hashedPassword = await bcrypt.hash(password, 10);

        // Check if student already exists by email, username, or student_number
        const [existingStudents] = await db.execute(
            `SELECT id FROM students WHERE email = ? OR username = ? OR student_number = ?`,
            [email, username, student_number]
        );

        let studentId;

        if (existingStudents.length > 0) {
            // Student exists: Update their record safely
            studentId = existingStudents[0].id;
            await db.execute(
                `UPDATE students
                 SET full_name = ?, username = ?, student_number = ?, email = ?, password = ?, program = ?, lab_group_id = ?
                 WHERE id = ?`,
                [
                    full_name || '',
                    username,
                    student_number,
                    email,
                    hashedPassword,
                    program || '',
                    lab_group_id || null,
                    studentId
                ]
            );
            console.log("Student updated successfully:", username);
        } else {
            // Student does not exist: Insert new record
            const [result] = await db.execute(
                `INSERT INTO students (full_name, username, student_number, email, password, program, lab_group_id)
                 VALUES (?, ?, ?, ?, ?, ?, ?)`,
                [
                    full_name || '',
                    username,
                    student_number,
                    email,
                    hashedPassword,
                    program || '',
                    lab_group_id || null
                ]
            );
            studentId = result.insertId;
            console.log("Student inserted successfully:", username);
        }

        res.status(200).json({
            success: true,
            message: 'Student registered/synced successfully',
            studentId: studentId
        });

    } catch (error) {
        console.error("========== STUDENT SYNC ERROR ==========");
        console.error(error);

        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(409).json({
                success: false,
                message: 'Username, student number, or email already exists'
            });
        }

        res.status(500).json({
            success: false,
            message: 'Server error',
            error: error.message
        });
    }
};

exports.joinGroup = async (req, res) => {
    const student_id = req.body.student_id || req.body.studentId;
    const lab_group_id = req.body.lab_group_id || req.body.labGroupId;

    if (!student_id || !lab_group_id) {
        return res.status(400).json({
            message: 'student_id and lab_group_id are required'
        });
    }

    const connection = await db.getConnection();

    try {
        await connection.beginTransaction();

        const [groupRows] = await connection.execute(
            'SELECT max_occupancy FROM lab_groups WHERE id = ?',
            [lab_group_id]
        );

        if (groupRows.length === 0) {
            await connection.rollback();
            return res.status(404).json({
                message: 'Lab group not found'
            });
        }

        const maxOccupancy = groupRows[0].max_occupancy;

        const [countRows] = await connection.execute(
            'SELECT COUNT(*) AS member_count FROM students WHERE lab_group_id = ?',
            [lab_group_id]
        );

        if (countRows[0].member_count >= maxOccupancy) {
            await connection.rollback();
            return res.status(400).json({
                code: 'GROUP_FULL',
                message: `Lab group has reached its maximum capacity of ${maxOccupancy} members.`
            });
        }

        const [result] = await connection.execute(
            'UPDATE students SET lab_group_id = ? WHERE id = ?',
            [lab_group_id, student_id]
        );

        if (result.affectedRows === 0) {
            await connection.rollback();
            return res.status(404).json({
                message: 'Student record not found'
            });
        }

        await connection.commit();

        return res.status(200).json({
            message: 'Successfully joined lab group'
        });

    } catch (error) {
        await connection.rollback();
        console.error('Error joining group:', error);

        return res.status(500).json({
            message: 'Database error',
            error: error.message
        });
    } finally {
        connection.release();
    }
};

exports.searchStudents = async (req, res) => {
    const { search, program, group_id } = req.query;

    try {
        let query = `
            SELECT
                s.id,
                s.username,
                s.full_name AS name,
                s.student_number AS studentNum,
                s.email,
                s.password,
                s.program,
                g.group_name AS labGroup,
                'Student' AS role
            FROM students s
            LEFT JOIN lab_groups g ON s.lab_group_id = g.id
            WHERE 1=1
        `;

        let queryParams = [];

        if (search) {
            query += ` AND (s.full_name LIKE ? OR s.student_number LIKE ?)`;
            queryParams.push(`%${search}%`, `%${search}%`);
        }

        if (program) {
            query += ` AND s.program = ?`;
            queryParams.push(program);
        }

        if (group_id && group_id !== 'Unassigned') {
            query += ` AND s.lab_group_id = ?`;
            queryParams.push(group_id);
        }

        const [rows] = await db.execute(query, queryParams);

        res.status(200).json(rows);

    } catch (error) {
        console.error('Error searching students:', error);

        res.status(500).json({
            message: 'Database error',
            error: error.message
        });
    }
};

exports.getAllStudents = async (req, res) => {
    try {
        const [students] = await db.execute(`
            SELECT
                s.id,
                s.username,
                s.full_name AS name,
                s.student_number AS studentNum,
                s.email,
                s.password,
                s.program,
                COALESCE(g.group_name, 'Unassigned') AS labGroup,
                'Student' AS role
            FROM students s
            LEFT JOIN lab_groups g ON s.lab_group_id = g.id
        `);

        res.status(200).json(students);

    } catch (error) {
        console.error('Error fetching students:', error);

        res.status(500).json({
            message: 'Database error',
            error: error.message
        });
    }
};