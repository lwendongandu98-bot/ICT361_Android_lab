const db = require('../config/db');

// 1. Create or Update Student Profile
exports.createStudentProfile = async (req, res) => {
  const { student_id, student_number, user_id, full_name, program_id, year_of_study, labgroup_id } = req.body;

  if (!student_id || !student_number || !user_id || !full_name || !year_of_study) {
    return res.status(400).json({
      message: 'student_id, student_number, user_id, full_name, and year_of_study are required'
    });
  }

  // Validate 9-digit student number
  const cleanStudentNumber = student_number.toString().trim();
  if (!/^\d{9}$/.test(cleanStudentNumber)) {
    return res.status(400).json({ message: 'Student number must be a required string of exactly 9 digits.' });
  }

  try {
    const [existing] = await db.execute('SELECT * FROM students WHERE student_id = ?', [student_id]);

    if (existing.length > 0) {
      await db.execute(
        `UPDATE students
         SET full_name = ?, program_id = ?, year_of_study = ?, labgroup_id = ?
         WHERE student_id = ? AND is_deleted = 0`,
        [full_name, program_id || null, year_of_study, labgroup_id || null, student_id]
      );
      return res.status(200).json({ message: 'Student profile updated successfully' });
    }

    await db.execute(
      `INSERT INTO students (student_id, student_number, user_id, full_name, program_id, year_of_study, labgroup_id)
       VALUES (?, ?, ?, ?, ?, ?, ?)`,
      [student_id, cleanStudentNumber, user_id, full_name, program_id || null, year_of_study, labgroup_id || null]
    );

    res.status(201).json({ message: 'Student profile created successfully' });
  } catch (error) {
    console.error('Error in student profile:', error);

    if (error.code === 'ER_DUP_ENTRY') {
      return res.status(400).json({ message: 'Student number or User ID already exists.' });
    }

    if (error.errno === 1452 || error.code === 'ER_NO_REFERENCED_ROW_2') {
      return res.status(400).json({
        message: 'Invalid reference ID provided. Ensure user_id, program_id, and labgroup_id exist.'
      });
    }

    res.status(500).json({ message: 'Database error', error: error.message });
  }
};

// 2. Join or Transfer Lab Group (Enforces 15-member max with transaction lock)
exports.joinGroup = async (req, res) => {
  const { student_id, labgroup_id } = req.body;

  if (!student_id || !labgroup_id) {
    return res.status(400).json({ message: 'student_id and labgroup_id are required' });
  }

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    // Lock rows and check target lab group capacity
    const [countRows] = await connection.execute(
      `SELECT COUNT(*) AS member_count
       FROM students
       WHERE labgroup_id = ? AND is_deleted = 0
       FOR UPDATE`,
      [labgroup_id]
    );

    if (countRows[0].member_count >= 15) {
      await connection.rollback();
      return res.status(400).json({
        code: 'GROUP_FULL',
        message: 'Lab group has reached its maximum capacity of 15 members.'
      });
    }

    // Assign student
    const [result] = await connection.execute(
      'UPDATE students SET labgroup_id = ? WHERE student_id = ? AND is_deleted = 0',
      [labgroup_id, student_id]
    );

    if (result.affectedRows === 0) {
      await connection.rollback();
      return res.status(404).json({ message: 'Active student record not found' });
    }

    await connection.commit();
    res.status(200).json({ message: 'Successfully joined lab group' });

  } catch (error) {
    await connection.rollback();
    console.error('Error joining group:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  } finally {
    connection.release();
  }
};

// 3. Lecturer Search & Multi-Filter
exports.searchStudents = async (req, res) => {
  const { search, programme, group } = req.query;

  try {
    let query = `
      SELECT s.student_id, s.student_number, s.full_name, s.year_of_study,
             p.program_code, g.group_name
      FROM students s
      LEFT JOIN programs p ON s.program_id = p.program_id
      LEFT JOIN labgroup g ON s.labgroup_id = g.labgroup_id
      WHERE s.is_deleted = 0
    `;

    const queryParams = [];

    if (search) {
      query += ` AND (s.full_name LIKE ? OR s.student_number LIKE ?)`;
      queryParams.push(`%${search.trim()}%`, `%${search.trim()}%`);
    }

    if (programme) {
      query += ` AND p.program_code = ?`;
      queryParams.push(programme);
    }

    if (group === 'Unassigned') {
      query += ` AND s.labgroup_id IS NULL`;
    } else if (group) {
      query += ` AND g.group_name = ?`;
      queryParams.push(group);
    }

    const [rows] = await db.execute(query, queryParams);
    res.status(200).json(rows);

  } catch (error) {
    console.error('Error searching students:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  }
};

// 4. Soft Delete Student
exports.softDeleteStudent = async (req, res) => {
  const { student_id } = req.params;

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    const [result] = await connection.execute(
      `UPDATE students
       SET is_deleted = 1, deleted_at = NOW(), labgroup_id = NULL
       WHERE student_id = ? AND is_deleted = 0`,
      [student_id]
    );

    if (result.affectedRows === 0) {
      await connection.rollback();
      return res.status(404).json({ message: 'Student not found or already deleted' });
    }

    // Disable linked account
    await connection.execute(
      `UPDATE accounts a
       JOIN students s ON a.account_id = s.user_id
       SET a.role = 'disabled'
       WHERE s.student_id = ?`,
      [student_id]
    );

    await connection.commit();
    res.status(200).json({ message: 'Student record soft deleted successfully' });

  } catch (error) {
    await connection.rollback();
    console.error('Error deleting student:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  } finally {
    connection.release();
  }
};

// 5. Get All Active Students
exports.getAllStudents = async (req, res) => {
  try {
    const [students] = await db.execute(`
      SELECT s.student_id, s.student_number, s.full_name, s.year_of_study, g.group_name
      FROM students s
      LEFT JOIN labgroup g ON s.labgroup_id = g.labgroup_id
      WHERE s.is_deleted = 0
    `);
    res.status(200).json(students);
  } catch (error) {
    console.error('Error fetching students:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  }
};