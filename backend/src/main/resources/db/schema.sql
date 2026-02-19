-- =============================================
-- Company FileHub Database Schema
-- =============================================

CREATE DATABASE IF NOT EXISTS filehub
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE filehub;

-- 부서 테이블
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(20) NOT NULL UNIQUE COMMENT '사번',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    department_id BIGINT COMMENT '부서',
    email VARCHAR(100) COMMENT '이메일',
    password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
    role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '역할',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    INDEX idx_employee_id (employee_id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 파일 테이블
CREATE TABLE files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name VARCHAR(500) NOT NULL COMMENT '원본 파일명',
    stored_name VARCHAR(500) NOT NULL COMMENT '저장된 파일명 (UUID)',
    file_path VARCHAR(1000) NOT NULL COMMENT '저장 경로',
    file_size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
    file_type VARCHAR(200) COMMENT 'MIME 타입',
    memo TEXT COMMENT '메모',
    uploader_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME COMMENT '만료 시간',
    FOREIGN KEY (uploader_id) REFERENCES users(id),
    INDEX idx_uploader (uploader_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 파일 수신자 테이블
CREATE TABLE file_recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    is_downloaded BOOLEAN NOT NULL DEFAULT FALSE COMMENT '다운로드 여부',
    first_downloaded_at DATETIME COMMENT '최초 다운로드 시간',
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(id),
    UNIQUE KEY uk_file_recipient (file_id, recipient_id),
    INDEX idx_recipient (recipient_id),
    INDEX idx_downloaded (is_downloaded)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 다운로드 로그 테이블
CREATE TABLE download_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id BIGINT NOT NULL,
    downloader_id BIGINT NOT NULL,
    downloaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45) COMMENT 'IP 주소',
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (downloader_id) REFERENCES users(id),
    INDEX idx_file_id (file_id),
    INDEX idx_downloader (downloader_id),
    INDEX idx_downloaded_at (downloaded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 공지사항 테이블
CREATE TABLE notices (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL COMMENT '제목',
    content    TEXT         NOT NULL COMMENT '내용',
    author_id  BIGINT       NOT NULL COMMENT '작성자',
    is_pinned  BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '상단고정',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id),
    INDEX idx_author     (author_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 마이그레이션: 기존 DB에 expires_at 추가
-- =============================================
-- ALTER TABLE files ADD COLUMN expires_at DATETIME COMMENT '만료 시간';
-- UPDATE files SET expires_at = DATE_ADD(created_at, INTERVAL 30 DAY);

-- =============================================
-- 마이그레이션: 기존 DB에 role 컬럼 추가
-- =============================================
-- ALTER TABLE users ADD COLUMN role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '역할';
