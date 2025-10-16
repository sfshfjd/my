package com.example.mentalhealth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "用户信息更新请求")
public class UpdateProfileRequest {
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "新的邮箱地址", example = "newemail@example.com")
    private String email;
    
    @Size(max = 100, message = "归属长度不能超过100个字符")
    @Schema(description = "用户归属/所属机构", example = "心理咨询中心", maxLength = 100)
    private String affiliation;
    
    @Size(max = 50, message = "姓名长度不能超过50个字符")
    @Schema(description = "用户真实姓名", example = "张三", maxLength = 50)
    private String name;
    
    // 构造函数
    public UpdateProfileRequest() {
    }
    
    public UpdateProfileRequest(String email, String affiliation, String name) {
        this.email = email;
        this.affiliation = affiliation;
        this.name = name;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAffiliation() {
        return affiliation;
    }
    
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
