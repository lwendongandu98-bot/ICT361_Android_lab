const express = require('express');
const router = express.Router();
const submissionController = require('../controllers/submissionController');

router.get('/', submissionController.getAllSubmissions);
router.post('/', submissionController.createSubmission);

module.exports = router;