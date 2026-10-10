const db = require('../config/db');

// Get all courses (used for dropdowns/spinners)
exports.getAllCourses = async (req, res) => {
    try {
        const [courses] = await db.execute('SELECT * FROM courses');
        res.status(200).json(courses);
    } catch (error) {
        console.error('Error fetching courses:', error);
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};

// Create a new course
exports.createCourse = async (req, res) => {
    const { course_code, course_name } = req.body;

    if (!course_code || !course_name) {
        return res.status(400).json({ message: 'Course code and course name are required' });
    }

    try {
        const [result] = await db.execute(
            'INSERT INTO courses (course_code, course_name) VALUES (?, ?)',
            [course_code, course_name]
        );

        res.status(201).json({
            message: 'Course created successfully',
            courseId: result.insertId
        });
    } catch (error) {
        if (error.code === 'ER_DUP_ENTRY') {
            return res.status(400).json({ message: 'Course code already exists' });
        }
        res.status(500).json({ message: 'Database error', error: error.message });
    }
};