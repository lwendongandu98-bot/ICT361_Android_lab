const db = require('../config/db');

exports.createLab = async (req, res) => {
    const title = req.body.title;
    const description = req.body.description;
    const due_date = req.body.due_date || req.body.dueDate;
    // Accept multiple naming conventions for file path/url consistency
    const file_path = req.body.file_path || req.body.filePath || req.body.file_url || req.body.fileUrl;
    const status = req.body.status;

    if (!title || !due_date) {
        return res.status(400).json({ message: 'Title and due date are required' });
    }

    try {
        const [result] = await db.execute(
            'INSERT INTO labs (title, description, due_date, file_path, status) VALUES (?, ?, ?, ?, ?)',
            [title, description || null, due_date, file_path || null, status || 'DRAFT']
        );

        res.status(201).json({
            message: 'Lab created successfully',
            labId: result.insertId
        });
    } catch (error) {
        console.error('Error creating lab:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};

exports.getAllLabs = async (req, res) => {
    try {
        const [labs] = await db.execute('SELECT * FROM labs ORDER BY created_at DESC');
        res.status(200).json(labs);
    } catch (error) {
        console.error('Error fetching labs:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};

exports.submitLab = async (req, res) => {
    const lab_id = req.body.lab_id || req.body.labId;
    const studentIdentifier = req.body.student_id || req.body.studentId || req.body.studentNumber;
    const file_path = req.body.file_path || req.body.filePath || req.body.file_url || req.body.fileUrl;

    if (!lab_id || !studentIdentifier || !file_path) {
        return res.status(400).json({ message: 'lab_id, student_id (or studentNumber), and file_path are required' });
    }

    try {
        let resolvedStudentId = studentIdentifier;

        // If the Android app passed a student number string instead of a numeric ID, resolve it safely
        if (isNaN(studentIdentifier)) {
            const [studentRows] = await db.execute(
                'SELECT id FROM students WHERE student_number = ? OR username = ?',
                [studentIdentifier, studentIdentifier]
            );

            if (studentRows.length > 0) {
                resolvedStudentId = studentRows[0].id;
            } else {
                return res.status(404).json({ message: 'Student record not found for the provided identifier' });
            }
        }

        const [result] = await db.execute(
            'INSERT INTO submissions (lab_id, student_id, file_path, status) VALUES (?, ?, ?, ?)',
            [lab_id, resolvedStudentId, file_path, 'PENDING']
        );

        res.status(201).json({
            message: 'Lab submitted successfully',
            submissionId: result.insertId
        });
    } catch (error) {
        console.error('Error submitting lab:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};

exports.getSubmissionsByLab = async (req, res) => {
    const { labId } = req.params;

    try {
        const [submissions] = await db.execute(`
            SELECT s.id, s.file_path, s.submitted_at, s.status,
                   st.full_name AS student_name, st.student_number
            FROM submissions s
            JOIN students st ON s.student_id = st.id
            WHERE s.lab_id = ?
        `, [labId]);

        res.status(200).json(submissions);
    } catch (error) {
        console.error('Error fetching submissions:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};