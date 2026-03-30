-- ============================================================
-- Smart Classroom Management System - Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_classroom;
USE smart_classroom;

-- ------------------------------------------------------------
-- Table: users
-- Stores all system users (Admin, Faculty, Student)
-- ------------------------------------------------------------
CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,         -- BCrypt hashed
    full_name   VARCHAR(100) NOT NULL,
    role        ENUM('ADMIN','FACULTY','STUDENT') NOT NULL DEFAULT 'STUDENT',
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Table: classrooms
-- Represents a class/course (e.g., CS301)
-- ------------------------------------------------------------
CREATE TABLE classrooms (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_code   VARCHAR(20)  NOT NULL UNIQUE,
    class_name   VARCHAR(100) NOT NULL,
    description  TEXT,
    faculty_id   BIGINT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (faculty_id) REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Table: enrollments
-- Maps students to classrooms (many-to-many)
-- ------------------------------------------------------------
CREATE TABLE enrollments (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT NOT NULL,
    classroom_id BIGINT NOT NULL,
    enrolled_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_enrollment (student_id, classroom_id),
    FOREIGN KEY (student_id)   REFERENCES users(id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);

-- ------------------------------------------------------------
-- Table: schedules
-- Class schedule entries per classroom
-- ------------------------------------------------------------
CREATE TABLE schedules (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    day_of_week  ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY') NOT NULL,
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    room_number  VARCHAR(20),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);

-- ------------------------------------------------------------
-- Table: attendance
-- Tracks per-student attendance per class session
-- ------------------------------------------------------------
CREATE TABLE attendance (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT   NOT NULL,
    classroom_id BIGINT   NOT NULL,
    date         DATE     NOT NULL,
    status       ENUM('PRESENT','ABSENT','LATE') NOT NULL DEFAULT 'ABSENT',
    marked_by    BIGINT,                          -- faculty who marked
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_attendance (student_id, classroom_id, date),
    FOREIGN KEY (student_id)   REFERENCES users(id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id),
    FOREIGN KEY (marked_by)    REFERENCES users(id)
);

-- ------------------------------------------------------------
-- Table: announcements
-- Announcements from faculty/admin to a class or all
-- ------------------------------------------------------------
CREATE TABLE announcements (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    content      TEXT         NOT NULL,
    posted_by    BIGINT       NOT NULL,
    classroom_id BIGINT,                          -- NULL = global announcement
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by)    REFERENCES users(id),
    FOREIGN KEY (classroom_id) REFERENCES classrooms(id)
);

-- ------------------------------------------------------------
-- Table: study_tasks
-- Study planner tasks with priority-based scheduling
-- ------------------------------------------------------------
CREATE TABLE study_tasks (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id   BIGINT       NOT NULL,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    subject      VARCHAR(100),
    priority     ENUM('HIGH','MEDIUM','LOW') NOT NULL DEFAULT 'MEDIUM',
    status       ENUM('PENDING','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'PENDING',
    due_date     DATE,
    estimated_hours INT DEFAULT 1,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id)
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Passwords are BCrypt hashes of: admin123 / faculty123 / student123
INSERT INTO users (username, email, password, full_name, role) VALUES
('admin',     'admin@smartclass.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'System Admin',      'ADMIN'),
('drpatel',   'patel@smartclass.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Dr. Arun Patel',    'FACULTY'),
('drmeena',   'meena@smartclass.com',   '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Dr. Sunita Meena',  'FACULTY'),
('rahul',     'rahul@student.com',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Rahul Sharma',      'STUDENT'),
('priya',     'priya@student.com',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Priya Nair',        'STUDENT'),
('arjun',     'arjun@student.com',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Arjun Verma',       'STUDENT');

-- NOTE: The BCrypt hash above decodes to "password" for quick testing.
-- Use the /register endpoint or update via the app to set real passwords.

INSERT INTO classrooms (class_code, class_name, description, faculty_id) VALUES
('CS301', 'Data Structures & Algorithms', 'Core CS subject covering arrays, trees, graphs', 2),
('CS302', 'Database Management Systems',  'Relational DB, SQL, normalization',              2),
('CS401', 'Machine Learning',             'Supervised and unsupervised learning techniques', 3);

INSERT INTO enrollments (student_id, classroom_id) VALUES
(4, 1),(4, 2),(4, 3),
(5, 1),(5, 2),
(6, 1),(6, 3);

INSERT INTO schedules (classroom_id, day_of_week, start_time, end_time, room_number) VALUES
(1, 'MONDAY',    '09:00:00', '10:00:00', 'A-101'),
(1, 'WEDNESDAY', '09:00:00', '10:00:00', 'A-101'),
(2, 'TUESDAY',   '11:00:00', '12:00:00', 'B-202'),
(2, 'THURSDAY',  '11:00:00', '12:00:00', 'B-202'),
(3, 'FRIDAY',    '14:00:00', '15:30:00', 'C-303');

INSERT INTO announcements (title, content, posted_by, classroom_id) VALUES
('Welcome to CS301!',            'First class on Monday. Bring your textbooks.',         2, 1),
('Quiz 1 Scheduled',             'Quiz on Arrays and LinkedLists next week.',             2, 1),
('Assignment 1 Released',        'DBMS normalization assignment. Due in 7 days.',         2, 2),
('Campus Holiday - Republic Day','College will remain closed on 26th January.',           1, NULL);

INSERT INTO study_tasks (student_id, title, description, subject, priority, status, due_date, estimated_hours) VALUES
(4, 'Revise Binary Trees',     'Cover insertion, deletion, traversal',       'DSA',  'HIGH',   'IN_PROGRESS', DATE_ADD(CURDATE(), INTERVAL 2 DAY),  3),
(4, 'Complete ER Diagram',     'Draw ER diagram for college DB assignment',   'DBMS', 'HIGH',   'PENDING',     DATE_ADD(CURDATE(), INTERVAL 1 DAY),  2),
(4, 'Read ML Chapter 5',       'Linear regression theory and examples',       'ML',   'MEDIUM', 'PENDING',     DATE_ADD(CURDATE(), INTERVAL 5 DAY),  2),
(4, 'Practice SQL Queries',    'Complex joins and subqueries',                'DBMS', 'MEDIUM', 'PENDING',     DATE_ADD(CURDATE(), INTERVAL 3 DAY),  1),
(4, 'Watch Sorting Lecture',   'QuickSort, MergeSort video on YouTube',       'DSA',  'LOW',    'PENDING',     DATE_ADD(CURDATE(), INTERVAL 7 DAY),  1),
(5, 'Graph Algorithms',        'BFS, DFS, Dijkstra implementation',           'DSA',  'HIGH',   'PENDING',     DATE_ADD(CURDATE(), INTERVAL 2 DAY),  4),
(5, 'Normalization Practice',  '1NF, 2NF, 3NF exercises',                    'DBMS', 'MEDIUM', 'COMPLETED',   DATE_ADD(CURDATE(), INTERVAL -1 DAY), 2);
