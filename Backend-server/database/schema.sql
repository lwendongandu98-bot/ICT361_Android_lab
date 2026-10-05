-- Create and select database
CREATE DATABASE IF NOT EXISTS group_management_db;
USE group_management_db;

-- 1. Accounts Table (Authentication & User Roles)
CREATE TABLE IF NOT EXISTS accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('student', 'lecturer', 'admin') NOT NULL DEFAULT 'student',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Programs Table (Academic Programs / Courses)
CREATE TABLE IF NOT EXISTS programs (
    program_id INT AUTO_INCREMENT PRIMARY KEY,
    program_code VARCHAR(20) NOT NULL UNIQUE,
    program_name VARCHAR(150) NOT NULL
);

-- 3. Lecturers Table
CREATE TABLE IF NOT EXISTS lecturers (
    lecturer_id VARCHAR(50) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES accounts(id) ON DELETE CASCADE
);

-- 4. Labgroup Table (Renamed from groups)
CREATE TABLE IF NOT EXISTS labgroup (
    labgroup_id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    project_topic VARCHAR(255),
    lecturer_id VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lecturer_id) REFERENCES lecturers(lecturer_id) ON DELETE SET NULL
);

-- 5. Students Table (Linked to Accounts, Programs, and Labgroup)
CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(50) PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    program_id INT NULL,
    year_of_study INT NOT NULL,
    labgroup_id INT NULL,
    FOREIGN KEY (user_id) REFERENCES accounts(id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE SET NULL,
    FOREIGN KEY (labgroup_id) REFERENCES labgroup(labgroup_id) ON DELETE SET NULL
);

-- 6. Sync Receipts Table (Android-to-Backend Sync Logging)
CREATE TABLE IF NOT EXISTS sync_receipts (
    receipt_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    last_synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('success', 'failed', 'pending') DEFAULT 'success',
    FOREIGN KEY (user_id) REFERENCES accounts(id) ON DELETE CASCADE
);