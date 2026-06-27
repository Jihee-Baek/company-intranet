package com.company.filehub.service;

import com.company.filehub.dto.DownloadLogResponse;
import com.company.filehub.dto.FileResponse;
import com.company.filehub.entity.*;
import com.company.filehub.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final FileRecipientRepository fileRecipientRepository;
    private final DownloadLogRepository downloadLogRepository;
    private final UserRepository userRepository;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Transactional
    public List<FileResponse> uploadFiles(List<MultipartFile> files, List<Long> recipientIds,
                                           String memo, Long uploaderId) throws IOException {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        List<FileResponse> responses = new ArrayList<>();

        for (MultipartFile multipartFile : files) {
            // 파일 저장
            String storedName = UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
            Path filePath = Paths.get(uploadDir).resolve(storedName);
            log.info("Saving file to disk: {} (original: {}, size: {})", filePath, multipartFile.getOriginalFilename(), multipartFile.getSize());
            Files.copy(multipartFile.getInputStream(), filePath);

            // 엔티티 생성
            FileEntity fileEntity = FileEntity.builder()
                    .originalName(multipartFile.getOriginalFilename())
                    .storedName(storedName)
                    .filePath(filePath.toString())
                    .fileSize(multipartFile.getSize())
                    .fileType(multipartFile.getContentType())
                    .memo(memo)
                    .uploader(uploader)
                    .build();

            fileRepository.save(fileEntity);
            log.info("Saved file metadata in database with ID: {}", fileEntity.getId());

            // 수신자 지정
            for (Long recipientId : recipientIds) {
                User recipient = userRepository.findById(recipientId)
                        .orElseThrow(() -> {
                            log.warn("Recipient not found: {}", recipientId);
                            return new IllegalArgumentException("수신자를 찾을 수 없습니다: " + recipientId);
                        });

                FileRecipient fr = FileRecipient.builder()
                        .file(fileEntity)
                        .recipient(recipient)
                        .build();
                fileRecipientRepository.save(fr);
                fileEntity.getRecipients().add(fr);
                log.info("Assigned recipient ID: {} to file ID: {}", recipientId, fileEntity.getId());
            }

            responses.add(FileResponse.from(fileEntity, uploaderId));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public Page<FileResponse> getReceivedFiles(Long userId, String keyword, Pageable pageable) {
        Page<FileEntity> page;
        if (keyword != null && !keyword.isBlank()) {
            page = fileRepository.searchReceivedFiles(userId, keyword, pageable);
        } else {
            page = fileRepository.findByRecipientId(userId, pageable);
        }
        return page.map(f -> FileResponse.from(f, userId));
    }

    @Transactional(readOnly = true)
    public Page<FileResponse> getSentFiles(Long userId, String keyword, Pageable pageable) {
        Page<FileEntity> page;
        if (keyword != null && !keyword.isBlank()) {
            page = fileRepository.searchSentFiles(userId, keyword, pageable);
        } else {
            page = fileRepository.findByUploaderId(userId, pageable);
        }
        return page.map(f -> FileResponse.from(f, userId));
    }

    @Transactional(readOnly = true)
    public FileResponse getFileDetail(Long fileId, Long userId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));

        // 접근 권한 확인: 업로더이거나 수신자여야 함
        boolean isUploader = file.getUploader().getId().equals(userId);
        boolean isRecipient = file.getRecipients().stream()
                .anyMatch(r -> r.getRecipient().getId().equals(userId));

        if (!isUploader && !isRecipient) {
            log.warn("Access denied for file ID: {} to user ID: {}", fileId, userId);
            throw new SecurityException("파일에 대한 접근 권한이 없습니다");
        }

        return FileResponse.from(file, userId);
    }

    @Transactional
    public Resource downloadFile(Long fileId, Long userId, String ipAddress) throws MalformedURLException {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));

        // 접근 권한 확인
        boolean isUploader = file.getUploader().getId().equals(userId);
        boolean isRecipient = file.getRecipients().stream()
                .anyMatch(r -> r.getRecipient().getId().equals(userId));

        if (!isUploader && !isRecipient) {
            log.warn("Download denied for file ID: {} to user ID: {}", fileId, userId);
            throw new SecurityException("파일에 대한 접근 권한이 없습니다");
        }

        // 수신자인 경우 다운로드 상태 업데이트
        fileRecipientRepository.findByFileIdAndRecipientId(fileId, userId)
                .ifPresent(fr -> {
                    fr.markAsDownloaded();
                    log.info("Recipient ID: {} marked file ID: {} as downloaded", userId, fileId);
                });

        // 다운로드 로그 기록
        User downloader = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        DownloadLog logEntity = DownloadLog.builder()
                .file(file)
                .downloader(downloader)
                .ipAddress(ipAddress)
                .build();
        downloadLogRepository.save(logEntity);
        log.info("Saved download log ID: {} for file ID: {} by user ID: {}", logEntity.getId(), fileId, userId);

        // 파일 리소스 반환
        Path filePath = Paths.get(file.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            log.error("Physical file not found at path: {}", file.getFilePath());
            throw new IllegalStateException("파일이 서버에 존재하지 않습니다");
        }

        return resource;
    }

    @Transactional(readOnly = true)
    public String getOriginalFileName(Long fileId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));
        return file.getOriginalName();
    }

    @Transactional(readOnly = true)
    public FileEntity getFileEntity(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));
    }

    @Transactional(readOnly = true)
    public Resource previewFile(Long fileId, Long userId) throws MalformedURLException {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));

        // 접근 권한 확인
        boolean isUploader = file.getUploader().getId().equals(userId);
        boolean isRecipient = file.getRecipients().stream()
                .anyMatch(r -> r.getRecipient().getId().equals(userId));

        if (!isUploader && !isRecipient) {
            log.warn("Preview denied for file ID: {} to user ID: {}", fileId, userId);
            throw new SecurityException("파일에 대한 접근 권한이 없습니다");
        }

        // 미리보기 지원 타입 확인
        String fileType = file.getFileType();
        if (fileType == null || (!fileType.startsWith("image/") && !fileType.equals("application/pdf"))) {
            log.warn("Preview failed: unsupported file type '{}' for file ID: {}", fileType, fileId);
            throw new IllegalArgumentException("미리보기를 지원하지 않는 파일 형식입니다");
        }

        Path filePath = Paths.get(file.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            log.error("Physical preview file not found at path: {}", file.getFilePath());
            throw new IllegalStateException("파일이 서버에 존재하지 않습니다");
        }

        return resource;
    }

    @Transactional(readOnly = true)
    public List<DownloadLogResponse> getDownloadLogs(Long fileId, Long userId) {
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다"));

        // 업로더만 로그 확인 가능
        boolean isUploader = file.getUploader().getId().equals(userId);
        boolean isRecipient = file.getRecipients().stream()
                .anyMatch(r -> r.getRecipient().getId().equals(userId));

        if (!isUploader && !isRecipient) {
            log.warn("Log access denied for file ID: {} to user ID: {}", fileId, userId);
            throw new SecurityException("다운로드 로그에 대한 접근 권한이 없습니다");
        }

        return downloadLogRepository.findByFileIdOrderByDownloadedAtDesc(fileId).stream()
                .map(DownloadLogResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return fileRecipientRepository.countByRecipientIdAndIsDownloadedFalse(userId);
    }
}
