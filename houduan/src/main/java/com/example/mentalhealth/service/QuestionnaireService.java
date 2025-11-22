package com.example.mentalhealth.service;

import com.example.mentalhealth.dto.QuestionnaireInfoResponse;
import com.example.mentalhealth.dto.QuestionnaireUploadResponse;
import com.example.mentalhealth.entity.Questionnaire;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.repository.QuestionnaireRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionnaireService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireService.class);
    
    @Autowired
    private QuestionnaireRepository questionnaireRepository;
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${file.max.size:52428800}") // 默认50MB
    private long maxFileSize;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    // 支持的文件类型
    private static final String[] ALLOWED_EXTENSIONS = {
        ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt", 
        ".jpg", ".jpeg", ".png", ".gif"
    };
    
    /**
     * 上传问卷
     */
    public QuestionnaireUploadResponse uploadQuestionnaire(
            MultipartFile file, 
            String title, 
            String description,
            String category,
            String tags,
            User user) {
        try {
            // 验证文件
            validateFile(file);
            
            // 如果没有提供标题，使用文件名
            if (!StringUtils.hasText(title)) {
                title = file.getOriginalFilename();
            }
            
            // 生成存储文件名
            String storedName = generateStoredFileName(file.getOriginalFilename());
            
            // 创建问卷专用目录
            Path uploadPath = createQuestionnaireDirectory();
            
            // 保存文件到磁盘
            Path filePath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 保存问卷记录到数据库
            Questionnaire questionnaire = new Questionnaire(
                title,
                file.getOriginalFilename(),
                storedName,
                filePath.toString(),
                file.getSize(),
                file.getContentType(),
                user
            );
            questionnaire.setDescription(description);
            questionnaire.setCategory(category);
            questionnaire.setTags(tags);
            questionnaire = questionnaireRepository.save(questionnaire);
            
            // 生成下载URL
            String downloadUrl = generateDownloadUrl(questionnaire.getId());
            
            logger.info("问卷上传成功: {} (用户: {})", title, user.getUsername());
            
            return new QuestionnaireUploadResponse(questionnaire, downloadUrl);
            
        } catch (Exception e) {
            logger.error("问卷上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("问卷上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载问卷
     */
    @Transactional
    public Resource downloadQuestionnaire(Long questionnaireId, User user) {
        try {
            // 查找问卷记录
            Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findByIdAndIsActiveTrue(questionnaireId);
            if (questionnaireOpt.isEmpty()) {
                throw new RuntimeException("问卷不存在");
            }
            
            Questionnaire questionnaire = questionnaireOpt.get();
            Path filePath = Paths.get(questionnaire.getFilePath());
            
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                throw new RuntimeException("问卷文件不存在: " + questionnaire.getTitle());
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // 增加下载次数
                questionnaire.incrementDownloadCount();
                questionnaireRepository.save(questionnaire);
                
                logger.info("问卷下载: {} (用户: {})", questionnaire.getTitle(), user.getUsername());
                return resource;
            } else {
                throw new RuntimeException("无法读取问卷文件: " + questionnaire.getTitle());
            }
            
        } catch (Exception e) {
            logger.error("问卷下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("问卷下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的问卷列表
     */
    public List<QuestionnaireInfoResponse> getUserQuestionnaires(User user) {
        List<Questionnaire> questionnaires = questionnaireRepository
                .findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(user);
        return questionnaires.stream()
                .map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 分页获取用户的问卷列表
     */
    public Page<QuestionnaireInfoResponse> getUserQuestionnaires(User user, Pageable pageable) {
        Page<Questionnaire> questionnaires = questionnaireRepository
                .findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);
        return questionnaires.map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())));
    }
    
    /**
     * 获取所有已发布的问卷
     */
    public List<QuestionnaireInfoResponse> getPublishedQuestionnaires() {
        List<Questionnaire> questionnaires = questionnaireRepository
                .findByStatusAndIsActiveTrueOrderByPublishedAtDesc(Questionnaire.QuestionnaireStatus.PUBLISHED);
        return questionnaires.stream()
                .map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 分页获取已发布的问卷
     */
    public Page<QuestionnaireInfoResponse> getPublishedQuestionnaires(Pageable pageable) {
        Page<Questionnaire> questionnaires = questionnaireRepository
                .findByStatusAndIsActiveTrueOrderByPublishedAtDesc(
                    Questionnaire.QuestionnaireStatus.PUBLISHED, pageable);
        return questionnaires.map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())));
    }
    
    /**
     * 根据状态获取用户问卷
     */
    public List<QuestionnaireInfoResponse> getUserQuestionnairesByStatus(
            User user, Questionnaire.QuestionnaireStatus status) {
        List<Questionnaire> questionnaires = questionnaireRepository
                .findByCreatedByAndStatusAndIsActiveTrueOrderByCreatedAtDesc(user, status);
        return questionnaires.stream()
                .map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索问卷
     */
    public List<QuestionnaireInfoResponse> searchQuestionnaires(String keyword) {
        List<Questionnaire> questionnaires = questionnaireRepository.findByTitleContaining(keyword);
        return questionnaires.stream()
                .map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索用户问卷
     */
    public List<QuestionnaireInfoResponse> searchUserQuestionnaires(User user, String keyword) {
        List<Questionnaire> questionnaires = questionnaireRepository
                .findByCreatedByAndTitleContaining(user, keyword);
        return questionnaires.stream()
                .map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取问卷详情
     */
    public QuestionnaireInfoResponse getQuestionnaireInfo(Long questionnaireId) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository.findByIdAndIsActiveTrue(questionnaireId);
        if (questionnaireOpt.isEmpty()) {
            throw new RuntimeException("问卷不存在");
        }
        
        Questionnaire questionnaire = questionnaireOpt.get();
        return new QuestionnaireInfoResponse(questionnaire, generateDownloadUrl(questionnaire.getId()));
    }
    
    /**
     * 发布问卷
     */
    @Transactional
    public void publishQuestionnaire(Long questionnaireId, User user) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository
                .findByIdAndCreatedByAndIsActiveTrue(questionnaireId, user);
        if (questionnaireOpt.isEmpty()) {
            throw new RuntimeException("问卷不存在或无权限操作");
        }
        
        Questionnaire questionnaire = questionnaireOpt.get();
        questionnaire.publish();
        questionnaireRepository.save(questionnaire);
        
        logger.info("问卷已发布: {} (用户: {})", questionnaire.getTitle(), user.getUsername());
    }
    
    /**
     * 归档问卷
     */
    @Transactional
    public void archiveQuestionnaire(Long questionnaireId, User user) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository
                .findByIdAndCreatedByAndIsActiveTrue(questionnaireId, user);
        if (questionnaireOpt.isEmpty()) {
            throw new RuntimeException("问卷不存在或无权限操作");
        }
        
        Questionnaire questionnaire = questionnaireOpt.get();
        questionnaire.archive();
        questionnaireRepository.save(questionnaire);
        
        logger.info("问卷已归档: {} (用户: {})", questionnaire.getTitle(), user.getUsername());
    }
    
    /**
     * 删除问卷（软删除）
     */
    @Transactional
    public void deleteQuestionnaire(Long questionnaireId, User user) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository
                .findByIdAndCreatedByAndIsActiveTrue(questionnaireId, user);
        if (questionnaireOpt.isEmpty()) {
            throw new RuntimeException("问卷不存在或无权限删除");
        }
        
        Questionnaire questionnaire = questionnaireOpt.get();
        questionnaire.setIsActive(false);
        questionnaire.setUpdatedAt(LocalDateTime.now());
        questionnaireRepository.save(questionnaire);
        
        logger.info("问卷已删除: {} (用户: {})", questionnaire.getTitle(), user.getUsername());
    }
    
    /**
     * 更新问卷信息
     */
    @Transactional
    public QuestionnaireInfoResponse updateQuestionnaire(
            Long questionnaireId,
            String title,
            String description,
            String category,
            String tags,
            User user) {
        Optional<Questionnaire> questionnaireOpt = questionnaireRepository
                .findByIdAndCreatedByAndIsActiveTrue(questionnaireId, user);
        if (questionnaireOpt.isEmpty()) {
            throw new RuntimeException("问卷不存在或无权限修改");
        }
        
        Questionnaire questionnaire = questionnaireOpt.get();
        
        if (StringUtils.hasText(title)) {
            questionnaire.setTitle(title);
        }
        if (description != null) {
            questionnaire.setDescription(description);
        }
        if (category != null) {
            questionnaire.setCategory(category);
        }
        if (tags != null) {
            questionnaire.setTags(tags);
        }
        
        questionnaire = questionnaireRepository.save(questionnaire);
        
        logger.info("问卷信息已更新: {} (用户: {})", questionnaire.getTitle(), user.getUsername());
        
        return new QuestionnaireInfoResponse(questionnaire, generateDownloadUrl(questionnaire.getId()));
    }
    
    /**
     * 获取问卷统计信息
     */
    public QuestionnaireStatistics getUserQuestionnaireStatistics(User user) {
        long totalCount = questionnaireRepository.countByCreatedByAndIsActiveTrue(user);
        long draftCount = questionnaireRepository.countByCreatedByAndStatusAndIsActiveTrue(
                user, Questionnaire.QuestionnaireStatus.DRAFT);
        long publishedCount = questionnaireRepository.countByCreatedByAndStatusAndIsActiveTrue(
                user, Questionnaire.QuestionnaireStatus.PUBLISHED);
        long archivedCount = questionnaireRepository.countByCreatedByAndStatusAndIsActiveTrue(
                user, Questionnaire.QuestionnaireStatus.ARCHIVED);
        
        return new QuestionnaireStatistics(totalCount, draftCount, publishedCount, archivedCount);
    }
    
    /**
     * 根据分类获取问卷
     */
    public List<QuestionnaireInfoResponse> getQuestionnairesByCategory(String category) {
        List<Questionnaire> questionnaires = questionnaireRepository
                .findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(category);
        return questionnaires.stream()
                .map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 获取热门问卷
     */
    public List<QuestionnaireInfoResponse> getPopularQuestionnaires(int limit) {
        List<Questionnaire> questionnaires = questionnaireRepository
                .findPopularQuestionnaires(Pageable.ofSize(limit));
        return questionnaires.stream()
                .map(q -> new QuestionnaireInfoResponse(q, generateDownloadUrl(q.getId())))
                .collect(Collectors.toList());
    }
    
    // 私有方法
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // 验证文件扩展名
        if (!isAllowedFileType(originalFilename)) {
            throw new IllegalArgumentException("不支持的文件类型。支持的格式：PDF, DOC, DOCX, XLS, XLSX, TXT, JPG, JPEG, PNG, GIF");
        }
    }
    
    private boolean isAllowedFileType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (extension.equals(allowedExt)) {
                return true;
            }
        }
        return false;
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    private String generateStoredFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return "questionnaire_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }
    
    private Path createQuestionnaireDirectory() throws IOException {
        Path uploadPath = Paths.get(uploadDir, "questionnaires");
        Files.createDirectories(uploadPath);
        return uploadPath;
    }
    
    private String generateDownloadUrl(Long questionnaireId) {
        return "http://localhost:" + serverPort + "/api/questionnaires/download/" + questionnaireId;
    }
    
    // 问卷统计信息内部类
    public static class QuestionnaireStatistics {
        private long totalCount;
        private long draftCount;
        private long publishedCount;
        private long archivedCount;
        
        public QuestionnaireStatistics(long totalCount, long draftCount, long publishedCount, long archivedCount) {
            this.totalCount = totalCount;
            this.draftCount = draftCount;
            this.publishedCount = publishedCount;
            this.archivedCount = archivedCount;
        }
        
        // Getters
        public long getTotalCount() { return totalCount; }
        public long getDraftCount() { return draftCount; }
        public long getPublishedCount() { return publishedCount; }
        public long getArchivedCount() { return archivedCount; }
    }
}
