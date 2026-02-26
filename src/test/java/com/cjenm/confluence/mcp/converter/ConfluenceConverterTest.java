package com.cjenm.confluence.mcp.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfluenceConverterTest {

    private ConfluenceConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ConfluenceConverter();
    }

    @Nested
    @DisplayName("인라인 변환")
    class InlineTests {

        @Test
        @DisplayName("볼드 텍스트")
        void bold() {
            String result = converter.convert("**bold text**");
            assertThat(result).contains("*bold text*");
            assertThat(result).doesNotContain("**");
        }

        @Test
        @DisplayName("이탤릭 텍스트")
        void italic() {
            String result = converter.convert("*italic text*");
            assertThat(result).contains("_italic text_");
        }

        @Test
        @DisplayName("취소선 텍스트")
        void strikethrough() {
            String result = converter.convert("~~strikethrough~~");
            assertThat(result).contains("-strikethrough-");
        }

        @Test
        @DisplayName("인라인 코드")
        void inlineCode() {
            String result = converter.convert("Use `System.out.println()` here");
            assertThat(result).contains("{{System.out.println()}}");
        }

        @Test
        @DisplayName("인라인 코드 내 중괄호 이스케이프")
        void inlineCodeWithBraces() {
            String result = converter.convert("Use `Map<String, Object>` here");
            assertThat(result).contains("{{Map<String, Object>}}");
        }

        @Test
        @DisplayName("링크 - 텍스트와 URL")
        void linkWithText() {
            String result = converter.convert("[공식 문서](https://example.com)");
            assertThat(result).contains("[공식 문서|https://example.com]");
        }

        @Test
        @DisplayName("이미지")
        void image() {
            String result = converter.convert("![alt text](https://example.com/image.png)");
            assertThat(result).contains("!https://example.com/image.png|alt=alt text!");
        }

        @Test
        @DisplayName("텍스트 내 중괄호 이스케이프")
        void braceEscape() {
            String result = converter.convert("JSON format: {key: value}");
            assertThat(result).contains("\\{key: value\\}");
        }
    }

    @Nested
    @DisplayName("블록 변환")
    class BlockTests {

        @Test
        @DisplayName("제목 h1~h6")
        void headings() {
            assertThat(converter.convert("# H1")).contains("h1. H1");
            assertThat(converter.convert("## H2")).contains("h2. H2");
            assertThat(converter.convert("### H3")).contains("h3. H3");
            assertThat(converter.convert("#### H4")).contains("h4. H4");
            assertThat(converter.convert("##### H5")).contains("h5. H5");
            assertThat(converter.convert("###### H6")).contains("h6. H6");
        }

        @Test
        @DisplayName("인용문")
        void blockquote() {
            String result = converter.convert("> This is a quote");
            assertThat(result).contains("{quote}");
            assertThat(result).contains("This is a quote");
            assertThat(result).contains("{quote}");
        }

        @Test
        @DisplayName("코드 블록 - 언어 지정")
        void fencedCodeBlock() {
            String result = converter.convert("""
                    ```java
                    public class Main {}
                    ```""");
            assertThat(result).contains("{code:language=java}");
            assertThat(result).contains("public class Main {}");
            assertThat(result).contains("{code}");
        }

        @Test
        @DisplayName("코드 블록 - 테마 적용")
        void fencedCodeBlockWithTheme() {
            String result = converter.convert("""
                    ```java
                    int x = 1;
                    ```""", ConvertOptions.withTheme("Midnight"));
            assertThat(result).contains("{code:language=java|theme=Midnight}");
        }

        @Test
        @DisplayName("코드 블록 - mermaid 특수 처리")
        void mermaidCodeBlock() {
            String result = converter.convert("""
                    ```mermaid
                    graph TD
                    A --> B
                    ```""");
            assertThat(result).contains("{code:language=text|title=mermaid|collapse=true}");
        }

        @Test
        @DisplayName("수평선")
        void thematicBreak() {
            String result = converter.convert("---");
            assertThat(result).contains("----");
        }
    }

    @Nested
    @DisplayName("리스트 변환")
    class ListTests {

        @Test
        @DisplayName("순서 없는 리스트")
        void bulletList() {
            String result = converter.convert("""
                    - item 1
                    - item 2
                    - item 3""");
            assertThat(result).contains("* item 1");
            assertThat(result).contains("* item 2");
            assertThat(result).contains("* item 3");
        }

        @Test
        @DisplayName("순서 있는 리스트")
        void orderedList() {
            String result = converter.convert("""
                    1. first
                    2. second
                    3. third""");
            assertThat(result).contains("# first");
            assertThat(result).contains("# second");
            assertThat(result).contains("# third");
        }

        @Test
        @DisplayName("중첩 리스트")
        void nestedList() {
            String result = converter.convert("""
                    - parent 1
                      - child 1
                      - child 2
                    - parent 2""");
            assertThat(result).contains("* parent 1");
            assertThat(result).contains("** child 1");
            assertThat(result).contains("** child 2");
            assertThat(result).contains("* parent 2");
        }

        @Test
        @DisplayName("체크박스 리스트")
        void taskList() {
            String result = converter.convert("""
                    - [ ] 미완료 항목
                    - [x] 완료 항목""");
            assertThat(result).contains("* (x) 미완료 항목");
            assertThat(result).contains("* (/) 완료 항목");
        }
    }

    @Nested
    @DisplayName("테이블 변환")
    class TableTests {

        @Test
        @DisplayName("기본 테이블")
        void basicTable() {
            String result = converter.convert("""
                    | Name | Age |
                    |------|-----|
                    | Alice | 30 |
                    | Bob | 25 |""");
            assertThat(result).contains("|| Name || Age ||");
            assertThat(result).contains("| Alice | 30 |");
            assertThat(result).contains("| Bob | 25 |");
        }
    }

    @Nested
    @DisplayName("후처리")
    class PostProcessTests {

        @Test
        @DisplayName("빈 입력")
        void emptyInput() {
            assertThat(converter.convert("")).isEmpty();
            assertThat(converter.convert("   ")).isEmpty();
            assertThat(converter.convert(null)).isEmpty();
        }

        @Test
        @DisplayName("연속 빈 줄 축소")
        void excessNewlines() {
            String result = converter.convert("paragraph 1\n\n\n\n\nparagraph 2");
            // 3개 이상 연속 빈 줄이 2개로 축소되는지 확인
            assertThat(result).doesNotContain("\n\n\n");
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTests {

        @Test
        @DisplayName("기본 문서 템플릿 변환")
        void basicDocumentConversion() {
            String markdown = """
                    # 문서 제목

                    ## 개요

                    이 문서는 프로젝트의 **주요 기능**과 _설계 원칙_을 설명합니다.

                    ## 주요 기능

                    - 실시간 데이터 처리
                    - 자동 알림 시스템

                    ## 설치 방법

                    1. 저장소를 클론합니다
                    2. 의존성을 설치합니다

                    ```bash
                    git clone https://github.com/example/project.git
                    npm install
                    ```

                    > 자세한 내용은 [공식 문서](https://example.com)를 참조하세요.

                    ---

                    *마지막 업데이트: 2024-01-15*""";

            String result = converter.convert(markdown);

            assertThat(result)
                    .contains("h1. 문서 제목")
                    .contains("h2. 개요")
                    .contains("*주요 기능*")
                    .contains("_설계 원칙_")
                    .contains("* 실시간 데이터 처리")
                    .contains("# 저장소를 클론합니다")
                    .contains("{code:language=bash}")
                    .contains("[공식 문서|https://example.com]")
                    .contains("{quote}")
                    .contains("----")
                    .contains("_마지막 업데이트: 2024-01-15_");
        }
    }
}
