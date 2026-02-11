package com.company.filehub.controller;

import com.company.filehub.dto.*;
import com.company.filehub.entity.FileEntity;
import com.company.filehub.security.UserPrincipal;
import com.company.filehub.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

        List<FileResponse> responses = fileService.uploadFiles(files, recipientIds, memo, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("파일 업로드 완료", responses));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<Page<FileResponse>>> getReceivedFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal principal) {

        String field = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FileResponse> files = fileService.getReceivedFiles(principal.getId(), keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok(files));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<Page<FileResponse>>> getSentFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserPrincipal principal) {

        String field = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FileResponse> files = fileService.getSentFiles(principal.getId(), keyword, pageable);
        return ResponseEntity.ok(ApiResponse.ok(files));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FileResponse>> getFileDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        FileResponse response = fileService.getFileDetail(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request) throws Exception {

        String ipAddress = request.getRemoteAddr();
        Resource resource = fileService.downloadFile(id, principal.getId(), ipAddress);
        String originalName = fileService.getOriginalFileName(id);
        String encodedName = URLEncoder.encode(originalName, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewFile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {

        Resource resource = fileService.previewFile(id, principal.getId());
        FileEntity fileEntity = fileService.getFileEntity(id);
        MediaType mediaType = MediaType.parseMediaType(fileEntity.getFileType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponse<List<DownloadLogResponse>>> getDownloadLogs(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        List<DownloadLogResponse> logs = fileService.getDownloadLogs(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {

        long count = fileService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(count));
    }
}
