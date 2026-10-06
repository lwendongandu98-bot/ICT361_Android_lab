-- ICT361 Mobile Application Development - Database Initialization Script
CREATE DATABASE IF NOT EXISTS ict361_lab_db;
USE ict361_lab_db;

-- 1. Accounts Table (Auth module & Role Management)
CREATE TABLE IF NOT EXISTS accounts (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('student', 'lecturer', 'admin', 'disabled') NOT NULL DEFAULT 'student',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Programs Table (Academic Programs: CS, IT, DS)
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
    FOREIGN KEY (user_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- 4. Labgroup Table (Max 15 active students per group enforced in controller)
CREATE TABLE IF NOT EXISTS labgroup (
    labgroup_id INT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL UNIQUE, -- e.g., G01, G02, G03, G04
    project_topic VARCHAR(255),
    lecturer_id VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lecturer_id) REFERENCES lecturers(lecturer_id) ON DELETE SET NULL
);

-- 5. Students Table (Includes Immutable Student ID, Unique 9-Digit Number, & Soft Deletion)
CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(50) PRIMARY KEY, -- Immutable UUID/String ID
    student_number VARCHAR(9) NOT NULL UNIQUE, -- Trimmed 9-digit student number
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    program_id INT NULL,
    year_of_study INT NOT NULL DEFAULT 1,
    labgroup_id INT NULL,
    is_deleted TINYINT(1) DEFAULT 0, -- Soft deletion flag
    deleted_at DATETIME NULL, -- Soft deletion timestamp
    FOREIGN KEY (user_id) REFERENCES accounts(account_id) ON DELETE CASCADE,
    FOREIGN KEY (program_id) REFERENCES programs(program_id) ON DELETE SET NULL,
    FOREIGN KEY (labgroup_id) REFERENCES labgroup(labgroup_id) ON DELETE SET NULL
);

-- 6. Claim Codes Table (For pre-existing student profile account verification/ownership)
CREATE TABLE IF NOT EXISTS claim_codes (
    code VARCHAR(50) PRIMARY KEY,
    student_number VARCHAR(9) NOT NULL,
    is_used TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. Sync Receipts & Idempotency Table (For handling offline retries without duplicate effects)
CREATE TABLE IF NOT EXISTS operation_receipts (
    operation_id VARCHAR(100) PRIMARY KEY,
    endpoint VARCHAR(100) NOT NULL,
    response_body JSON NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Sync Receipts Table (Android-to-Backend Sync Logging)
CREATE TABLE IF NOT EXISTS sync_receipts (
    receipt_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    last_synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('success', 'failed', 'pending') DEFAULT 'success',
    FOREIGN KEY (user_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);

-- Default Data Ingestion (Academic Reference Data)
INSERT IGNORE INTO programs (program_code, program_name) VALUES
('CS', 'Bachelor of Science in Computer Science'),
('IT', 'Bachelor of Science in Information Technology'),
('DS', 'Bachelor of Science in Data Science');

INSERT IGNORE INTO labgroup (group_name) VALUES
('G01'), ('G02'), ('G03'), ('G04');