const db = require('../config/db');

// Create or sync a submission
exports.createSubmission = async (req, res) => {
    const id = req.body.id;

    let labParam =
        req.body.lab_id ||
        req.body.labId;

    const studentIdentifier =
        req.body.student_id ||
        req.body.studentId ||
        req.body.studentNumber ||
        req.body.student_number;

    const file_path =
        req.body.file_path ||
        req.body.filePath ||
        req.body.file_url ||
        req.body.fileUrl;

    const submitted_at =
        req.body.submitted_at ||
        req.body.submittedAt;

    const checked_at =
        req.body.checked_at ||
        req.body.checkedAt;

    const status = req.body.status;
    const grade = req.body.grade;
    const feedback = req.body.feedback;

    console.log("========== SUBMISSION RECEIVED ==========");
    console.log(req.body);

    if (!labParam || !studentIdentifier) {
        return res.status(400).json({
            message: 'Lab ID and Student ID are required',
            received: req.body
        });
    }

    try {
        // 1. Resolve Student ID safely (handles whitespace, id, student_number, username, or full_name)
        const cleanIdentifier = studentIdentifier ? String(studentIdentifier).trim() : '';

        const [studentRows] = await db.execute(
            `SELECT id FROM students
             WHERE id = ?
                OR student_number = ?
                OR username = ?
                OR full_name = ?`,
            [cleanIdentifier, cleanIdentifier, cleanIdentifier, cleanIdentifier]
        );

        if (studentRows.length === 0) {
            console.log("Student not found for identifier:", cleanIdentifier);
            return res.status(404).json({
                message: "Student not found for identifier: " + cleanIdentifier
            });
        }

        const resolvedStudentId = studentRows[0].id;

        // 2. Resolve Lab ID safely (handles numeric ID or lab title/name)
        let resolvedLabId = null;
        if (!isNaN(labParam)) {
            resolvedLabId = parseInt(labParam);
        } else {
            const [labRows] = await db.execute(
                `SELECT id FROM labs WHERE title = ? OR lab_name = ?`,
                [labParam, labParam]
            );
            if (labRows.length > 0) {
                resolvedLabId = labRows[0].id;
            } else {
                resolvedLabId = labParam;
            }
        }

        const currentTime = new Date()
            .toISOString()
            .slice(0, 19)
            .replace('T', ' ');

        const query = id
            ? `
                INSERT INTO submissions
                (id, lab_id, student_id, file_path, submitted_at, status, checked_at, grade, feedback)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    file_path = VALUES(file_path),
                    status = VALUES(status),
                    checked_at = VALUES(checked_at),
                    grade = VALUES(grade),
                    feedback = VALUES(feedback)
              `
            : `
                INSERT INTO submissions
                (lab_id, student_id, file_path, submitted_at, status, checked_at, grade, feedback)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
              `;

        const params = id
            ? [
                id,
                resolvedLabId,
                resolvedStudentId,
                file_path || '',
                submitted_at || currentTime,
                status || 'PENDING',
                checked_at || null,
                grade || null,
                feedback || ''
            ]
            : [
                resolvedLabId,
                resolvedStudentId,
                file_path || '',
                submitted_at || currentTime,
                status || 'PENDING',
                checked_at || null,
                grade || null,
                feedback || ''
            ];

        console.log("========== SQL PARAMS ==========");
        console.log(params);

        const [result] = await db.execute(query, params);

        console.log("Submission inserted successfully:", result.insertId);

        return res.status(201).json({
            message: "Submission synced successfully",
            submissionId: result.insertId || id
        });

    } catch (error) {
        console.error("Error syncing submission:", error);
        return res.status(500).json({
            message: "Server error",
            error: error.message
        });
    }
};

// Get all submissions with student details joined
exports.getAllSubmissions = async (req, res) => {
    try {
        const [rows] = await db.execute(`
            SELECT
                sub.*,
                st.full_name AS student_name,
                st.username AS student_username,
                st.student_number AS student_number,
                st.email AS student_email,
                st.program AS student_program
            FROM submissions sub
            LEFT JOIN students st ON sub.student_id = st.id
        `);

        res.status(200).json(rows);
    } catch (error) {
        console.error('Error fetching submissions:', error);
        res.status(500).json({
            message: 'Server error',
            error: error.message
        });
    }
};