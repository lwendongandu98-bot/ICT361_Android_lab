const express = require('express');
const router = express.Router();
const labController = require('../controllers/labController');

router.get('/', labController.getAllLabs);
router.post('/', labController.createLab);
router.post('/submit', labController.submitLab);
router.get('/:labId/submissions', labController.getSubmissionsByLab);

module.exports = router;