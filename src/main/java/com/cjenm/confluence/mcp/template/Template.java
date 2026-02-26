package com.cjenm.confluence.mcp.template;

/**
 * 마크다운 문서 템플릿.
 *
 * @param id          템플릿 고유 식별자
 * @param name        템플릿 이름
 * @param description 템플릿 설명
 * @param content     마크다운 콘텐츠
 */
public record Template(String id, String name, String description, String content) {
}
