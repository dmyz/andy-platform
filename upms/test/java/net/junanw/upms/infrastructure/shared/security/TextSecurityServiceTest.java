package net.junanw.upms.infrastructure.shared.security;

import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextSecurityServiceTest {

    private final TextSecurityService textSecurityService = new TextSecurityService();

    @Test
    void sanitizePlainTextShouldEscapeHtmlAndRemoveUnsafeControls() {
        String result = textSecurityService.sanitizePlainText(" <script>\u0000alert('x')</script> ", "内容", true, 100);

        assertEquals("&lt;script&gt;alert(&#x27;x&#x27;)&lt;&#x2F;script&gt;", result);
    }

    @Test
    void sanitizeForOutputShouldEscapeHistoricalRawHtml() {
        String result = textSecurityService.sanitizeForOutput("a<b>c</b>");

        assertEquals("a&lt;b&gt;c&lt;&#x2F;b&gt;", result);
    }

    @Test
    void ensureLengthShouldRejectLongText() {
        assertThrows(BusinessException.class, () -> textSecurityService.ensureLength("123456", "字段", 5));
    }
}
