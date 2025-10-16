package com.example.mentalhealth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 100, message = "归属长度不能超过100个字符")
    private String affiliation;
    
    @Size(max = 50, message = "姓名长度不能超过50个字符")
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
