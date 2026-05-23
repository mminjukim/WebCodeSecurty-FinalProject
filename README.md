## Introduction

2026년 1학기 웹/코드보안 [팀-박시현] 기말 프로젝트입니다.

## Commit convention

e.g. `타입/#이슈번호: 커밋 메시지 작성`

| Type     | Description                      |
|----------|----------------------------------|
| Feat     | 새로운 기능 개발                 |
| Fix      | 오류, 동작하지 않는 코드 등 수정 |
| Refactor | 구조 개선, 코드 개선 등 리팩터링 |
| Chore    | 기타 부수적인 코드 수정          |

## Collaboration workflow

- 작업 내용을 제목으로 이슈 생성, 이슈 타입 지정
- 브랜치 생성 - e.g. `소문자타입/작업-내용을-작성`
- 브랜치에 push 후, 작업 내용을 제목으로 PR 생성
- 셀프/상호 코드리뷰 후 `양식-코드리뷰보고서` 작성해서 공유 (이후 해당내용 리팩터링) 

## 프로토타입 실행하기 
JDK 21, MySQL 설치 필요 

### 로컬 MySQL에서 데이터베이스 및 사용자 생성 
```sql
mysql -u root -p 

CREATE DATABASE docsystem_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'docsystem_user'@'localhost' IDENTIFIED BY '1234';

GRANT ALL PRIVILEGES ON docsystem_db.* TO 'docsystem_user'@'localhost';

FLUSH PRIVILEGES;
```

### 프로젝트 다운로드 
```bash
git clone https://github.com/mminjukim/WebCodeSecurty-FinalProject.git

cd WebCodeSecurty-FinalProject
```

### 소스코드 컴파일 및 실행 
- **windows**
    ```bash
    dir /s /B src\*.java > sources.txt

    javac -d bin -cp "lib\*" @sources.txt

    java -cp "bin;lib\*" main.java.DocSystem
    ```
- **mac os**
    ```bash
    javac -d bin -cp "lib/*" $(find src -name "*.java")

    java -cp "bin:lib/*" main.java.DocSystem
    ```
