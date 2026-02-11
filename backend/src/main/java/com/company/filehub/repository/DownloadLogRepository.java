package com.company.filehub.repository;

import com.company.filehub.entity.DownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {
    List<DownloadLog> findByFileIdOrderByDownloadedAtDesc(Long fileId);
}
