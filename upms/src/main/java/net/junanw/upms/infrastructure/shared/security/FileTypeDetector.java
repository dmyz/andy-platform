package net.junanw.upms.infrastructure.shared.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 文件类型检测工具。
 *
 * <p>基于文件头和保守文本判断识别允许上传的文件类型，不依赖第三方库。
 */
public final class FileTypeDetector {

    private static final Map<String, List<String>> EXTENSIONS_BY_MIME = Map.of(
            "image/png", List.of("png"),
            "image/jpeg", List.of("jpg", "jpeg"),
            "image/webp", List.of("webp"),
            "application/pdf", List.of("pdf"),
            "text/plain", List.of("txt")
    );

    private FileTypeDetector() {
    }

    /**
     * 检测文件实际 MIME。
     *
     * @param content 文件内容
     * @return 检测结果；无法识别时返回 application/octet-stream
     */
    public static String detectMimeType(byte[] content) {
        if (content == null || content.length == 0) {
            return "application/octet-stream";
        }
        if (startsWith(content, new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'})) {
            return "image/png";
        }
        if (startsWith(content, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return "image/jpeg";
        }
        if (content.length >= 12
                && content[0] == 'R' && content[1] == 'I' && content[2] == 'F' && content[3] == 'F'
                && content[8] == 'W' && content[9] == 'E' && content[10] == 'B' && content[11] == 'P') {
            return "image/webp";
        }
        if (startsWith(content, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
            return "application/pdf";
        }
        if (isPlainText(content)) {
            return "text/plain";
        }
        return "application/octet-stream";
    }

    /**
     * 判断声明 MIME、扩展名和实际 MIME 是否一致。
     *
     * @param declaredMime 声明 MIME
     * @param extension 扩展名
     * @param content 文件内容
     * @return 是否一致
     */
    public static boolean matches(String declaredMime, String extension, byte[] content) {
        String normalizedMime = normalizeMime(declaredMime);
        String detectedMime = detectMimeType(content);
        String normalizedExtension = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        return normalizedMime.equals(detectedMime)
                && EXTENSIONS_BY_MIME.getOrDefault(normalizedMime, List.of()).contains(normalizedExtension);
    }

    /**
     * 归一化声明 MIME。
     *
     * @param value 原始 MIME
     * @return 去掉参数后的 MIME
     */
    public static String normalizeMime(String value) {
        if (value == null || value.isBlank()) {
            return "application/octet-stream";
        }
        int parameterIndex = value.indexOf(';');
        String mime = parameterIndex >= 0 ? value.substring(0, parameterIndex) : value;
        return mime.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean startsWith(byte[] content, byte[] prefix) {
        if (content.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (content[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPlainText(byte[] content) {
        int printable = 0;
        for (byte item : content) {
            int value = item & 0xFF;
            if (value == 0) {
                return false;
            }
            if (value == '\n' || value == '\r' || value == '\t' || value >= 0x20) {
                printable++;
            }
        }
        return printable == content.length;
    }
}
