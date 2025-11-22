package com.example.mentalhealth.repository;

import com.example.mentalhealth.entity.Questionnaire;
import com.example.mentalhealth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionnaireRepository extends JpaRepository<Questionnaire, Long> {
    
    // 根据ID和用户查找问卷
    Optional<Questionnaire> findByIdAndCreatedByAndIsActiveTrue(Long id, User createdBy);
    
    // 根据ID查找活跃问卷
    Optional<Questionnaire> findByIdAndIsActiveTrue(Long id);
    
    // 查找用户的所有活跃问卷
    List<Questionnaire> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(User createdBy);
    
    // 分页查询用户的问卷
    Page<Questionnaire> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(User createdBy, Pageable pageable);
    
    // 根据状态查找用户问卷
    List<Questionnaire> findByCreatedByAndStatusAndIsActiveTrueOrderByCreatedAtDesc(
        User createdBy, Questionnaire.QuestionnaireStatus status);
    
    // 根据分类查找问卷
    List<Questionnaire> findByCategoryAndIsActiveTrueOrderByCreatedAtDesc(String category);
    
    // 根据标题模糊查询
    @Query("SELECT q FROM Questionnaire q WHERE q.title LIKE %:title% AND q.isActive = true ORDER BY q.createdAt DESC")
    List<Questionnaire> findByTitleContaining(@Param("title") String title);
    
    // 根据用户和标题模糊查询
    @Query("SELECT q FROM Questionnaire q WHERE q.createdBy = :user AND q.title LIKE %:title% AND q.isActive = true ORDER BY q.createdAt DESC")
    List<Questionnaire> findByCreatedByAndTitleContaining(@Param("user") User user, @Param("title") String title);
    
    // 查找所有已发布的问卷
    List<Questionnaire> findByStatusAndIsActiveTrueOrderByPublishedAtDesc(Questionnaire.QuestionnaireStatus status);
    
    // 分页查询所有已发布的问卷
    Page<Questionnaire> findByStatusAndIsActiveTrueOrderByPublishedAtDesc(
        Questionnaire.QuestionnaireStatus status, Pageable pageable);
    
    // 统计用户的问卷总数
    long countByCreatedByAndIsActiveTrue(User createdBy);
    
    // 统计用户某个状态的问卷数量
    long countByCreatedByAndStatusAndIsActiveTrue(User createdBy, Questionnaire.QuestionnaireStatus status);
    
    // 查找热门问卷（按下载次数排序）
    @Query("SELECT q FROM Questionnaire q WHERE q.status = 'PUBLISHED' AND q.isActive = true ORDER BY q.downloadCount DESC")
    List<Questionnaire> findPopularQuestionnaires(Pageable pageable);
    
    // 查找最新发布的问卷
    @Query("SELECT q FROM Questionnaire q WHERE q.status = 'PUBLISHED' AND q.isActive = true ORDER BY q.publishedAt DESC")
    List<Questionnaire> findLatestPublishedQuestionnaires(Pageable pageable);
    
    // 根据标签搜索
    @Query("SELECT q FROM Questionnaire q WHERE q.tags LIKE %:tag% AND q.status = 'PUBLISHED' AND q.isActive = true ORDER BY q.createdAt DESC")
    List<Questionnaire> findByTagsContaining(@Param("tag") String tag);
    
    // 增加下载次数
    @Modifying
    @Query("UPDATE Questionnaire q SET q.downloadCount = q.downloadCount + 1, q.updatedAt = CURRENT_TIMESTAMP WHERE q.id = :id")
    void incrementDownloadCount(@Param("id") Long id);
    
    // 查找用户某个分类的问卷
    List<Questionnaire> findByCreatedByAndCategoryAndIsActiveTrueOrderByCreatedAtDesc(User createdBy, String category);
    
    // 获取所有分类
    @Query("SELECT DISTINCT q.category FROM Questionnaire q WHERE q.category IS NOT NULL AND q.isActive = true")
    List<String> findAllCategories();
}
