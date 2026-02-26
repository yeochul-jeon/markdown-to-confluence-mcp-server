# Markdown to Confluence MCP Server

Markdown 텍스트를 Confluence Wiki Markup으로 변환하는 Spring AI MCP Server입니다.

## 주요 기능

- **Markdown → Confluence Wiki Markup 변환** — GFM 테이블, 취소선, 체크박스 등 확장 문법 지원
- **코드 블록 테마** — DJango, Emacs, Eclipse 등 7종 테마 적용
- **내장 문서 템플릿 4종** — 기본 문서, 테이블 문서, API 문서, 회의록
- **HTTP(SSE) / stdio 트랜스포트** — Claude Desktop 등 다양한 MCP 클라이언트와 연동 가능

## 기술 스택

| 기술 | 버전 |
|------|------|
| Java | 17 |
| Spring Boot | 4.0.0 |
| Spring AI | 2.0.0-SNAPSHOT |
| flexmark | 0.64.8 |

## MCP Tools

### convertMarkdown

마크다운 텍스트를 Confluence 위키 마크업으로 변환합니다.

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `markdown` | String | Y | 변환할 마크다운 텍스트 |
| `theme` | String | N | 코드 블록 테마. 생략 시 테마 미적용 |

### listTemplates

사용 가능한 마크다운 문서 템플릿 목록을 반환합니다.

| 템플릿 ID | 이름 | 설명 |
|-----------|------|------|
| `basic-doc` | 기본 문서 | 제목, 단락, 리스트, 코드 블록이 포함된 기본 문서 구조 |
| `table-doc` | 테이블 문서 | 테이블을 활용한 데이터 정리 문서 |
| `api-doc` | API 문서 | REST API 엔드포인트 문서 템플릿 |
| `meeting-note` | 회의록 | 회의 내용, 결정 사항, 액션 아이템 기록용 |

### getTemplate

지정한 ID의 마크다운 문서 템플릿 상세 내용을 반환합니다.

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `templateId` | String | Y | 템플릿 ID (`basic-doc`, `table-doc`, `api-doc`, `meeting-note`) |

### convertTemplate

지정한 ID의 템플릿을 Confluence 위키 마크업으로 변환하여 반환합니다.

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `templateId` | String | Y | 템플릿 ID |
| `theme` | String | N | 코드 블록 테마. 생략 시 테마 미적용 |

## 빌드 및 실행

### 빌드

```bash
./gradlew build
```

### HTTP(SSE) 모드

```bash
java -jar build/libs/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar
```

기본 포트 `8080`에서 SSE 엔드포인트가 활성화됩니다.

### stdio 모드

```bash
java -Dspring.profiles.active=stdio -jar build/libs/markdown-to-confluence-mcp-server-0.0.1-SNAPSHOT.jar
```

## Claude Desktop 설정

`claude_desktop_config.json`에 아래와 같이 추가합니다.

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

## 지원하는 Markdown 요소

### 인라인

- **볼드** (`**text**`)
- *이탤릭* (`*text*`)
- ~~취소선~~ (`~~text~~`)
- `인라인 코드`
- [링크](url) (`[text](url)`)
- 이미지 (`![alt](url)`)

### 블록

- 제목 (h1 ~ h6)
- 인용문 (`>`)
- 코드 블록 (``` ```)
- 수평선 (`---`)

### 리스트

- 순서 없는 리스트 (`-`, `*`)
- 순서 있는 리스트 (`1.`, `2.`)
- 중첩 리스트
- 체크박스 (`- [ ]`, `- [x]`)

### 테이블

- GFM 테이블 (`| col1 | col2 |`)

## 코드 블록 테마

`convertMarkdown` 또는 `convertTemplate`의 `theme` 파라미터에 아래 값을 지정할 수 있습니다.

| 테마 | 설명 |
|------|------|
| `DJango` | DJango 스타일 |
| `Emacs` | Emacs 스타일 |
| `FadeToGrey` | FadeToGrey 스타일 |
| `Midnight` | Midnight 다크 스타일 |
| `RDark` | RDark 다크 스타일 |
| `Eclipse` | Eclipse 스타일 |
| `Confluence` | Confluence 기본 스타일 |

테마를 생략하면 Confluence 코드 매크로에 테마가 적용되지 않습니다.
