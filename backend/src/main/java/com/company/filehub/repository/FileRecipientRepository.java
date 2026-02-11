package com.company.filehub.repository;

import com.company.filehub.entity.FileRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileRecipientRepository extends JpaRepository<FileRecipient, Long> {
    List<FileRecipient> findByFileId(Long fileId);
    Optional<FileRecipient> findByFileIdAndRecipientId(Long fileId, Long recipientId);
    long countByRecipientIdAndIsDownloadedFalse(Long recipientId);
}
