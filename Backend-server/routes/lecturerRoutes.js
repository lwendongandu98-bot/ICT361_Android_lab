const express = require('express');
const router = express.Router();
const lecturerController = require('../controllers/lecturerController');

router.post('/', lecturerController.registerLecturer);

module.exports = router;