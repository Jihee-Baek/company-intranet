# Phase 2 고도화 변경 내역 (2026.02.11)

## 개요

Phase 2 로드맵 5가지 기능을 구현하였습니다.

1. 다중 파일 동시 업로드
2. 파일명/보낸사람 검색
3. 정렬 옵션 (이름순, 크기순, 날짜순)
4. 이미지/PDF 인라인 미리보기
5. 파일 만료 정책 (30일 자동 삭제)

---

## 1. 다중 파일 동시 업로드

### 변경 사항

- 기존 단일 파일 업로드(`file`)를 다중 파일 업로드(`files`)로 변경
- 선택된 파일 목록을 UI에 표시하고 개별 삭제 가능
- 같은 수신자/메모가 모든 파일에 공유 적용됨

### 변경 파일

| 파일 | 내용 |
|------|------|
| `backend/.../controller/FileController.java` | `@RequestParam("file") MultipartFile` → `@RequestParam("files") List<MultipartFile>`, 반환 타입 `List<FileResponse>` |
| `backend/.../service/FileService.java` | `uploadFile()` → `uploadFiles()`, 파일 목록 순회하며 각각 저장 |
| `frontend/src/api/files.js` | FormData에 `files` 키로 다중 파일 append |
| `frontend/src/pages/UploadPage.js` | `multiple: true`, state `files`(배열), 파일 목록 UI + 개별 삭제 버튼 |

### API 변경

```
POST /api/files/upload

- 요청: files(MultipartFile[]), recipientIds, memo
- 응답: List<FileResponse> (기존: 단일 FileResponse)
```

---

## 2. 파일명/보낸사람 검색

### 변경 사항

- 받은 파일함/보낸 파일함에 검색 input 추가
- 파일명(`originalName`) 또는 보낸사람 이름(`uploader.name`)으로 LIKE 검색
- 300ms 디바운스 적용으로 타이핑 중 불필요한 API 호출 방지

### 변경 파일

| 파일 | 내용 |
|------|------|
| `backend/.../repository/FileRepository.java` | `searchReceivedFiles()`, `searchSentFiles()` JPQL 쿼리 추가 |
| `backend/.../service/FileService.java` | `getReceivedFiles()`, `getSentFiles()`에 `keyword` 파라미터 추가, keyword 유무에 따라 분기 |
| `backend/.../controller/FileController.java` | `/received`, `/sent` 엔드포인트에 `@RequestParam(required=false) String keyword` 추가 |
| `frontend/src/api/files.js` | `getReceivedFiles()`, `getSentFiles()`에 `keyword` 파라미터 전달 |
| `frontend/src/pages/ReceivedPage.js` | 검색 input + 디바운스(300ms) |
| `frontend/src/pages/SentPage.js` | 검색 input + 디바운스(300ms) |

### API 변경

```
GET /api/files/received?keyword=검색어
GET /api/files/sent?keyword=검색어
```

---

## 3. 정렬 옵션

### 변경 사항

- 날짜순(기본), 이름순, 크기순 정렬 드롭다운 추가
- 오름차순/내림차순 토글 버튼 추가
- 허용 필드 화이트리스트: `createdAt`, `originalName`, `fileSize`
- 기존 JPQL의 고정 `ORDER BY` 제거 → Spring Data Pageable Sort 활용

### 변경 파일

| 파일 | 내용 |
|------|------|
| `backend/.../repository/FileRepository.java` | 기존 쿼리에서 `ORDER BY f.createdAt DESC` 제거 |
| `backend/.../controller/FileController.java` | `sortBy`, `sortDir` 파라미터 수신, `ALLOWED_SORT_FIELDS` 화이트리스트 검증 후 `PageRequest.of(page, size, sort)` 생성 |
| `frontend/src/api/files.js` | `sortBy`, `sortDir` 파라미터 전달 |
| `frontend/src/pages/ReceivedPage.js` | 정렬 드롭다운 + 오름차순/내림차순 토글 |
| `frontend/src/pages/SentPage.js` | 정렬 드롭다운 + 오름차순/내림차순 토글 |

### API 변경

```
GET /api/files/received?sortBy=createdAt&sortDir=desc
GET /api/files/sent?sortBy=originalName&sortDir=asc
```

---

## 4. 이미지/PDF 인라인 미리보기

### 변경 사항

- `GET /api/files/{id}/preview` 엔드포인트 신규 추가
- 이미지(`image/*`) → `<img>` 태그, PDF(`application/pdf`) → `<iframe>`으로 인라인 표시
- 비지원 파일 형식은 400 Bad Request 반환
- 다운로드 로그를 기록하지 않음 (미리보기는 조회 목적)
- 프론트엔드에서 Authorization 헤더가 필요하므로 blob URL 변환 방식 사용

### 변경 파일

| 파일 | 내용 |
|------|------|
| `backend/.../controller/FileController.java` | `@GetMapping("/{id}/preview")` 추가, `Content-Disposition: inline` |
| `backend/.../service/FileService.java` | `previewFile()` 메서드 추가 (권한 확인 + 타입 검증), `getFileEntity()` 메서드 추가 |
| `frontend/src/api/files.js` | `previewFile(id)` 추가 (`responseType: 'blob'`) |
| `frontend/src/pages/FileDetailPage.js` | 미리보기 영역 추가, blob URL 생성/해제, `isPreviewable()` 판별 함수 |

### API 변경

```
GET /api/files/{id}/preview

- 지원 타입: image/*, application/pdf
- 응답: 파일 바이너리 (Content-Disposition: inline)
- 비지원 타입: 400 Bad Request
```

---

## 5. 파일 만료 정책 (30일 자동 삭제)

### 변경 사항

- 파일 업로드 시 `expiresAt = createdAt + 30일` 자동 설정
- 매일 새벽 2시 스케줄러가 만료 파일을 디스크 + DB에서 삭제
- 파일 상세 페이지에서 만료일 표시 (빨간색)
- `application.yml`에 보존 기간 설정 추가

### DB 스키마 변경

```sql
-- files 테이블에 컬럼 추가
expires_at DATETIME COMMENT '만료 시간'
```

### 기존 DB 마이그레이션

```sql
ALTER TABLE files ADD COLUMN expires_at DATETIME COMMENT '만료 시간';
UPDATE files SET expires_at = DATE_ADD(created_at, INTERVAL 30 DAY);
```

> `ddl-auto: validate` 설정이므로 위 SQL을 직접 실행해야 합니다.

### 변경 파일

| 파일 | 내용 |
|------|------|
| `backend/.../resources/db/schema.sql` | `files` 테이블에 `expires_at` 컬럼 추가 |
| `backend/.../entity/FileEntity.java` | `expiresAt` 필드 추가, `@PrePersist`에서 30일 후 자동 설정 |
| `backend/.../dto/FileResponse.java` | `expiresAt` 필드 추가 + `from()` 매퍼 반영 |
| `backend/.../repository/FileRepository.java` | `findByExpiresAtBefore()` 메서드 추가 |
| `backend/.../FileHubApplication.java` | `@EnableScheduling` 추가 |
| `backend/.../resources/application.yml` | `app.file.retention-days: 30` 추가 |
| **신규** `backend/.../service/FileCleanupService.java` | `@Scheduled(cron = "0 0 2 * * *")` 만료 파일 자동 삭제 |
| `frontend/src/pages/FileDetailPage.js` | 만료일 표시 (`YYYY.MM.DD 만료`, 빨간색) |

---

## 전체 수정 파일 목록

| 구분 | 파일 | 작업 |
|------|------|------|
| DB | `schema.sql` | files에 expires_at 추가 + 마이그레이션 SQL |
| 백엔드 수정 | `entity/FileEntity.java` | expiresAt 필드, @PrePersist 30일 |
| 백엔드 수정 | `controller/FileController.java` | 다중 업로드, 검색, 정렬, 미리보기 |
| 백엔드 수정 | `service/FileService.java` | 다중 업로드, 검색, 미리보기 |
| 백엔드 수정 | `repository/FileRepository.java` | 검색 쿼리, 정렬(ORDER BY 제거), 만료 조회 |
| 백엔드 수정 | `dto/FileResponse.java` | expiresAt 필드 추가 |
| 백엔드 수정 | `FileHubApplication.java` | @EnableScheduling |
| 백엔드 수정 | `application.yml` | retention-days 설정 |
| 백엔드 신규 | `service/FileCleanupService.java` | 만료 파일 자동 삭제 스케줄러 |
| 프론트 수정 | `api/files.js` | 다중 업로드, 검색, 정렬, 미리보기 파라미터 |
| 프론트 수정 | `pages/UploadPage.js` | 다중 파일 선택 UI |
| 프론트 수정 | `pages/ReceivedPage.js` | 검색바, 정렬 드롭다운 |
| 프론트 수정 | `pages/SentPage.js` | 검색바, 정렬 드롭다운 |
| 프론트 수정 | `pages/FileDetailPage.js` | 미리보기, 만료일 표시 |

---

## 검증 체크리스트

- [ ] 다중 파일 업로드: 2~3개 파일 동시 전송 → 받은 파일함에 모두 표시
- [ ] 검색: 파일명/보낸사람으로 검색 → 결과 필터링
- [ ] 정렬: 이름순/크기순/날짜순 + 오름차순/내림차순 전환
- [ ] 미리보기: 이미지 인라인 표시, PDF iframe 표시, 비지원 타입 미리보기 없음
- [ ] 만료: 새 파일 업로드 시 expiresAt = createdAt+30d 설정, 상세 페이지 만료일 표시
- [ ] 마이그레이션 SQL 실행 후 기존 데이터 정상 동작
