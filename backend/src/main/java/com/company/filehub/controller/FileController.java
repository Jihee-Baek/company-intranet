package com.company.filehub.controller;

import com.company.filehub.dto.*;
import com.company.filehub.entity.FileEntity;
import com.company.filehub.security.UserPrincipal;
import com.company.filehub.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "originalName", "fileSize");

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<List<FileResponse>>> uploadFile(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("recipientIds") List<Long> recipientIds,
            @RequestParam(value = "memo", required = false) String memo,
            @AuthenticationPrincipal UserPrincipal principal) throws IOException {

        log.info("Request received: POST /api/files/upload (fileCount: {}, recipientIds: {}, uploaderId: {})",
                files.size(), recipientIds, principal.getId());
        try {
            List<FileResponse> responses = fileService.uploadFiles(files, recipientIds, memo, principal.getId());
            log.info("Successfully uploaded {} files by user ID: {}", responses.size(), principal.getId());
            return ResponseEntity.ok(ApiResponse.ok("파일 업로드 완료", responses));
        } catch (Exception e) {
            log.error("Failed to upload files for uploader ID: {}", principal.getId(), e);
            throw e;
        }
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<FileResponse>>> getReceivedFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("Request received: GET /api/files/received (userId: {}, page: {}, size: {}, keyword: {})",
                principal.getId(), page, size, keyword);
        try {
            String field = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<FileResponse> files = fileService.getReceivedFiles(principal.getId(), keyword, pageable);
            log.info("Successfully fetched {} received files for user ID: {}", files.getNumberOfElements(), principal.getId());
            return ResponseEntity.ok(ApiResponse.ok(files));
        } catch (Exception e) {
            log.error("Failed to get received files for user ID: {}", principal.getId(), e);
            throw e;
        }
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<FileResponse>>> getSentFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("Request received: GET /api/files/sent (userId: {}, page: {}, size: {}, keyword: {})",
                principal.getId(), page, size, keyword);
        try {
            String field = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
            Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<FileResponse> files = fileService.getSentFiles(principal.getId(), keyword, pageable);
            log.info("Successfully fetched {} sent files for user ID: {}", files.getNumberOfElements(), principal.getId());
            return ResponseEntity.ok(ApiResponse.ok(files));
        } catch (Exception e) {
            log.error("Failed to get sent files for user ID: {}", principal.getId(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> getFileDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("Request received: GET /api/files/{} (userId: {})", id, principal.getId());
        try {
            FileResponse response = fileService.getFileDetail(id, principal.getId());
            log.info("Successfully fetched file detail for file ID: {} and user ID: {}", id, principal.getId());
            return ResponseEntity.ok(ApiResponse.ok(response));
        } catch (Exception e) {
            log.error("Failed to get file detail for file ID: {} and user ID: {}", id, principal.getId(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request) throws Exception {

        String ipAddress = request.getRemoteAddr();
        log.info("Request received: GET /api/files/{}/download (userId: {}, ip: {})", id, principal.getId(), ipAddress);
        try {
            Resource resource = fileService.downloadFile(id, principal.getId(), ipAddress);
            String originalName = fileService.getOriginalFileName(id);
            String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");
            log.info("Successfully prepared download for file ID: {}, originalName: {}", id, originalName);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedName)
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to download file ID: {} for user ID: {}", id, principal.getId(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {

        log.info("Request received: GET /api/files/{}/preview (userId: {})", id, principal.getId());
        try {
            Resource resource = fileService.previewFile(id, principal.getId());
            FileEntity fileEntity = fileService.getFileEntity(id);
            MediaType mediaType = MediaType.parseMediaType(fileEntity.getFileType());
            log.info("Successfully prepared preview for file ID: {}, type: {}", id, fileEntity.getFileType());

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to preview file ID: {} for user ID: {}", id, principal.getId(), e);
            throw e;
        }
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponse<List<DownloadLogResponse>>> getDownloadLogs(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("Request received: GET /api/files/{}/logs (userId: {})", id, principal.getId());
        try {
            List<DownloadLogResponse> logs = fileService.getDownloadLogs(id, principal.getId());
            log.info("Successfully fetched download logs (count: {}) for file ID: {}", logs.size(), id);
            return ResponseEntity.ok(ApiResponse.ok(logs));
        } catch (Exception e) {
            log.error("Failed to get download logs for file ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("Request received: GET /api/files/unread-count (userId: {})", principal.getId());
        try {
            long count = fileService.getUnreadCount(principal.getId());
            log.info("Successfully fetched unread count: {} for user ID: {}", count, principal.getId());
            return ResponseEntity.ok(ApiResponse.ok(count));
        } catch (Exception e) {
            log.error("Failed to get unread count for user ID: {}", principal.getId(), e);
            throw e;
        }
    }
}
