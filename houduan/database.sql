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

