package com.company.filehub.dto;

import com.company.filehub.entity.Notice;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class NoticeResponse {

    private Long id;
    private String title;
    private String content;
    private boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;

    public static NoticeResponse from(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .isPinned(notice.isPinned())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .authorName(notice.getAuthor().getName())
                .build();
    }
}
