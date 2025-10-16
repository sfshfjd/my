package com.example.mentalhealth.controller;

import com.example.mentalhealth.dto.ApiResponse;
import com.example.mentalhealth.dto.UpdateProfileRequest;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 公共接口，无需认证
     * @return 响应
     */
    @GetMapping("/public")
    public ResponseEntity<?> allAccess() {
        return ResponseEntity.ok(ApiResponse.success("这是一个公共接口，无需认证即可访问"));
    }
    
    /**
     * 需要认证的接口
     * @return 当前用户信息
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> userAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        return ResponseEntity.ok(ApiResponse.success(
            "欢迎 " + user.getUsername() + "！这是需要认证的用户接口", 
            user
        ));
    }
    
    /**
     * 管理员专用接口
     * @return 响应
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminAccess() {
        return ResponseEntity.ok(ApiResponse.success("这是管理员专用接口"));
    }
    
    /**
     * 获取当前用户信息
     * @return 当前用户信息
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        
        // 创建用户信息响应，不包含敏感信息
        UserProfileResponse profile = new UserProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name(),
            user.getAffiliation(),
            user.getName(),
            user.getCreatedAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", profile));
    }
    
    /**
     * 更新当前用户信息
     * @param updateRequest 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest updateRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            
            // 更新用户信息
            User updatedUser = authService.updateUserProfile(
                currentUser.getId(),
                updateRequest.getEmail(),
                updateRequest.getAffiliation(),
                updateRequest.getName()
            );
            
            // 创建响应对象
            UserProfileResponse profile = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getEmail(),
                updatedUser.getRole().name(),
                updatedUser.getAffiliation(),
                updatedUser.getName(),
                updatedUser.getCreatedAt()
            );
            
            return ResponseEntity.ok(ApiResponse.success("个人信息更新成功", profile));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("更新过程中发生错误", 500));
        }
    }
    
    // 内部类：用户信息响应
    public static class UserProfileResponse {
        private Long id;
        private String username;
        private String email;
        private String role;
        private String affiliation;
        private String name;
        private java.time.LocalDateTime createdAt;
        
        public UserProfileResponse(Long id, String username, String email, 
                                 String role, String affiliation, String name,
                                 java.time.LocalDateTime createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
            this.affiliation = affiliation;
            this.name = name;
            this.createdAt = createdAt;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getAffiliation() { return affiliation; }
        public String getName() { return name; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    }
}
