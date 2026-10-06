const express = require('express');
const router = express.Router();
const programController = require('../controllers/programController');
const { verifyToken } = require('../middleware/auth');

router.get('/', verifyToken, programController.getAllPrograms);

module.exports = router;