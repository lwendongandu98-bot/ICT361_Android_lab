const express = require('express');
const router = express.Router();
const c = require('../controllers/studentController');
const { verifyToken, requireRole } = require('../middleware/auth');

router.post('/', verifyToken, requireRole('lecturer'), c.createStudent);
router.get('/', verifyToken, requireRole('lecturer'), c.getStudents);
router.get('/:id', verifyToken, c.getStudent);
router.put('/:id', verifyToken, c.updateStudent);
router.delete('/:id', verifyToken, requireRole('lecturer'), c.deleteStudent);

module.exports = router;