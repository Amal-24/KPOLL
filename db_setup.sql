-- K-PollMan 2026 Database Schema

CREATE DATABASE IF NOT EXISTS kpollman2026;
USE kpollman2026;

-- Table for Constituencies
CREATE TABLE IF NOT EXISTS Constituencies (
    constituency_id INT PRIMARY KEY AUTO_INCREMENT,
    constituency_name VARCHAR(100) NOT NULL UNIQUE,
    total_voters INT DEFAULT 0
);

-- Table for Polling Booths
CREATE TABLE IF NOT EXISTS Booths (
    booth_id INT PRIMARY KEY AUTO_INCREMENT,
    booth_name VARCHAR(100) NOT NULL,
    constituency_id INT,
    booth_type ENUM('Urban', 'Rural', 'Remote') DEFAULT 'Urban',
    booth_password VARCHAR(50) DEFAULT 'password123',
    FOREIGN KEY (constituency_id) REFERENCES Constituencies(constituency_id)
);

-- Table for Voters
CREATE TABLE IF NOT EXISTS Voters (
    epic_no VARCHAR(20) PRIMARY KEY,
    voter_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    photo_path VARCHAR(255),
    serial_no INT NOT NULL,
    booth_id INT,
    voted_status BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (booth_id) REFERENCES Booths(booth_id)
);

-- Table for Polling Issues (Team 3)
CREATE TABLE IF NOT EXISTS PollingIssues (
    issue_id INT PRIMARY KEY AUTO_INCREMENT,
    category ENUM('EVM', 'VoterList', 'Accessibility', 'LawAndOrder', 'Other') NOT NULL,
    description TEXT,
    booth_id INT,
    urgency_level ENUM('Low', 'Medium', 'High', 'Critical') DEFAULT 'Medium',
    status ENUM('Open', 'In-Progress', 'Resolved', 'Closed') DEFAULT 'Open',
    assigned_official VARCHAR(100),
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    resolution_notes TEXT,
    reported_by VARCHAR(50),
    FOREIGN KEY (booth_id) REFERENCES Booths(booth_id)
);

-- Table for Queue Management (Team 2)
CREATE TABLE IF NOT EXISTS QueueStatus (
    booth_id INT PRIMARY KEY,
    current_queue_length INT DEFAULT 0,
    avg_wait_time_mins INT DEFAULT 0,
    active_stations INT DEFAULT 1,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booth_id) REFERENCES Booths(booth_id)
);

-- Table for Turnout Tracking (Team 4)
CREATE TABLE IF NOT EXISTS HourlyTurnout (
    turnout_id INT PRIMARY KEY AUTO_INCREMENT,
    booth_id INT,
    report_hour INT NOT NULL, -- 7, 8, 9, ..., 18 (for 7 AM to 6 PM)
    male_votes INT DEFAULT 0,
    female_votes INT DEFAULT 0,
    third_gender_votes INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booth_id) REFERENCES Booths(booth_id),
    UNIQUE (booth_id, report_hour)
);

-- Table for Counting Centers (Team 5)
CREATE TABLE IF NOT EXISTS CountingCenters (
    center_id INT PRIMARY KEY AUTO_INCREMENT,
    center_name VARCHAR(100) NOT NULL,
    location VARCHAR(100)
);

-- Table for Counting Tables (Team 5)
CREATE TABLE IF NOT EXISTS CountingTables (
    table_id INT PRIMARY KEY AUTO_INCREMENT,
    center_id INT,
    constituency_id INT,
    supervisor_name VARCHAR(100),
    assistant_name VARCHAR(100),
    FOREIGN KEY (center_id) REFERENCES CountingCenters(center_id),
    FOREIGN KEY (constituency_id) REFERENCES Constituencies(constituency_id)
);

-- Table for Candidates (Team 6 & 7)
CREATE TABLE IF NOT EXISTS Candidates (
    candidate_id INT PRIMARY KEY AUTO_INCREMENT,
    candidate_name VARCHAR(100) NOT NULL,
    party_name VARCHAR(100) NOT NULL,
    constituency_id INT,
    party_symbol_path VARCHAR(255),
    FOREIGN KEY (constituency_id) REFERENCES Constituencies(constituency_id)
);

-- Table for Round-wise Counting Results (Team 5 & 6)
CREATE TABLE IF NOT EXISTS RoundResults (
    result_id INT PRIMARY KEY AUTO_INCREMENT,
    round_no INT NOT NULL,
    table_id INT,
    candidate_id INT,
    votes_counted INT DEFAULT 0,
    is_verified BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (table_id) REFERENCES CountingTables(table_id),
    FOREIGN KEY (candidate_id) REFERENCES Candidates(candidate_id)
);

-- Table for Final Results (Team 6 & 7)
CREATE TABLE IF NOT EXISTS FinalResults (
    candidate_id INT PRIMARY KEY,
    constituency_id INT,
    total_votes INT DEFAULT 0,
    vote_share_percentage DECIMAL(5,2) DEFAULT 0.00,
    is_winner BOOLEAN DEFAULT FALSE,
    certified_at TIMESTAMP NULL,
    FOREIGN KEY (candidate_id) REFERENCES Candidates(candidate_id),
    FOREIGN KEY (constituency_id) REFERENCES Constituencies(constituency_id)
);

-- Initial Data for Testing
INSERT INTO Constituencies (constituency_name, total_voters) VALUES ('Thiruvananthapuram', 150000);
INSERT INTO Booths (booth_name, constituency_id, booth_type) VALUES ('Booth 1 - Central School', 1, 'Urban');
INSERT INTO Candidates (candidate_name, party_name, constituency_id) VALUES ('John Doe', 'Party A', 1), ('Jane Smith', 'Party B', 1);



-- 🔥 Officials Table (Multi-Level Access)
CREATE TABLE IF NOT EXISTS Officials (
    official_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    
    role ENUM('BOOTH', 'CONSTITUENCY', 'ADMIN') NOT NULL,
    
    booth_id INT NULL,
    constituency_id INT NULL,
    
    FOREIGN KEY (booth_id) REFERENCES Booths(booth_id),
    FOREIGN KEY (constituency_id) REFERENCES Constituencies(constituency_id)
);

-- Booth Level Official
INSERT INTO Officials (username, password, role, booth_id)
VALUES ('booth1', 'pass123', 'BOOTH', 1);

-- Constituency Level Official
INSERT INTO Officials (username, password, role, constituency_id)
VALUES ('const1', 'pass123', 'CONSTITUENCY', 1);

-- Admin (Full Access)
INSERT INTO Officials (username, password, role)
VALUES ('admin', 'admin123', 'ADMIN');