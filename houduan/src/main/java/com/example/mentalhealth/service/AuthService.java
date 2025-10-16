package com.example.mentalhealth.service;

import com.example.mentalhealth.dto.LoginRequest;
import com.example.mentalhealth.dto.RegisterRequest;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
}
