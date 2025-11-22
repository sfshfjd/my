package com.example.mentalhealth.controller;

import com.example.mentalhealth.dto.ApiResponse;
import com.example.mentalhealth.dto.QuestionnaireInfoResponse;
import com.example.mentalhealth.dto.QuestionnaireUploadResponse;
import com.example.mentalhealth.entity.Questionnaire;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.service.QuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
@Tag(name = "问卷管理", description = "问卷上传、下载、管理相关接口")
public class QuestionnaireController {
    
    private static final Logger logger = LoggerFactory.getLogger(QuestionnaireController.class);
    
    @Autowired
    private QuestionnaireService questionnaireService;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传问卷", description = "上传问卷文件，支持多种文档格式")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "上传成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "文件格式不支持或文件过大"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授权")
    })
    public ResponseEntity<ApiResponse<QuestionnaireUploadResponse>> uploadQuestionnaire(
            @Parameter(description = "要上传的问卷文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "问卷标题") @RequestParam(value = "title", required = false) String title,
            @Parameter(description = "问卷描述") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "问卷分类") @RequestParam(value = "category", required = false) String category,
            @Parameter(description = "问卷标签") @RequestParam(value = "tags", required = false) String tags,
            @AuthenticationPrincipal User user) {
        
        try {
            QuestionnaireUploadResponse response = questionnaireService.uploadQuestionnaire(
                    file, title, description, category, tags, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "问卷上传成功", response));
        } catch (IllegalArgumentException e) {
            logger.warn("问卷上传验证失败: {} (用户: {})", e.getMessage(), user.getUsername());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("问卷上传失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "问卷上传失败: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/download/{questionnaireId}")
    @Operation(summary = "下载问卷", description = "根据问卷ID下载问卷文件")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "下载成功", 
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "问卷不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授权")
    })
    public ResponseEntity<Resource> downloadQuestionnaire(
            @Parameter(description = "问卷ID") @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User user) {
        
        try {
            Resource resource = questionnaireService.downloadQuestionnaire(questionnaireId, user);
            QuestionnaireInfoResponse questionnaireInfo = questionnaireService.getQuestionnaireInfo(questionnaireId);
            
            // 编码文件名以支持中文
            String encodedFilename = URLEncoder.encode(questionnaireInfo.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + questionnaireInfo.getOriginalName() + 
                            "\"; filename*=UTF-8''" + encodedFilename)
                    .header(HttpHeaders.CONTENT_TYPE, questionnaireInfo.getFileType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(questionnaireInfo.getFileSize()))
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("问卷下载失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/my")
    @Operation(summary = "获取我的问卷列表", description = "获取当前用户创建的所有问卷")
    public ResponseEntity<ApiResponse<List<QuestionnaireInfoResponse>>> getMyQuestionnaires(
            @AuthenticationPrincipal User user) {
        
        try {
            List<QuestionnaireInfoResponse> questionnaires = questionnaireService.getUserQuestionnaires(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取问卷列表成功", questionnaires));
        } catch (Exception e) {
            logger.error("获取问卷列表失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取问卷列表失败", null));
        }
    }
    
    @GetMapping("/my/paged")
    @Operation(summary = "分页获取我的问卷", description = "分页获取当前用户创建的问卷")
    public ResponseEntity<ApiResponse<Page<QuestionnaireInfoResponse>>> getMyQuestionnairesPaged(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QuestionnaireInfoResponse> questionnaires = questionnaireService.getUserQuestionnaires(user, pageable);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取问卷列表成功", questionnaires));
        } catch (Exception e) {
            logger.error("分页获取问卷列表失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取问卷列表失败", null));
        }
    }
    
    @GetMapping("/published")
    @Operation(summary = "获取已发布的问卷", description = "获取所有已发布的问卷列表")
    public ResponseEntity<ApiResponse<List<QuestionnaireInfoResponse>>> getPublishedQuestionnaires() {
        
        try {
            List<QuestionnaireInfoResponse> questionnaires = questionnaireService.getPublishedQuestionnaires();
            return ResponseEntity.ok(new ApiResponse<>(true, "获取已发布问卷成功", questionnaires));
        } catch (Exception e) {
            logger.error("获取已发布问卷失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取已发布问卷失败", null));
        }
    }
    
    @GetMapping("/published/paged")
    @Operation(summary = "分页获取已发布的问卷", description = "分页获取所有已发布的问卷")
    public ResponseEntity<ApiResponse<Page<QuestionnaireInfoResponse>>> getPublishedQuestionnairesPaged(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<QuestionnaireInfoResponse> questionnaires = questionnaireService.getPublishedQuestionnaires(pageable);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取已发布问卷成功", questionnaires));
        } catch (Exception e) {
            logger.error("分页获取已发布问卷失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取已发布问卷失败", null));
        }
    }
    
    @GetMapping("/my/status/{status}")
    @Operation(summary = "根据状态获取我的问卷", description = "根据问卷状态获取当前用户的问卷")
    public ResponseEntity<ApiResponse<List<QuestionnaireInfoResponse>>> getMyQuestionnairesByStatus(
            @Parameter(description = "问卷状态") @PathVariable Questionnaire.QuestionnaireStatus status,
            @AuthenticationPrincipal User user) {
        
        try {
            List<QuestionnaireInfoResponse> questionnaires = questionnaireService
                    .getUserQuestionnairesByStatus(user, status);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取问卷列表成功", questionnaires));
        } catch (Exception e) {
            logger.error("根据状态获取问卷失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取问卷列表失败", null));
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索问卷", description = "根据关键词搜索问卷")
    public ResponseEntity<ApiResponse<List<QuestionnaireInfoResponse>>> searchQuestionnaires(
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        
        try {
            List<QuestionnaireInfoResponse> questionnaires = questionnaireService.searchQuestionnaires(keyword);
            return ResponseEntity.ok(new ApiResponse<>(true, "搜索问卷成功", questionnaires));
        } catch (Exception e) {
            logger.error("搜索问卷失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "搜索问卷失败", null));
        }
    }
    
    @GetMapping("/my/search")
    @Operation(summary = "搜索我的问卷", description = "在当前用户的问卷中搜索")
    public ResponseEntity<ApiResponse<List<QuestionnaireInfoResponse>>> searchMyQuestionnaires(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @AuthenticationPrincipal User user) {
        
        try {
            List<QuestionnaireInfoResponse> questionnaires = questionnaireService
                    .searchUserQuestionnaires(user, keyword);
            return ResponseEntity.ok(new ApiResponse<>(true, "搜索问卷成功", questionnaires));
        } catch (Exception e) {
            logger.error("搜索问卷失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "搜索问卷失败", null));
        }
    }
    
    @GetMapping("/{questionnaireId}")
    @Operation(summary = "获取问卷详情", description = "根据ID获取问卷详细信息")
    public ResponseEntity<ApiResponse<QuestionnaireInfoResponse>> getQuestionnaireInfo(
            @Parameter(description = "问卷ID") @PathVariable Long questionnaireId) {
        
        try {
            QuestionnaireInfoResponse questionnaire = questionnaireService.getQuestionnaireInfo(questionnaireId);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取问卷信息成功", questionnaire));
        } catch (Exception e) {
            logger.error("获取问卷信息失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{questionnaireId}")
    @Operation(summary = "更新问卷信息", description = "更新问卷的标题、描述等信息")
    public ResponseEntity<ApiResponse<QuestionnaireInfoResponse>> updateQuestionnaire(
            @Parameter(description = "问卷ID") @PathVariable Long questionnaireId,
            @Parameter(description = "问卷标题") @RequestParam(required = false) String title,
            @Parameter(description = "问卷描述") @RequestParam(required = false) String description,
            @Parameter(description = "问卷分类") @RequestParam(required = false) String category,
            @Parameter(description = "问卷标签") @RequestParam(required = false) String tags,
            @AuthenticationPrincipal User user) {
        
        try {
            QuestionnaireInfoResponse questionnaire = questionnaireService.updateQuestionnaire(
                    questionnaireId, title, description, category, tags, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "问卷信息更新成功", questionnaire));
        } catch (Exception e) {
            logger.error("更新问卷信息失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @PostMapping("/{questionnaireId}/publish")
    @Operation(summary = "发布问卷", description = "将问卷状态设置为已发布")
    public ResponseEntity<ApiResponse<Void>> publishQuestionnaire(
            @Parameter(description = "问卷ID") @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User user) {
        
        try {
            questionnaireService.publishQuestionnaire(questionnaireId, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "问卷发布成功", null));
        } catch (Exception e) {
            logger.error("发布问卷失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @PostMapping("/{questionnaireId}/archive")
    @Operation(summary = "归档问卷", description = "将问卷状态设置为已归档")
    public ResponseEntity<ApiResponse<Void>> archiveQuestionnaire(
            @Parameter(description = "问卷ID") @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User user) {
        
        try {
            questionnaireService.archiveQuestionnaire(questionnaireId, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "问卷归档成功", null));
        } catch (Exception e) {
            logger.error("归档问卷失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{questionnaireId}")
    @Operation(summary = "删除问卷", description = "删除指定的问卷（软删除）")
    public ResponseEntity<ApiResponse<Void>> deleteQuestionnaire(
            @Parameter(description = "问卷ID") @PathVariable Long questionnaireId,
            @AuthenticationPrincipal User user) {
        
        try {
            questionnaireService.deleteQuestionnaire(questionnaireId, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "问卷删除成功", null));
        } catch (Exception e) {
            logger.error("删除问卷失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取问卷统计", description = "获取当前用户的问卷统计信息")
    public ResponseEntity<ApiResponse<QuestionnaireService.QuestionnaireStatistics>> getQuestionnaireStatistics(
            @AuthenticationPrincipal User user) {
        
        try {
            QuestionnaireService.QuestionnaireStatistics statistics = 
                    questionnaireService.getUserQuestionnaireStatistics(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取统计信息成功", statistics));
        } catch (Exception e) {
            logger.error("获取问卷统计失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取统计信息失败", null));
        }
    }
    
    @GetMapping("/category/{category}")
    @Operation(summary = "根据分类获取问卷", description = "获取指定分类的所有问卷")
    public ResponseEntity<ApiResponse<List<QuestionnaireInfoResponse>>> getQuestionnairesByCategory(
            @Parameter(description = "问卷分类") @PathVariable String category) {
        
        try {
            List<QuestionnaireInfoResponse> questionnaires = questionnaireService
                    .getQuestionnairesByCategory(category);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取问卷列表成功", questionnaires));
        } catch (Exception e) {
            logger.error("根据分类获取问卷失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取问卷列表失败", null));
        }
    }
    
    @GetMapping("/popular")
    @Operation(summary = "获取热门问卷", description = "获取下载次数最多的问卷")
    public ResponseEntity<ApiResponse<List<QuestionnaireInfoResponse>>> getPopularQuestionnaires(
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<QuestionnaireInfoResponse> questionnaires = questionnaireService
                    .getPopularQuestionnaires(limit);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取热门问卷成功", questionnaires));
        } catch (Exception e) {
            logger.error("获取热门问卷失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取热门问卷失败", null));
        }
    }
}
