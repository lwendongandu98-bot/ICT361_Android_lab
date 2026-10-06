CREATE DATABASE IF NOT EXISTS ict361_lab_db;
USE ict361_lab_db;

CREATE TABLE IF NOT EXISTS Accounts (
    Account_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'student',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
<<<<<<< Updated upstream
);
=======
);

-const db = require('../config/db');

 // Create a new lab group
 exports.createGroup = async (req, res) => {
   const { group_name, project_topic, lecturer_id } = req.body;

   if (!group_name) {
     return res.status(400).json({ message: 'Group name is required' });
   }

   try {
     const [result] = await db.execute(
       'INSERT INTO labgroup (group_name, project_topic, lecturer_id) VALUES (?, ?, ?)',
       [group_name, project_topic || null, lecturer_id || null]
     );

     res.status(201).json({
       message: 'Group created successfully',
       labgroupId: result.insertId
     });
   } catch (error) {
     console.error('Error creating group:', error);
     res.status(500).json({ message: 'Database error', error: error.message });
   }
 };

 // Get all lab groups
 exports.getAllGroups = async (req, res) => {
   try {
     const [groups] = await db.execute('SELECT * FROM labgroup');
     res.status(200).json(groups);
   } catch (error) {
     console.error('Error fetching groups:', error);
     res.status(500).json({ message: 'Database error', error: error.message });
   }
 };

 // Delete a lab group by ID
 exports.deleteGroup = async (req, res) => {
   const { id } = req.params;

   try {
     const [result] = await db.execute('DELETE FROM labgroup WHERE labgroup_id = ?', [id]);

     if (result.affectedRows === 0) {
       return res.status(404).json({ message: 'Group not found' });
     }

     res.status(200).json({ message: 'Group deleted successfully' });
   } catch (error) {
     console.error('Error deleting group:', error);
     res.status(500).json({ message: 'Database error', error: error.message });
   }
 };
>>>>>>> Stashed changes
