package com.company.filehub.service;

import com.company.filehub.dto.NoticeRequest;
import com.company.filehub.dto.NoticeResponse;
import com.company.filehub.entity.Notice;
import com.company.filehub.entity.User;
import com.company.filehub.repository.NoticeRepository;
import com.company.filehub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<NoticeResponse> getNotices(int page, int size) {
        return noticeRepository
                .findAllByOrderByIsPinnedDescCreatedAtDesc(PageRequest.of(page, size))
                .map(NoticeResponse::from);
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다"));
        return NoticeResponse.from(notice);
    }

    @Transactional
    public NoticeResponse createNotice(NoticeRequest request, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author)
                .isPinned(request.isPinned())
                .build();

        return NoticeResponse.from(noticeRepository.save(notice));
    }

    @Transactional
    public NoticeResponse updateNotice(Long id, NoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다"));

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPinned(request.isPinned());

        return NoticeResponse.from(noticeRepository.save(notice));
    }

    @Transactional
    public void deleteNotice(Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new IllegalArgumentException("존재하지 않는 공지사항입니다");
        }
        noticeRepository.deleteById(id);
    }
}
