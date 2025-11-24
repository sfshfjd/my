package com.example.mentalhealth.service;

import com.example.mentalhealth.dto.LoginRequest;
import com.example.mentalhealth.dto.RegisterRequest;
import com.example.mentalhealth.entity.Role;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册结果消息
     */
    public String registerUser(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在！");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被注册！");
        }
        
        // 检查密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致！");
        }
        
        // 创建新用户
        User user = new User(
            registerRequest.getUsername(),
            registerRequest.getEmail(),
            passwordEncoder.encode(registerRequest.getPassword())
        );
        
        userRepository.save(user);
        
        return "用户注册成功！";
    }
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 认证对象
     */
    public Authentication authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }
    
    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户对象
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户对象
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * 更新用户个人信息
     * @param userId 用户ID
     * @param email 新邮箱（可选）
     * @param affiliation 新归属（可选）
     * @param name 新姓名（可选）
     * @return 更新后的用户对象
     */
    public User updateUserProfile(Long userId, String email, String affiliation, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 如果要更新邮箱，检查新邮箱是否已被其他用户使用
        if (email != null && !email.trim().isEmpty() && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("邮箱已被其他用户注册");
            }
            user.setEmail(email);
        }
        
        // 更新归属
        if (affiliation != null) {
            user.setAffiliation(affiliation.trim().isEmpty() ? null : affiliation);
        }
        
        // 更新姓名
        if (name != null) {
            user.setName(name.trim().isEmpty() ? null : name);
        }
        
        return userRepository.save(user);
    }
    
    // ==================== 管理员用户管理功能 ====================
    
    /**
     * 获取所有用户列表（管理员）
     * @return 所有用户列表
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 分页获取所有用户（管理员）
     * @param pageable 分页参数
     * @return 分页用户列表
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    /**
     * 根据ID获取用户（管理员）
     * @param userId 用户ID
     * @return 用户对象
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    /**
     * 创建用户（管理员）
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @param role 角色
     * @param name 姓名
     * @param affiliation 归属
     * @return 创建的用户
     */
    public User createUser(String username, String email, String password, 
                          Role role, String name, String affiliation) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        User user = new User(username, email, passwordEncoder.encode(password));
        user.setRole(role != null ? role : Role.USER);
        user.setName(name);
        user.setAffiliation(affiliation);
        
        return userRepository.save(user);
    }
    
    /**
     * 更新用户信息（管理员）
     * @param userId 用户ID
     * @param email 邮箱
     * @param role 角色
     * @param enabled 是否启用
     * @param name 姓名
     * @param affiliation 归属
     * @return 更新后的用户
     */
    public User updateUser(Long userId, String email, Role role, 
                          Boolean enabled, String name, String affiliation) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 更新邮箱
        if (email != null && !email.trim().isEmpty() && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("邮箱已被其他用户注册");
            }
            user.setEmail(email);
        }
        
        // 更新角色
        if (role != null) {
            user.setRole(role);
        }
        
        // 更新启用状态
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        
        // 更新姓名
        if (name != null) {
            user.setName(name.trim().isEmpty() ? null : name);
        }
        
        // 更新归属
        if (affiliation != null) {
            user.setAffiliation(affiliation.trim().isEmpty() ? null : affiliation);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * 删除用户（管理员）
     * @param userId 用户ID
     */
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.delete(user);
    }
    
    /**
     * 禁用用户（管理员）
     * @param userId 用户ID
     */
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnabled(false);
        userRepository.save(user);
    }
    
    /**
     * 启用用户（管理员）
     * @param userId 用户ID
     */
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnabled(true);
        userRepository.save(user);
    }
    
    /**
     * 重置用户密码（管理员）
     * @param userId 用户ID
     * @param newPassword 新密码
     */
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    /**
     * 搜索用户（管理员）
     * @param keyword 关键词（用户名或邮箱）
     * @return 匹配的用户列表
     */
    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameContainingOrEmailContaining(keyword, keyword);
    }
    
    /**
     * 获取用户统计信息（管理员）
     * @return 统计信息
     */
    public UserStatistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long adminCount = userRepository.countByRole(Role.ADMIN);
        long userCount = userRepository.countByRole(Role.USER);
        long enabledCount = userRepository.countByEnabled(true);
        long disabledCount = userRepository.countByEnabled(false);
        
        return new UserStatistics(totalUsers, adminCount, userCount, enabledCount, disabledCount);
    }
    
    /**
     * 用户统计信息内部类
     */
    public static class UserStatistics {
        private long totalUsers;
        private long adminCount;
        private long userCount;
        private long enabledCount;
        private long disabledCount;
        
        public UserStatistics(long totalUsers, long adminCount, long userCount, 
                            long enabledCount, long disabledCount) {
            this.totalUsers = totalUsers;
            this.adminCount = adminCount;
            this.userCount = userCount;
            this.enabledCount = enabledCount;
            this.disabledCount = disabledCount;
        }
        
        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getAdminCount() { return adminCount; }
        public long getUserCount() { return userCount; }
        public long getEnabledCount() { return enabledCount; }
        public long getDisabledCount() { return disabledCount; }
    }
}
