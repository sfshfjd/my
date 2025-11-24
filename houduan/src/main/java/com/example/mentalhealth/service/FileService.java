package com.example.mentalhealth.service;

import com.example.mentalhealth.dto.FileInfoResponse;
import com.example.mentalhealth.dto.FileUploadResponse;
import com.example.mentalhealth.entity.FileRecord;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.repository.FileRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
public class FileService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    
    @Autowired
    private FileRecordRepository fileRecordRepository;
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${file.max.size:52428800}") // 默认50MB
    private long maxFileSize;
    
    @Value("${server.port:8080}")
    private String serverPort;
    
    // 支持的文件类型
    private static final String[] ALLOWED_DOCUMENT_EXTENSIONS = {".pdf", ".doc", ".docx"};
    private static final String[] ALLOWED_VIDEO_EXTENSIONS = {".mp4", ".avi", ".mov", ".wmv", ".flv", ".mkv"};
    private static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"};
    
    /**
     * 上传文件
     */
    public FileUploadResponse uploadFile(MultipartFile file, String description, User user) {
        try {
            // 验证文件
            validateFile(file);
            // 确定文件分类
            FileRecord.FileCategory category = determineFileCategory(file.getOriginalFilename());
            // 生成存储文件名
            String storedName = generateStoredFileName(file.getOriginalFilename());
            // 创建上传目录
            Path uploadPath = createUploadDirectory(category);
            // 保存文件到磁盘
            Path filePath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            // 保存文件记录到数据库
            FileRecord fileRecord = new FileRecord(
                file.getOriginalFilename(),
                storedName,
                filePath.toString(),
                file.getSize(),
                file.getContentType(),
                category,
                user
            );
            fileRecord.setDescription(description);
            fileRecord = fileRecordRepository.save(fileRecord);
            // 生成下载URL
            String downloadUrl = generateDownloadUrl(fileRecord.getId());
            logger.info("文件上传成功: {} (用户: {})", file.getOriginalFilename(), user.getUsername());
            return new FileUploadResponse(fileRecord, downloadUrl);
        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件
     */
    public Resource downloadFile(Long fileId, User user) {
        try {
            // 查找文件记录
            Optional<FileRecord> fileRecordOpt = fileRecordRepository.findByIdAndUploadedByAndIsActiveTrue(fileId, user);
            if (fileRecordOpt.isEmpty()) {
                throw new RuntimeException("文件不存在或无权限访问");
            }
            FileRecord fileRecord = fileRecordOpt.get();
            Path filePath = Paths.get(fileRecord.getFilePath());
            // 检查文件是否存在
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在: " + fileRecord.getOriginalName());
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                logger.info("文件下载: {} (用户: {})", fileRecord.getOriginalName(), user.getUsername());
                return resource;
            } else {
                throw new RuntimeException("无法读取文件: " + fileRecord.getOriginalName());
            }
        } catch (Exception e) {
            logger.error("文件下载失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的文件列表
     */
    public List<FileInfoResponse> getUserFiles(User user) {
        List<FileRecord> files = fileRecordRepository.findByUploadedByAndIsActiveTrueOrderByCreatedAtDesc(user);
        return files.stream()
                .map(file -> new FileInfoResponse(file, generateDownloadUrl(file.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 管理员分页获取全部文件
     */
    public Page<FileInfoResponse> getAllFiles(Pageable pageable) {
        Page<FileRecord> files = fileRecordRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return files.map(file -> new FileInfoResponse(file, generateDownloadUrl(file.getId())));
    }
    
    /**
     * 分页获取用户的文件列表
     */
    public Page<FileInfoResponse> getUserFiles(User user, Pageable pageable) {
        Page<FileRecord> files = fileRecordRepository.findByUploadedByAndIsActiveTrueOrderByCreatedAtDesc(user, pageable);
        return files.map(file -> new FileInfoResponse(file, generateDownloadUrl(file.getId())));
    }
    
    /**
     * 根据分类获取用户文件
     */
    public List<FileInfoResponse> getUserFilesByCategory(User user, FileRecord.FileCategory category) {
        List<FileRecord> files = fileRecordRepository.findByUploadedByAndCategoryAndIsActiveTrueOrderByCreatedAtDesc(user, category);
        return files.stream()
                .map(file -> new FileInfoResponse(file, generateDownloadUrl(file.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索用户文件
     */
    public List<FileInfoResponse> searchUserFiles(User user, String filename) {
        List<FileRecord> files = fileRecordRepository.findByUploadedByAndOriginalNameContaining(user, filename);
        return files.stream()
                .map(file -> new FileInfoResponse(file, generateDownloadUrl(file.getId())))
                .collect(Collectors.toList());
    }
    
    /**
     * 删除文件（软删除）
     */
    public void deleteFile(Long fileId, User user) {
        Optional<FileRecord> fileRecordOpt = fileRecordRepository.findByIdAndUploadedByAndIsActiveTrue(fileId, user);
        if (fileRecordOpt.isEmpty()) {
            throw new RuntimeException("文件不存在或无权限删除");
        }
        
        FileRecord fileRecord = fileRecordOpt.get();
        fileRecord.setIsActive(false);
        fileRecord.setUpdatedAt(LocalDateTime.now());
        fileRecordRepository.save(fileRecord);
        
        logger.info("文件已删除: {} (用户: {})", fileRecord.getOriginalName(), user.getUsername());
    }
    
    /**
     * 获取文件信息
     */
    public FileInfoResponse getFileInfo(Long fileId, User user) {
        Optional<FileRecord> fileRecordOpt = fileRecordRepository.findByIdAndUploadedByAndIsActiveTrue(fileId, user);
        if (fileRecordOpt.isEmpty()) {
            throw new RuntimeException("文件不存在或无权限访问");
        }
        
        FileRecord fileRecord = fileRecordOpt.get();
        return new FileInfoResponse(fileRecord, generateDownloadUrl(fileRecord.getId()));
    }
    
    /**
     * 管理员获取任意文件信息
     */
    public FileInfoResponse getFileInfoAsAdmin(Long fileId) {
        FileRecord fileRecord = fileRecordRepository.findByIdAndIsActiveTrue(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        return new FileInfoResponse(fileRecord, generateDownloadUrl(fileRecord.getId()));
    }
    
    /**
     * 管理员下载任意文件
     */
    public Resource downloadFileAsAdmin(Long fileId) {
        FileRecord fileRecord = fileRecordRepository.findByIdAndIsActiveTrue(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        Path filePath = Paths.get(fileRecord.getFilePath());
        try {
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在: " + fileRecord.getOriginalName());
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                logger.info("管理员下载文件: {}", fileRecord.getOriginalName());
                return resource;
            } else {
                throw new RuntimeException("无法读取文件: " + fileRecord.getOriginalName());
            }
        } catch (IOException e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员删除文件（软删除）
     */
    public void deleteFileAsAdmin(Long fileId) {
        FileRecord fileRecord = fileRecordRepository.findByIdAndIsActiveTrue(fileId)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        fileRecord.setIsActive(false);
        fileRecord.setUpdatedAt(LocalDateTime.now());
        fileRecordRepository.save(fileRecord);
        
        logger.info("管理员删除文件: {}", fileRecord.getOriginalName());
    }
    
    /**
     * 获取用户文件统计信息
     */
    public FileStatistics getUserFileStatistics(User user) {
        long totalFiles = fileRecordRepository.countByUploadedByAndIsActiveTrue(user);
        long documentCount = fileRecordRepository.countByUploadedByAndCategoryAndIsActiveTrue(user, FileRecord.FileCategory.DOCUMENT);
        long videoCount = fileRecordRepository.countByUploadedByAndCategoryAndIsActiveTrue(user, FileRecord.FileCategory.VIDEO);
        long imageCount = fileRecordRepository.countByUploadedByAndCategoryAndIsActiveTrue(user, FileRecord.FileCategory.IMAGE);
        Long totalSize = fileRecordRepository.sumFileSizeByUser(user);
        
        return new FileStatistics(totalFiles, documentCount, videoCount, imageCount, totalSize != null ? totalSize : 0L);
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
            throw new IllegalArgumentException("不支持的文件类型。支持的格式：PDF, DOC, DOCX, MP4, AVI, MOV, WMV, FLV, MKV, JPG, JPEG, PNG, GIF, BMP, WEBP");
        }
    }
    
    private boolean isAllowedFileType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        for (String allowedExt : ALLOWED_DOCUMENT_EXTENSIONS) {
            if (extension.equals(allowedExt)) return true;
        }
        for (String allowedExt : ALLOWED_VIDEO_EXTENSIONS) {
            if (extension.equals(allowedExt)) return true;
        }
        for (String allowedExt : ALLOWED_IMAGE_EXTENSIONS) {
            if (extension.equals(allowedExt)) return true;
        }
        
        return false;
    }
    
    private FileRecord.FileCategory determineFileCategory(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        
        for (String docExt : ALLOWED_DOCUMENT_EXTENSIONS) {
            if (extension.equals(docExt)) return FileRecord.FileCategory.DOCUMENT;
        }
        for (String videoExt : ALLOWED_VIDEO_EXTENSIONS) {
            if (extension.equals(videoExt)) return FileRecord.FileCategory.VIDEO;
        }
        for (String imageExt : ALLOWED_IMAGE_EXTENSIONS) {
            if (extension.equals(imageExt)) return FileRecord.FileCategory.IMAGE;
        }
        
        return FileRecord.FileCategory.OTHER;
    }
    
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    private String generateStoredFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }
    
    private Path createUploadDirectory(FileRecord.FileCategory category) throws IOException {
        String categoryDir = category.name().toLowerCase();
        Path uploadPath = Paths.get(uploadDir, categoryDir);
        Files.createDirectories(uploadPath);
        return uploadPath;
    }
    
    private String generateDownloadUrl(Long fileId) {
        return "http://localhost:" + serverPort + "/api/files/download/" + fileId;
    }
    
    // 文件统计信息内部类
    public static class FileStatistics {
        private long totalFiles;
        private long documentCount;
        private long videoCount;
        private long imageCount;
        private long totalSize;
        
        public FileStatistics(long totalFiles, long documentCount, long videoCount, long imageCount, long totalSize) {
            this.totalFiles = totalFiles;
            this.documentCount = documentCount;
            this.videoCount = videoCount;
            this.imageCount = imageCount;
            this.totalSize = totalSize;
        }
        
        // Getters
        public long getTotalFiles() { return totalFiles; }
        public long getDocumentCount() { return documentCount; }
        public long getVideoCount() { return videoCount; }
        public long getImageCount() { return imageCount; }
        public long getTotalSize() { return totalSize; }
        
        public String getFormattedTotalSize() {
            if (totalSize < 1024) {
                return totalSize + " B";
            } else if (totalSize < 1024 * 1024) {
                return String.format("%.1f KB", totalSize / 1024.0);
            } else if (totalSize < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
            } else {
                return String.format("%.1f GB", totalSize / (1024.0 * 1024.0 * 1024.0));
            }
        }
    }
}
