package com.company.filehub.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_recipients",
       uniqueConstraints = @UniqueConstraint(columnNames = {"file_id", "recipient_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FileRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private FileEntity file;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(name = "is_downloaded", nullable = false)
    @Builder.Default
    private Boolean isDownloaded = false;

    @Column(name = "first_downloaded_at")
    private LocalDateTime firstDownloadedAt;

    public void markAsDownloaded() {
        if (!this.isDownloaded) {
            this.isDownloaded = true;
            this.firstDownloadedAt = LocalDateTime.now();
        }
    }
}
