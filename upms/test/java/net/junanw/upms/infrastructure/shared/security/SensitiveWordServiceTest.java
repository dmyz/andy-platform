package net.junanw.upms.infrastructure.shared.security;

import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SensitiveWordServiceTest {

    @Test
    void rejectIfPresentShouldMatchLocalWordsIgnoringCase() {
        SensitiveWordService service = new SensitiveWordService(List.of("# comment", "Blocked-Example"));

        assertThrows(BusinessException.class, () -> service.rejectIfPresent("内容", "prefix blocked-example suffix"));
    }

    @Test
    void rejectIfPresentShouldIgnoreBlankAndComments() {
        SensitiveWordService service = new SensitiveWordService(List.of("", "  ", "# blocked-example"));

        assertDoesNotThrow(() -> service.rejectIfPresent("内容", "blocked-example"));
    }
}
