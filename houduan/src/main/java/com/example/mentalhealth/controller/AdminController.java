package com.example.mentalhealth.controller;

import com.example.mentalhealth.dto.ApiResponse;
import com.example.mentalhealth.dto.FileInfoResponse;
import com.example.mentalhealth.dto.QuestionnaireInfoResponse;
import com.example.mentalhealth.entity.Role;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.service.AuthService;
import com.example.mentalhealth.service.FileService;
import com.example.mentalhealth.service.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员功能", description = "管理员用户管理接口")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private QuestionnaireService questionnaireService;
    
    @Autowired
    private FileService fileService;

    /**
     * 获取所有用户列表
     */
    @GetMapping("/users")
    @Operation(summary = "获取所有用户", description = "获取系统中所有用户的列表")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "权限不足")
    })
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = authService.getAllUsers();
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", userResponses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping("/users/paged")
    @Operation(summary = "分页获取用户", description = "分页获取系统中的用户列表")
    public ResponseEntity<?> getUsersPaged(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> usersPage = authService.getAllUsers(pageable);
            Page<UserResponse> userResponses = usersPage.map(UserResponse::new);
            return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", userResponses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取用户列表失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = authService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", new UserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 创建用户
     */
    @PostMapping("/users")
    @Operation(summary = "创建用户", description = "管理员创建新用户")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = authService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole(),
                    request.getName(),
                    request.getAffiliation()
            );
            return ResponseEntity.ok(ApiResponse.success("用户创建成功", new UserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{userId}")
    @Operation(summary = "更新用户信息", description = "管理员更新用户信息")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {
        try {
            User user = authService.updateUser(
                    userId,
                    request.getEmail(),
                    request.getRole(),
                    request.getEnabled(),
                    request.getName(),
                    request.getAffiliation()
            );
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", new UserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户", description = "管理员删除用户")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            authService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("用户删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 禁用用户
     */
    @PostMapping("/users/{userId}/disable")
    @Operation(summary = "禁用用户", description = "禁用指定用户账号")
    public ResponseEntity<?> disableUser(@PathVariable Long userId) {
        try {
            authService.disableUser(userId);
            return ResponseEntity.ok(ApiResponse.success("用户已禁用"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 启用用户
     */
    @PostMapping("/users/{userId}/enable")
    @Operation(summary = "启用用户", description = "启用指定用户账号")
    public ResponseEntity<?> enableUser(@PathVariable Long userId) {
        try {
            authService.enableUser(userId);
            return ResponseEntity.ok(ApiResponse.success("用户已启用"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/users/{userId}/reset-password")
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long userId,
            @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetUserPassword(userId, request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 搜索用户
     */
    @GetMapping("/users/search")
    @Operation(summary = "搜索用户", description = "根据用户名或邮箱搜索用户")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword) {
        try {
            List<User> users = authService.searchUsers(keyword);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.success("搜索成功", userResponses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计", description = "获取系统用户统计信息")
    public ResponseEntity<?> getUserStatistics() {
        try {
            AuthService.UserStatistics stats = authService.getUserStatistics();
            return ResponseEntity.ok(ApiResponse.success("获取统计信息成功", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取统计信息失败: " + e.getMessage()));
        }
    }
    
    // ==================== 管理员问卷管理 ====================
    
    @GetMapping("/questionnaires/paged")
    @Operation(summary = "管理员分页获取问卷", description = "管理员查看所有问卷")
    public ResponseEntity<?> getQuestionnairesForAdmin(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QuestionnaireInfoResponse> questionnaires = questionnaireService.getAllQuestionnaires(pageable);
            return ResponseEntity.ok(ApiResponse.success("获取问卷列表成功", questionnaires));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取问卷列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/questionnaires/{questionnaireId}")
    @Operation(summary = "管理员查看问卷详情", description = "查看任意问卷的详细信息")
    public ResponseEntity<?> getQuestionnaireInfoForAdmin(@PathVariable Long questionnaireId) {
        try {
            QuestionnaireInfoResponse questionnaire = questionnaireService.getQuestionnaireInfo(questionnaireId);
            return ResponseEntity.ok(ApiResponse.success("获取问卷信息成功", questionnaire));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/questionnaires/{questionnaireId}/download")
    @Operation(summary = "管理员下载问卷", description = "管理员可下载任意问卷文件")
    public ResponseEntity<Resource> downloadQuestionnaireForAdmin(
            @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User adminUser) {
        try {
            Resource resource = questionnaireService.downloadQuestionnaire(questionnaireId, adminUser);
            QuestionnaireInfoResponse info = questionnaireService.getQuestionnaireInfo(questionnaireId);
            String encodedFilename = URLEncoder.encode(info.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + info.getOriginalName() +
                                    "\"; filename*=UTF-8''" + encodedFilename)
                    .header(HttpHeaders.CONTENT_TYPE, info.getFileType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(info.getFileSize()))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @DeleteMapping("/questionnaires/{questionnaireId}")
    @Operation(summary = "管理员删除问卷", description = "管理员可删除任意问卷（软删除）")
    public ResponseEntity<?> deleteQuestionnaireForAdmin(@PathVariable Long questionnaireId) {
        try {
            questionnaireService.deleteQuestionnaireAsAdmin(questionnaireId);
            return ResponseEntity.ok(ApiResponse.success("问卷删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    // ==================== 管理员文件管理 ====================
    
    @GetMapping("/files/paged")
    @Operation(summary = "管理员分页获取文件", description = "管理员查看所有上传的文件")
    public ResponseEntity<?> getFilesForAdmin(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FileInfoResponse> files = fileService.getAllFiles(pageable);
            return ResponseEntity.ok(ApiResponse.success("获取文件列表成功", files));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取文件列表失败: " + e.getMessage()));
        }
    }
    
    @GetMapping("/files/{fileId}")
    @Operation(summary = "管理员查看文件详情", description = "查看任意文件的详细信息")
    public ResponseEntity<?> getFileInfoForAdmin(@PathVariable Long fileId) {
        try {
            FileInfoResponse fileInfo = fileService.getFileInfoAsAdmin(fileId);
            return ResponseEntity.ok(ApiResponse.success("获取文件信息成功", fileInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/files/{fileId}/download")
    @Operation(summary = "管理员下载文件", description = "管理员可下载任意文件")
    public ResponseEntity<Resource> downloadFileForAdmin(@PathVariable Long fileId) {
        try {
            Resource resource = fileService.downloadFileAsAdmin(fileId);
            FileInfoResponse fileInfo = fileService.getFileInfoAsAdmin(fileId);
            String encodedFilename = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileInfo.getOriginalName() +
                                    "\"; filename*=UTF-8''" + encodedFilename)
                    .header(HttpHeaders.CONTENT_TYPE, fileInfo.getFileType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.getFileSize()))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @DeleteMapping("/files/{fileId}")
    @Operation(summary = "管理员删除文件", description = "管理员可删除任意文件（软删除）")
    public ResponseEntity<?> deleteFileForAdmin(@PathVariable Long fileId) {
        try {
            fileService.deleteFileAsAdmin(fileId);
            return ResponseEntity.ok(ApiResponse.success("文件删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // ==================== DTO 内部类 ====================

    /**
     * 用户响应DTO
     */
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String role;
        private Boolean enabled;
        private String name;
        private String affiliation;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        public UserResponse(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.role = user.getRole().name();
            this.enabled = user.getEnabled();
            this.name = user.getName();
            this.affiliation = user.getAffiliation();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public Boolean getEnabled() { return enabled; }
        public String getName() { return name; }
        public String getAffiliation() { return affiliation; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    /**
     * 创建用户请求DTO
     */
    @Schema(description = "创建用户请求")
    public static class CreateUserRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码长度不能少于6个字符")
        private String password;

        private Role role = Role.USER;
        private String name;
        private String affiliation;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAffiliation() { return affiliation; }
        public void setAffiliation(String affiliation) { this.affiliation = affiliation; }
    }

    /**
     * 更新用户请求DTO
     */
    @Schema(description = "更新用户请求")
    public static class UpdateUserRequest {
        @Email(message = "邮箱格式不正确")
        private String email;
        private Role role;
        private Boolean enabled;
        private String name;
        private String affiliation;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAffiliation() { return affiliation; }
        public void setAffiliation(String affiliation) { this.affiliation = affiliation; }
    }

    /**
     * 重置密码请求DTO
     */
    @Schema(description = "重置密码请求")
    public static class ResetPasswordRequest {
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, message = "密码长度不能少于6个字符")
        private String newPassword;

        // Getters and Setters
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}