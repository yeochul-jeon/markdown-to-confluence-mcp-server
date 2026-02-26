package com.cjenm.confluence.mcp.tool;

import com.cjenm.confluence.mcp.converter.ConfluenceConverter;
import com.cjenm.confluence.mcp.converter.ConvertOptions;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 마크다운 → Confluence 위키 마크업 변환 MCP 도구.
 */
@Service
public class ConverterTool {

    private final ConfluenceConverter converter;

    public ConverterTool(ConfluenceConverter converter) {
        this.converter = converter;
    }

    @Tool(description = "마크다운 텍스트를 Confluence 위키 마크업으로 변환합니다. " +
            "제목, 볼드, 이탤릭, 취소선, 코드 블록, 링크, 이미지, 인용문, 리스트, 테이블 등을 지원합니다.")
    public String convertMarkdown(
            @ToolParam(description = "변환할 마크다운 텍스트") String markdown,
            @ToolParam(description = "코드 블록 테마 (DJango, Emacs, FadeToGrey, Midnight, RDark, Eclipse, Confluence). 생략 시 테마 미적용", required = false) @Nullable String theme) {

        var options = (theme != null && !theme.isBlank())
                ? ConvertOptions.withTheme(theme)
                : ConvertOptions.defaults();
        return converter.convert(markdown, options);
    }
}
