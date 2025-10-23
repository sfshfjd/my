package com.example.mentalhealth.controller;

import com.example.mentalhealth.dto.ApiResponse;
import com.example.mentalhealth.dto.FileInfoResponse;
import com.example.mentalhealth.dto.FileUploadResponse;
import com.example.mentalhealth.entity.FileRecord;
import com.example.mentalhealth.entity.User;
import com.example.mentalhealth.service.FileService;
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
@RequestMapping("/api/files")
@Tag(name = "文件管理", description = "文件上传、下载、管理相关接口")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    @Autowired
    private FileService fileService;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传文件", description = "支持上传文档(PDF, DOC, DOCX)和视频文件")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "上传成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "文件格式不支持或文件过大"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授权")
    })
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "要上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件描述（可选）") @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal User user) {
        
        try {
            FileUploadResponse response = fileService.uploadFile(file, description, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "文件上传成功", response));
        } catch (IllegalArgumentException e) {
            logger.warn("文件上传验证失败: {} (用户: {})", e.getMessage(), user.getUsername());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("文件上传失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "文件上传失败: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/download/{fileId}")
    @Operation(summary = "下载文件", description = "根据文件ID下载文件")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "下载成功", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "文件不存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "未授权")
    })
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User user) {
        
        try {
            Resource resource = fileService.downloadFile(fileId, user);
            FileInfoResponse fileInfo = fileService.getFileInfo(fileId, user);
            
            // 编码文件名以支持中文
            String encodedFilename = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileInfo.getOriginalName() + "\"; filename*=UTF-8''" + encodedFilename)
                    .header(HttpHeaders.CONTENT_TYPE, fileInfo.getFileType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.getFileSize()))
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("文件下载失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/list")
    @Operation(summary = "获取用户文件列表", description = "获取当前用户的所有文件列表")
    public ResponseEntity<ApiResponse<List<FileInfoResponse>>> getUserFiles(
            @AuthenticationPrincipal User user) {
        
        try {
            List<FileInfoResponse> files = fileService.getUserFiles(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取文件列表成功", files));
        } catch (Exception e) {
            logger.error("获取文件列表失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取文件列表失败", null));
        }
    }
    
    @GetMapping("/list/paged")
    @Operation(summary = "分页获取用户文件列表", description = "分页获取当前用户的文件列表")
    public ResponseEntity<ApiResponse<Page<FileInfoResponse>>> getUserFilesPaged(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<FileInfoResponse> files = fileService.getUserFiles(user, pageable);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取文件列表成功", files));
        } catch (Exception e) {
            logger.error("分页获取文件列表失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取文件列表失败", null));
        }
    }
    
    @GetMapping("/list/category/{category}")
    @Operation(summary = "根据分类获取文件", description = "根据文件分类获取用户文件列表")
    public ResponseEntity<ApiResponse<List<FileInfoResponse>>> getUserFilesByCategory(
            @Parameter(description = "文件分类") @PathVariable FileRecord.FileCategory category,
            @AuthenticationPrincipal User user) {
        
        try {
            List<FileInfoResponse> files = fileService.getUserFilesByCategory(user, category);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取" + category + "文件列表成功", files));
        } catch (Exception e) {
            logger.error("根据分类获取文件列表失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取文件列表失败", null));
        }
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索文件", description = "根据文件名搜索用户文件")
    public ResponseEntity<ApiResponse<List<FileInfoResponse>>> searchFiles(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @AuthenticationPrincipal User user) {
        
        try {
            List<FileInfoResponse> files = fileService.searchUserFiles(user, keyword);
            return ResponseEntity.ok(new ApiResponse<>(true, "搜索文件成功", files));
        } catch (Exception e) {
            logger.error("搜索文件失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "搜索文件失败", null));
        }
    }
    
    @GetMapping("/info/{fileId}")
    @Operation(summary = "获取文件详情", description = "根据文件ID获取文件详细信息")
    public ResponseEntity<ApiResponse<FileInfoResponse>> getFileInfo(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User user) {
        
        try {
            FileInfoResponse fileInfo = fileService.getFileInfo(fileId, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取文件信息成功", fileInfo));
        } catch (Exception e) {
            logger.error("获取文件信息失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{fileId}")
    @Operation(summary = "删除文件", description = "删除指定的文件（软删除）")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "文件ID") @PathVariable Long fileId,
            @AuthenticationPrincipal User user) {
        
        try {
            fileService.deleteFile(fileId, user);
            return ResponseEntity.ok(new ApiResponse<>(true, "文件删除成功", null));
        } catch (Exception e) {
            logger.error("删除文件失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取文件统计", description = "获取当前用户的文件统计信息")
    public ResponseEntity<ApiResponse<FileService.FileStatistics>> getFileStatistics(
            @AuthenticationPrincipal User user) {
        
        try {
            FileService.FileStatistics statistics = fileService.getUserFileStatistics(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取统计信息成功", statistics));
        } catch (Exception e) {
            logger.error("获取文件统计失败: {} (用户: {})", e.getMessage(), user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取统计信息失败", null));
        }
    }
}
