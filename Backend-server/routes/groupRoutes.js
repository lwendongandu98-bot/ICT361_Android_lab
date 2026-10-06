const express = require('express');
const router = express.Router();
const groupController = require('../controllers/groupController');

// Lab Group API Endpoints
router.post('/', groupController.createGroup);
router.get('/', groupController.getAllGroups);
router.delete('/:id', groupController.deleteGroup);

// Export router instance directly
module.exports = router;