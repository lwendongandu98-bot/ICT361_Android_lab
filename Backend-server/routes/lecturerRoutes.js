const express = require('express');
const router = express.Router();
const lecturerController = require('../controllers/lecturerController');

router.post('/', lecturerController.registerLecturer);
router.get('/:employeeNumber', lecturerController.getLecturerProfile);
router.post('/update', lecturerController.updateLecturerProfile);

module.exports = router;
