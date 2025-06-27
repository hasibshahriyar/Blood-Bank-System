-- ========================================
-- BLOOD DONATION SYSTEM - FINAL CORRECTED DATABASE SCHEMA
-- Complete SQL with proper table relationships and connections
-- ========================================

-- Drop existing database if it exists (for clean creation)
DROP DATABASE IF EXISTS blood_donation_system;

-- Create Database
CREATE DATABASE blood_donation_system;
USE blood_donation_system;

-- Set character set for proper encoding
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ========================================
-- 1. PASSWORD TABLE (Created first - no dependencies)
-- ========================================
CREATE TABLE password (
    id INT PRIMARY KEY AUTO_INCREMENT,
    password VARCHAR(255) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_created_date (created_date)
);

-- ========================================
-- 2. PERSON TABLE (Depends on password table)
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
    ready_to_donate BOOLEAN DEFAULT TRUE,
    need_blood BOOLEAN DEFAULT FALSE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    total_donations INT DEFAULT 0,
    last_donation_date DATE,

    FOREIGN KEY (password_id) REFERENCES password(id) ON DELETE CASCADE,

    INDEX idx_phone_number (phone_number),
    INDEX idx_email (email),
    INDEX idx_blood_group (blood_group),
    INDEX idx_is_active (is_active),
    INDEX idx_ready_to_donate (ready_to_donate),
    INDEX idx_need_blood (need_blood),
    INDEX idx_registration_date (registration_date)
);

-- ========================================
-- 3. ADDRESS TABLE (Depends on person table)
-- ========================================
CREATE TABLE address (
    id INT PRIMARY KEY AUTO_INCREMENT,
    person_id INT NOT NULL,
    division VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL,
    thana VARCHAR(100),
    detailed_address TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_person_id (person_id),
    INDEX idx_division (division),
    INDEX idx_district (district),
    INDEX idx_location (division, district)
);

-- Add address_id to person table after address table is created
ALTER TABLE person ADD COLUMN address_id INT,
ADD FOREIGN KEY (address_id) REFERENCES address(id) ON DELETE SET NULL,
ADD INDEX idx_address_id (address_id);

-- ========================================
-- 4. BLOOD_REQUESTS TABLE (Depends on person table)
-- ========================================
CREATE TABLE blood_requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    requester_id INT NOT NULL,
    patient_name VARCHAR(100) NOT NULL,
    blood_group_needed ENUM('O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-') NOT NULL,
    units_needed INT NOT NULL DEFAULT 1,
    urgency_level ENUM('Low', 'Medium', 'High', 'Critical') DEFAULT 'Medium',
    hospital_name VARCHAR(200) NOT NULL,
    hospital_address TEXT NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    needed_by_date DATE NOT NULL,
    additional_message TEXT,
    request_status ENUM('Active', 'Fulfilled', 'Cancelled', 'Expired') DEFAULT 'Active',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (requester_id) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_blood_group_needed (blood_group_needed),
    INDEX idx_urgency (urgency_level),
    INDEX idx_status (request_status),
    INDEX idx_needed_by_date (needed_by_date),
    INDEX idx_requester (requester_id),
    INDEX idx_created_date (created_date)
);

-- ========================================
-- 5. DONATION_RESPONSES TABLE (Depends on blood_requests and person tables)
-- ========================================
CREATE TABLE donation_responses (
    response_id INT PRIMARY KEY AUTO_INCREMENT,
    request_id INT NOT NULL,
    donor_id INT NOT NULL,
    response_type ENUM('Accept', 'Decline', 'Maybe') NOT NULL,
    response_message TEXT,
    response_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_confirmed BOOLEAN DEFAULT FALSE,
    confirmation_date TIMESTAMP NULL,

    FOREIGN KEY (request_id) REFERENCES blood_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (donor_id) REFERENCES person(id) ON DELETE CASCADE,

    UNIQUE KEY unique_donor_request (request_id, donor_id),

    INDEX idx_request (request_id),
    INDEX idx_donor (donor_id),
    INDEX idx_response_type (response_type),
    INDEX idx_response_date (response_date)
);

-- ========================================
-- 6. DONATION_HISTORY TABLE (Depends on person and blood_requests tables)
-- ========================================
CREATE TABLE donation_history (
    donation_id INT PRIMARY KEY AUTO_INCREMENT,
    donor_id INT NOT NULL,
    recipient_id INT,
    request_id INT,
    donation_date DATE NOT NULL,
    blood_group ENUM('O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-') NOT NULL,
    units_donated DECIMAL(3,1) NOT NULL DEFAULT 1.0,
    donation_center VARCHAR(200),
    status ENUM('Completed', 'Cancelled') DEFAULT 'Completed',
    next_eligible_date DATE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (donor_id) REFERENCES person(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES person(id) ON DELETE SET NULL,
    FOREIGN KEY (request_id) REFERENCES blood_requests(request_id) ON DELETE SET NULL,

    INDEX idx_donor (donor_id),
    INDEX idx_donation_date (donation_date),
    INDEX idx_blood_group (blood_group),
    INDEX idx_status (status),
    INDEX idx_next_eligible (next_eligible_date)
);

-- ========================================
-- 7. NOTIFICATIONS TABLE (Depends on person table)
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
);

-- ========================================
-- 8. BLOOD_INVENTORY TABLE (Independent table)
-- ========================================
CREATE TABLE blood_inventory (
    inventory_id INT PRIMARY KEY AUTO_INCREMENT,
    blood_group ENUM('O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-') NOT NULL,
    total_units DECIMAL(6,2) DEFAULT 0.00,
    available_units DECIMAL(6,2) DEFAULT 0.00,
    minimum_threshold DECIMAL(6,2) DEFAULT 10.00,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    location VARCHAR(100) DEFAULT 'Main Blood Bank',

    UNIQUE KEY unique_blood_location (blood_group, location),
    INDEX idx_blood_group (blood_group),
    INDEX idx_available_units (available_units)
);

-- ========================================
-- 9. USER_SESSIONS TABLE (Depends on person table)
-- ========================================
CREATE TABLE user_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    person_id INT NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    ip_address VARCHAR(45),
    is_active BOOLEAN DEFAULT TRUE,

    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_person (person_id),
    INDEX idx_is_active (is_active),
    INDEX idx_login_time (login_time)
);

-- ========================================
-- 10. PASSWORD_RESET_TOKENS TABLE (Depends on person table)
-- ========================================
CREATE TABLE password_reset_tokens (
    token_id INT PRIMARY KEY AUTO_INCREMENT,
    person_id INT NOT NULL,
    reset_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (person_id) REFERENCES person(id) ON DELETE CASCADE,

    INDEX idx_person (person_id),
    INDEX idx_reset_token (reset_token),
    INDEX idx_expires_at (expires_at),
    INDEX idx_is_used (is_used)
);

-- ========================================
-- INITIAL DATA INSERTION
-- ========================================

-- Insert initial blood inventory data
INSERT INTO blood_inventory (blood_group, total_units, available_units, minimum_threshold, location) VALUES
('O+', 60.0, 50.0, 20.0, 'Main Blood Bank'),
('O-', 35.0, 30.0, 15.0, 'Main Blood Bank'),
('A+', 50.0, 45.0, 15.0, 'Main Blood Bank'),
('A-', 30.0, 25.0, 10.0, 'Main Blood Bank'),
('B+', 40.0, 35.0, 12.0, 'Main Blood Bank'),
('B-', 25.0, 20.0, 8.0, 'Main Blood Bank'),
('AB+', 20.0, 15.0, 8.0, 'Main Blood Bank'),
('AB-', 15.0, 10.0, 5.0, 'Main Blood Bank');

-- ========================================
-- CREATE VIEWS FOR COMMON QUERIES
-- ========================================

-- View for active blood requests with requester details
CREATE VIEW active_blood_requests AS
SELECT
    br.request_id,
    br.patient_name,
    br.blood_group_needed,
    br.units_needed,
    br.urgency_level,
    br.hospital_name,
    br.contact_phone,
    br.needed_by_date,
    br.additional_message,
    CONCAT(p.first_name, ' ', p.last_name) AS requester_name,
    p.email AS requester_email,
    p.phone_number AS requester_phone,
    br.created_date
FROM blood_requests br
JOIN person p ON br.requester_id = p.id
WHERE br.request_status = 'Active'
AND br.needed_by_date >= CURDATE()
ORDER BY br.urgency_level DESC, br.needed_by_date ASC;

-- View for donor statistics
CREATE VIEW donor_statistics AS
SELECT
    p.id,
    CONCAT(p.first_name, ' ', p.last_name) AS full_name,
    p.blood_group,
    p.total_donations,
    p.last_donation_date,
    COUNT(dh.donation_id) AS completed_donations
FROM person p
LEFT JOIN donation_history dh ON p.id = dh.donor_id AND dh.status = 'Completed'
WHERE p.is_active = TRUE
GROUP BY p.id, p.first_name, p.last_name, p.blood_group, p.total_donations, p.last_donation_date;

-- View for complete user profile with address
CREATE VIEW user_profile_view AS
SELECT
    p.id,
    CONCAT(p.first_name, ' ', p.last_name) AS full_name,
    p.phone_number,
    p.email,
    p.blood_group,
    p.gender,
    p.date_of_birth,
    p.nid_number,
    p.total_donations,
    p.last_donation_date,
    p.ready_to_donate,
    p.need_blood,
    a.division,
    a.district,
    a.thana,
    a.detailed_address
FROM person p
LEFT JOIN address a ON p.address_id = a.id
WHERE p.is_active = TRUE;

-- View for available donors by blood group
CREATE VIEW available_donors AS
SELECT
    p.id,
    CONCAT(p.first_name, ' ', p.last_name) AS full_name,
    p.phone_number,
    p.email,
    p.blood_group,
    p.total_donations,
    a.division,
    a.district,
    a.thana
FROM person p
LEFT JOIN address a ON p.address_id = a.id
WHERE p.is_active = TRUE
AND p.ready_to_donate = TRUE;

-- ========================================
-- STORED PROCEDURES
-- ========================================

-- Procedure to check donor eligibility (56-day rule)
DELIMITER //
CREATE PROCEDURE CheckDonorEligibility(
    IN donor_id INT,
    OUT is_eligible BOOLEAN,
    OUT eligibility_message TEXT
)
BEGIN
    DECLARE last_donation DATE DEFAULT NULL;
    DECLARE days_since_donation INT DEFAULT 0;
    DECLARE user_active BOOLEAN DEFAULT FALSE;

    -- Check if user exists and is active
    SELECT is_active, last_donation_date
    INTO user_active, last_donation
    FROM person
    WHERE id = donor_id;

    IF user_active IS NULL THEN
        SET is_eligible = FALSE;
        SET eligibility_message = 'User not found';
    ELSEIF user_active = FALSE THEN
        SET is_eligible = FALSE;
        SET eligibility_message = 'User account is inactive';
    ELSE
        -- Calculate days since last donation
        IF last_donation IS NOT NULL THEN
            SET days_since_donation = DATEDIFF(CURDATE(), last_donation);
        ELSE
            SET days_since_donation = 999; -- First time donor
        END IF;

        -- Check 56-day minimum gap between donations
        IF days_since_donation >= 56 THEN
            SET is_eligible = TRUE;
            SET eligibility_message = 'Eligible for donation';
        ELSE
            SET is_eligible = FALSE;
            SET eligibility_message = CONCAT('Must wait ', 56 - days_since_donation, ' more days before next donation');
        END IF;
    END IF;
END //
DELIMITER ;

-- Procedure to record a donation
DELIMITER //
CREATE PROCEDURE RecordDonation(
    IN p_donor_id INT,
    IN p_recipient_id INT,
    IN p_request_id INT,
    IN p_blood_group VARCHAR(5),
    IN p_units_donated DECIMAL(3,1),
    IN p_donation_center VARCHAR(200)
)
BEGIN
    DECLARE next_eligible DATE;

    -- Calculate next eligible donation date (56 days later)
    SET next_eligible = DATE_ADD(CURDATE(), INTERVAL 56 DAY);

    -- Insert donation record
    INSERT INTO donation_history (
        donor_id, recipient_id, request_id, donation_date,
        blood_group, units_donated, donation_center, next_eligible_date
    ) VALUES (
        p_donor_id, p_recipient_id, p_request_id, CURDATE(),
        p_blood_group, p_units_donated, p_donation_center, next_eligible
    );

    -- Update user's total donations and last donation date
    UPDATE person
    SET total_donations = total_donations + 1,
        last_donation_date = CURDATE()
    WHERE id = p_donor_id;

    -- Update blood inventory
    UPDATE blood_inventory
    SET total_units = total_units + p_units_donated,
        available_units = available_units + p_units_donated
    WHERE blood_group = p_blood_group;

    -- Mark request as fulfilled if it exists
    IF p_request_id IS NOT NULL THEN
        UPDATE blood_requests
        SET request_status = 'Fulfilled'
        WHERE request_id = p_request_id;
    END IF;

END //
DELIMITER ;

-- ========================================
-- TRIGGERS
-- ========================================

-- Trigger to update person's donation stats
DELIMITER //
CREATE TRIGGER update_donation_stats
AFTER INSERT ON donation_history
FOR EACH ROW
BEGIN
    UPDATE person
    SET total_donations = total_donations + 1,
        last_donation_date = NEW.donation_date
    WHERE id = NEW.donor_id;
END //
DELIMITER ;

-- Trigger to create notification when blood request is made
DELIMITER //
CREATE TRIGGER create_request_notification
AFTER INSERT ON blood_requests
FOR EACH ROW
BEGIN
    INSERT INTO notifications (person_id, notification_type, title, message, related_id)
    VALUES (
        NEW.requester_id,
        'Blood_Request',
        'Blood Request Created',
        CONCAT('Your blood request for ', NEW.patient_name, ' has been submitted successfully.'),
        NEW.request_id
    );
END //
DELIMITER ;

-- Trigger to create notification when donation response is made
DELIMITER //
CREATE TRIGGER create_response_notification
AFTER INSERT ON donation_responses
FOR EACH ROW
BEGIN
    DECLARE requester_id INT;

    -- Get the requester ID from the blood request
    SELECT br.requester_id INTO requester_id
    FROM blood_requests br
    WHERE br.request_id = NEW.request_id;

    -- Create notification for the requester
    INSERT INTO notifications (person_id, notification_type, title, message, related_id)
    VALUES (
        requester_id,
        'Donation_Response',
        CONCAT('Donation Response: ', NEW.response_type),
        CONCAT('A donor has responded to your blood request with: ', NEW.response_type),
        NEW.response_id
    );
END //
DELIMITER ;

-- ========================================
-- FUNCTIONS
-- ========================================

-- Function to get blood compatibility
DELIMITER //
CREATE FUNCTION GetCompatibleDonors(recipient_blood_group VARCHAR(5))
RETURNS TEXT
READS SQL DATA
DETERMINISTIC
BEGIN
    CASE recipient_blood_group
        WHEN 'A+' THEN RETURN 'A+,A-,O+,O-';
        WHEN 'A-' THEN RETURN 'A-,O-';
        WHEN 'B+' THEN RETURN 'B+,B-,O+,O-';
        WHEN 'B-' THEN RETURN 'B-,O-';
        WHEN 'AB+' THEN RETURN 'A+,A-,B+,B-,AB+,AB-,O+,O-';
        WHEN 'AB-' THEN RETURN 'A-,B-,AB-,O-';
        WHEN 'O+' THEN RETURN 'O+,O-';
        WHEN 'O-' THEN RETURN 'O-';
        ELSE RETURN '';
    END CASE;
END //
DELIMITER ;

-- ========================================
-- SAMPLE DATA FOR TESTING (Optional)
-- ========================================

-- Insert sample password
INSERT INTO password (password) VALUES
('123'),
('123');

-- Insert sample persons
INSERT INTO person (first_name, last_name, phone_number, email, blood_group, gender, date_of_birth, password_id) VALUES
('John', 'Doe', '01712345678', 'john.doe@example.com', 'O+', 'Male', '1990-01-15', 1),
('Jane', 'Smith', '01787654321', 'jane.smith@example.com', 'A+', 'Female', '1992-05-20', 2);

-- Insert sample addresses
INSERT INTO address (person_id, division, district, thana) VALUES
(1, 'Dhaka', 'Dhaka', 'Dhanmondi'),
(2, 'Chittagong', 'Chittagong', 'Kotwali');

-- Update person table with address IDs
UPDATE person SET address_id = 1 WHERE id = 1;
UPDATE person SET address_id = 2 WHERE id = 2;

-- ========================================
-- VERIFICATION QUERIES
-- ========================================

-- Show all tables
SHOW TABLES;

-- Show table structures
-- DESCRIBE person;
-- DESCRIBE address;
-- DESCRIBE blood_requests;

-- ========================================
-- DATABASE SCHEMA COMPLETION MESSAGE
-- ========================================
SELECT 'Blood Donation System Database Created Successfully!' AS Status;

