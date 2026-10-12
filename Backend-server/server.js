const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');

dotenv.config();

// Route Imports
const authRoutes = require('./routes/authRoutes');
const groupRoutes = require('./routes/groupRoutes');
const studentRoutes = require('./routes/studentRoutes');
const courseRoutes = require('./routes/courseRoutes');
const labRoutes = require('./routes/labRoutes');
const announcementRoutes = require('./routes/announcementRoutes');
const submissionRoutes = require('./routes/submissionRoutes');

const app = express();

// Global Middleware
app.use(express.json());
app.use(cors());

// Request Logger (Helps you see if your Samsung phone is hitting the server)
app.use((req, res, next) => {
  console.log(`[Incoming Request] ${req.method} ${req.url} from ${req.ip}`);
  next();
});

// API Route Mounts
app.use('/api/auth', authRoutes);
app.use('/api/groups', groupRoutes);
app.use('/api/students', studentRoutes);
app.use('/api/courses', courseRoutes);
app.use('/api/labs', labRoutes);
app.use('/api/announcements', announcementRoutes);
app.use('/api/submissions', submissionRoutes);

// Health Check Endpoint (Matches RetrofitClient BASE_URL root test)
app.get('/', (req, res) => {
  res.send('ICT361 Backend API is Running');
});

const PORT = 3000; // Locked to 3000 to match your RetrofitClient configuration
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running on port ${PORT} and listening to all network interfaces`);
});