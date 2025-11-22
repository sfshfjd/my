package com.example.mentalhealth.dto;

import com.example.mentalhealth.entity.Questionnaire;

import java.time.LocalDateTime;

public class QuestionnaireInfoResponse {
    
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
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private String creatorName;
    private Integer downloadCount;
    private String downloadUrl;
    
    // 构造函数
    public QuestionnaireInfoResponse() {
    }
    
    public QuestionnaireInfoResponse(Questionnaire questionnaire, String downloadUrl) {
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
        this.updatedAt = questionnaire.getUpdatedAt();
        this.publishedAt = questionnaire.getPublishedAt();
        this.creatorName = questionnaire.getCreatedBy().getUsername();
        this.downloadCount = questionnaire.getDownloadCount();
        this.downloadUrl = downloadUrl;
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public String getCreatorName() {
        return creatorName;
    }
    
    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
    
    public Integer getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    /**
     * 获取格式化的文件大小
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "未知";
        }
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 获取状态的中文名称
     */
    public String getStatusDisplayName() {
        if (status == null) {
            return "未知";
        }
        
        return switch (status) {
            case DRAFT -> "草稿";
            case PUBLISHED -> "已发布";
            case ARCHIVED -> "已归档";
        };
    }
}
