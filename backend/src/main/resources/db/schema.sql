-- -- =============================================
-- -- Company FileHub Database Schema
-- -- =============================================
-- 
-- CREATE DATABASE IF NOT EXISTS filehub
--     DEFAULT CHARACTER SET utf8mb4
--     DEFAULT COLLATE utf8mb4_unicode_ci;
-- 
-- USE filehub;
-- 
-- -- 부서 테이블
-- CREATE TABLE departments (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(100) NOT NULL UNIQUE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 
-- -- 사용자 테이블
-- CREATE TABLE users (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     employee_id VARCHAR(20) NOT NULL UNIQUE COMMENT '사번',
--     name VARCHAR(50) NOT NULL COMMENT '이름',
--     department_id BIGINT COMMENT '부서',
--     email VARCHAR(100) COMMENT '이메일',
--     password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호',
--     role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '역할',
--     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (department_id) REFERENCES departments(id),
--     INDEX idx_employee_id (employee_id),
--     INDEX idx_name (name)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 
-- -- 파일 테이블
-- CREATE TABLE files (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     original_name VARCHAR(500) NOT NULL COMMENT '원본 파일명',
--     stored_name VARCHAR(500) NOT NULL COMMENT '저장된 파일명 (UUID)',
--     file_path VARCHAR(1000) NOT NULL COMMENT '저장 경로',
--     file_size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
--     file_type VARCHAR(200) COMMENT 'MIME 타입',
--     memo TEXT COMMENT '메모',
--     uploader_id BIGINT NOT NULL,
--     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     expires_at DATETIME COMMENT '만료 시간',
--     FOREIGN KEY (uploader_id) REFERENCES users(id),
--     INDEX idx_uploader (uploader_id),
--     INDEX idx_created_at (created_at)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 
-- -- 파일 수신자 테이블
-- CREATE TABLE file_recipients (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     file_id BIGINT NOT NULL,
--     recipient_id BIGINT NOT NULL,
--     is_downloaded BOOLEAN NOT NULL DEFAULT FALSE COMMENT '다운로드 여부',
--     first_downloaded_at DATETIME COMMENT '최초 다운로드 시간',
--     FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
--     FOREIGN KEY (recipient_id) REFERENCES users(id),
--     UNIQUE KEY uk_file_recipient (file_id, recipient_id),
--     INDEX idx_recipient (recipient_id),
--     INDEX idx_downloaded (is_downloaded)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 
-- -- 다운로드 로그 테이블
-- CREATE TABLE download_logs (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     file_id BIGINT NOT NULL,
--     downloader_id BIGINT NOT NULL,
--     downloaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     ip_address VARCHAR(45) COMMENT 'IP 주소',
--     FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
--     FOREIGN KEY (downloader_id) REFERENCES users(id),
--     INDEX idx_file_id (file_id),
--     INDEX idx_downloader (downloader_id),
--     INDEX idx_downloaded_at (downloaded_at)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 
-- -- 공지사항 테이블
-- CREATE TABLE notices (
--     id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
--     title      VARCHAR(200) NOT NULL COMMENT '제목',
--     content    TEXT         NOT NULL COMMENT '내용',
--     author_id  BIGINT       NOT NULL COMMENT '작성자',
--     is_pinned  BOOLEAN      NOT NULL DEFAULT FALSE COMMENT '상단고정',
--     created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
--     FOREIGN KEY (author_id) REFERENCES users(id),
--     INDEX idx_author     (author_id),
--     INDEX idx_created_at (created_at)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- 
-- -- =============================================
-- -- 마이그레이션: 기존 DB에 expires_at 추가
-- -- =============================================
-- -- ALTER TABLE files ADD COLUMN expires_at DATETIME COMMENT '만료 시간';
-- -- UPDATE files SET expires_at = DATE_ADD(created_at, INTERVAL 30 DAY);
-- 
-- -- =============================================
-- -- 마이그레이션: 기존 DB에 role 컬럼 추가
-- -- =============================================
-- -- ALTER TABLE users ADD COLUMN role ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER' COMMENT '역할';


-- =============================================
-- Company FileHub Database Schema (PostgreSQL)
-- =============================================

-- 기존 테이블 및 타입 초기화 (외래 키 의존성 고려하여 CASCADE 적용)
DROP TABLE IF EXISTS download_logs CASCADE;
DROP TABLE IF EXISTS file_recipients CASCADE;
DROP TABLE IF EXISTS files CASCADE;
DROP TABLE IF EXISTS notices CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS departments CASCADE;

DROP TYPE IF EXISTS user_role;

-- 1. ENUM 타입 정의 (기존 MySQL ENUM 매핑)
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

-- 2. 부서 테이블
CREATE TABLE departments (
    id bigint GENERATED ALWAYS AS IDENTITY,
    name varchar(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_departments_name UNIQUE (name)
);

-- 3. 사용자 테이블
CREATE TABLE users (
    id bigint GENERATED ALWAYS AS IDENTITY,
    employee_id varchar(20) NOT NULL,
    name varchar(50) NOT NULL,
    email varchar(100) DEFAULT NULL,
    password varchar(255) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    department_id bigint DEFAULT NULL,
    role user_role NOT NULL DEFAULT 'USER',
    PRIMARY KEY (id),
    CONSTRAINT uk_users_employee_id UNIQUE (employee_id),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id)
);

-- 코멘트 등록 (PostgreSQL 방식)
COMMENT ON COLUMN users.employee_id IS '사번';
COMMENT ON COLUMN users.name IS '이름';
COMMENT ON COLUMN users.department_id IS '부서';
COMMENT ON COLUMN users.email IS '이메일';
COMMENT ON COLUMN users.password IS '암호화된 비밀번호';
COMMENT ON COLUMN users.role IS '역할';

-- users 테이블 인덱스
CREATE INDEX idx_users_employee_id ON users (employee_id);
CREATE INDEX idx_users_name ON users (name);
CREATE INDEX idx_users_department_id ON users (department_id);


-- 4. 파일 테이블
CREATE TABLE files (
    id bigint GENERATED ALWAYS AS IDENTITY,
    original_name varchar(500) NOT NULL,
    stored_name varchar(500) NOT NULL,
    file_path varchar(1000) NOT NULL,
    file_size bigint NOT NULL,
    file_type varchar(200) DEFAULT NULL,
    memo text DEFAULT NULL,
    uploader_id bigint NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at timestamp DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_files_uploader FOREIGN KEY (uploader_id) REFERENCES users (id)
);

COMMENT ON COLUMN files.original_name IS '원본 파일명';
COMMENT ON COLUMN files.stored_name IS '저장된 파일명';
COMMENT ON COLUMN files.file_path IS '서버 내 저장 경로';
COMMENT ON COLUMN files.file_size IS '파일 크기(Byte)';
COMMENT ON COLUMN files.uploader_id IS '업로더';
COMMENT ON COLUMN files.expires_at IS '만료 시간';

-- files 테이블 인덱스
CREATE INDEX idx_files_uploader ON files (uploader_id);
CREATE INDEX idx_files_created_at ON files (created_at);


-- 5. 다운로드 이력 테이블
CREATE TABLE download_logs (
    id bigint GENERATED ALWAYS AS IDENTITY,
    file_id bigint NOT NULL,
    downloader_id bigint NOT NULL,
    downloaded_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address varchar(45) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_download_logs_file FOREIGN KEY (file_id) REFERENCES files (id) ON DELETE CASCADE,
    CONSTRAINT fk_download_logs_downloader FOREIGN KEY (downloader_id) REFERENCES users (id)
);

-- download_logs 테이블 인덱스
CREATE INDEX idx_download_logs_file_id ON download_logs (file_id);
CREATE INDEX idx_download_logs_downloader ON download_logs (downloader_id);
CREATE INDEX idx_download_logs_downloaded_at ON download_logs (downloaded_at);

-- 6. file_recipients 테이블 생성
CREATE TABLE file_recipients (
    id bigint GENERATED ALWAYS AS IDENTITY,
    file_id bigint NOT NULL,
    recipient_id bigint NOT NULL,
    is_downloaded boolean NOT NULL DEFAULT FALSE,
    first_downloaded_at timestamp DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_file_recipient UNIQUE (file_id, recipient_id),
    CONSTRAINT fk_file_recipients_file FOREIGN KEY (file_id) REFERENCES files (id) ON DELETE CASCADE,
    CONSTRAINT fk_file_recipients_recipient FOREIGN KEY (recipient_id) REFERENCES users (id)
);

-- file_recipients 테이블 인덱스
CREATE INDEX idx_file_recipients_recipient ON file_recipients (recipient_id);
CREATE INDEX idx_file_recipients_downloaded ON file_recipients (is_downloaded);


-- 7. 공지사항 테이블
CREATE TABLE notices (
    id bigint GENERATED ALWAYS AS IDENTITY,
    title varchar(200) NOT NULL,
    content text NOT NULL,
    author_id bigint NOT NULL,
    is_pinned boolean NOT NULL DEFAULT FALSE,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_notices_author FOREIGN KEY (author_id) REFERENCES users (id)
);

COMMENT ON COLUMN notices.title IS '제목';
COMMENT ON COLUMN notices.content IS '내용';
COMMENT ON COLUMN notices.author_id IS '작성자';
COMMENT ON COLUMN notices.is_pinned IS '상단고정';

-- notices 테이블 인덱스
CREATE INDEX idx_notices_author ON notices (author_id);
CREATE INDEX idx_notices_created_at ON notices (created_at);


-- =============================================
-- 8. 자동으로 updated_at을 갱신하기 위한 트리거 설정
-- =============================================
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- users 테이블 트리거 연결
CREATE TRIGGER update_users_modtime
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE PROCEDURE update_modified_column();

-- notices 테이블 트리거 연결
CREATE TRIGGER update_notices_modtime
    BEFORE UPDATE ON notices
    FOR EACH ROW
    EXECUTE PROCEDURE update_modified_column();