const express = require('express');
const dotenv = require('dotenv');

dotenv.config();

// Route Imports
const authRoutes = require('./routes/authRoutes');
const groupRoutes = require('./routes/groupRoutes');
const studentRoutes = require('./routes/studentRoutes');

const app = express();

// Global Middleware
app.use(express.json());

// API Route Mounts
app.use('/api/auth', authRoutes);
app.use('/api/groups', groupRoutes);
app.use('/api/students', studentRoutes);

// Health Check Endpoint
app.get('/', (req, res) => {
  res.send('ICT361 Backend API is Running');
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});