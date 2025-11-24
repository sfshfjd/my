package com.example.mentalhealth.repository;

import com.example.mentalhealth.entity.Role;
import com.example.mentalhealth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    Boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    Boolean existsByEmail(String email);
    
    // ==================== 管理员用户管理查询 ====================
    
    /**
     * 根据角色统计用户数量
     * @param role 角色
     * @return 用户数量
     */
    long countByRole(Role role);
    
    /**
     * 根据启用状态统计用户数量
     * @param enabled 是否启用
     * @return 用户数量
     */
    long countByEnabled(Boolean enabled);
    
    /**
     * 搜索用户（根据用户名或邮箱）
     * @param username 用户名关键词
     * @param email 邮箱关键词
     * @return 匹配的用户列表
     */
    List<User> findByUsernameContainingOrEmailContaining(String username, String email);
}
