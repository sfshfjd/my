package com.example.mentalhealth.controller;

import com.example.mentalhealth.dto.*;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.service.AuthService;
import com.example.mentalhealth.util.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authService.authenticateUser(loginRequest);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            User user = (User) authentication.getPrincipal();
            JwtResponse jwtResponse = new JwtResponse(jwt, 
                                                    user.getId(),
                                                    user.getUsername(),
                                                    user.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", jwtResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("用户名或密码错误", 401));
        }
    }
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            String message = authService.registerUser(registerRequest);
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("注册过程中发生错误", 500));
        }
    }
    
    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean available = !authService.findByUsername(username).isPresent();
        if (available) {
            return ResponseEntity.ok(ApiResponse.success("用户名可用"));
        } else {
            return ResponseEntity.ok(ApiResponse.error("用户名已存在"));
        }
    }
    
    /**
     * 检查邮箱是否可用
     * @param email 邮箱
     * @return 检查结果
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean available = !authService.findByEmail(email).isPresent();
        if (available) {
            return ResponseEntity.ok(ApiResponse.success("邮箱可用"));
        } else {
            return ResponseEntity.ok(ApiResponse.error("邮箱已被注册"));
        }
    }
}
