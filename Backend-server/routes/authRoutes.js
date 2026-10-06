const express = require('express');
const router = express.Router();
const authController = require('../controllers/authController');

// Authentication API Endpoints
router.post('/register', authController.register);
router.post('/login', authController.login);

// Export router instance directly
module.exports = router;