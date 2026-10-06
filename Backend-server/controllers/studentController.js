const db = require('../config/db');

// CHANGE ONLY THESE if your students table uses different names
const T = {
  table: 'students',
  id: 'student_id',
  user: 'user_id',
  name: 'full_name',
  number: 'student_number',
  program: 'program_id',
  group: 'labgroup_id',
  deleted: 'deleted_at'
};

const isLecturer = (req) => req.user.role === 'lecturer';

// ADD (lecturer only)
exports.createStudent = async (req, res) => {
  const { name, number, program_id, labgroup_id } = req.body;
  const cleanNumber = (number || '').trim();

  if (!name || name.trim().length < 2 || name.trim().length > 100) {
    return res.status(400).json({ message: 'Name must be 2-100 characters' });
  }
  if (!/^\d{9}$/.test(cleanNumber)) {
    return res.status(400).json({ message: 'Student number must be exactly 9 digits' });
  }

  try {
    const [result] = await db.execute(
      `INSERT INTO ${T.table} (${T.name}, ${T.number}, ${T.program}, ${T.group}) VALUES (?, ?, ?, ?)`,
      [name.trim(), cleanNumber, program_id || null, labgroup_id || null]
    );
    res.status(201).json({ message: 'Student added', student_id: result.insertId });
  } catch (error) {
    if (error.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ message: 'Student number already exists' });
    }
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// LIST + SEARCH + FILTER (lecturer only)
exports.getStudents = async (req, res) => {
  const { q, program_id, labgroup_id } = req.query;
  const page = Math.max(parseInt(req.query.page) || 1, 1);
  const limit = 20;
  const offset = (page - 1) * limit;

  let sql = `SELECT * FROM ${T.table} WHERE ${T.deleted} IS NULL`;
  const params = [];

  if (q) {
    sql += ` AND (${T.name} LIKE ? OR ${T.number} LIKE ?)`;
    params.push(`%${q}%`, `%${q}%`);
  }
  if (program_id) {
    sql += ` AND ${T.program} = ?`;
    params.push(program_id);
  }
  if (labgroup_id === 'unassigned') {
    sql += ` AND ${T.group} IS NULL`;
  } else if (labgroup_id) {
    sql += ` AND ${T.group} = ?`;
    params.push(labgroup_id);
  }
  sql += ` LIMIT ${limit} OFFSET ${offset}`;

  try {
    const [rows] = await db.execute(sql, params);
    res.status(200).json({ page, students: rows });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// VIEW ONE (owner or lecturer)
exports.getStudent = async (req, res) => {
  try {
    const [rows] = await db.execute(
      `SELECT * FROM ${T.table} WHERE ${T.id} = ? AND ${T.deleted} IS NULL`,
      [req.params.id]
    );
    if (rows.length === 0) return res.status(404).json({ message: 'Student not found' });

    const student = rows[0];
    if (!isLecturer(req) && student[T.user] !== req.user.id) {
      return res.status(403).json({ error: 'FORBIDDEN' });
    }
    res.status(200).json(student);
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// EDIT (owner: name + programme only; lecturer: also student number)
exports.updateStudent = async (req, res) => {
  const { name, number, program_id } = req.body;

  try {
    const [rows] = await db.execute(
      `SELECT * FROM ${T.table} WHERE ${T.id} = ? AND ${T.deleted} IS NULL`,
      [req.params.id]
    );
    if (rows.length === 0) return res.status(404).json({ message: 'Student not found' });

    const student = rows[0];
    if (!isLecturer(req) && student[T.user] !== req.user.id) {
      return res.status(403).json({ error: 'FORBIDDEN' });
    }

    const newName = name ? name.trim() : student[T.name];
    if (newName.length < 2 || newName.length > 100) {
      return res.status(400).json({ message: 'Name must be 2-100 characters' });
    }

    let newNumber = student[T.number];
    if (number !== undefined) {
      if (!isLecturer(req)) {
        return res.status(403).json({ message: 'Only a lecturer can change a student number' });
      }
      newNumber = number.trim();
      if (!/^\d{9}$/.test(newNumber)) {
        return res.status(400).json({ message: 'Student number must be exactly 9 digits' });
      }
    }

    await db.execute(
      `UPDATE ${T.table} SET ${T.name} = ?, ${T.number} = ?, ${T.program} = ? WHERE ${T.id} = ?`,
      [newName, newNumber, program_id || student[T.program], req.params.id]
    );
    res.status(200).json({ message: 'Student updated' });
  } catch (error) {
    if (error.code === 'ER_DUP_ENTRY') {
      return res.status(409).json({ message: 'Student number already exists' });
    }
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

// DELETE = soft delete (lecturer only)
exports.deleteStudent = async (req, res) => {
  try {
    const [result] = await db.execute(
      `UPDATE ${T.table} SET ${T.deleted} = NOW(), ${T.group} = NULL WHERE ${T.id} = ? AND ${T.deleted} IS NULL`,
      [req.params.id]
    );
    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'Student not found or already deleted' });
    }
    res.status(200).json({ message: 'Student deleted' });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};