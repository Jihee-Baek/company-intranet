package com.company.filehub.controller;

import com.company.filehub.dto.ApiResponse;
import com.company.filehub.dto.NoticeRequest;
import com.company.filehub.dto.NoticeResponse;
import com.company.filehub.security.UserPrincipal;
import com.company.filehub.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NoticeResponse>>> getNotices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok("공지사항 목록 조회 성공", noticeService.getNotices(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> getNotice(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("공지사항 조회 성공", noticeService.getNotice(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> createNotice(
            @Valid @RequestBody NoticeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        NoticeResponse response = noticeService.createNotice(request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("공지사항이 등록되었습니다", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> updateNotice(
            @PathVariable Long id,
            @Valid @RequestBody NoticeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("공지사항이 수정되었습니다", noticeService.updateNotice(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.ok("공지사항이 삭제되었습니다", null));
    }
}
