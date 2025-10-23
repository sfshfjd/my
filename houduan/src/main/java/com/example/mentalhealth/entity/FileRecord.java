package com.example.mentalhealth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_records")
public class FileRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "文件名不能为空")
    @Column(name = "original_name", nullable = false)
    private String originalName; // 原始文件名
    
    @NotBlank(message = "存储文件名不能为空")
    @Column(name = "stored_name", nullable = false, unique = true)
    private String storedName; // 存储时的文件名（防止重名）
    
    @NotBlank(message = "文件路径不能为空")
    @Column(name = "file_path", nullable = false)
    private String filePath; // 文件存储路径
    
    @Column(name = "file_size")
    private Long fileSize; // 文件大小（字节）
    
    @Column(name = "file_type")
    private String fileType; // 文件类型（MIME类型）
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private FileCategory category; // 文件分类
    
    @NotNull(message = "上传用户不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy; // 上传用户
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "description")
    private String description; // 文件描述
    
    @Column(name = "is_active")
    private Boolean isActive = true; // 是否有效（软删除标记）
    
    // 文件分类枚举
    public enum FileCategory {
        DOCUMENT,  // 文档（Word, PDF）
        VIDEO,     // 视频
        IMAGE,     // 图片
        OTHER      // 其他
    }
    
    // 构造函数
    public FileRecord() {
    }
    
    public FileRecord(String originalName, String storedName, String filePath, 
                     Long fileSize, String fileType, FileCategory category, User uploadedBy) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.category = category;
        this.uploadedBy = uploadedBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
    
    public FileCategory getCategory() {
        return category;
    }
    
    public void setCategory(FileCategory category) {
        this.category = category;
    }
    
    public User getUploadedBy() {
        return uploadedBy;
    }
    
    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
