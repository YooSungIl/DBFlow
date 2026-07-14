# DBFlow AGENTS

> 이 파일은 DBFlow를 수정하는 AI Assistant와 개발자가 반드시 먼저 읽고 따라야 하는 작업 기준이다.
> 프로젝트 현황의 상세 설명은 `PROJECT_CONTEXT.md`를 함께 참고한다.

## 1. 프로젝트 소개 (Project Overview)

### DBFlow란?

DBFlow는 데이터베이스 오브젝트의 변경 사항을 Git처럼 관리하기 위한 Java
기반 CLI 프로젝트이다.

데이터베이스 변경 이력을 체계적으로 관리하여 배포 시 누락, 설계 오류 및
운영 리스크를 최소화하는 것을 목표로 한다.

주요 사용 대상은 다음과 같다.

-   Developer
-   DBA
-   Data Modeler
-   Database Administrator
-   기타 데이터베이스 관련 전문가

------------------------------------------------------------------------

## 2. 프로젝트 목표 (Project Goals)

### 단기 목표

-   PostgreSQL 데이터베이스 변경 이력 관리
-   CLI 기반 형상관리 기능 제공
-   Snapshot / Diff / Commit 기능 구현
-   제품(MVP) 완성

### 장기 목표

-   모든 RDBMS 지원
-   NoSQL 지원
-   GUI 제공
-   Git 수준의 데이터베이스 형상관리 도구로 발전

------------------------------------------------------------------------

## 3. 기술 스택 (Technology Stack)

### Language

-   Java 17

### Build

-   Gradle Wrapper 8.14 / Kotlin DSL
-   Shadow plugin 8.3.6

### CLI

-   Picocli

### ORM

-   MyBatis

### Local Database

-   SQLite

### External Database

-   PostgreSQL

### Current Versions

-   Product: `1.0.0`
-   Picocli: `4.7.7`
-   MyBatis: `3.5.16`
-   SQLite JDBC: `3.50.3.0`
-   PostgreSQL JDBC: `42.7.7`
-   Bundled Runtime: Azul Zulu JRE `17.0.17+10`, macOS ARM64

------------------------------------------------------------------------

## 4. 핵심 개념 (Core Concepts)

### Connect

외부 데이터베이스의 작업 영역을 저장한다.

현재는 PostgreSQL Schema 단위로 관리한다.

### Work

Connect에서 저장한 작업 영역을 선택한다.

모든 작업은 현재 Work 기준으로 수행된다.

### Snapshot

Snapshot은 데이터베이스 오브젝트 상태를 저장한다.

총 3개의 Snapshot을 관리한다.

#### Collect Snapshot

실시간으로 수집한 현재 데이터베이스 상태

#### Current Snapshot

현재까지 Commit된 최종 상태

#### History Snapshot

모든 Commit 시점의 변경 이력

### Diff

현재 수집한 오브젝트와 Current Snapshot을 비교하여 변경 내용을 생성한다.

### Commit

Diff 결과를 Commit으로 저장한다.

Commit에는 다음 정보가 저장된다.

-   변경 대상
-   변경 내용
-   변경 이유
-   변경 시각
-   변경 사용자

### Install

`dbf install`은 사용자 데이터 영역을 최초 초기화한다.

-   `~/.dbflow`, `data`, `security` 생성
-   디렉터리 권한 `700`, SQLite 파일 권한 `600` 적용
-   `~/.dbflow/data/dbflow.db` 생성
-   버전별 SQL 마이그레이션 실행
-   스키마 최종 버전과 실행 이력 저장
-   실패 시 이번 설치에서 생성한 사용자 데이터를 원복

------------------------------------------------------------------------

## 5. 프로젝트 구조 (Project Structure)

### Distribution Package

``` text
dbflow/
├── install.sh
├── bin/dbf
├── lib/dbflow-1.0.0-SNAPSHOT.jar
└── runtime/java17/Contents/Home/
```

### 설명

**bin**

-   실행 스크립트 이름은 `dbf`이다. `dbf.sh`로 되돌리지 않는다.
-   `~/.local/bin/dbf` 심볼릭 링크로 명령을 제공한다.

**lib**

-   DBFlow 실행 Jar

**runtime**

-   macOS ARM64용 Zulu Java 17 Runtime

**프로그램 설치 영역**

-   `~/.local/share/dbflow`

**사용자 데이터 영역**

-   `~/.dbflow/data/dbflow.db`
-   `~/.dbflow/security/master.key`

**.dbflow/data**

-   SQLite 데이터 저장소

------------------------------------------------------------------------

## 6. Source Structure

``` text
Command
↓
Service
↓
Repository
↓
Mapper
↓
SQLite / PostgreSQL
```

### 패키지 설명

**command**

CLI 명령 처리

**application**

Service 계층

**common**

공통 기능

-   console
-   enums
-   exception
-   validation

**domain**

도메인 모델

**dto**

조회 결과 조립과 콘솔 표시를 위한 데이터 전달 객체

**infrastructure**

외부 시스템 연동

-   MyBatis
-   Database
-   설치/마이그레이션
-   사용자 경로와 POSIX 권한
-   암호화 키

**resources**

-   설정파일
-   Mapper XML

**docs**

문서 (SQL 및 백업 포함, 설계 포함)
Git 관리 대상 아님
------------------------------------------------------------------------
## 7. 개발 원칙 (Development Principles)
우선순위
1.  데이터의 안전성과 정확성을 가장 우선한다.
2.  누가 보더라도 쉽게 유지보수할 수 있는 구조를 만든다.
3.  단순하지만 명확한 설계를 선택한다.
4.  확장 가능한 구조를 고려한다.
------------------------------------------------------------------------
## 8. 코딩 규칙 (Coding Conventions)
-   Command는 명령어별 패키지로 분리한다.
-   Service는 필요한 경우 다른 Service를 호출한다.
-   여러 Repository에서 하나의 트랜잭션을 사용하는 경우 Service에서
    SqlSession을 관리한다.
-   Console은 CLI 입출력만 담당한다.
-   예외는 계층별(Command, Service, Repository, Console)로 관리한다.
-   MyBatis는 Main(SQLite)과 External(PostgreSQL)을 구분하여 사용한다.
-   외부 DB는 Connect에 저장된 DbConfig 정보를 사용하여 연결한다.
-   클래스 및 메서드는 의미가 명확한 전체 이름을 사용한다.
-   사용자 데이터 경로는 `DbFlowPathResolver`를 사용한다. `~/.dbflow` 경로를 다른 곳에 중복 구현하지 않는다.
-   파일 권한은 `DbFlowFilePermissions`를 사용한다.
-   제품 버전은 `gradle.properties`의 `dbflowVersion`만 수정한다.
-   SQL 로그는 배포 환경에서 기본 비활성화하고 개발 시에만 명시적으로 활성화한다.
------------------------------------------------------------------------
## 9. 계층별 역할 (Layer Responsibilities)
-   **Command**: 입력 / 출력
-   **Service**: 비즈니스 로직, 필요 시 트랜잭션 관리
-   **Repository**: DB 접근, SqlSession 관리
-   **Mapper**: SQL 실행
-   **Domain**: 비즈니스 모델
------------------------------------------------------------------------
## 10. 예외 처리 규칙 (Exception Handling)
-   Command
-   Service
-   Repository
-   Console
Command 계층에서 사용자에게 최종 오류를 출력한다.
DB 오류는 핵심 내용만 출력한다.
------------------------------------------------------------------------
## 11. 데이터베이스 규칙 (Database Rules)
-   SQLite는 제품 내부 저장소이다.
-   PostgreSQL은 메타데이터 수집 대상이다.
-   SQLite 제품 DB의 고정 경로는 `~/.dbflow/data/dbflow.db`이다.
-   마이그레이션 파일명은 `V{MAJOR}.{MINOR}.{PATCH}__{description}.sql` 형식이다.
-   한 번 배포된 마이그레이션 SQL은 수정하지 않고 새 PATCH 파일을 추가한다.
-   같은 MAJOR이면서 제품 MINOR 이하인 SQL을 `MINOR → PATCH` 순서로 실행한다.
-   성공한 SQL은 재실행하지 않으며 SHA-256 체크섬 불일치 시 중단한다.
-   SQL 실행, `DBF_SCHEMA_VERSION` 반영, SUCCESS 이력 저장은 파일별 단일 트랜잭션이다.
-   MAJOR 변경은 자동 마이그레이션하지 않고 재설치 대상으로 판단한다.
------------------------------------------------------------------------
## 12. AI Assistant Guidelines
#### 12.1 기본 원칙 (General Principles)
* DBFlow는 장기적으로 유지·발전시킬 제품을 목표로 한다.
* 새로운 기술을 적용하는 것보다 프로젝트의 일관성과 유지보수성을 우선한다.
* 코드의 양보다 코드의 명확성과 이해하기 쉬운 구조를 더 중요하게 생각한다.
* MVP 단계에서는 과도한 추상화나 복잡한 설계를 지양한다.
* 기존 아키텍처와 설계 의도를 존중하며 기능을 추가하거나 개선한다.
#### 12.2 변경 작업 전 확인 사항 (Before Making Changes)
AI는 코드를 수정하기 전에 반드시 다음 사항을 확인한다.
* 관련 클래스와 호출 흐름을 충분히 이해한다.
* 변경 범위를 최소화한다.
* 기존 기능이 변경되지 않는 방향으로 개선한다.
* 동일한 목적의 코드가 이미 존재하는지 먼저 확인한다.
* 중복 구현보다 기존 코드를 재사용하는 방법을 우선 고려한다.
#### 12.3 아키텍처 원칙 (Architecture Rules)
다음 아키텍처는 특별한 이유가 없는 한 변경하지 않는다.
~~~
Command
↓
Service
↓
Repository
↓
Mapper
↓
Database
~~~
각 계층은 자신의 역할만 수행한다.
* Command는 CLI 입력과 출력만 담당한다.
* Service는 비즈니스 로직을 담당한다.
* Repository는 데이터 접근과 트랜잭션을 담당한다.
* Mapper는 SQL 실행만 담당한다.
* Domain은 비즈니스 데이터를 표현한다.
#### 12.4 코드 작성 원칙 (Coding Style)
코드를 작성하거나 수정할 때 다음 원칙을 따른다.
* 이해하기 쉬운 코드를 우선한다.
* 명확한 변수명과 메서드명을 사용한다.
* 과도한 Stream 체인이나 복잡한 람다는 지양한다.
* 불필요한 추상화는 만들지 않는다.
* Enum으로 관리할 수 있는 값은 Enum을 사용한다.
* Domain 객체가 존재하는 경우 Map 사용을 지양한다.
* 동일한 코딩 스타일을 프로젝트 전체에서 유지한다.
#### 12.5 리팩토링 원칙 (Refactoring Rules)
리팩토링은 기능 변경이 아니라 코드 품질 개선을 위한 작업이다.
리팩토링 시에는
* 기존 동작을 변경하지 않는다.
* Public API는 가능한 유지한다.
* 변경 이유를 명확하게 설명한다.
* 작은 단위로 점진적으로 개선한다.
* 대규모 구조 변경은 반드시 사전 설명 후 진행한다.
#### 12.6 절대 하지 말아야 할 사항 (Things Never To Do)
다음 사항은 특별한 요청이 없는 한 수행하지 않는다.
* 기존 아키텍처를 임의로 변경하지 않는다.
* Command에서 Repository를 직접 호출하지 않는다.
* Command에 비즈니스 로직을 작성하지 않는다.
* Service에서 콘솔 출력(System.out.println)을 수행하지 않는다.
* Mapper에 비즈니스 로직을 작성하지 않는다.
* 동일한 기능을 중복 구현하지 않는다.
* Domain 객체 대신 Map을 사용하는 코드를 추가하지 않는다.
* 기존 설계 의도를 무시하고 새로운 프레임워크를 도입하지 않는다.
* 코드 길이를 줄이기 위해 가독성을 희생하지 않는다.
* 충분한 근거 없이 파일 구조를 변경하지 않는다.
* `gradle.properties` 외 다른 소스에 제품 버전을 하드코딩하지 않는다.
* 배포 빌드에서 Runtime을 자동으로 다운로드하지 않는다.
* 기존 마이그레이션 SQL 파일을 수정하여 이력을 훼손하지 않는다.
* DB 접속 비밀번호, 평문 또는 암호화 키를 로그나 콘솔에 출력하지 않는다.
#### 12.7 AI 리뷰 원칙 (Code Review Guidelines)
AI가 코드를 리뷰할 경우 다음 순서로 검토한다.
1. 아키텍처 문제가 있는가?
2. 설계적으로 더 좋은 방법이 있는가?
3. 유지보수성을 개선할 수 있는가?
4. 중복 코드가 존재하는가?
5. 성능 개선이 필요한 부분이 있는가?
6. 코드 스타일이 프로젝트 규칙과 일치하는가?

단순한 문법 지적보다 설계와 구조 개선을 우선적으로 제안한다.
#### 12.8 AI 응답 원칙 (Response Guidelines)
AI는 변경 사항을 제안할 때 다음 내용을 함께 설명한다.
* 왜 변경하는지
* 기대 효과
* 장점
* 단점
* 기존 방식과의 차이
  큰 구조 변경은 사용자의 동의를 받은 후 진행한다.
------------------------------------------------------------------------
## 13. 현재 개발 상태 (Current Status)
### 완료
-   User
-   Connect
-   Work
-   Diff
-   Commit
-   DB 접속 비밀번호 AES-256-GCM 암호화
-   `dbf install` 사용자 데이터/SQLite/스키마 초기화
-   스키마 버전 및 마이그레이션 이력 관리
-   Zulu Runtime 고정 다운로드 및 SHA-256 검증
-   macOS ARM64 ZIP 패키징
-   Gradle 기반 제품 버전 단일 원천화
-   배포 SQL 로그 기본 비활성화
### 현재 상태
-   MVP 기능 구현 완료
-   기본 구조 완성
-   개인 PC 설치 및 실사용 준비 완료
-   포트폴리오/사용자 안내 문서 1차 작성 단계
### 앞으로
-   리팩토링
-   공통화
-   성능 개선
-   테스트
-   문서화
-   포트폴리오 작성
-   개인 실사용 피드백 반영
-   Index/Constraint 등 지원 오브젝트 확대
------------------------------------------------------------------------
## 14. 프로젝트 철학 (Project Philosophy)
-   데이터베이스 변경 이력은 코드만큼 중요하다.
-   모든 변경은 추적 가능해야 한다.
-   단순한 코드보다 명확한 구조를 우선한다.
-   빠른 개발보다 유지보수 가능한 개발을 지향한다.
-   MVP는 완성이 아니라 시작이다.

------------------------------------------------------------------------

## 15. 버전 및 마이그레이션 불변 규칙

- 제품 버전의 유일한 원천은 `gradle.properties`의 `dbflowVersion`이다.
- Gradle은 빌드 시 `META-INF/dbflow-version.properties`를 생성한다.
- Java는 `DbFlowVersion`을 통해 생성된 리소스를 읽는다.
- JAR, ZIP, `dbf --version`, 마이그레이션 `APP_VERSION`은 모두 같은 값을 사용한다.
- 현재 JAR 이름은 `dbflow-{VERSION}-SNAPSHOT.jar`이다.
- 현재 ZIP 이름은 `dbflow-{VERSION}-macos-arm64.zip`이다.
- DB 스키마 버전 PATCH는 제품 PATCH와 독립적인 마이그레이션 순번이다.

## 16. 설치 및 패키징 불변 규칙

- Shell `install.sh`은 프로그램 영역 복사와 Java `dbf install` 호출을 담당한다.
- Java `dbf install`은 사용자 데이터와 DB 초기화를 담당한다.
- 프로그램 영역과 사용자 데이터 영역의 책임을 섞지 않는다.
- `install()`이 전체 Java 설치 순서와 rollback을 책임진다. 하위 단계가 임의로 전체 rollback하지 않는다.
- Runtime ZIP은 `packaging/runtime`의 로컬 캐시이며 Git에 커밋하지 않는다.
- Runtime 버전/URL/체크섬은 `packaging/runtime/runtime.properties`에서 관리한다.
- `packageDbFlow`는 Runtime이 없을 때 자동 다운로드하지 않고 실패해야 한다.
- `downloadJavaRuntime`만 네트워크 다운로드를 수행한다.

## 17. 로그 정책

- 현재 제품은 별도 로그 파일을 생성하지 않는다.
- 배포 실행에서는 MyBatis SQL 로그를 출력하지 않는다.
- 정상 CLI 출력과 사용자용 오류 메시지는 로그가 아니므로 유지한다.
- 개발 시 `-Ddbflow.sqlLog=true`로 SQL 로그를 활성화할 수 있다.
- `./gradlew run`은 개발 편의를 위해 SQL 로그를 활성화한다.
- `stderr` 전체를 숨겨 사용자에게 필요한 오류까지 제거하지 않는다.

## 18. 필수 검증 명령

변경 범위에 맞는 테스트 후, 배포 관련 변경은 가능한 다음 명령으로 검증한다.

```bash
./gradlew test
./gradlew packageDbFlow
```

Runtime을 최초 준비할 때만 실행한다.

```bash
./gradlew downloadJavaRuntime
```

패키징 결과:

```text
build/distributions/dbflow-{VERSION}-macos-arm64.zip
```
