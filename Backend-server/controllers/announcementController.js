const db = require('../config/db');

// Get all announcements
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

// Create a new announcement (Lecturer action)
exports.createAnnouncement = async (req, res) => {
    const { title, content, lecturer_id } = req.body;

    if (!title || !content) {
        return res.status(400).json({ message: 'Title and content are required' });
    }

    try {
        const [result] = await db.execute(
            'INSERT INTO announcements (title, content, lecturer_id) VALUES (?, ?, ?)',
            [title, content, lecturer_id || null]
        );

        res.status(201).json({
            message: 'Announcement posted successfully',
            announcementId: result.insertId
        });
    } catch (error) {
        console.error('Error creating announcement:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};