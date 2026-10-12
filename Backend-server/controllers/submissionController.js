const db = require('../config/db');

// Create or sync a submission
exports.createSubmission = async (req, res) => {
    const { id, lab_id, student_id, file_url, status, grade, feedback } = req.body;

    if (!lab_id || !student_id) {
        return res.status(400).json({ message: 'Lab ID and Student ID are required for submissions' });
    }

    try {
        // If an ID was provided by Room, use upsert logic
        const query = id ?
            `INSERT INTO submissions (id, lab_id, student_id, file_url, status, grade, feedback)
             VALUES (?, ?, ?, ?, ?, ?, ?)
             ON DUPLICATE KEY UPDATE file_url = VALUES(file_url), status = VALUES(status), grade = VALUES(grade), feedback = VALUES(feedback)` :
            `INSERT INTO submissions (lab_id, student_id, file_url, status, grade, feedback)
             VALUES (?, ?, ?, ?, ?, ?)`;

        const params = id ?
            [id, lab_id, student_id, file_url || '', status || 'Pending', grade || null, feedback || ''] :
            [lab_id, student_id, file_url || '', status || 'Pending', grade || null, feedback || ''];

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
        res.status(500).json({ message: 'Server error', error: error.message });
    }
};