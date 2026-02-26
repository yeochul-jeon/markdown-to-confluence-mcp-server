package com.cjenm.confluence.mcp.converter;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListItem;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;

/**
 * flexmark AST 노드를 Confluence Wiki Markup으로 렌더링하는 NodeRenderer 구현.
 * <p>
 * 인라인 코드는 {@code \0CS\0}...{@code \0CE\0} 플레이스홀더로 보호하며,
 * 후처리에서 {@code \{\{...\}\}}로 복원한다.
 */
public class ConfluenceNodeRenderer implements NodeRenderer {

    private static final String CODE_SPAN_START = "\0CS\0";
    private static final String CODE_SPAN_END = "\0CE\0";

    private final ConvertOptions options;

    public ConfluenceNodeRenderer(ConvertOptions options) {
        this.options = options;
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> handlers = new HashSet<>();

        // 인라인
        handlers.add(new NodeRenderingHandler<>(StrongEmphasis.class, this::renderStrong));
        handlers.add(new NodeRenderingHandler<>(Emphasis.class, this::renderEmphasis));
        handlers.add(new NodeRenderingHandler<>(Strikethrough.class, this::renderStrikethrough));
        handlers.add(new NodeRenderingHandler<>(Code.class, this::renderCode));
        handlers.add(new NodeRenderingHandler<>(Link.class, this::renderLink));
        handlers.add(new NodeRenderingHandler<>(AutoLink.class, this::renderAutoLink));
        handlers.add(new NodeRenderingHandler<>(MailLink.class, this::renderMailLink));
        handlers.add(new NodeRenderingHandler<>(Image.class, this::renderImage));
        handlers.add(new NodeRenderingHandler<>(SoftLineBreak.class, this::renderSoftLineBreak));
        handlers.add(new NodeRenderingHandler<>(HardLineBreak.class, this::renderHardLineBreak));
        handlers.add(new NodeRenderingHandler<>(Text.class, this::renderText));
        handlers.add(new NodeRenderingHandler<>(TextBase.class, this::renderTextBase));
        handlers.add(new NodeRenderingHandler<>(HtmlEntity.class, this::renderHtmlEntity));
        handlers.add(new NodeRenderingHandler<>(HtmlInline.class, this::renderHtmlInline));

        // 블록
        handlers.add(new NodeRenderingHandler<>(Heading.class, this::renderHeading));
        handlers.add(new NodeRenderingHandler<>(Paragraph.class, this::renderParagraph));
        handlers.add(new NodeRenderingHandler<>(BlockQuote.class, this::renderBlockQuote));
        handlers.add(new NodeRenderingHandler<>(FencedCodeBlock.class, this::renderFencedCodeBlock));
        handlers.add(new NodeRenderingHandler<>(IndentedCodeBlock.class, this::renderIndentedCodeBlock));
        handlers.add(new NodeRenderingHandler<>(ThematicBreak.class, this::renderThematicBreak));
        handlers.add(new NodeRenderingHandler<>(HtmlBlock.class, this::renderHtmlBlock));

        // 리스트
        handlers.add(new NodeRenderingHandler<>(BulletList.class, this::renderBulletList));
        handlers.add(new NodeRenderingHandler<>(OrderedList.class, this::renderOrderedList));
        handlers.add(new NodeRenderingHandler<>(BulletListItem.class, this::renderBulletListItem));
        handlers.add(new NodeRenderingHandler<>(OrderedListItem.class, this::renderOrderedListItem));
        handlers.add(new NodeRenderingHandler<>(TaskListItem.class, this::renderTaskListItem));

        // 테이블
        handlers.add(new NodeRenderingHandler<>(TableBlock.class, this::renderTableBlock));
        handlers.add(new NodeRenderingHandler<>(TableHead.class, this::renderTableHead));
        handlers.add(new NodeRenderingHandler<>(TableBody.class, this::renderTableBody));
        handlers.add(new NodeRenderingHandler<>(TableRow.class, this::renderTableRow));
        handlers.add(new NodeRenderingHandler<>(TableCell.class, this::renderTableCell));
        handlers.add(new NodeRenderingHandler<>(TableSeparator.class, this::renderTableSeparator));

        return handlers;
    }

    // ===== 인라인 렌더러 =====

    private void renderStrong(StrongEmphasis node, NodeRendererContext context, HtmlWriter html) {
        html.raw("*");
        context.renderChildren(node);
        html.raw("*");
    }

    private void renderEmphasis(Emphasis node, NodeRendererContext context, HtmlWriter html) {
        html.raw("_");
        context.renderChildren(node);
        html.raw("_");
    }

    private void renderStrikethrough(Strikethrough node, NodeRendererContext context, HtmlWriter html) {
        html.raw("-");
        context.renderChildren(node);
        html.raw("-");
    }

    private void renderCode(Code node, NodeRendererContext context, HtmlWriter html) {
        String text = node.getText().toString();
        String escaped = text
                .replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}");
        html.raw(CODE_SPAN_START + escaped + CODE_SPAN_END);
    }

    private void renderLink(Link node, NodeRendererContext context, HtmlWriter html) {
        String url = node.getUrl().toString();
        // 인라인 텍스트를 별도 버퍼로 렌더링
        String text = renderChildrenToString(node, context);
        if (!text.isEmpty() && !text.equals(url)) {
            html.raw("[" + text + "|" + url + "]");
        } else {
            html.raw("[" + url + "]");
        }
    }

    private void renderAutoLink(AutoLink node, NodeRendererContext context, HtmlWriter html) {
        String url = node.getText().toString();
        html.raw("[" + url + "]");
    }

    private void renderMailLink(MailLink node, NodeRendererContext context, HtmlWriter html) {
        String email = node.getText().toString();
        html.raw("[mailto:" + email + "]");
    }

    private void renderImage(Image node, NodeRendererContext context, HtmlWriter html) {
        String url = node.getUrl().toString();
        String alt = node.getText().toString();
        if (!alt.isEmpty()) {
            html.raw("!" + url + "|alt=" + alt + "!");
        } else {
            html.raw("!" + url + "!");
        }
    }

    private void renderSoftLineBreak(SoftLineBreak node, NodeRendererContext context, HtmlWriter html) {
        html.raw("\n");
    }

    private void renderHardLineBreak(HardLineBreak node, NodeRendererContext context, HtmlWriter html) {
        html.raw("\n");
    }

    private void renderText(Text node, NodeRendererContext context, HtmlWriter html) {
        String text = node.getChars().toString();
        text = text.replace("{", "\\{").replace("}", "\\}");
        html.raw(text);
    }

    private void renderTextBase(TextBase node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
    }

    private void renderHtmlEntity(HtmlEntity node, NodeRendererContext context, HtmlWriter html) {
        html.raw(node.getChars().toString());
    }

    private void renderHtmlInline(HtmlInline node, NodeRendererContext context, HtmlWriter html) {
        html.raw(node.getChars().toString());
    }

    // ===== 블록 렌더러 =====

    private void renderHeading(Heading node, NodeRendererContext context, HtmlWriter html) {
        html.raw("h" + node.getLevel() + ". ");
        context.renderChildren(node);
        html.raw("\n\n");
    }

    private void renderParagraph(Paragraph node, NodeRendererContext context, HtmlWriter html) {
        // 리스트 아이템 안의 첫 번째 Paragraph는 줄바꿈 없이 인라인으로 처리
        if (node.getParent() instanceof ListItem) {
            context.renderChildren(node);
            return;
        }
        context.renderChildren(node);
        html.raw("\n\n");
    }

    private void renderBlockQuote(BlockQuote node, NodeRendererContext context, HtmlWriter html) {
        html.raw("{quote}\n");
        context.renderChildren(node);
        html.raw("{quote}\n\n");
    }

    private void renderFencedCodeBlock(FencedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        String lang = node.getInfo().toString().trim();
        String text = node.getContentChars().toString();
        // 마지막 줄바꿈 제거
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        var params = new StringJoiner("|");
        boolean isMermaid = "mermaid".equals(lang);
        if (!lang.isEmpty()) {
            params.add("language=" + (isMermaid ? "text" : lang));
        }
        if (isMermaid) {
            params.add("title=mermaid");
            params.add("collapse=true");
        }
        if (options.theme() != null && !options.theme().isEmpty()) {
            params.add("theme=" + options.theme());
        }

        String paramStr = params.length() > 0 ? ":" + params : "";
        html.raw("{code" + paramStr + "}\n" + text + "\n{code}\n\n");
    }

    private void renderIndentedCodeBlock(IndentedCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        String text = node.getContentChars().toString();
        if (text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        String paramStr = "";
        if (options.theme() != null && !options.theme().isEmpty()) {
            paramStr = ":theme=" + options.theme();
        }
        html.raw("{code" + paramStr + "}\n" + text + "\n{code}\n\n");
    }

    private void renderThematicBreak(ThematicBreak node, NodeRendererContext context, HtmlWriter html) {
        html.raw("----\n\n");
    }

    private void renderHtmlBlock(HtmlBlock node, NodeRendererContext context, HtmlWriter html) {
        html.raw(node.getChars().toString());
    }

    // ===== 리스트 렌더러 =====

    private void renderBulletList(BulletList node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
        // 최상위 리스트인 경우 뒤에 빈 줄 추가
        if (!(node.getParent() instanceof ListItem)) {
            html.raw("\n");
        }
    }

    private void renderOrderedList(OrderedList node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
        if (!(node.getParent() instanceof ListItem)) {
            html.raw("\n");
        }
    }

    private void renderBulletListItem(BulletListItem node, NodeRendererContext context, HtmlWriter html) {
        String prefix = computeListPrefix(node, '*');
        html.raw(prefix + " ");
        renderListItemContent(node, context, html);
        html.raw("\n");
    }

    private void renderOrderedListItem(OrderedListItem node, NodeRendererContext context, HtmlWriter html) {
        String prefix = computeListPrefix(node, '#');
        html.raw(prefix + " ");
        renderListItemContent(node, context, html);
        html.raw("\n");
    }

    private void renderTaskListItem(TaskListItem node, NodeRendererContext context, HtmlWriter html) {
        String prefix = computeListPrefix(node, '*');
        String marker = node.isItemDoneMarker() ? "(/) " : "(x) ";
        html.raw(prefix + " " + marker);
        renderListItemContent(node, context, html);
        html.raw("\n");
    }

    /**
     * 리스트 중첩 깊이에 따라 접두사 문자열을 계산한다.
     * 예: 1단계 '*', 2단계 '**', 혼합 시 '*#' 등
     */
    private String computeListPrefix(ListItem node, char marker) {
        var sb = new StringBuilder();
        // 현재 노드에서 부모를 따라 올라가며 접두사를 역순으로 구성
        Node current = node.getParent(); // ListBlock (BulletList or OrderedList)
        while (current != null) {
            if (current instanceof BulletList) {
                sb.insert(0, '*');
                // 부모가 ListItem이면 계속 올라감
                current = current.getParent();
                if (current instanceof ListItem) {
                    current = current.getParent();
                } else {
                    break;
                }
            } else if (current instanceof OrderedList) {
                sb.insert(0, '#');
                current = current.getParent();
                if (current instanceof ListItem) {
                    current = current.getParent();
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        // sb가 비어있으면 기본 마커 사용
        return sb.isEmpty() ? String.valueOf(marker) : sb.toString();
    }

    /**
     * 리스트 아이템의 콘텐츠를 렌더링한다. (하위 리스트 제외한 인라인 텍스트만)
     */
    private void renderListItemContent(ListItem node, NodeRendererContext context, HtmlWriter html) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof BulletList || child instanceof OrderedList) {
                // 하위 리스트는 별도 줄에서 렌더링되므로 여기서는 줄바꿈 후 자식으로 위임
                html.raw("\n");
                context.render(child);
            } else {
                context.render(child);
            }
        }
    }

    // ===== 테이블 렌더러 =====

    private boolean isHeaderRow = false;

    private void renderTableBlock(TableBlock node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
        html.raw("\n");
    }

    private void renderTableHead(TableHead node, NodeRendererContext context, HtmlWriter html) {
        isHeaderRow = true;
        context.renderChildren(node);
        isHeaderRow = false;
    }

    private void renderTableBody(TableBody node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
    }

    private void renderTableRow(TableRow node, NodeRendererContext context, HtmlWriter html) {
        if (isHeaderRow) {
            html.raw("|| ");
        } else {
            html.raw("| ");
        }
        context.renderChildren(node);
        html.raw("\n");
    }

    private void renderTableCell(TableCell node, NodeRendererContext context, HtmlWriter html) {
        context.renderChildren(node);
        if (node.getNext() != null) {
            if (isHeaderRow) {
                html.raw(" || ");
            } else {
                html.raw(" | ");
            }
        } else {
            // 마지막 셀
            if (isHeaderRow) {
                html.raw(" ||");
            } else {
                html.raw(" |");
            }
        }
    }

    private void renderTableSeparator(TableSeparator node, NodeRendererContext context, HtmlWriter html) {
        // Confluence에서는 테이블 구분선을 렌더링하지 않음
    }

    // ===== 유틸리티 =====

    /**
     * 자식 노드를 별도 버퍼에 렌더링하여 문자열로 반환한다.
     */
    private String renderChildrenToString(Node node, NodeRendererContext context) {
        // HtmlWriter의 raw 출력을 직접 캡처할 수 없으므로,
        // 자식 텍스트 노드를 직접 순회하여 구성한다.
        var sb = new StringBuilder();
        collectInlineText(node, sb, context);
        return sb.toString();
    }

    private void collectInlineText(Node node, StringBuilder sb, NodeRendererContext context) {
        for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
            if (child instanceof Text) {
                String text = child.getChars().toString();
                sb.append(text.replace("{", "\\{").replace("}", "\\}"));
            } else if (child instanceof Code) {
                String text = ((Code) child).getText().toString()
                        .replace("\\", "\\\\")
                        .replace("{", "\\{")
                        .replace("}", "\\}");
                sb.append(CODE_SPAN_START).append(text).append(CODE_SPAN_END);
            } else if (child instanceof StrongEmphasis) {
                sb.append("*");
                collectInlineText(child, sb, context);
                sb.append("*");
            } else if (child instanceof Emphasis) {
                sb.append("_");
                collectInlineText(child, sb, context);
                sb.append("_");
            } else if (child instanceof Strikethrough) {
                sb.append("-");
                collectInlineText(child, sb, context);
                sb.append("-");
            } else if (child instanceof SoftLineBreak || child instanceof HardLineBreak) {
                sb.append("\n");
            } else if (child instanceof HtmlEntity) {
                sb.append(child.getChars().toString());
            } else {
                collectInlineText(child, sb, context);
            }
        }
    }

    /**
     * NodeRendererFactory 구현.
     */
    public static class Factory implements NodeRendererFactory {
        private final ConvertOptions options;

        public Factory(ConvertOptions options) {
            this.options = options;
        }

        @Override
        public NodeRenderer apply(DataHolder dataHolder) {
            return new ConfluenceNodeRenderer(options);
        }
    }
}
