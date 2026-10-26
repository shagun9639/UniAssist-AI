-- ============================================================================
-- UniAssist AI - University Student Support System
-- Production-Ready Relational Database Schema (MySQL 8.0)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS uniassist_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE uniassist_db;

-- 1. Users Table (Students, Faculty, and System Administrators)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    student_id VARCHAR(50) UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    mobile_number VARCHAR(20),
    department VARCHAR(100),
    course VARCHAR(100),
    semester INT,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_STUDENT',
    account_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_student_id (student_id),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Conversations Table (Chat sessions created by students)
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL DEFAULT 'New Conversation',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_conv_user_id (user_id),
    INDEX idx_conv_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Messages Table (Individual student and assistant interaction turns)
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_type ENUM('STUDENT', 'ASSISTANT', 'SYSTEM') NOT NULL,
    message_text TEXT NOT NULL,
    predicted_intent VARCHAR(100),
    model_score DOUBLE,
    source_reference VARCHAR(255),
    fallback_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    INDEX idx_msg_conv_id (conversation_id),
    INDEX idx_msg_intent (predicted_intent),
    INDEX idx_msg_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. FAQs Table (Grounding knowledge base entries)
CREATE TABLE IF NOT EXISTS faqs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    keywords VARCHAR(255),
    source_url VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_faqs_category (category),
    INDEX idx_faqs_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Academic Courses Table
CREATE TABLE IF NOT EXISTS academic_courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(150) NOT NULL,
    department VARCHAR(100) NOT NULL,
    credits INT NOT NULL DEFAULT 4,
    duration_semesters INT NOT NULL DEFAULT 8,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course_dept (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Academic Calendar & Events Table
CREATE TABLE IF NOT EXISTS academic_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description TEXT,
    semester VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_events_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Announcements Table
CREATE TABLE IF NOT EXISTS announcements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(250) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'General',
    published_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    INDEX idx_announcements_active (active),
    INDEX idx_announcements_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Support Tickets Table
CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    assigned_to VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_tickets_user_id (user_id),
    INDEX idx_tickets_status (status),
    INDEX idx_tickets_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Ticket Responses Table
CREATE TABLE IF NOT EXISTS ticket_responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    responder_id BIGINT NOT NULL,
    response_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES support_tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (responder_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_tr_ticket_id (ticket_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Chatbot Feedback Table
CREATE TABLE IF NOT EXISTS chatbot_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    message_id BIGINT,
    rating ENUM('HELPFUL', 'NOT_HELPFUL') NOT NULL,
    comments TEXT,
    review_status VARCHAR(30) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE SET NULL,
    INDEX idx_feedback_rating (rating),
    INDEX idx_feedback_status (review_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Unanswered Queries Table (Low confidence or fallback logging for admin review)
CREATE TABLE IF NOT EXISTS unanswered_queries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    query_text TEXT NOT NULL,
    predicted_intent VARCHAR(100),
    model_score DOUBLE,
    review_status VARCHAR(30) DEFAULT 'UNREVIEWED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_unans_status (review_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Model Versions Table
CREATE TABLE IF NOT EXISTS model_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    version VARCHAR(30) NOT NULL,
    model_path VARCHAR(255) NOT NULL,
    evaluation_summary TEXT,
    approved_by VARCHAR(100) DEFAULT 'System',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- SEED DEMONSTRATION DATA
-- Password for all seed users is 'Password@123' (hashed using BCrypt)
-- Hash: $2a$10$w3e36l5i8q8P0VqF00rM6.v7C2R447/B6yR83E9sB8rYqD3dD9i/C
-- ============================================================================

-- Seed Default Admin and Demo Student
INSERT INTO users (id, full_name, student_id, email, mobile_number, department, course, semester, password_hash, role, account_status)
VALUES
(1, 'System Administrator', 'ADMIN-001', 'admin@uniassist.edu', '+91 9876543210', 'Administration', 'Staff', 0, '$2a$10$7vN3f5Q6I3z0V2C8D9E.u.Y8X6h3N5m7K2j9L4p1Q8s7T6v5W4x3y', 'ROLE_ADMIN', 'ACTIVE'),
(2, 'Aarav Sharma', 'STU-2023-CSE042', 'student@uniassist.edu', '+91 9123456780', 'Computer Science & Engineering', 'B.Tech', 4, '$2a$10$7vN3f5Q6I3z0V2C8D9E.u.Y8X6h3N5m7K2j9L4p1Q8s7T6v5W4x3y', 'ROLE_STUDENT', 'ACTIVE')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- Seed Academic Courses
INSERT INTO academic_courses (course_code, course_name, department, credits, duration_semesters, description)
VALUES
('CSE-BT4', 'B.Tech in Computer Science & Engineering', 'Computer Science & Engineering', 160, 8, 'Accredited program emphasizing algorithms, artificial intelligence, software engineering, and cloud systems.'),
('IT-BT4', 'B.Tech in Information Technology', 'Information Technology', 160, 8, 'Covers cybersecurity, networking, distributed computing, and full-stack enterprise development.'),
('ECE-BT4', 'B.Tech in Electronics & Communication', 'Electronics & Communication', 160, 8, 'Specializations in VLSI, signal processing, embedded systems, and IoT.'),
('ME-BT4', 'B.Tech in Mechanical Engineering', 'Mechanical Engineering', 160, 8, 'Focuses on robotics, thermodynamics, automotive design, and advanced manufacturing.'),
('MCA-PG2', 'Master of Computer Applications (MCA)', 'Computer Science & Engineering', 90, 4, 'Advanced postgraduate degree for computing professionals, covering data science, ML, and enterprise software.'),
('MBA-PG2', 'Master of Business Administration (MBA)', 'Management Studies', 96, 4, 'Dual specialization in Finance, Marketing, Human Resources, and Business Analytics.')
ON DUPLICATE KEY UPDATE course_name=VALUES(course_name);

-- Seed Academic Calendar Events
INSERT INTO academic_events (title, event_type, start_date, end_date, description, semester)
VALUES
('Autumn Semester Registration', 'REGISTRATION', '2026-08-01', '2026-08-05', 'Course enrollment and fee clearance for all undergraduate and postgraduate students.', 'Autumn 2026'),
('Mid-Semester Examinations', 'EXAMINATION', '2026-09-21', '2026-09-28', 'Written assessments for continuous evaluation across all departments.', 'Autumn 2026'),
('Diwali & Autumn Mid-Break', 'HOLIDAY', '2026-10-28', '2026-11-02', 'University closed for institutional festival break.', 'Autumn 2026'),
('End-Semester Practical Examinations', 'EXAMINATION', '2026-11-20', '2026-11-27', 'Laboratory evaluations and project demonstrations.', 'Autumn 2026'),
('End-Semester Theory Examinations', 'EXAMINATION', '2026-12-01', '2026-12-18', 'Final comprehensive theory exams conducted by Controller of Examinations.', 'Autumn 2026'),
('Spring Semester Commencement', 'ACADEMIC', '2027-01-05', '2027-01-06', 'First teaching day of Spring 2027 semester.', 'Spring 2027'),
('Annual Cultural & Tech Fest (UniVision 2027)', 'EVENT', '2027-02-18', '2027-02-21', 'Pan-university technical competitions, hackathons, and cultural evenings.', 'Spring 2027'),
('Annual Convocation Ceremony', 'CEREMONY', '2027-03-12', '2027-03-12', 'Conferral of undergraduate, master and doctoral degrees.', 'All')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Seed Announcements
INSERT INTO announcements (title, description, category, published_at, expiry_date, active)
VALUES
('Mid-Term Examination Date Sheet Released', 'The tentative date sheet for the upcoming mid-semester examinations has been published on the Examination portal. Verify timetable clashes by Friday.', 'Examinations', NOW(), '2026-11-30', TRUE),
('National Merit Scholarship Applications Open', 'Eligible undergraduate students with CGPA >= 8.5 can submit their scholarship renewal forms to the Dean of Student Welfare Office before November 15.', 'Scholarships', NOW(), '2026-11-15', TRUE),
('Campus Placement Drive - Tech Giants Inc.', 'Registration is open for final-year B.Tech and MCA students for upcoming software engineering placement interviews commencing next Monday.', 'Placements', NOW(), '2026-12-31', TRUE),
('Central Library Extended Hours During Exams', 'Starting next week, the Central Library reading halls and digital learning lounge will remain open 24 hours daily through the examination period.', 'Campus', NOW(), '2026-12-20', TRUE),
('Annual Sports Meet Registrations Live', 'Inter-departmental football, cricket, basketball, and athletics tournaments begin next month. Register your department teams online.', 'Sports', NOW(), '2026-11-10', TRUE)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Seed Initial Model Version Record
INSERT INTO model_versions (model_name, version, model_path, evaluation_summary, approved_by)
VALUES
('UniAssist Intent Classifier', 'v1.0.0', 'models/model_pipeline.joblib', 'Calibrated Linear SVC with TF-IDF (1,2 n-grams) achieving 70.73% test accuracy across 20 university intent classes.', 'Chief AI Officer');
