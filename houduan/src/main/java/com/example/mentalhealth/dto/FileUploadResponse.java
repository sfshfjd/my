package com.example.mentalhealth.dto;

import com.example.mentalhealth.entity.FileRecord;

import java.time.LocalDateTime;

public class FileUploadResponse {
    
    private Long id;
    private String originalName;
    private String storedName;
    private Long fileSize;
    private String fileType;
    private FileRecord.FileCategory category;
    private String description;
    private LocalDateTime uploadTime;
    private String downloadUrl;
    private String message;
    
    // 构造函数
    public FileUploadResponse() {
    }
    
    public FileUploadResponse(String message) {
        this.message = message;
    }
    
    public FileUploadResponse(FileRecord fileRecord, String downloadUrl) {
        this.id = fileRecord.getId();
        this.originalName = fileRecord.getOriginalName();
        this.storedName = fileRecord.getStoredName();
        this.fileSize = fileRecord.getFileSize();
        this.fileType = fileRecord.getFileType();
        this.category = fileRecord.getCategory();
        this.description = fileRecord.getDescription();
        this.uploadTime = fileRecord.getCreatedAt();
        this.downloadUrl = downloadUrl;
        this.message = "文件上传成功";
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
