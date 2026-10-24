const db = require('../config/db');

exports.getAllAnnouncements = async (req, res) => {
    try {
        const [announcements] = await db.execute(`
            SELECT a.id, a.title, a.content, a.created_at, l.full_name AS lecturer_name
            FROM announcements a
            LEFT JOIN lecturers l ON a.lecturer_id = l.id
            ORDER BY a.created_at DESC
        `);
        res.status(200).json(announcements);
    } catch (error) {
        console.error('Error fetching announcements:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};

exports.createAnnouncement = async (req, res) => {
    const { id, title } = req.body;
    const content = req.body.content || req.body.message;
    const lecturerIdentifier = req.body.lecturer_id || req.body.lecturerId;

    if (!title || !content) {
        return res.status(400).json({ message: 'Title and content are required' });
    }

    try {
        let resolvedLecturerId = lecturerIdentifier || null;

        // Resolve lecturer employee number/username string to database numeric ID if necessary
        if (resolvedLecturerId && isNaN(resolvedLecturerId)) {
            const [lecturerRows] = await db.execute(
                'SELECT id FROM lecturers WHERE employee_number = ? OR email = ?',
                [resolvedLecturerId, resolvedLecturerId]
            );

            if (lecturerRows.length > 0) {
                resolvedLecturerId = lecturerRows[0].id;
            } else {
                resolvedLecturerId = null; // Fallback to null if not found
            }
        }

        // Support offline-first Room database sync via upsert logic if an ID is present
        const query = id ?
            `INSERT INTO announcements (id, title, content, lecturer_id)
             VALUES (?, ?, ?, ?)
             ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), lecturer_id = VALUES(lecturer_id)` :
            `INSERT INTO announcements (title, content, lecturer_id)
             VALUES (?, ?, ?)`;

        const params = id ?
            [id, title, content, resolvedLecturerId] :
            [title, content, resolvedLecturerId];

        const [result] = await db.execute(query, params);

        res.status(201).json({
            message: 'Announcement posted/synced successfully',
            announcementId: result.insertId || id
        });
    } catch (error) {
        console.error('Error creating announcement:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};