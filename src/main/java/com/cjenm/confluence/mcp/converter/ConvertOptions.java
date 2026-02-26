package com.cjenm.confluence.mcp.converter;

/**
 * 변환 옵션 레코드.
 *
 * @param theme 코드 블록 테마 (예: DJango, Emacs, Midnight 등). null이면 테마 미적용
 */
public record ConvertOptions(String theme) {

    public static ConvertOptions defaults() {
        return new ConvertOptions(null);
    }

    public static ConvertOptions withTheme(String theme) {
        return new ConvertOptions(theme);
    }
}
