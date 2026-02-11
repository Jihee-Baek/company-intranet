package com.company.filehub.dto;

import com.company.filehub.entity.FileEntity;
import com.company.filehub.entity.FileRecipient;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FileResponse {
    private Long id;
    private String originalName;
    private Long fileSize;
    private String fileType;
    private String memo;
    private UploaderInfo uploader;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<RecipientInfo> recipients;
    private Boolean isDownloaded; // 현재 사용자 기준

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UploaderInfo {
        private Long id;
        private String employeeId;
        private String name;
        private String department;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RecipientInfo {
        private Long id;
        private String employeeId;
        private String name;
        private String department;
        private Boolean isDownloaded;
        private LocalDateTime firstDownloadedAt;
    }

    public static FileResponse from(FileEntity file, Long currentUserId) {
        List<RecipientInfo> recipientInfos = file.getRecipients().stream()
                .map(r -> RecipientInfo.builder()
                        .id(r.getRecipient().getId())
                        .employeeId(r.getRecipient().getEmployeeId())
                        .name(r.getRecipient().getName())
                        .department(r.getRecipient().getDepartmentName())
                        .isDownloaded(r.getIsDownloaded())
                        .firstDownloadedAt(r.getFirstDownloadedAt())
                        .build())
                .toList();

        Boolean downloaded = file.getRecipients().stream()
                .filter(r -> r.getRecipient().getId().equals(currentUserId))
                .findFirst()
                .map(FileRecipient::getIsDownloaded)
                .orElse(null);

        return FileResponse.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .fileSize(file.getFileSize())
                .fileType(file.getFileType())
                .memo(file.getMemo())
                .uploader(UploaderInfo.builder()
                        .id(file.getUploader().getId())
                        .employeeId(file.getUploader().getEmployeeId())
                        .name(file.getUploader().getName())
                        .department(file.getUploader().getDepartmentName())
                        .build())
                .createdAt(file.getCreatedAt())
                .expiresAt(file.getExpiresAt())
                .recipients(recipientInfos)
                .isDownloaded(downloaded)
                .build();
    }
}
