const db = require('../config/db');

// Create or update student profile
exports.createStudentProfile = async (req, res) => {
  const { student_id, user_id, full_name, program_id, year_of_study, labgroup_id } = req.body;

  if (!student_id || !user_id || !full_name || !year_of_study) {
    return res.status(400).json({ message: 'student_id, user_id, full_name, and year_of_study are required' });
  }

  try {
    const [existing] = await db.execute('SELECT * FROM students WHERE student_id = ?', [student_id]);

    if (existing.length > 0) {
      await db.execute(
        'UPDATE students SET full_name = ?, program_id = ?, year_of_study = ?, labgroup_id = ? WHERE student_id = ?',
        [full_name, program_id || null, year_of_study, labgroup_id || null, student_id]
      );
      return res.status(200).json({ message: 'Student profile updated successfully' });
    }

    await db.execute(
      'INSERT INTO students (student_id, user_id, full_name, program_id, year_of_study, labgroup_id) VALUES (?, ?, ?, ?, ?, ?)',
      [student_id, user_id, full_name, program_id || null, year_of_study, labgroup_id || null]
    );

    res.status(201).json({ message: 'Student profile created successfully' });
  } catch (error) {
    console.error('Error in student profile:', error);

    // Handle foreign key constraint failure (errno 1452)
    if (error.errno === 1452 || error.code === 'ER_NO_REFERENCED_ROW_2') {
      return res.status(400).json({
        message: 'Invalid reference ID provided. Ensure the user_id, program_id, and labgroup_id exist in the database.'
      });
    }

    res.status(500).json({ message: 'Database error', error: error.message });
  }
};

// Join a Lab Group
exports.joinGroup = async (req, res) => {
  const { student_id, labgroup_id } = req.body;

  if (!student_id || !labgroup_id) {
    return res.status(400).json({ message: 'student_id and labgroup_id are required' });
  }

  try {
    const [result] = await db.execute(
      'UPDATE students SET labgroup_id = ? WHERE student_id = ?',
      [labgroup_id, student_id]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'Student record not found' });
    }

    res.status(200).json({ message: 'Successfully joined lab group' });
  } catch (error) {
    console.error('Error joining group:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  }
};

// Get all students with group details
exports.getAllStudents = async (req, res) => {
  try {
    const [students] = await db.execute(`
      SELECT s.student_id, s.full_name, s.year_of_study, g.group_name, g.project_topic
      FROM students s
      LEFT JOIN labgroup g ON s.labgroup_id = g.labgroup_id
    `);
    res.status(200).json(students);
  } catch (error) {
    console.error('Error fetching students:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  }
};