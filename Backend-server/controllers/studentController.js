const db = require('../config/db');

// 1. Join or Transfer Lab Group (Enforces 15-member max occupancy)
exports.joinGroup = async (req, res) => {
  const { student_id, lab_group_id } = req.body;

  if (!student_id || !lab_group_id) {
    return res.status(400).json({ message: 'student_id and lab_group_id are required' });
  }

  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    // Check target group capacity limit
    const [groupRows] = await connection.execute(
      'SELECT max_occupancy FROM lab_groups WHERE id = ?',
      [lab_group_id]
    );

    if (groupRows.length === 0) {
      await connection.rollback();
      return res.status(404).json({ message: 'Lab group not found' });
    }

    const maxOccupancy = groupRows[0].max_occupancy;

    // Count current members in this group
    const [countRows] = await connection.execute(
      `SELECT COUNT(*) AS member_count FROM students WHERE lab_group_id = ?`,
      [lab_group_id]
    );

    if (countRows[0].member_count >= maxOccupancy) {
      await connection.rollback();
      return res.status(400).json({
        code: 'GROUP_FULL',
        message: `Lab group has reached its maximum capacity of ${maxOccupancy} members.`
      });
    }

    // Assign student to the lab group
    const [result] = await connection.execute(
      'UPDATE students SET lab_group_id = ? WHERE id = ?',
      [lab_group_id, student_id]
    );

    if (result.affectedRows === 0) {
      await connection.rollback();
      return res.status(404).json({ message: 'Student record not found' });
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

// 2. Search Students for Lecturers
exports.searchStudents = async (req, res) => {
  const { search, program, group_id } = req.query;

  try {
    let query = `
      SELECT s.id, s.student_number, s.full_name, s.username, s.program, g.group_name
      FROM students s
      LEFT JOIN lab_groups g ON s.lab_group_id = g.id
      WHERE 1=1
    `;

    const queryParams = [];

    if (search) {
      query += ` AND (s.full_name LIKE ? OR s.student_number LIKE ?)`;
      queryParams.push(`%${search.trim()}%`, `%${search.trim()}%`);
    }

    if (program) {
      query += ` AND s.program = ?`;
      queryParams.push(program);
    }

    if (group_id === 'Unassigned') {
      query += ` AND s.lab_group_id IS NULL`;
    } else if (group_id) {
      query += ` AND s.lab_group_id = ?`;
      queryParams.push(group_id);
    }

    const [rows] = await db.execute(query, queryParams);
    res.status(200).json(rows);

  } catch (error) {
    console.error('Error searching students:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  }
};

// 3. Get All Students
exports.getAllStudents = async (req, res) => {
  try {
    const [students] = await db.execute(`
      SELECT s.id, s.student_number, s.full_name, s.username, s.program, g.group_name
      FROM students s
      LEFT JOIN lab_groups g ON s.lab_group_id = g.id
    `);
    res.status(200).json(students);
  } catch (error) {
    console.error('Error fetching students:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  }
};