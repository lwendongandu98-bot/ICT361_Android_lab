const express = require('express');
const cors = require('cors');
require('dotenv').config();

const authRoutes = require('./routes/authRoutes');

const programRoutes = require('./routes/programRoutes');
const labgroupRoutes = require('./routes/labgroupRoutes');
const studentRoutes = require('./routes/studentRoutes');

const app = express();

app.use(cors());
app.use(express.json());

// API Routes
app.use('/api/auth', authRoutes);

app.use('/api/programs', programRoutes);
app.use('/api/labgroups', labgroupRoutes);
app.use('/api/students', studentRoutes);

// Base route test
app.get('/', (req, res) => {
    res.send('ICT361 Backend API is running...');
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
});