CREATE DATABASE IF NOT EXISTS course_monitoring;
USE course_monitoring;

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(200) NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    role          ENUM('ADMIN','FACULTY') NOT NULL
);

CREATE TABLE IF NOT EXISTS courses (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    fees            DECIMAL(10,2) NOT NULL,
    duration_months INT NOT NULL
);

CREATE TABLE IF NOT EXISTS batches (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    course_id  INT NOT NULL,
    faculty_id INT NOT NULL,
    name       VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    timing     VARCHAR(50) NOT NULL,
    FOREIGN KEY (course_id)  REFERENCES courses(id) ON DELETE CASCADE,
    FOREIGN KEY (faculty_id) REFERENCES users(id)   ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS course_plans (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    batch_id   INT NOT NULL,
    day_no     INT NOT NULL,
    topic      VARCHAR(200) NOT NULL,
    status     ENUM('PENDING','COMPLETED') NOT NULL DEFAULT 'PENDING',
    updated_at TIMESTAMP NULL,
    UNIQUE KEY uq_batch_day (batch_id, day_no),
    FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE
);
-- The first admin account is created by the app on first run (see Main.java).
