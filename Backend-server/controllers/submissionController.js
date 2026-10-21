const db = require('../config/db');

// Create or sync a submission
exports.createSubmission = async (req, res) => {
    const { id, lab_id } = req.body;
    const studentIdentifier = req.body.student_id || req.body.studentId || req.body.studentNumber;

    // Support both file_path and file_url naming conventions from Android
    const file_path = req.body.file_path || req.body.filePath || req.body.file_url || req.body.fileUrl;

    const { status, grade, feedback, submitted_at, checked_at } = req.body;

    if (!lab_id || !studentIdentifier) {
        return res.status(400).json({ message: 'Lab ID and Student ID (or studentNumber) are required for submissions' });
    }

    try {
        let resolvedStudentId = studentIdentifier;

        // Resolve student identifier against id, student_number, or username in MySQL to prevent foreign key errors
        const [studentRows] = await db.execute(
            'SELECT id FROM students WHERE id = ? OR student_number = ? OR username = ?',
            [studentIdentifier, studentIdentifier, studentIdentifier]
        );

        if (studentRows.length > 0) {
            resolvedStudentId = studentRows[0].id; // Maps to the actual MySQL primary key ID
        } else {
            return res.status(404).json({ message: 'Student record not found in MySQL for identifier: ' + studentIdentifier });
        }

        // If an ID was provided by Room, use upsert logic matching actual MySQL columns
        const query = id ?
            `INSERT INTO submissions (id, lab_id, student_id, file_path, submitted_at, status, checked_at, grade, feedback)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
             ON DUPLICATE KEY UPDATE file_path = VALUES(file_path), status = VALUES(status), checked_at = VALUES(checked_at), grade = VALUES(grade), feedback = VALUES(feedback)` :
            `INSERT INTO submissions (lab_id, student_id, file_path, submitted_at, status, checked_at, grade, feedback)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?)`;

        const currentTime = new Date().toISOString().slice(0, 19).replace('T', ' ');

        const params = id ?
            [
                id,
                lab_id,
                resolvedStudentId,
                file_path || '',
                submitted_at || currentTime,
                status || 'PENDING',
                checked_at || null,
                grade || null,
                feedback || ''
            ] :
            [
                lab_id,
                resolvedStudentId,
                file_path || '',
                submitted_at || currentTime,
                status || 'PENDING',
                checked_at || null,
                grade || null,
                feedback || ''
            ];

        const [result] = await db.execute(query, params);

        res.status(201).json({
            message: 'Submission synced successfully',
            submissionId: result.insertId || id
        });
    } catch (error) {
        console.error('Error syncing submission:', error);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};

// Get all submissions
exports.getAllSubmissions = async (req, res) => {
    try {
        const [rows] = await db.execute('SELECT * FROM submissions');
        res.json(rows);
    } catch (error) {
        console.error('Error fetching submissions:', error);
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};