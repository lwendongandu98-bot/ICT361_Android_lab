const db = require('../config/db');

exports.getAllCourses = async (req, res) => {
    try {
        const [courses] = await db.execute('SELECT * FROM courses');
        res.status(200).json(courses);
    } catch (error) {
        console.error('Error fetching courses:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};

exports.createCourse = async (req, res) => {
    const course_code = req.body.course_code || req.body.courseCode;
    const course_name = req.body.course_name || req.body.courseName;

    if (!course_code || !course_name) {
        return res.status(400).json({ message: 'Course code and course name are required' });
    }

    try {
        const [result] = await db.execute(
            `INSERT INTO courses (course_code, course_name) VALUES (?, ?)
             ON DUPLICATE KEY UPDATE course_name = VALUES(course_name)`,
            [course_code, course_name]
        );

        res.status(201).json({
            message: 'Course synced successfully',
            courseId: result.insertId || null
        });
    } catch (error) {
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(409).json({ message: 'Course code already exists' });
        }
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};