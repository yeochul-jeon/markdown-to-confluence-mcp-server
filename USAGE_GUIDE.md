# 사용법 및 AI 클라이언트 연동 가이드

Markdown to Confluence MCP Server를 다양한 AI 클라이언트에서 사용하기 위한 연동 설정 가이드입니다.

이 MCP 서버는 두 가지 트랜스포트 모드를 지원합니다:

| 모드 | 설명 | 용도 |
|------|------|------|
| **HTTP(SSE)** | HTTP 서버로 실행, SSE 스트리밍 | 원격 접속, 여러 클라이언트 동시 연결 |
| **stdio** | 표준 입출력 파이프 | Claude Desktop 등 로컬 프로세스 연동 |

---

## 사전 준비

### 1. Java 17 이상 설치

```bash
java -version
```

Java 17 이상이 설치되어 있어야 합니다. 설치되어 있지 않다면 [Adoptium](https://adoptium.net/) 또는 [SDKMAN](https://sdkman.io/)을 통해 설치하세요.

### 2. JAR 빌드

```bash
cd mcp-server
./gradlew build
```

### 3. JAR 경로 확인

빌드 완료 후 JAR 파일 경로를 확인합니다:

```bash
ls build/libs/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar
```

이후 설정에서 이 경로의 **절대 경로**를 사용합니다. 예시:

```
/Users/사용자/project/markdown-to-confluence-converter/mcp-server/build/libs/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar
```

> **참고:** 아래 설정 예시에서 `/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar` 부분을 실제 JAR 절대 경로로 교체하세요.

---

## 실행 모드

### HTTP(SSE) 모드

기본 실행 모드입니다. 포트 `8080`에서 SSE 엔드포인트가 활성화됩니다.

```bash
java -jar build/libs/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar
```

- 엔드포인트: `http://localhost:8080/mcp/sse`
- 여러 클라이언트가 동시에 연결 가능
- 원격 서버에서 실행하여 네트워크를 통한 접근 가능

### stdio 모드

`stdio` 프로파일을 활성화하여 표준 입출력 기반으로 동작합니다.

```bash
java -Dspring.profiles.active=stdio -jar build/libs/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar
```

- Claude Desktop 등 로컬 프로세스와 직접 연동
- 단일 클라이언트 전용

---

## AI 클라이언트별 연동 설정

### Claude Desktop

**설정 파일 경로:**

| OS | 경로 |
|----|------|
| macOS | `~/Library/Application Support/Claude/claude_desktop_config.json` |
| Windows | `%APPDATA%\Claude\claude_desktop_config.json` |

**stdio 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**SSE 설정 (mcp-remote 사용):**

Claude Desktop은 SSE 트랜스포트를 직접 지원하지 않습니다. SSE로 연결하려면 `mcp-remote` 래퍼를 사용하세요:

```bash
npm install -g mcp-remote
```

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "command": "npx",
      "args": [
        "mcp-remote",
        "http://localhost:8080/mcp/sse"
      ]
    }
  }
}
```

> **주의:** 설정 변경 후 Claude Desktop을 재시작해야 적용됩니다.

---

### Claude Code (CLI)

**명령어로 추가:**

stdio 모드:

```bash
claude mcp add markdown-to-confluence -- java -Dspring.profiles.active=stdio -jar /absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar
```

HTTP 모드:

```bash
claude mcp add --transport http markdown-to-confluence http://localhost:8080/mcp/sse
```

**`.mcp.json` 파일 (프로젝트 공유용):**

프로젝트 루트에 `.mcp.json` 파일을 생성하면 팀원과 설정을 공유할 수 있습니다:

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

---

### Gemini CLI

**설정 파일 경로:** `~/.gemini/settings.json`

**stdio 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**HTTP 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "httpUrl": "http://localhost:8080/mcp/sse"
    }
  }
}
```

> **주의:** Gemini CLI는 HTTP 연결 시 `url`이 아닌 `httpUrl` 키를 사용합니다.

---

### Cursor

**설정 파일 경로:** 프로젝트 루트의 `.cursor/mcp.json`

**stdio 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**SSE 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "type": "sse",
      "url": "http://localhost:8080/mcp/sse"
    }
  }
}
```

> **주의:** Cursor의 SSE 설정에는 `"type": "sse"` 필드가 반드시 필요합니다.

---

### VS Code (GitHub Copilot)

**설정 파일 경로:** 프로젝트 루트의 `.vscode/mcp.json`

> **주의:** VS Code는 다른 클라이언트의 `mcpServers`와 달리 **`servers`** 키를 사용합니다.

**stdio 설정:**

```json
{
  "servers": {
    "markdown-to-confluence": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**HTTP 설정:**

```json
{
  "servers": {
    "markdown-to-confluence": {
      "type": "http",
      "url": "http://localhost:8080/mcp/sse"
    }
  }
}
```

---

### JetBrains IDE (IntelliJ IDEA, WebStorm 등)

**설정 방법:** `Settings` > `Tools` > `AI Assistant` > `MCP` 에서 설정합니다.

**stdio 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**HTTP 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "url": "http://localhost:8080/mcp/sse"
    }
  }
}
```

---

### Cline

**설정 파일 경로:**

| OS | 경로 |
|----|------|
| macOS | `~/Library/Application Support/Code/User/globalStorage/saoudrizwan.claude-dev/settings/cline_mcp_settings.json` |
| Windows | `%APPDATA%\Code\User\globalStorage\saoudrizwan.claude-dev\settings\cline_mcp_settings.json` |

**stdio 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

**SSE 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "url": "http://localhost:8080/mcp/sse"
    }
  }
}
```

---

### Windsurf

**설정 파일 경로:** `~/.codeium/windsurf/mcp_config.json`

**stdio 설정:**

```json
{
  "mcpServers": {
    "markdown-to-confluence": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/absolute/path/to/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

---

## 클라이언트별 설정 비교표

| 클라이언트 | 설정 파일 | 루트 키 | stdio | SSE/HTTP |
|-----------|----------|---------|:-----:|:--------:|
| Claude Desktop | `claude_desktop_config.json` | `mcpServers` | O | △ (mcp-remote) |
| Claude Code | `.mcp.json` 또는 CLI | `mcpServers` | O | O |
| Gemini CLI | `~/.gemini/settings.json` | `mcpServers` | O | O (`httpUrl`) |
| Cursor | `.cursor/mcp.json` | `mcpServers` | O | O (`type` 필수) |
| VS Code | `.vscode/mcp.json` | **`servers`** | O | O |
| JetBrains | Settings UI | `mcpServers` | O | O |
| Cline | `cline_mcp_settings.json` | `mcpServers` | O | O |
| Windsurf | `mcp_config.json` | `mcpServers` | O | - |

---

## 사용 예시

MCP 서버가 연결되면 AI 클라이언트에서 다음과 같은 작업을 요청할 수 있습니다.

### convertMarkdown — 마크다운 변환

> "아래 마크다운을 Confluence 위키 마크업으로 변환해줘"

```
# 제목
- 항목 1
- 항목 2

| 이름 | 역할 |
|------|------|
| 홍길동 | 개발자 |
```

변환 결과:

```
h1. 제목
* 항목 1
* 항목 2

|| 이름 || 역할 ||
| 홍길동 | 개발자 |
```

### convertMarkdown (테마 적용) — 코드 블록 테마 지정

> "이 마크다운을 Eclipse 테마로 변환해줘"

코드 블록이 포함된 마크다운에 테마를 적용하면, Confluence 코드 매크로에 해당 테마가 설정됩니다.

### listTemplates — 템플릿 목록 조회

> "사용 가능한 템플릿 목록을 보여줘"

4종의 내장 템플릿(`basic-doc`, `table-doc`, `api-doc`, `meeting-note`)을 확인할 수 있습니다.

### getTemplate — 템플릿 내용 조회

> "API 문서 템플릿 내용을 보여줘"

`api-doc` 템플릿의 마크다운 원본을 확인할 수 있습니다.

### convertTemplate — 템플릿 변환

> "회의록 템플릿을 Confluence 마크업으로 변환해줘"

지정한 템플릿을 바로 Confluence 위키 마크업으로 변환하여 반환합니다.

---

## 문제 해결 (Troubleshooting)

### Java 미설치 또는 버전 불일치

```
Error: java: command not found
```

또는 Java 17 미만 버전인 경우:

```
UnsupportedClassVersionError
```

**해결:** Java 17 이상을 설치하고 `java -version`으로 확인하세요.

### JAR 경로 오류

```
Error: Unable to access jarfile
```

**해결:** 설정 파일의 JAR 경로가 올바른 절대 경로인지 확인하세요. 상대 경로는 클라이언트에 따라 동작하지 않을 수 있습니다.

### 포트 충돌 (HTTP 모드)

```
Web server failed to start. Port 8080 was already in use.
```

**해결:** 이미 8080 포트를 사용하는 프로세스를 종료하거나, 다른 포트로 실행하세요:

```bash
java -Dserver.port=9090 -jar build/libs/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar
```

포트를 변경한 경우 클라이언트 설정의 URL도 함께 수정해야 합니다.

### 설정 변경 후 반영되지 않음

대부분의 AI 클라이언트는 설정 파일 변경 후 **재시작**이 필요합니다.

- **Claude Desktop:** 앱 완전 종료 후 재실행
- **Cursor / VS Code:** 에디터 재시작 또는 MCP 서버 새로고침
- **Claude Code:** 세션 재시작 (`/mcp` 명령으로 연결 상태 확인 가능)

### 로그 확인

문제 진단이 필요한 경우, HTTP 모드로 실행하면 서버 로그를 직접 확인할 수 있습니다:

```bash
java -jar build/libs/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar 2>&1 | tee server.log
```
