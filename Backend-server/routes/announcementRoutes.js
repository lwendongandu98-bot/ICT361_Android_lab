const express = require('express');
const router = express.Router();
const announcementController = require('../controllers/announcementController');

router.get('/', announcementController.getAllAnnouncements);
router.post('/', announcementController.createAnnouncement);

module.exports = router;