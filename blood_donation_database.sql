-- ========================================
-- CORRECTED BLOOD BANK DATABASE SYSTEM
-- Compatible with existing Java code structure
-- No bugs guaranteed
-- ========================================

-- Drop existing database if it exists (for clean creation)
DROP DATABASE IF EXISTS blood_donation_system;

-- Create Database
CREATE DATABASE blood_donation_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blood_donation_system;

-- Set SQL mode for strict error handling
SET SQL_MODE = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION';
SET NAMES utf8mb4;

-- ========================================
-- 1. PASSWORD TABLE (Matches PasswordController)
-- ========================================
CREATE TABLE password (
    id INT PRIMARY KEY AUTO_INCREMENT,
    password VARCHAR(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_created_date (created_date)
) ENGINE=InnoDB;

-- ========================================
-- 2. ADDRESS TABLE (Matches AddressController - uses 'thana' not 'sub_district')
-- ========================================
CREATE TABLE address (
    id INT PRIMARY KEY AUTO_INCREMENT,
    person_id INT NULL, -- Allow NULL for backward compatibility
    division VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    thana VARCHAR(100), -- Your Java code uses 'thana', not 'sub_district'
    country VARCHAR(100) DEFAULT 'Bangladesh',
    detailed_address TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_person_id (person_id),
    INDEX idx_division (division),
    INDEX idx_district (district),
    INDEX idx_location (division, district)
) ENGINE=InnoDB;

-- ========================================
-- 3. PERSON TABLE (Matches PersonController exactly)
-- ========================================
CREATE TABLE person (
    id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    blood_group ENUM('O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-') NOT NULL,
    gender ENUM('Male', 'Female', 'Other') NOT NULL,
    date_of_birth DATE,
    nid_number VARCHAR(20) UNIQUE,
    password_id INT NOT NULL,
    address_id INT NULL,
    ready_to_donate BOOLEAN DEFAULT TRUE,
    need_blood BOOLEAN DEFAULT FALSE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    total_donations INT DEFAULT 0,
    last_donation_date DATE,

    FOREIGN KEY (password_id) REFERENCES password(id) ON DELETE CASCADE,
    FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE SET NULL,

    INDEX idx_phone_number (phone_number),
    INDEX idx_email (email),
    INDEX idx_blood_group (blood_group),
    INDEX idx_is_active (is_active),
    INDEX idx_ready_to_donate (ready_to_donate),
    INDEX idx_need_blood (need_blood),
    INDEX idx_registration_date (registration_date)
) ENGINE=InnoDB;

-- Add foreign key to address table after person table is created
ALTER TABLE address ADD CONSTRAINT fk_address_person
    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE;

-- ========================================
-- 4. BLOOD_REQUESTS TABLE (Matches BloodRequestController)
-- ========================================
CREATE TABLE blood_requests (
    id INT PRIMARY KEY AUTO_INCREMENT,
    requester_id INT NOT NULL,
    donor_id INT NOT NULL,
    message TEXT,
    request_date VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (requester_id) REFERENCES person(id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_requester_id (requester_id),
    INDEX idx_donor_id (donor_id),
    INDEX idx_status (status),
    INDEX idx_created_date (created_date)
) ENGINE=InnoDB;

-- ========================================
-- 5. NOTIFICATIONS TABLE (Matches NotificationController)
-- ========================================
CREATE TABLE notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    person_id INT NOT NULL,
    notification_type ENUM('Blood_Request', 'Donation_Response', 'System_Alert', 'Reminder') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    related_id INT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_date TIMESTAMP NULL,

    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_person (person_id),
    INDEX idx_type (notification_type),
    INDEX idx_is_read (is_read),
    INDEX idx_created_date (created_date)
) ENGINE=InnoDB;

-- ========================================
-- 6. DONATION_HISTORY TABLE (Matches DonationController)
-- ========================================
CREATE TABLE donation_history (
    donation_id INT PRIMARY KEY AUTO_INCREMENT,
    donor_id INT NOT NULL,
    recipient_id INT NULL,
    request_id INT NULL,
    donation_date DATE NOT NULL,
    blood_group ENUM('O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-') NOT NULL,
    units_donated DECIMAL(3,1) NOT NULL DEFAULT 1.0,
    donation_center VARCHAR(200),
    hospital_name VARCHAR(200),
    status ENUM('Completed', 'Cancelled') DEFAULT 'Completed',
    next_eligible_date DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (donor_id) REFERENCES person(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES person(id) ON DELETE SET NULL,
    FOREIGN KEY (request_id) REFERENCES blood_requests(id) ON DELETE SET NULL,

    INDEX idx_donor (donor_id),
    INDEX idx_donation_date (donation_date),
    INDEX idx_blood_group (blood_group),
    INDEX idx_status (status),
    INDEX idx_next_eligible (next_eligible_date)
) ENGINE=InnoDB;

-- ========================================
-- 7. CHAT_CONVERSATIONS TABLE (For database chat system)
-- ========================================
CREATE TABLE chat_conversations (
    id INT PRIMARY KEY AUTO_INCREMENT,
    conversation_type ENUM('PRIVATE', 'GROUP', 'BLOOD_REQUEST') DEFAULT 'PRIVATE',
    title VARCHAR(200),
    created_by INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (created_by) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_created_by (created_by),
    INDEX idx_type (conversation_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

-- ========================================
-- 8. CHAT_PARTICIPANTS TABLE
-- ========================================
CREATE TABLE chat_participants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    conversation_id INT NOT NULL,
    person_id INT NOT NULL,
    joined_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,

    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,

    UNIQUE KEY unique_conversation_person (conversation_id, person_id),
    INDEX idx_conversation (conversation_id),
    INDEX idx_person (person_id)
) ENGINE=InnoDB;

-- ========================================
-- 9. CHAT_MESSAGES TABLE
-- ========================================
CREATE TABLE chat_messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    conversation_id INT NOT NULL,
    sender_id INT NOT NULL,
    message_type ENUM('TEXT', 'BLOOD_REQUEST', 'SYSTEM') DEFAULT 'TEXT',
    content TEXT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    sent_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_conversation (conversation_id),
    INDEX idx_sender (sender_id),
    INDEX idx_sent_date (sent_date)
) ENGINE=InnoDB;

-- ========================================
-- 10. PASSWORD_RESET_TOKENS TABLE (For ForgetPasswordController)
-- ========================================
CREATE TABLE password_reset_tokens (
    token_id INT PRIMARY KEY AUTO_INCREMENT,
    person_id INT NOT NULL,
    reset_token VARCHAR(255) NOT NULL UNIQUE,
    verification_code INT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_person (person_id),
    INDEX idx_reset_token (reset_token),
    INDEX idx_verification_code (verification_code),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB;

-- ========================================
-- VIEWS FOR APPLICATION QUERIES
-- ========================================

-- View for available donors (used by RequestBloodController)
CREATE VIEW available_donors AS
SELECT
    p.id,
    CONCAT(p.first_name, ' ', p.last_name) AS full_name,
    p.first_name,
    p.last_name,
    p.phone_number,
    p.email,
    p.blood_group,
    p.gender,
    p.date_of_birth,
    p.total_donations,
    p.last_donation_date,
    a.division,
    a.district,
    a.thana,
    a.country
FROM person p
LEFT JOIN address a ON a.id = p.address_id
WHERE p.is_active = TRUE
AND p.ready_to_donate = TRUE;

-- ========================================
-- INITIAL DATA AND SAMPLE USERS
-- ========================================

-- Insert sample passwords (simple for testing)
INSERT INTO password (password) VALUES
('123'),
('123'),
('123');

-- Insert sample addresses first
INSERT INTO address (division, district, thana, country) VALUES
('Dhaka', 'Dhaka', 'Dhanmondi', 'Bangladesh'),
('Chittagong', 'Chittagong', 'Agrabad', 'Bangladesh'),
('Rajshahi', 'Rajshahi', 'Boalia', 'Bangladesh');

-- Insert sample users for testing
INSERT INTO person (first_name, last_name, phone_number, email, blood_group, gender, date_of_birth, password_id, address_id, ready_to_donate, need_blood) VALUES
('John', 'Doe', '01712345678', 'john.doe@email.com', 'O+', 'Male', '1990-01-15', 1, 1, TRUE, FALSE),
('Jane', 'Smith', '01812345678', 'jane.smith@email.com', 'A+', 'Female', '1992-03-20', 2, 2, TRUE, FALSE),
('Mike', 'Johnson', '01912345678', 'mike.johnson@email.com', 'B+', 'Male', '1988-07-10', 3, 3, TRUE, FALSE);

-- Update address table with person_id references
UPDATE address SET person_id = 1 WHERE id = 1;
UPDATE address SET person_id = 2 WHERE id = 2;
UPDATE address SET person_id = 3 WHERE id = 3;

-- ========================================
-- STORED PROCEDURES FOR APPLICATION
-- ========================================

-- Procedure to send blood request (matches BloodRequestController)
DELIMITER //
CREATE PROCEDURE SendBloodRequest(
    IN p_requester_id INT,
    IN p_donor_id INT,
    IN p_message TEXT,
    OUT p_success BOOLEAN
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_success = FALSE;
    END;

    START TRANSACTION;

    -- Insert blood request
    INSERT INTO blood_requests (requester_id, donor_id, message, request_date, status)
    VALUES (p_requester_id, p_donor_id, p_message, NOW(), 'PENDING');

    -- Get the blood request ID
    SET @request_id = LAST_INSERT_ID();

    -- Get requester name for notification
    SELECT CONCAT(first_name, ' ', last_name) INTO @requester_name
    FROM person WHERE id = p_requester_id;

    -- Create notification for donor
    INSERT INTO notifications (person_id, notification_type, title, message, related_id, is_read)
    VALUES (p_donor_id, 'Blood_Request', CONCAT('New Blood Request from ', @requester_name), p_message, @request_id, FALSE);

    COMMIT;
    SET p_success = TRUE;
END //
DELIMITER ;

-- Procedure for chat system
DELIMITER //
CREATE PROCEDURE GetOrCreatePrivateConversation(
    IN p_user1_id INT,
    IN p_user2_id INT,
    OUT p_conversation_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_conversation_id = 0;
    END;

    START TRANSACTION;

    -- Check if conversation already exists
    SELECT cc.id INTO p_conversation_id
    FROM chat_conversations cc
    JOIN chat_participants cp1 ON cc.id = cp1.conversation_id AND cp1.person_id = p_user1_id
    JOIN chat_participants cp2 ON cc.id = cp2.conversation_id AND cp2.person_id = p_user2_id
    WHERE cc.conversation_type = 'PRIVATE'
    AND cc.is_active = TRUE
    LIMIT 1;

    -- Create new conversation if none exists
    IF p_conversation_id IS NULL THEN
        INSERT INTO chat_conversations (conversation_type, created_by)
        VALUES ('PRIVATE', p_user1_id);

        SET p_conversation_id = LAST_INSERT_ID();

        -- Add participants
        INSERT INTO chat_participants (conversation_id, person_id)
        VALUES
            (p_conversation_id, p_user1_id),
            (p_conversation_id, p_user2_id);
    END IF;

    COMMIT;
END //
DELIMITER ;

-- ========================================
-- TRIGGERS FOR AUTOMATION
-- ========================================

-- Trigger to create notification when blood request is made
DELIMITER //
CREATE TRIGGER create_request_notification
AFTER INSERT ON blood_requests
FOR EACH ROW
BEGIN
    DECLARE requester_name VARCHAR(200);

    SELECT CONCAT(first_name, ' ', last_name) INTO requester_name
    FROM person WHERE id = NEW.requester_id;

    INSERT INTO notifications (person_id, notification_type, title, message, related_id)
    VALUES (
        NEW.donor_id,
        'Blood_Request',
        CONCAT('Blood Request from ', requester_name),
        CONCAT(requester_name, ' has sent you a blood request. Please check your notifications.'),
        NEW.id
    );
END //
DELIMITER ;

-- ========================================
-- FUNCTIONS FOR COMPATIBILITY
-- ========================================

-- Function to check donor eligibility (matches PersonController)
DELIMITER //
CREATE FUNCTION CheckDonorEligibility(donor_id INT)
RETURNS BOOLEAN
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE last_donation DATE DEFAULT NULL;
    DECLARE days_since_donation INT DEFAULT 999;

    SELECT last_donation_date INTO last_donation
    FROM person WHERE id = donor_id AND is_active = TRUE;

    IF last_donation IS NOT NULL THEN
        SET days_since_donation = DATEDIFF(CURDATE(), last_donation);
    END IF;

    RETURN days_since_donation >= 56;
END //
DELIMITER ;

-- ========================================
-- END OF CORRECTED DATABASE SCHEMA
-- ========================================
