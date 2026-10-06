const db = require('../config/db');

exports.getAllPrograms = async (req, res) => {
  try {
    const [rows] = await db.execute('SELECT * FROM programs');
    res.status(200).json(rows);
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};