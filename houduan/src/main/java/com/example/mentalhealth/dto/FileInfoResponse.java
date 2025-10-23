package com.example.mentalhealth.dto;

import com.example.mentalhealth.entity.FileRecord;

import java.time.LocalDateTime;

public class FileInfoResponse {
    
    private Long id;
    private String originalName;
    private String storedName;
    private Long fileSize;
    private String fileType;
    private FileRecord.FileCategory category;
    private String description;
    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;
    private String uploaderName;
    private String downloadUrl;
    
    // 构造函数
    public FileInfoResponse() {
    }
    
    public FileInfoResponse(FileRecord fileRecord, String downloadUrl) {
        this.id = fileRecord.getId();
        this.originalName = fileRecord.getOriginalName();
        this.storedName = fileRecord.getStoredName();
        this.fileSize = fileRecord.getFileSize();
        this.fileType = fileRecord.getFileType();
        this.category = fileRecord.getCategory();
        this.description = fileRecord.getDescription();
        this.uploadTime = fileRecord.getCreatedAt();
        this.updateTime = fileRecord.getUpdatedAt();
        this.uploaderName = fileRecord.getUploadedBy().getUsername();
        this.downloadUrl = downloadUrl;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public FileRecord.FileCategory getCategory() {
        return category;
    }
    
    public void setCategory(FileRecord.FileCategory category) {
        this.category = category;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getUploaderName() {
        return uploaderName;
    }
    
    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
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
     * 获取文件分类的中文名称
     */
    public String getCategoryDisplayName() {
        if (category == null) {
            return "未知";
        }
        
        return switch (category) {
            case DOCUMENT -> "文档";
            case VIDEO -> "视频";
            case IMAGE -> "图片";
            case OTHER -> "其他";
        };
    }
}
