-- 创建数据库
CREATE DATABASE IF NOT EXISTS login_register_db;
USE login_register_db;

-- 创建用户表（如果使用JPA自动创建表可以跳过这一步）
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID（主键）',
    username VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名（唯一）',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱（唯一）',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '角色（USER-普通用户，ADMIN-管理员）',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否启用',
    affiliation VARCHAR(100) COMMENT '归属',
    name VARCHAR(50) COMMENT '姓名',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT='用户表';

-- 插入测试数据（可选）
INSERT INTO users (username, email, password, role) VALUES 
('admin', 'admin@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN'),
('user', 'user@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER');

-- 注意：上面的密码是"password"的BCrypt加密结果

-- 如果需要在现有数据库中添加归属字段和姓名字段，使用以下ALTER语句：
-- ALTER TABLE users ADD COLUMN affiliation VARCHAR(100) COMMENT '归属';
-- ALTER TABLE users ADD COLUMN name VARCHAR(50) COMMENT '姓名';

-- 如果需要为现有表的字段添加注释，使用以下ALTER语句：
-- ALTER TABLE users MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT '用户ID（主键）';
-- ALTER TABLE users MODIFY COLUMN username VARCHAR(20) NOT NULL UNIQUE COMMENT '用户名（唯一）';
-- ALTER TABLE users MODIFY COLUMN email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱（唯一）';
-- ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）';
-- ALTER TABLE users MODIFY COLUMN role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER' COMMENT '角色（USER-普通用户，ADMIN-管理员）';
-- ALTER TABLE users MODIFY COLUMN is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账户是否启用';
-- ALTER TABLE users MODIFY COLUMN affiliation VARCHAR(100) COMMENT '归属';
-- ALTER TABLE users MODIFY COLUMN name VARCHAR(50) COMMENT '姓名';
-- ALTER TABLE users MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
-- ALTER TABLE users MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
-- ALTER TABLE users COMMENT='用户表';

-- 创建文件记录表
CREATE TABLE IF NOT EXISTS file_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '文件记录ID（主键）',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL UNIQUE COMMENT '存储文件名（唯一）',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(100) COMMENT '文件类型（MIME类型）',
    category ENUM('DOCUMENT', 'VIDEO', 'IMAGE', 'OTHER') NOT NULL COMMENT '文件分类',
    uploaded_by BIGINT NOT NULL COMMENT '上传用户ID',
    description TEXT COMMENT '文件描述',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否有效（软删除标记）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 外键约束
    CONSTRAINT fk_file_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_uploaded_by (uploaded_by),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    INDEX idx_is_active (is_active),
    INDEX idx_file_type (file_type)
) COMMENT='文件记录表';

-- 创建问卷表
CREATE TABLE IF NOT EXISTS questionnaires (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '问卷ID（主键）',
    title VARCHAR(255) NOT NULL COMMENT '问卷标题',
    description TEXT COMMENT '问卷描述',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL UNIQUE COMMENT '存储文件名（唯一）',
    file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_type VARCHAR(100) COMMENT '文件类型（MIME类型）',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT '问卷状态',
    created_by BIGINT NOT NULL COMMENT '创建用户ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    published_at TIMESTAMP NULL COMMENT '发布时间',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否有效（软删除标记）',
    category VARCHAR(100) COMMENT '问卷分类',
    tags VARCHAR(500) COMMENT '标签（用逗号分隔）',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    
    -- 外键约束
    CONSTRAINT fk_questionnaire_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    
    -- 索引
    INDEX idx_created_by (created_by),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at),
    INDEX idx_published_at (published_at),
    INDEX idx_is_active (is_active),
    INDEX idx_download_count (download_count)
) COMMENT='问卷表';

