package com.cjenm.confluence.mcp.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.data.MutableDataSet;

import org.springframework.stereotype.Component;

/**
 * 마크다운 → Confluence Wiki Markup 변환 엔진.
 * <p>
 * flexmark-java를 사용하여 마크다운을 파싱하고, {@link ConfluenceNodeRenderer}를 통해
 * Confluence 위키 마크업으로 변환한다.
 */
@Component
public class ConfluenceConverter {

    private static final Pattern CODE_BLOCK_PATTERN =
            Pattern.compile("\\{code(?::[^}]*)?\\}[\\s\\S]*?\\{code\\}");
    private static final Pattern RESIDUAL_BOLD_PATTERN =
            Pattern.compile("\\*\\*(.+?)\\*\\*");
    private static final Pattern EXCESS_NEWLINES_PATTERN =
            Pattern.compile("\n{3,}");

    /**
     * 마크다운 텍스트를 Confluence 위키 마크업으로 변환한다.
     *
     * @param markdown 변환할 마크다운 텍스트
     * @param options  변환 옵션
     * @return Confluence 위키 마크업 문자열
     */
    public String convert(String markdown, ConvertOptions options) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        var dataSet = new MutableDataSet();
        dataSet.set(Parser.EXTENSIONS, List.of(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListExtension.create()
        ));

        Parser parser = Parser.builder(dataSet).build();
        Document document = parser.parse(markdown);

        HtmlRenderer renderer = HtmlRenderer.builder(dataSet)
                .nodeRendererFactory(new ConfluenceNodeRenderer.Factory(options))
                .build();

        String raw = renderer.render(document);
        return postProcess(raw);
    }

    /**
     * 기본 옵션으로 변환한다.
     */
    public String convert(String markdown) {
        return convert(markdown, ConvertOptions.defaults());
    }

    /**
     * 후처리: {code} 블록 보호, 잔여 bold 변환, 빈 줄 정리, 인라인 코드 복원
     */
    private String postProcess(String text) {
        // 1. {code} 블록을 플레이스홀더로 보호
        List<String> codeBlocks = new ArrayList<>();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            codeBlocks.add(matcher.group());
            matcher.appendReplacement(sb, "\0CODE_BLOCK_" + (codeBlocks.size() - 1) + "\0");
        }
        matcher.appendTail(sb);
        String processed = sb.toString();

        // 2. 잔여 **bold** → *bold* 변환
        processed = RESIDUAL_BOLD_PATTERN.matcher(processed).replaceAll("*$1*");

        // 3. 연속 3개 이상 빈 줄 → 2개로 축소
        processed = EXCESS_NEWLINES_PATTERN.matcher(processed).replaceAll("\n\n");

        // 4. {code} 블록 복원
        for (int i = 0; i < codeBlocks.size(); i++) {
            processed = processed.replace("\0CODE_BLOCK_" + i + "\0", codeBlocks.get(i));
        }

        // 5. 인라인 코드 플레이스홀더 → {{}} 복원
        processed = processed.replace("\0CS\0", "{{").replace("\0CE\0", "}}");

        return processed.strip() + "\n";
    }
}
