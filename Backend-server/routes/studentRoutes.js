const express = require('express');
const router = express.Router();
const studentController = require('../controllers/studentController');
const idempotency = require('../middleware/idempotency');

router.get('/search', studentController.searchStudents);
router.post('/join-group', idempotency, studentController.joinGroup);
router.get('/', studentController.getAllStudents);

module.exports = router;