package com.company.filehub.repository;

import com.company.filehub.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findAllByOrderByIsPinnedDescCreatedAtDesc(Pageable pageable);
}
