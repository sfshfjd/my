package com.example.mentalhealth.dto;

import com.example.mentalhealth.entity.Questionnaire;

import java.time.LocalDateTime;

public class QuestionnaireUploadResponse {
    
    private Long id;
    private String title;
    private String description;
    private String originalName;
    private String storedName;
    private Long fileSize;
    private String fileType;
    private Questionnaire.QuestionnaireStatus status;
    private String category;
    private String tags;
    private LocalDateTime createdAt;
    private String downloadUrl;
    private String message;
    
    // 构造函数
    public QuestionnaireUploadResponse() {
    }
    
    public QuestionnaireUploadResponse(String message) {
        this.message = message;
    }
    
    public QuestionnaireUploadResponse(Questionnaire questionnaire, String downloadUrl) {
        this.id = questionnaire.getId();
        this.title = questionnaire.getTitle();
        this.description = questionnaire.getDescription();
        this.originalName = questionnaire.getOriginalName();
        this.storedName = questionnaire.getStoredName();
        this.fileSize = questionnaire.getFileSize();
        this.fileType = questionnaire.getFileType();
        this.status = questionnaire.getStatus();
        this.category = questionnaire.getCategory();
        this.tags = questionnaire.getTags();
        this.createdAt = questionnaire.getCreatedAt();
        this.downloadUrl = downloadUrl;
        this.message = "问卷上传成功";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getOriginalName() {
        return originalName;
    }
    
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    
    public String getStoredName() {
        return storedName;
    }
    
    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public Questionnaire.QuestionnaireStatus getStatus() {
        return status;
    }
    
    public void setStatus(Questionnaire.QuestionnaireStatus status) {
        this.status = status;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
