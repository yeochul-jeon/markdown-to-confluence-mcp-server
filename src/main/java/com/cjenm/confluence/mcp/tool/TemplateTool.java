package com.cjenm.confluence.mcp.tool;

import java.util.List;

import com.cjenm.confluence.mcp.converter.ConfluenceConverter;
import com.cjenm.confluence.mcp.converter.ConvertOptions;
import com.cjenm.confluence.mcp.template.Template;
import com.cjenm.confluence.mcp.template.TemplateRepository;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * 마크다운 문서 템플릿 관리 MCP 도구.
 */
@Service
public class TemplateTool {

    private final TemplateRepository templateRepository;
    private final ConfluenceConverter converter;

    public TemplateTool(TemplateRepository templateRepository, ConfluenceConverter converter) {
        this.templateRepository = templateRepository;
        this.converter = converter;
    }

    public record TemplateInfo(String id, String name, String description) {
    }

    public record TemplateDetail(String id, String name, String description, String content) {
    }

    public record ConvertedTemplate(String id, String name, String confluenceMarkup) {
    }

    @Tool(description = "사용 가능한 마크다운 문서 템플릿 목록을 반환합니다. " +
            "기본 문서, 테이블 문서, API 문서, 회의록 템플릿을 제공합니다.")
    public List<TemplateInfo> listTemplates() {
        return templateRepository.findAll().stream()
                .map(t -> new TemplateInfo(t.id(), t.name(), t.description()))
                .toList();
    }

    @Tool(description = "지정한 ID의 마크다운 문서 템플릿 상세 내용을 반환합니다.")
    public TemplateDetail getTemplate(
            @ToolParam(description = "템플릿 ID (basic-doc, table-doc, api-doc, meeting-note)") String templateId) {

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 템플릿 ID: " + templateId));
        return new TemplateDetail(template.id(), template.name(), template.description(), template.content());
    }

    @Tool(description = "지정한 ID의 템플릿을 Confluence 위키 마크업으로 변환하여 반환합니다.")
    public ConvertedTemplate convertTemplate(
            @ToolParam(description = "템플릿 ID (basic-doc, table-doc, api-doc, meeting-note)") String templateId,
            @ToolParam(description = "코드 블록 테마 (DJango, Emacs, FadeToGrey, Midnight, RDark, Eclipse, Confluence). 생략 시 테마 미적용", required = false) @Nullable String theme) {

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 템플릿 ID: " + templateId));

        var options = (theme != null && !theme.isBlank())
                ? ConvertOptions.withTheme(theme)
                : ConvertOptions.defaults();

        String markup = converter.convert(template.content(), options);
        return new ConvertedTemplate(template.id(), template.name(), markup);
    }
}
