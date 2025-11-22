package com.example.mentalhealth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "questionnaires")
public class Questionnaire {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "问卷标题不能为空")
    @Column(name = "title", nullable = false)
    private String title; // 问卷标题
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 问卷描述
    
    @NotBlank(message = "原始文件名不能为空")
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
    @Column(name = "status", nullable = false)
    private QuestionnaireStatus status = QuestionnaireStatus.DRAFT; // 问卷状态
    
    @NotNull(message = "创建用户不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy; // 创建用户
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 发布时间
    
    @Column(name = "is_active")
    private Boolean isActive = true; // 是否有效（软删除标记）
    
    @Column(name = "category")
    private String category; // 问卷分类（如：心理评估、满意度调查等）
    
    @Column(name = "tags")
    private String tags; // 标签（用逗号分隔）
    
    @Column(name = "download_count")
    private Integer downloadCount = 0; // 下载次数
    
    // 问卷状态枚举
    public enum QuestionnaireStatus {
        DRAFT,      // 草稿
        PUBLISHED,  // 已发布
        ARCHIVED    // 已归档
    }
    
    // 构造函数
    public Questionnaire() {
    }
    
    public Questionnaire(String title, String originalName, String storedName, 
                        String filePath, Long fileSize, String fileType, User createdBy) {
        this.title = title;
        this.originalName = originalName;
        this.storedName = storedName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.createdBy = createdBy;
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
    
    public QuestionnaireStatus getStatus() {
        return status;
    }
    
    public void setStatus(QuestionnaireStatus status) {
        this.status = status;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    
    public Integer getDownloadCount() {
        return downloadCount;
    }
    
    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
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
    
    /**
     * 增加下载次数
     */
    public void incrementDownloadCount() {
        if (this.downloadCount == null) {
            this.downloadCount = 0;
        }
        this.downloadCount++;
    }
    
    /**
     * 发布问卷
     */
    public void publish() {
        this.status = QuestionnaireStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    
    /**
     * 归档问卷
     */
    public void archive() {
        this.status = QuestionnaireStatus.ARCHIVED;
    }
}
