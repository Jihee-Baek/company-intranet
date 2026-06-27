package com.company.filehub.controller;

import com.company.filehub.dto.ApiResponse;
import com.company.filehub.dto.NoticeRequest;
import com.company.filehub.dto.NoticeResponse;
import com.company.filehub.security.UserPrincipal;
import com.company.filehub.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request received: GET /api/notices (page: {}, size: {})", page, size);
        try {
            Page<NoticeResponse> response = noticeService.getNotices(page, size);
            log.info("Successfully fetched {} notices", response.getNumberOfElements());
            return ResponseEntity.ok(ApiResponse.ok("공지사항 목록 조회 성공", response));
        } catch (Exception e) {
            log.error("Failed to fetch notice list", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(@PathVariable Long id) {
        log.info("Request received: GET /api/notices/{}", id);
        try {
            NoticeResponse response = noticeService.getNotice(id);
            log.info("Successfully fetched notice ID: {}", id);
            return ResponseEntity.ok(ApiResponse.ok("공지사항 조회 성공", response));
        } catch (Exception e) {
            log.error("Failed to fetch notice ID: {}", id, e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @Valid @RequestBody NoticeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Request received: POST /api/notices (title: {}, authorId: {})", request.getTitle(), principal.getId());
        try {
            NoticeResponse response = noticeService.createNotice(request, principal.getId());
            log.info("Successfully created notice ID: {} by user ID: {}", response.getId(), principal.getId());
            return ResponseEntity.ok(ApiResponse.ok("공지사항이 등록되었습니다", response));
        } catch (Exception e) {
            log.error("Failed to create notice", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody NoticeRequest request) {
        log.info("Request received: PUT /api/notices/{} (title: {})", id, request.getTitle());
        try {
            NoticeResponse response = noticeService.updateNotice(id, request);
            log.info("Successfully updated notice ID: {}", id);
            return ResponseEntity.ok(ApiResponse.ok("공지사항이 수정되었습니다", response));
        } catch (Exception e) {
            log.error("Failed to update notice ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        log.info("Request received: DELETE /api/notices/{}", id);
        try {
            noticeService.deleteNotice(id);
            log.info("Successfully deleted notice ID: {}", id);
            return ResponseEntity.ok(ApiResponse.ok("공지사항이 삭제되었습니다", null));
        } catch (Exception e) {
            log.error("Failed to delete notice ID: {}", id, e);
            throw e;
        }
    }
}
