package com.company.filehub.dto;

import com.company.filehub.entity.DownloadLog;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class DownloadLogResponse {
    private Long id;
    private String downloaderName;
    private String downloaderEmployeeId;
    private String department;
    private LocalDateTime downloadedAt;
    private String ipAddress;

    public static DownloadLogResponse from(DownloadLog log) {
        return DownloadLogResponse.builder()
                .id(log.getId())
                .downloaderName(log.getDownloader().getName())
                .downloaderEmployeeId(log.getDownloader().getEmployeeId())
                .department(log.getDownloader().getDepartmentName())
                .downloadedAt(log.getDownloadedAt())
                .ipAddress(log.getIpAddress())
                .build();
    }
}
