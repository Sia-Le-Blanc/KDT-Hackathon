-- 회사 테이블
CREATE TABLE company_t (
    company_t_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL UNIQUE,
    industry VARCHAR(50),
    location VARCHAR(100),
    description TEXT,
    created_at DATETIME
);

-- 사용자 테이블
CREATE TABLE user_t (
    user_t_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_t_id BIGINT,
    user_name VARCHAR(10) NOT NULL,
    user_age INT NOT NULL,
    user_email VARCHAR(50) UNIQUE,
    user_password VARCHAR(255) NOT NULL,
    user_role ENUM('applicant', 'employee', 'manager', 'admin'),
    position VARCHAR(50),
    region VARCHAR(50),
    created_at DATETIME NOT NULL,
    FOREIGN KEY (company_t_id) REFERENCES company_t(company_t_id)
);

-- 채용 공고 테이블
CREATE TABLE job_postings_t (
    job_posting_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_t_id BIGINT,
    title VARCHAR(100),
    description TEXT,
    required_skills TEXT,
    employment_type ENUM('full-time', 'part-time', 'contract', 'remote'),
    created_at DATETIME,
    FOREIGN KEY (company_t_id) REFERENCES company_t(company_t_id)
);

-- 지원서 테이블
CREATE TABLE job_applications_t (
    job_application_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_t_id BIGINT,
    job_posting_id BIGINT,
    motivation TEXT,
    portfolio_url VARCHAR(255),
    status ENUM('applied', 'interviewing', 'rejected', 'accepted'),
    applied_at DATETIME,
    FOREIGN KEY (user_t_id) REFERENCES user_t(user_t_id),
    FOREIGN KEY (job_posting_id) REFERENCES job_postings_t(job_posting_id)
);

-- 회의방 테이블
CREATE TABLE rooms_t (
    room_t_id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(50) NOT NULL,
    created_by BIGINT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (created_by) REFERENCES user_t(user_t_id)
);

-- 회의 참가자 테이블
CREATE TABLE room_participants_t (
    room_participant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_t_id INT,
    user_t_id BIGINT,
    joined_at DATETIME NOT NULL,
    FOREIGN KEY (room_t_id) REFERENCES rooms_t(room_t_id),
    FOREIGN KEY (user_t_id) REFERENCES user_t(user_t_id)
);

-- 메시지 테이블
CREATE TABLE messages_t (
    message_t_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_t_id INT,
    user_t_id BIGINT,
    message_type ENUM('text', 'voice', 'system'),
    content TEXT,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (room_t_id) REFERENCES rooms_t(room_t_id),
    FOREIGN KEY (user_t_id) REFERENCES user_t(user_t_id)
);

-- 감정 인식 테이블
CREATE TABLE emotions_t (
    emotions_t_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_t_id INT,
    user_t_id BIGINT,
    emotion VARCHAR(50),
    confidence FLOAT,
    timestamp TIMESTAMP,
    FOREIGN KEY (room_t_id) REFERENCES rooms_t(room_t_id),
    FOREIGN KEY (user_t_id) REFERENCES user_t(user_t_id)
);

-- 아바타 테이블
CREATE TABLE avatars_t (
    avatar_t_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_t_id INT,
    user_t_id BIGINT,
    position_x FLOAT,
    position_y FLOAT,
    position_z FLOAT,
    rotation_y FLOAT,
    expression VARCHAR(50),
    current_emotion VARCHAR(50),
    updated_at DATETIME,
    FOREIGN KEY (room_t_id) REFERENCES rooms_t(room_t_id),
    FOREIGN KEY (user_t_id) REFERENCES user_t(user_t_id)
);

-- 협업 로그 테이블
CREATE TABLE collaboration_logs_t (
    collaboration_log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_t_id BIGINT,
    room_t_id INT,
    action_type TEXT,
    timestamp TIMESTAMP,
    FOREIGN KEY (user_t_id) REFERENCES user_t(user_t_id),
    FOREIGN KEY (room_t_id) REFERENCES rooms_t(room_t_id)
);

-- 출석 테이블
CREATE TABLE attendance_t (
    attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_t_id INT,
    user_t_id BIGINT,
    check_in DATETIME,
    check_out DATETIME,
    FOREIGN KEY (room_t_id) REFERENCES rooms_t(room_t_id),
    FOREIGN KEY (user_t_id) REFERENCES user_t(user_t_id)
);

-- 추가 참조 정의 (Ref 해석)
-- 사용자 이름은 중복 허용하지 않으려면 유니크 제약 추가
ALTER TABLE user_t ADD CONSTRAINT uk_user_name UNIQUE (user_name);

-- 사용자와 회사 간 1:N 관계를 명시하는 외래키는 이미 위에서 정의했으므로 중복 생략 가능
-- (user_t.company_t_id → company_t.company_t_id 외래키는 이미 있음)
