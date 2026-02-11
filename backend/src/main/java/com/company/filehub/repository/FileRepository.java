package com.company.filehub.repository;

import com.company.filehub.entity.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    @Query("SELECT f FROM FileEntity f WHERE f.uploader.id = :uploaderId")
    Page<FileEntity> findByUploaderId(@Param("uploaderId") Long uploaderId, Pageable pageable);

    @Query("SELECT DISTINCT f FROM FileEntity f JOIN f.recipients r WHERE r.recipient.id = :recipientId")
    Page<FileEntity> findByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("SELECT DISTINCT f FROM FileEntity f JOIN f.recipients r " +
           "WHERE r.recipient.id = :recipientId " +
           "AND (f.originalName LIKE %:keyword% OR f.uploader.name LIKE %:keyword%)")
    Page<FileEntity> searchReceivedFiles(@Param("recipientId") Long recipientId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);

    @Query("SELECT f FROM FileEntity f " +
           "WHERE f.uploader.id = :uploaderId " +
           "AND (f.originalName LIKE %:keyword% OR f.uploader.name LIKE %:keyword%)")
    Page<FileEntity> searchSentFiles(@Param("uploaderId") Long uploaderId,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    List<FileEntity> findByExpiresAtBefore(LocalDateTime now);
}
