const express = require('express');
const router = express.Router();
const studentController = require('../controllers/studentController');
const idempotency = require('../middleware/idempotency');

// Student API Endpoints
router.get('/search', studentController.searchStudents);
router.post('/join-group', idempotency, studentController.joinGroup);
router.get('/', studentController.getAllStudents);

// Supports Android background sync and student creation/upsert (`POST /api/students`)
router.post('/', studentController.createStudent);

module.exports = router;