package com.example.mentalhealth.repository;

import com.example.mentalhealth.entity.FileRecord;
import com.example.mentalhealth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    
    // 根据存储名称查找文件
    Optional<FileRecord> findByStoredNameAndIsActiveTrue(String storedName);
    
    // 根据用户查找所有活跃文件
    List<FileRecord> findByUploadedByAndIsActiveTrueOrderByCreatedAtDesc(User uploadedBy);
    
    // 分页查询用户的文件
    Page<FileRecord> findByUploadedByAndIsActiveTrueOrderByCreatedAtDesc(User uploadedBy, Pageable pageable);
    
    // 根据文件分类查找文件
    List<FileRecord> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(FileRecord.FileCategory category);
    
    // 根据文件分类和用户查找文件
    List<FileRecord> findByUploadedByAndCategoryAndIsActiveTrueOrderByCreatedAtDesc(
        User uploadedBy, FileRecord.FileCategory category);
    
    // 根据文件类型查找文件
    List<FileRecord> findByFileTypeAndIsActiveTrueOrderByCreatedAtDesc(String fileType);
    
    // 根据原始文件名模糊查询
    @Query("SELECT f FROM FileRecord f WHERE f.originalName LIKE %:filename% AND f.isActive = true ORDER BY f.createdAt DESC")
    List<FileRecord> findByOriginalNameContaining(@Param("filename") String filename);
    
    // 根据用户和文件名模糊查询
    @Query("SELECT f FROM FileRecord f WHERE f.uploadedBy = :user AND f.originalName LIKE %:filename% AND f.isActive = true ORDER BY f.createdAt DESC")
    List<FileRecord> findByUploadedByAndOriginalNameContaining(@Param("user") User user, @Param("filename") String filename);
    
    // 查询指定时间范围内的文件
    @Query("SELECT f FROM FileRecord f WHERE f.createdAt BETWEEN :startDate AND :endDate AND f.isActive = true ORDER BY f.createdAt DESC")
    List<FileRecord> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // 统计用户的文件总数
    long countByUploadedByAndIsActiveTrue(User uploadedBy);
    
    // 统计用户某个分类的文件数量
    long countByUploadedByAndCategoryAndIsActiveTrue(User uploadedBy, FileRecord.FileCategory category);
    
    // 计算用户文件总大小
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileRecord f WHERE f.uploadedBy = :user AND f.isActive = true")
    Long sumFileSizeByUser(@Param("user") User user);
    
    // 查找所有活跃文件（管理员使用）
    Page<FileRecord> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // 根据ID和用户查找文件（确保用户只能访问自己的文件）
    Optional<FileRecord> findByIdAndUploadedByAndIsActiveTrue(Long id, User uploadedBy);
    
    // 软删除文件（标记为不活跃）
    @Query("UPDATE FileRecord f SET f.isActive = false, f.updatedAt = CURRENT_TIMESTAMP WHERE f.id = :id AND f.uploadedBy = :user")
    void softDeleteByIdAndUser(@Param("id") Long id, @Param("user") User user);
}
