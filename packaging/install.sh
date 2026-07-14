#!/bin/bash

# ==============================================================================
# DBFlow macOS Apple Silicon(M1/M2/M3/M4) 설치 스크립트
#
# 배포 패키지 예상 구조:
#
# dbflow/
# ├── install.sh
# ├── bin/
# │   └── dbf
# ├── lib/
# │   └── dbflow-1.0.0-SNAPSHOT.jar
# └── runtime/
#     └── java17/
#
# 설치 후 프로그램 파일:
#
# ~/.local/share/dbflow/
# ├── bin/
# ├── lib/
# └── runtime/
#
# 시스템 명령 링크:
#
# ~/.local/bin/dbf
#     -> ~/.local/share/dbflow/bin/dbf
#
# 사용자 데이터:
#
# ~/.dbflow/
#
# 사용자 데이터 디렉터리와 SQLite DB 초기화는 이 스크립트가 아니라
# Java의 `dbf install` 명령이 담당한다.
# ==============================================================================


# ------------------------------------------------------------------------------
# 오류 발생 시 즉시 종료한다.
#
# -e : 명령 실행 실패 시 종료
# -u : 선언되지 않은 변수 사용 시 종료
# -o pipefail : 파이프 중간 명령이 실패해도 전체 실패로 처리
# ------------------------------------------------------------------------------
set -euo pipefail


# ------------------------------------------------------------------------------
# 공통 출력 함수
# ------------------------------------------------------------------------------
print_info() {
    printf '[INFO] %s\n' "$1"
}

print_error() {
    printf '[ERROR] %s\n' "$1" >&2
}


# ------------------------------------------------------------------------------
# 현재 운영체제와 CPU 아키텍처를 확인한다.
#
# 이 설치 파일은 현재 macOS Apple Silicon 전용이다.
# Apple Silicon에서는 uname -m 결과가 arm64로 나온다.
# ------------------------------------------------------------------------------
OS_NAME="$(uname -s)"
CPU_ARCH="$(uname -m)"

if [[ "$OS_NAME" != "Darwin" ]]; then
    print_error "이 설치 패키지는 macOS 전용입니다. 현재 OS: $OS_NAME"
    exit 1
fi

if [[ "$CPU_ARCH" != "arm64" ]]; then
    print_error "이 설치 패키지는 Apple Silicon 전용입니다. 현재 아키텍처: $CPU_ARCH"
    exit 1
fi


# ------------------------------------------------------------------------------
# 사용자가 어느 위치에서 install.sh를 실행하더라도
# install.sh 파일이 실제로 존재하는 패키지 루트를 계산한다.
#
# 예:
#   ~/Downloads/dbflow/install.sh
#   /tmp/dbflow/install.sh
#
# 어디에서 실행해도 PACKAGE_DIR은 해당 dbflow 디렉터리를 가리킨다.
# ------------------------------------------------------------------------------
PACKAGE_DIR="$(
    cd "$(dirname "$0")"
    pwd
)"


# ------------------------------------------------------------------------------
# 고정 설치 경로
#
# INSTALL_DIR:
#   DBFlow 프로그램 실행 파일, JAR, 포함 JDK가 저장되는 위치
#
# COMMAND_DIR:
#   사용자가 어느 위치에서든 `dbf` 명령을 실행할 수 있도록
#   심볼릭 링크를 생성하는 위치
#
# USER_DATA_DIR:
#   Java 설치 명령이 DB, 암호화 키 등을 생성하는 사용자 데이터 위치
#   이 스크립트에서는 직접 생성하지 않고 확인용으로만 사용한다.
# ------------------------------------------------------------------------------
INSTALL_DIR="$HOME/.local/share/dbflow"
COMMAND_DIR="$HOME/.local/bin"
COMMAND_LINK="$COMMAND_DIR/dbf"
USER_DATA_DIR="$HOME/.dbflow"


# ------------------------------------------------------------------------------
# 배포 패키지 필수 구성 경로
# ------------------------------------------------------------------------------
PACKAGE_BIN="$PACKAGE_DIR/bin/dbf"
PACKAGE_LIB_DIR="$PACKAGE_DIR/lib"
PACKAGE_RUNTIME_DIR="$PACKAGE_DIR/runtime"


# ------------------------------------------------------------------------------
# 배포 패키지 구성이 올바른지 검사한다.
# 필수 파일이나 디렉터리가 빠져 있으면 설치를 시작하지 않는다.
# ------------------------------------------------------------------------------
print_info "DBFlow 설치 패키지를 검사합니다."

if [[ ! -f "$PACKAGE_BIN" ]]; then
    print_error "실행 파일을 찾을 수 없습니다: $PACKAGE_BIN"
    exit 1
fi

if [[ ! -d "$PACKAGE_LIB_DIR" ]]; then
    print_error "lib 디렉터리를 찾을 수 없습니다: $PACKAGE_LIB_DIR"
    exit 1
fi

if [[ ! -d "$PACKAGE_RUNTIME_DIR" ]]; then
    print_error "runtime 디렉터리를 찾을 수 없습니다: $PACKAGE_RUNTIME_DIR"
    exit 1
fi


# ------------------------------------------------------------------------------
# lib 디렉터리에 JAR가 최소 한 개 존재하는지 검사한다.
#
# JAR 이름을 아직 확정하지 않았으므로:
#   dbflow.jar
#   dbflow_v1.0.0.jar
#   dbflow-1.0.0-SNAPSHOT.jar
#
# 등의 이름을 모두 허용한다.
#
# 실제 bin/dbf 스크립트도 같은 규칙으로 JAR를 찾거나,
# 추후 JAR 이름을 dbflow.jar로 고정하는 것이 좋다.
# ------------------------------------------------------------------------------
JAR_COUNT="$(find "$PACKAGE_LIB_DIR" -maxdepth 1 -type f -name '*.jar' | wc -l | tr -d ' ')"

if [[ "$JAR_COUNT" -eq 0 ]]; then
    print_error "lib 디렉터리에 실행 가능한 JAR 파일이 없습니다."
    exit 1
fi


# ------------------------------------------------------------------------------
# 완전 초기 설치 기준이므로 기존 설치가 존재하면 중단한다.
#
# 향후 업데이트 기능을 구현할 때는 이 부분을:
#   - 설치 버전 확인
#   - update.sh 또는 dbf update 안내
#
# 흐름으로 확장할 수 있다.
# ------------------------------------------------------------------------------
if [[ -e "$INSTALL_DIR" ]]; then
    print_error "기존 DBFlow 프로그램 설치 디렉터리가 존재합니다: $INSTALL_DIR"
    print_error "초기 설치를 계속하지 않고 중단합니다."
    exit 1
fi

if [[ -L "$COMMAND_LINK" || -e "$COMMAND_LINK" ]]; then
    print_error "기존 dbf 명령 파일 또는 링크가 존재합니다: $COMMAND_LINK"
    print_error "초기 설치를 계속하지 않고 중단합니다."
    exit 1
fi


# ------------------------------------------------------------------------------
# 설치 도중 실패했을 때 프로그램 파일을 정리하기 위한 함수다.
#
# 주의:
# ~/.dbflow 사용자 데이터는 Java 설치 명령에서 생성될 수 있다.
# 현재는 데이터 안전을 위해 Shell에서 자동 삭제하지 않는다.
# Java의 install 로직이 실패 시 자신이 생성한 초기 데이터를
# 정리하도록 구현하는 것이 안전하다.
# ------------------------------------------------------------------------------
cleanup_failed_install() {
    print_error "설치에 실패하여 프로그램 설치 파일을 정리합니다."

    rm -f "$COMMAND_LINK"
    rm -rf "$INSTALL_DIR"

    if [[ -e "$USER_DATA_DIR" ]]; then
        print_error "사용자 데이터 디렉터리가 남아 있을 수 있습니다: $USER_DATA_DIR"
        print_error "내용을 확인한 뒤 필요하면 직접 정리해 주세요."
    fi
}


# ------------------------------------------------------------------------------
# 설치 디렉터리를 생성한다.
# ------------------------------------------------------------------------------
print_info "프로그램 설치 디렉터리를 생성합니다: $INSTALL_DIR"

mkdir -p "$INSTALL_DIR"
mkdir -p "$COMMAND_DIR"


# ------------------------------------------------------------------------------
# ZIP을 해제한 임시 위치에서 고정 설치 위치로 프로그램 파일을 복사한다.
#
# 사용자가 Downloads 등의 압축 해제 디렉터리를 나중에 삭제하더라도
# DBFlow 명령이 계속 동작하도록 반드시 고정 위치로 복사한다.
# ------------------------------------------------------------------------------
print_info "DBFlow 프로그램 파일을 고정 설치 위치로 복사합니다."

if ! cp -R "$PACKAGE_DIR/bin" "$INSTALL_DIR/"; then
    cleanup_failed_install
    exit 1
fi

if ! cp -R "$PACKAGE_DIR/lib" "$INSTALL_DIR/"; then
    cleanup_failed_install
    exit 1
fi

if ! cp -R "$PACKAGE_DIR/runtime" "$INSTALL_DIR/"; then
    cleanup_failed_install
    exit 1
fi


# ------------------------------------------------------------------------------
# ZIP 압축 또는 해제 과정에서 실행 권한이 사라질 수 있으므로
# 설치된 dbf 실행 스크립트에 실행 권한을 부여한다.
# ------------------------------------------------------------------------------
INSTALLED_DBF="$INSTALL_DIR/bin/dbf"

print_info "dbf 실행 권한을 설정합니다."

if ! chmod 755 "$INSTALLED_DBF"; then
    cleanup_failed_install
    exit 1
fi


# ------------------------------------------------------------------------------
# 포함 JDK의 Java 실행 파일을 찾아 실행 권한을 설정한다.
#
# 예상 가능한 구조:
#   runtime/java17/bin/java
#   runtime/java17/Contents/Home/bin/java
#
# 실제 bin/dbf 스크립트의 JAVA_HOME 구조와 맞춰야 한다.
# ------------------------------------------------------------------------------
BUNDLED_JAVA="$(
    find "$INSTALL_DIR/runtime" \
        -type f \
        -path '*/bin/java' \
        -print \
        -quit
)"

if [[ -z "$BUNDLED_JAVA" ]]; then
    print_error "포함 Runtime에서 Java 실행 파일을 찾을 수 없습니다."
    cleanup_failed_install
    exit 1
fi

print_info "포함 Java 실행 권한을 설정합니다: $BUNDLED_JAVA"

if ! chmod 755 "$BUNDLED_JAVA"; then
    cleanup_failed_install
    exit 1
fi


# ------------------------------------------------------------------------------
# 설치된 최종 경로의 dbf를 이용해 Java 초기 설치 명령을 실행한다.
#
# Java `dbf install`이 담당할 작업:
#   1. ~/.dbflow 생성
#   2. ~/.dbflow/data 생성
#   3. SQLite DB 생성
#   4. 초기 테이블 생성
#   5. DB 스키마 버전 입력
#   6. 설치 이력 입력
#   7. security 디렉터리와 파일 권한 설정
#
# Java 설치 명령이 성공해야 다음 단계인 심볼릭 링크를 생성한다.
# ------------------------------------------------------------------------------
print_info "DBFlow 사용자 데이터와 SQLite 저장소를 초기화합니다."

if ! "$INSTALLED_DBF" install; then
    print_error "Java 초기 설치 명령 실행에 실패했습니다."
    cleanup_failed_install
    exit 1
fi


# ------------------------------------------------------------------------------
# 사용자가 어느 디렉터리에서든 `dbf` 명령을 실행할 수 있도록
# ~/.local/bin/dbf 심볼릭 링크를 생성한다.
#
# 링크 대상은 ZIP 압축 해제 위치가 아니라 반드시 고정 설치 위치다.
# ------------------------------------------------------------------------------
print_info "dbf 명령 심볼릭 링크를 생성합니다."

if ! ln -s "$INSTALLED_DBF" "$COMMAND_LINK"; then
    print_error "심볼릭 링크 생성에 실패했습니다."
    cleanup_failed_install
    exit 1
fi


# ------------------------------------------------------------------------------
# 설치된 명령이 실제로 실행 가능한지 최종 확인한다.
#
# PATH 등록 여부와 무관하게 링크의 절대 경로로 직접 실행한다.
# ------------------------------------------------------------------------------
print_info "설치 결과를 검증합니다."

if ! "$COMMAND_LINK" --version; then
    print_error "설치 후 dbf 실행 검증에 실패했습니다."
    cleanup_failed_install
    exit 1
fi


# ------------------------------------------------------------------------------
# ~/.local/bin이 PATH에 포함되어 있는지 확인한다.
#
# 초기 버전에서는 사용자의 ~/.zshrc 또는 ~/.bashrc를 자동 수정하지 않는다.
# 설정 파일을 임의로 변경하지 않고 필요한 명령만 안내한다.
# ------------------------------------------------------------------------------
PATH_NOTICE_REQUIRED=false

case ":$PATH:" in
    *":$COMMAND_DIR:"*)
        ;;
    *)
        PATH_NOTICE_REQUIRED=true
        ;;
esac


# ------------------------------------------------------------------------------
# 설치 완료 메시지
# ------------------------------------------------------------------------------
printf '\n'
print_info "DBFlow 설치가 완료되었습니다."
printf '프로그램 설치 경로: %s\n' "$INSTALL_DIR"
printf '사용자 데이터 경로: %s\n' "$USER_DATA_DIR"
printf '실행 명령 링크:     %s\n' "$COMMAND_LINK"

if [[ "$PATH_NOTICE_REQUIRED" == true ]]; then
    printf '\n'
    printf '~/.local/bin이 현재 PATH에 포함되어 있지 않습니다.\n'
    printf 'macOS 기본 zsh를 사용하는 경우 다음 내용을 ~/.zshrc에 추가하세요.\n'
    printf '\n'
    printf '  export PATH="$HOME/.local/bin:$PATH"\n'
    printf '\n'
    printf '적용 명령:\n'
    printf '\n'
    printf '  source ~/.zshrc\n'
fi

printf '\n'
printf '설치 확인:\n'
printf '\n'
printf '  dbf --version\n'
printf '\n'
