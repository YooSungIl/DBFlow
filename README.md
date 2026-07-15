![Status](https://img.shields.io/badge/Status-MVP_Completed-success?style=flat)
> 현재 PostgreSQL의 Table과 Column 변경 이력을 관리할 수 있는 MVP 개발을 완료했습니다.


# DBFlow
## DBFlow 개요
DBFlow는 Git의 버전 관리 개념을 데이터베이스 스키마 관리에 적용한 Java 기반 CLI 도구입니다.

PostgreSQL 메타데이터를 수집하고 이전 상태와의 차이점을 비교한 뒤, 변경 내용을 Commit 단위로 저장하여 데이터베이스 스키마의 변경 이력을 체계적으로 관리합니다.

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat&logo=gradle&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat&logo=sqlite&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-000000?style=flat)
![Picocli](https://img.shields.io/badge/Picocli-4CAF50?style=flat)


## 프로젝트 특징
- PostgreSQL Schema의 Table 및 Column 변경 사항 자동 탐지
- Snapshot, Diff, Commit 기반의 데이터베이스 변경 이력 관리
- 사용자, Commit 메시지, 변경 시각을 포함한 추적 가능한 이력 저장
- 외부 서버 없이 SQLite를 사용하는 로컬 중심 구조
- AES-256-GCM 기반 데이터베이스 접속 비밀번호 암호화
- Java Runtime을 포함한 독립 실행형 배포 패키지 제공
- Collector와 Repository를 분리하여 다양한 DBMS로 확장 가능한 구조


## DBFlow를 만드는 이유
데이터베이스 스키마는 서비스 운영과 신규 기능 개발 과정에서 지속적으로 변경됩니다. 
설계 문서나 데이터 모델을 별도로 관리하더라도 실제 데이터베이스에 반영된 변경 이력을 체계적으로 기록하고 추적하기는 쉽지 않습니다.
특히 여러 개발자가 함께 작업하거나 운영 환경에 배포하는 과정에서는 변경 사항이 누락되거나, 누가 언제 어떤 이유로 구조를 변경했는지 확인하기 어려운 문제가 발생할 수 있습니다.

그래서 Git이 소스 코드를 버전 관리하듯, 데이터베이스 스키마도 변경 이력을 체계적으로 관리할 수 있는 도구가 필요하다는 생각에서 DBFlow를 개발하게 되었습니다.

DBFlow는 다음과 같은 문제를 해결하는 것을 목표로 합니다.

| 기존 문제 | DBFlow의 해결 방법 |
| :--- | :--- |
| 배포 과정에서 데이터베이스 변경 사항이 누락될 수 있음 | 변경 사항을 자동으로 비교하고 이력으로 관리하여 누락 가능성을 낮춤 |
| 누가 언제 무엇을 변경했는지 추적하기 어려움 | Commit 단위로 사용자, 시간, 변경 내용을 저장하여 이력 관리 |
| 데이터베이스 변경 사항을 수작업으로 비교해야 함 | 메타데이터를 자동 수집하고 변경 사항을 비교하여 확인 시간 단축 |
| 데이터베이스 스키마를 체계적으로 관리할 방법이 부족함 | Git과 유사한 방식으로 데이터베이스 스키마를 형상관리 |

궁극적으로 DBFlow는 **“Git이 소스 코드를 관리하듯, 데이터베이스 스키마도 버전 관리할 수 있는 CLI 도구”**를 목표로 개발되고 있습니다.


## 핵심 기능
DBFlow는 PostgreSQL Schema의 현재 상태를 수집하고 마지막 Commit 상태와 비교하여 데이터베이스 오브젝트의 변경 이력을 관리합니다.

| 기능 | 설명 |
| :--- | :--- |
| **🔗 Connect** | PostgreSQL 접속 정보와 관리 대상 Schema를 등록하고 관리합니다. |
| **📂 Work** | 등록된 Connect 중 현재 변경 이력을 관리할 작업 영역을 선택합니다. |
| **🔍 Diff** | Table 및 Column 메타데이터를 수집하고 마지막 Commit 상태와 비교하여 생성, 변경, 삭제 내역을 확인합니다. |
| **💾 Commit** | 확인된 변경 사항을 제목, 설명, 작성자, Commit 시각과 함께 저장합니다. |
| **📜 Commit 조회** | `dbf commit list`와 `dbf commit show`를 통해 Commit 목록과 상세 변경 이력을 조회합니다. |
| **🔐 Security** | PostgreSQL 접속 비밀번호를 AES-256-GCM으로 암호화하고 마스터 키를 별도 파일로 관리합니다. |
| **📦 Standalone** | Java 17 Runtime을 배포 패키지에 포함하여 별도의 Java 설치 없이 실행할 수 있습니다. |

> **사용자는 `Connect → Work → Diff → Commit → Commit 조회` 흐름으로 데이터베이스 변경 이력을 관리합니다.**


## 아키텍처
### 1. 시스템 아키텍처
```text
                         User
                           │
                           ▼
                    DBFlow CLI (dbf)
                           │
                           ▼
               Picocli Command / Printer
                           │
                           ▼
                  Application Service
                           │
          ┌────────────────┴────────────────┐
          │                                 │
          ▼                                 ▼
 PostgreSQL Metadata                 SQLite Repository
      Collector                    (Config / Work / Commit)
          │                                 │
          ▼                                 ▼
 Target PostgreSQL DB                   DBFlow DB
(Table / Column Metadata)               (dbflow.db)
```

### 2. 변경 이력 처리 흐름
```text
         Target PostgreSQL
                │
                ▼
      Metadata Collection
                │
                ▼
       Collected Snapshot
                │
                ▼
 Compare with Current Snapshot
                │
                ▼
          Diff Result
                │
                ▼
       User Confirms Commit
                │
     ┌──────────┴──────────┐
     ▼                     ▼
 No Change           Changes Found
                           │
                           ▼
                    Work Change Set
                           │
                           ▼
                     Commit Creation
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
       Commit History       Current Snapshot Update
```


## 프로젝트 디렉터리 구조
```text
src/main/
├── java/io/dbflow/
│   ├── application/        # 비즈니스 로직
│   ├── command/            # CLI 명령 및 화면 출력
│   ├── common/             # 공통 기능
│   ├── domain/             # 핵심 도메인 모델
│   ├── dto/                # 조회 및 출력 모델
│   └── infrastructure/     # DB, 암호화, 설치 및 외부 연동
└── resources/
    ├── db/migration/       # 제품 DB 설치 및 마이그레이션 SQL
    └── mapper/             # MyBatis Mapper XML
```


## 명령어 실행 흐름
### 1. 초기 설정
1. 사용자 정보를 등록합니다.
   ```bash
   dbf user set
   ```
   
2. PostgreSQL 접속 정보와 관리 대상 Schema를 등록합니다.
   ```bash
   dbf connect add
   ```
   
3. 등록된 Connect의 별칭을 사용하여 현재 작업 영역을 선택합니다.
   ```bash
   dbf work set <alias>
   ```
   
### 2. 변경 이력 관리
1. PostgreSQL에서 Table 또는 Column 구조를 변경합니다.

2. 메타데이터를 수집하고 마지막 Commit 상태와 비교합니다.
   ```bash
   dbf diff
   ```
   
3. 확인한 변경 내용을 Commit으로 저장합니다.
   ```bash
   dbf commit
   ```
   
4. 저장된 Commit 목록과 상세 변경 내용을 조회합니다.
   ```bash
   dbf commit list
   dbf commit show <commit-id>
   ```


## 기술 스택
| 구분 | 기술 | 버전 | 사용 목적 |
| ---------------- | --------------------------- | ------------: | ------------------------------------ |
| Language | Java | 17 | CLI 애플리케이션과 핵심 비즈니스 로직 구현 |
| CLI | Picocli | 4.7.7 | 명령어 및 서브커맨드 구조 구성 |
| SQL Mapper | MyBatis | 3.5.16 | SQLite 및 PostgreSQL 쿼리 실행과 결과 매핑 |
| Product Database | SQLite JDBC | 3.50.3.0 | 사용자 정보, 접속 정보 및 데이터베이스 변경 이력 저장 |
| Target Database | PostgreSQL JDBC | 42.7.7 | PostgreSQL 연결 및 데이터베이스 오브젝트 메타데이터 수집 |
| Security | Java JCE / AES-256-GCM | Java 17 제공 | PostgreSQL 접속 비밀번호 암호화 및 변조 탐지 |
| Build | Gradle Wrapper / Kotlin DSL | 8.14 | 빌드, 테스트 및 배포 작업 관리 |
| Packaging | Shadow Plugin | 8.3.6 | 실행에 필요한 의존성을 포함한 Fat JAR 생성 |
| Runtime | Azul Zulu JRE | 17.0.17+10 | 별도의 Java 설치 없이 실행할 수 있는 Runtime 제공 |
| Distribution | Shell Script / ZIP | - | macOS ARM64용 설치 및 배포 패키지 구성 |


## 빌드 명령어 및 설치 방법
### 1. 빌드 방법
1. 테스트를 실행합니다.
```bash
./gradlew test
```
2. Java Runtime이 준비되지 않은 경우 최초 한 번만 다운로드합니다.
```bash
./gradlew downloadJavaRuntime
```
3. macOS ARM64 배포 패키지를 생성합니다.
```bash
./gradlew packageDbFlow
```
빌드 결과: `build/distributions/dbflow-1.0.0-macos-arm64.zip`

### 2. 설치 방법
1. 배포 ZIP 파일의 압축을 해제합니다.
```bash
unzip dbflow-1.0.0-macos-arm64.zip
```

2. 압축을 해제한 디렉터리에서 설치 스크립트를 실행합니다.
```bash
cd dbflow
./install.sh
```

3. 설치된 버전을 확인합니다.
```bash
dbf --version
```
`dbf` 명령을 찾을 수 없다면 `~/.local/bin`을 `PATH`에 추가합니다.
```bash
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

> 현재 배포 패키지는 macOS Apple Silicon(ARM64) 환경만 지원합니다. 향후 macOS Intel, Windows 및 Linux 환경으로 지원 범위를 확대할 예정입니다.


## 제품 설치 디렉터리 구조
### 1. 프로그램 설치 영역
```
~/.local/share/dbflow/
├── bin/
│   └── dbf                        # DBFlow 실행 스크립트
├── lib/
│   └── dbflow-1.0.0.jar           # DBFlow 실행 JAR
└── runtime/
    └── java17/                    # 번들된 Azul Zulu Java 17 Runtime
```

### 2. 사용자 데이터 영역
```
~/.dbflow/
├── data/
│   └── dbflow.db                 # 사용자 정보와 DB 변경 이력을 저장하는 SQLite DB
└── security/
    └── master.key                # DB 접속 비밀번호 암호화에 사용하는 AES-256 마스터 키
```

### 3. 명령 링크
```
~/.local/bin/
└── dbf                             # ~/.local/share/dbflow/bin/dbf를 가리키는 심볼릭 링크
```


로드맵

핵심 기능
- [x] 사용자 정보 등록 및 조회
- [x] PostgreSQL 접속 정보 등록, 조회, 수정 및 비활성화
- [x] 작업 대상 데이터베이스 및 Schema 선택
- [x] Table 및 Column 메타데이터 수집
- [x] Table 생성, 변경 및 삭제 비교
- [x] Column 생성, 변경 및 삭제 비교
- [x] Column 속성 변경 비교
- [x] Diff 결과 생성 및 조회
- [x] 변경 사항 Commit 저장
- [x] Commit 목록 및 상세 변경 이력 조회
- [ ] Commit 이력 조건 검색
- [ ] 변경 이력 내보내기

보안 및 제품 관리
- [x] PostgreSQL 접속 비밀번호 AES-256-GCM 암호화
- [x] 암호화 마스터 키 별도 저장
- [x] SQLite 기반 사용자 설정 및 변경 이력 저장
- [x] 제품 설치 시 SQLite 데이터베이스 초기화
- [x] 제품 및 데이터베이스 Schema 버전 관리
- [x] 데이터베이스 Migration 이력 관리
- [ ] 제품 데이터 백업 및 복원
- [ ] 자동 업데이트

지원 오브젝트
- [x] Table
- [x] Column
- [ ] Index
- [ ] Primary Key
- [ ] Foreign Key
- [ ] Unique Constraint
- [ ] Check Constraint
- [ ] Sequence
- [ ] View
- [ ] Function
- [ ] Procedure
- [ ] Trigger

지원 DBMS
- [x] PostgreSQL
- [ ] SQLite
- [ ] Oracle
- [ ] MySQL
- [ ] MariaDB

배포 환경
- [x] Java 17 Runtime 포함 독립 실행형 패키지
- [x] macOS Apple Silicon(ARM64)
- [ ] macOS Intel(x86_64)
- [ ] Windows
- [ ] Linux

인터페이스 확장
- [x] Java CLI
- [ ] REST API
- [ ] Web 관리 화면
- [ ] 변경 이력 대시보드
- [ ] 다중 사용자 협업 기능
