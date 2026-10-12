const db = require('../config/db');

exports.createLab = async (req, res) => {
    const title = req.body.title;
    const description = req.body.description;
    const due_date = req.body.due_date || req.body.dueDate;
    const file_path = req.body.file_path || req.body.filePath;
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
    const student_id = req.body.student_id || req.body.studentId || req.body.studentNumber;
    const file_path = req.body.file_path || req.body.filePath;

    if (!lab_id || !student_id || !file_path) {
        return res.status(400).json({ message: 'lab_id, student_id, and file_path are required' });
    }

    try {
        const [result] = await db.execute(
            'INSERT INTO submissions (lab_id, student_id, file_path, status) VALUES (?, ?, ?, ?)',
            [lab_id, student_id, file_path, 'PENDING']
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