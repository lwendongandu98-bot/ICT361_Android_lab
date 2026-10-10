const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');

dotenv.config();

// Route Imports
const authRoutes = require('./routes/authRoutes');
const groupRoutes = require('./routes/groupRoutes');
const studentRoutes = require('./routes/studentRoutes');
const courseRoutes = require('./routes/courseRoutes');
const labRoutes = require('./notes/../routes/labRoutes');
const announcementRoutes = require('./routes/announcementRoutes');

const app = express();

// Global Middleware
app.use(express.json());
app.use(cors());

// API Route Mounts
app.use('/api/auth', authRoutes);
app.use('/api/groups', groupRoutes);
app.use('/api/students', studentRoutes);
app.use('/api/courses', courseRoutes);
app.use('/api/labs', labRoutes);
app.use('/api/announcements', announcementRoutes);

// Health Check Endpoint
app.get('/', (req, res) => {
  res.send('ICT361 Backend API is Running');
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});