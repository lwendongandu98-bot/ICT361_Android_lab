const db = require('../config/db');

// 1. Create a new lab group (CREATE)
exports.createGroup = async (req, res) => {
  const { group_name, max_occupancy } = req.body;

  if (!group_name) {
    return res.status(400).json({ message: 'Group name is required' });
  }

  try {
    const [result] = await db.execute(
      'INSERT INTO lab_groups (group_name, max_occupancy) VALUES (?, ?)',
      [group_name, max_occupancy || 15]
    );

    res.status(201).json({
      message: 'Group created successfully',
      groupId: result.insertId
    });
  } catch (error) {
    console.error('Error creating group:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  }
};

// 2. Fetch all lab groups (READ)
exports.getAllGroups = async (req, res) => {
  try {
    const [groups] = await db.execute('SELECT * FROM lab_groups');
    res.status(200).json(groups);
  } catch (error) {
    console.error('Error fetching groups:', error);
    res.status(500).json({ message: 'Database error', error: error.message });
  }
};

// 3. Delete a lab group by ID (DELETE)
exports.deleteGroup = async (req, res) => {
  const { id } = req.params;

  try {
    const [result] = await db.execute('DELETE FROM lab_groups WHERE id = ?', [id]);

    if (result.affectedRows === 0) {
      return res.status(404).json({ message: 'Group not found' });
    }

    res.status(200).json({ message: 'Group deleted successfully' });
  } catch (error) {
    console.error('Error deleting group:', error);

    if (error.errno === 1451 || error.code === 'ER_ROW_IS_REFERENCED_2') {
      return res.status(400).json({
        message: 'Cannot delete group because students are currently assigned to it.'
      });
    }

    res.status(500).json({ message: 'Database error', error: error.message });
  }
};