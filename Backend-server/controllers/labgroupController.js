const db = require('../config/db');

exports.createGroup = async (req, res) => {
  const { group_name, project_topic } = req.body;

  if (!group_name || !group_name.trim()) {
    return res.status(400).json({ message: 'group_name is required' });
  }

  try {
    const [result] = await db.execute(
      'INSERT INTO labgroup (group_name, project_topic) VALUES (?, ?)',
      [group_name.trim(), project_topic || null]
    );
    res.status(201).json({
      message: 'Lab group created',
      labgroup_id: result.insertId
    });
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};

exports.getAllGroups = async (req, res) => {
  try {
    const [rows] = await db.execute('SELECT * FROM labgroup');
    res.status(200).json(rows);
  } catch (error) {
    res.status(500).json({ message: 'Server error', error: error.message });
  }
};