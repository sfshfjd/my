package com.example.mentalhealth.controller;

import com.example.mentalhealth.dto.*;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.service.AuthService;
import com.example.mentalhealth.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户登录、注册相关接口")
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
    @Operation(summary = "用户登录", description = "用户使用用户名和密码进行登录认证")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功", 
                    content = @Content(schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "400", description = "参数验证失败")
    })
    
    /*@PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Parameter(description = "登录信息", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {

        System.out.println("DEBUG LOGIN username=" + loginRequest.getUsername()
        + " password=[" + loginRequest.getPassword() + "] len="
        + (loginRequest.getPassword()==null? "null" : loginRequest.getPassword().length()));
        try {
            Authentication authentication = authService.authenticateUser(loginRequest);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            User user = (User) authentication.getPrincipal();
            JwtResponse jwtResponse = new JwtResponse(jwt, 
                                                    user.getId(),
                                                    user.getUsername(),
                                                    user.getEmail());
            
            return ResponseEntity.ok(com.example.mentalhealth.dto.ApiResponse.success("登录成功", jwtResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.example.mentalhealth.dto.ApiResponse.error("用户名或密码错误", 401));
        }
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth;
        try {
            auth = authService.authenticateUser(req);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.example.mentalhealth.dto.ApiResponse.error("密码错误", 401));
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(com.example.mentalhealth.dto.ApiResponse.error("用户不存在", 401));
        }

        Object principal = auth.getPrincipal();
        User user;
        if (principal instanceof User u) {
            user = u;
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            // 如果实际返回的是框架默认 UserDetails，需要再查数据库
            user = authService.findByUsername(ud.getUsername()).orElse(null);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.example.mentalhealth.dto.ApiResponse.error("Principal类型异常", 500));
        }

        String jwt;
        try {
            jwt = jwtUtils.generateJwtToken(auth);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.example.mentalhealth.dto.ApiResponse.error("JWT生成失败: " + e.getMessage(), 500));
        }

        JwtResponse data = new JwtResponse(
                jwt,
                user != null ? user.getId() : null,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null
        );

        return ResponseEntity.ok(com.example.mentalhealth.dto.ApiResponse.success("登录成功", data));
    }
    
    /**
     * 用户注册
     * @param registerRequest 注册请求
     * @return 注册响应
     */
    @Operation(summary = "用户注册", description = "新用户注册账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "400", description = "用户名已存在或邮箱已被注册"),
            @ApiResponse(responseCode = "500", description = "注册过程中发生错误")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "注册信息", required = true)
            @Valid @RequestBody RegisterRequest registerRequest) {
        try {
            String message = authService.registerUser(registerRequest);
            return ResponseEntity.ok(com.example.mentalhealth.dto.ApiResponse.success(message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(com.example.mentalhealth.dto.ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(com.example.mentalhealth.dto.ApiResponse.error("注册过程中发生错误", 500));
        }
    }
    
    /**
     * 检查用户名是否可用
     * @param username 用户名
     * @return 检查结果
     */
    @Operation(summary = "检查用户名可用性", description = "检查指定用户名是否已被注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功")
    })
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(
            @Parameter(description = "要检查的用户名", required = true)
            @RequestParam String username) {
        boolean available = !authService.findByUsername(username).isPresent();
        if (available) {
            return ResponseEntity.ok(com.example.mentalhealth.dto.ApiResponse.success("用户名可用"));
        } else {
            return ResponseEntity.ok(com.example.mentalhealth.dto.ApiResponse.error("用户名已存在"));
        }
    }
    
    /**
     * 检查邮箱是否可用
     * @param email 邮箱
     * @return 检查结果
     */
    @Operation(summary = "检查邮箱可用性", description = "检查指定邮箱是否已被注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "检查成功")
    })
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(
            @Parameter(description = "要检查的邮箱", required = true)
            @RequestParam String email) {
        boolean available = !authService.findByEmail(email).isPresent();
        if (available) {
            return ResponseEntity.ok(com.example.mentalhealth.dto.ApiResponse.success("邮箱可用"));
        } else {
            return ResponseEntity.ok(com.example.mentalhealth.dto.ApiResponse.error("邮箱已被注册"));
        }
    }
}
