# FileHub — 사내 파일 전송 인트라넷

사내 직원 간 안전하고 추적 가능한 파일 전달 시스템

## 프로젝트 구조

```
company-intranet/
├── backend/                          # Spring Boot 백엔드
│   ├── build.gradle
│   ├── src/main/java/com/company/filehub/
│   │   ├── FileHubApplication.java   # 메인 클래스
│   │   ├── config/
│   │   │   ├── SecurityConfig.java   # Spring Security + CORS
│   │   │   ├── FileStorageConfig.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── UserPrincipal.java
│   │   ├── entity/
│   │   │   ├── User.java
│   │   │   ├── FileEntity.java
│   │   │   ├── FileRecipient.java
│   │   │   └── DownloadLog.java
│   │   ├── repository/
│   │   │   ├── UserRepository.java
│   │   │   ├── FileRepository.java
│   │   │   ├── FileRecipientRepository.java
│   │   │   └── DownloadLogRepository.java
│   │   ├── dto/
│   │   │   ├── ApiResponse.java
│   │   │   ├── SignupRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── FileUploadRequest.java
│   │   │   ├── FileResponse.java
│   │   │   ├── DownloadLogResponse.java
│   │   │   └── UserSearchResponse.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── FileService.java
│   │   │   └── UserService.java
│   │   └── controller/
│   │       ├── AuthController.java
│   │       ├── FileController.java
│   │       └── UserController.java
│   └── src/main/resources/
│       ├── application.yml
│       └── db/schema.sql
├── frontend/                         # React 프론트엔드
│   ├── package.json
│   ├── public/index.html
│   └── src/
│       ├── index.js
│       ├── index.css
│       ├── App.js
│       ├── api/
│       │   ├── client.js             # Axios 인스턴스 + 인터셉터
│       │   ├── auth.js
│       │   ├── files.js
│       │   └── users.js
│       ├── context/
│       │   └── AuthContext.js
│       ├── components/
│       │   └── Layout.js
│       └── pages/
│           ├── LoginPage.js
│           ├── SignupPage.js
│           ├── UploadPage.js
│           ├── ReceivedPage.js
│           ├── SentPage.js
│           └── FileDetailPage.js
└── docs/
    └── QA_TEST_SCENARIOS.md
```

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | MySQL 8.0 |
| Frontend | React 18, React Router 6, Axios, react-dropzone |
| 인증 | JWT (jjwt 0.12) |
| 빌드 | Gradle (Backend), npm/CRA (Frontend) |

## 로컬 개발 환경 실행

### 1. 사전 요구사항
- Java 17+
- Node.js 18+
- MySQL 8.0

### 2. 데이터베이스 설정
```sql
-- MySQL에 접속 후 schema.sql 실행
source backend/src/main/resources/db/schema.sql;
```

### 3. 백엔드 실행
```bash
cd backend

# Gradle Wrapper 생성 (최초 1회)
gradle wrapper

# 실행
./gradlew bootRun
```
서버가 `http://localhost:8080`에서 실행됩니다.

### 4. 프론트엔드 실행
```bash
cd frontend
npm install
npm start
```
앱이 `http://localhost:3000`에서 실행됩니다.
(`package.json`의 `proxy` 설정으로 API 요청이 8080으로 프록시됩니다.)

## API 명세

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/auth/signup` | X | 회원가입 |
| POST | `/api/auth/login` | X | 로그인 (JWT 발급) |
| POST | `/api/files/upload` | O | 파일 업로드 (multipart) |
| GET | `/api/files/received` | O | 받은 파일 목록 |
| GET | `/api/files/sent` | O | 보낸 파일 목록 |
| GET | `/api/files/{id}` | O | 파일 상세 |
| GET | `/api/files/{id}/download` | O | 파일 다운로드 |
| GET | `/api/files/{id}/logs` | O | 다운로드 로그 |
| GET | `/api/files/unread-count` | O | 미확인 파일 수 |
| GET | `/api/users/search?keyword=` | O | 사용자 검색 |

## 향후 확장 로드맵

### Phase 2 — 고도화
- [ ] 다중 파일 동시 업로드
- [ ] 파일명/보낸사람 검색
- [ ] 정렬 옵션 (이름순, 크기순, 날짜순)
- [ ] 이미지/PDF 인라인 미리보기
- [ ] 파일 만료 정책 (30일 자동 삭제)

### Phase 3 — 엔터프라이즈
- [ ] 사내 SSO(LDAP/SAML) 연동
- [ ] 역할 기반 접근제어 (RBAC)
- [ ] 부서/팀 단위 파일 전송
- [ ] 감사 로그 대시보드
- [ ] 파일 저장소 S3/MinIO 전환

### Phase 4 — 운영 안정화
- [ ] 파일 바이러스 스캔 연동 (ClamAV)
- [ ] 업로드 대기열 (비동기 처리)
- [ ] 알림 시스템 (이메일/브라우저 Push)
- [ ] 모바일 앱 (React Native)
- [ ] 모니터링 (Prometheus + Grafana)
