const express = require('express');
const router = express.Router();
const labgroupController = require('../controllers/labgroupController');
const { verifyToken, requireRole } = require('../middleware/auth');

router.post('/', verifyToken, requireRole('lecturer'), labgroupController.createGroup);
router.get('/', verifyToken, labgroupController.getAllGroups);

module.exports = router;