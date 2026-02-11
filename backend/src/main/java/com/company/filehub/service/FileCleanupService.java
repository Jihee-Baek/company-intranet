package com.company.filehub.service;

import com.company.filehub.entity.FileEntity;
import com.company.filehub.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileCleanupService {

    private final FileRepository fileRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredFiles() {
        List<FileEntity> expiredFiles = fileRepository.findByExpiresAtBefore(LocalDateTime.now());

        if (expiredFiles.isEmpty()) {
            log.info("만료된 파일 없음");
            return;
        }

        log.info("만료된 파일 {}개 삭제 시작", expiredFiles.size());

        for (FileEntity file : expiredFiles) {
            try {
                Path filePath = Paths.get(file.getFilePath());
                Files.deleteIfExists(filePath);
                fileRepository.delete(file);
                log.info("파일 삭제 완료: {} (id={})", file.getOriginalName(), file.getId());
            } catch (Exception e) {
                log.error("파일 삭제 실패: {} (id={})", file.getOriginalName(), file.getId(), e);
            }
        }

        log.info("만료된 파일 삭제 작업 완료");
    }
}
