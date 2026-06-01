## Introduction

2026년 1학기 웹/코드보안 [팀-박시현] 기말 프로젝트입니다.

### Features

- 사용자 기능 
    - 회원가입 및 로그인 
    - 관리자 계정 초기화
    - 관리자/일반 사용자 메뉴 분리
    - 관리자 권한에서의 사용자 역할 관리
- 문서 기능 
    - 문서 업로드 및 AES 기반 암호화 저장
    - 문서 목록 조회 및 문서 열람
    - 역할 기반 문서 열람 권한 관리
    - 업로더 개인키 기반 전자서명 생성
    - 업로더 공개키 기반 전자서명 검증
    - 문서 비밀키 기반 문서/전자서명 암복호화
    - 역할 공개키 기반 전자봉투 생성
    - 역할 개인키 기반 전자봉투 개봉
- 문서 로그 기능  
    - 문서 열람 성공/실패 로그 기록
    - 로그 해시 체이닝 및 전자서명 기반 무결성 검증
- MySQL 기반 사용자, 역할, 문서, 권한, 로그 정보 저장
- 프로그램 시작 시 DB 테이블, 역할 키, 사용자 키/문서 저장 디렉터리 초기화

<br>

## Commit convention

e.g. `타입/#이슈번호: 커밋 메시지 작성`

| Type     | Description                      |
|----------|----------------------------------|
| Feat     | 새로운 기능 개발                 |
| Fix      | 오류, 동작하지 않는 코드 등 수정 |
| Refactor | 구조 개선, 코드 개선 등 리팩터링 |
| Chore    | 기타 부수적인 코드 수정          |

<br>

## Collaboration workflow

- 작업 내용을 제목으로 이슈 생성, 이슈 타입 지정
- 브랜치 생성 - e.g. `소문자타입/작업-내용을-작성`
- 브랜치에 push 후, 작업 내용을 제목으로 PR 생성
- 셀프/상호 코드리뷰 후 `양식-코드리뷰보고서` 작성해서 공유 (이후 해당내용 리팩터링) 

<br>

## 프로토타입 실행하기 
JDK 21, MySQL 설치 필요 

#### 로컬 MySQL에서 데이터베이스 및 사용자 생성 
```sql
mysql -u root -p 

CREATE DATABASE docsystem_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'docsystem_user'@'localhost' IDENTIFIED BY '1234';

GRANT ALL PRIVILEGES ON docsystem_db.* TO 'docsystem_user'@'localhost';

FLUSH PRIVILEGES;
```

#### 프로젝트 다운로드 
```bash
git clone https://github.com/mminjukim/WebCodeSecurty-FinalProject.git

cd WebCodeSecurty-FinalProject
```

#### 소스코드 컴파일 및 실행 
- **Windows**
    ```bash
    dir /s /B src\*.java > sources.txt

    javac -d bin -cp "lib\*" @sources.txt

    java -cp "bin;lib\*" main.java.DocSystem
    ```
- **Mac OS**
    ```bash
    javac -d bin -cp "lib/*" $(find src -name "*.java")

    java -cp "bin:lib/*" main.java.DocSystem
    ```

<br>

## Project structure

```text
  src/
  `-- main/
      `-- java/
          |-- DocSystem.java                         # 프로그램 진입점, 메뉴 제어, 로그인/관리자/일반 사용자 흐름 처리
          |
          |-- document/                              # 문서 업로드, 열람, 암복호화, 전자봉투 도메인
          |   |-- controller/
          |   |   `-- DocController.java             # 문서 업로드/열람 콘솔 입력 처리
          |   |-- dao/
          |   |   |-- DocumentDao.java               # 문서 저장, 문서 목록 조회, 문서 단건 조회
          |   |   `-- WhitelistDao.java              # 문서별 열람 허용 역할 저장 및 권한 확인
          |   |-- dto/
          |   |   |-- DocumentDto.java               # 문서 상세 정보 전달 객체
          |   |   `-- DocumentSummaryDto.java        # 문서 목록 표시용 요약 객체
          |   `-- service/
          |       |-- DocService.java                # 문서 업로드, 권한 검증, 복호화, 서명 검증 흐름 제어
          |       |-- DocEncryptService.java         # AES 기반 문서 및 전자서명 암호화
          |       |-- DocDecryptService.java         # AES 기반 문서 및 전자서명 복호화
          |       |-- DocSignatureService.java       # SHA256withRSA 전자서명 생성 및 검증
          |       `-- EnvelopeService.java           # RSA 기반 문서 비밀키 wrapping/unwrapping
          |
          |-- infrastructure/                        # 애플리케이션 설정, DB, 키, 생명주기 관리
          |   |-- database/
          |   |   |-- DBManager.java                 # MySQL 연결 생성
          |   |   |-- DataInitializer.java           # roles, users, documents, whitelists, read_logs 테이블 생성
          |   |   |-- QueryExecutor.java             # PreparedStatement 기반 SELECT/INSERT 공통 실행
          |   |   `-- SqlQueryBuilder.java           # SELECT, INSERT, UPDATE SQL 빌더
          |   |-- key/
          |   |   |-- KeyFileService.java            # 키 디렉터리 생성, 키 파일 저장 및 로드
          |   |   |-- KeyInitializer.java            # 역할/사용자 RSA 키쌍 생성
          |   |   `-- MasterKeyManager.java          # AES 마스터키 생성, 저장, 로드
          |   `-- lifecycle/
          |       |-- AppConfig.java                 # DAO, Service, Controller 의존성 생성 및 연결
          |       `-- ServerLifeCycle.java           # 프로그램 시작 초기화 및 종료 흐름 처리
          |
          |-- log/                                   # 문서 열람 로그 도메인
          |   |-- controller/
          |   |   `-- ReadLogController.java         # 문서 로그 조회 콘솔 입력 처리
          |   |-- dao/
          |   |   `-- ReadLogDao.java                # 열람 로그 저장 및 조회
          |   |-- dto/
          |   |   `-- ReadLogDto.java                # 열람 로그 정보 전달 객체
          |   `-- service/
          |       `-- ReadLogService.java            # 열람 로그 기록, 해시 체이닝, 서명 검증, 로그 출력
          |
          |-- user/                                  # 사용자, 역할, 관리자 기능 도메인
          |   |-- AdminInitializer.java              # 관리자 계정 초기화
          |   |-- UserRole.java                      # 사용자 역할 enum 정의
          |   |-- controller/
          |   |   `-- UserController.java            # 회원가입, 로그인, 사용자 역할 관리 입력 처리
          |   |-- dao/
          |   |   |-- RoleDao.java                   # 역할 정보 조회
          |   |   `-- UserDao.java                   # 사용자 저장, 조회, 역할 변경, 키 경로 조회
          |   |-- dto/
          |   |   |-- SignupRequestDto.java          # 회원가입 요청값 검증 및 전달
          |   |   `-- UserDto.java                   # 사용자 정보 전달 객체
          |   `-- service/
          |       `-- UserService.java               # 회원가입, 로그인, 역할 조회, 사용자 역할 변경 로직
          |
          `-- util/
              |-- PasswordHasher.java                # 사용자 비밀번호 해싱 및 검증
              `-- MasterKeyCryptor.java              # 마스터키 기반 AES 암호화/복호화 유틸
```

<br>

