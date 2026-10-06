const express = require('express');
const router = express.Router();
const studentController = require('../controllers/studentController');

router.post('/profile', studentController.createStudentProfile);
router.post('/join-group', studentController.joinGroup);
router.get('/', studentController.getAllStudents);

module.exports = router;