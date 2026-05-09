package net.junanw.upms.infrastructure.shared.web;

import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateTimeRangeParserTest {

    @Test
    void shouldParseDateAsRangeBoundaries() {
        assertEquals(LocalDateTime.of(2026, 5, 1, 0, 0), DateTimeRangeParser.parseStartTime("2026-05-01"));
        assertEquals(LocalDateTime.of(2026, 6, 2, 23, 59, 59, LocalTime.MAX.getNano()), DateTimeRangeParser.parseEndTime("2026-06-02"));
    }

    @Test
    void shouldParseFullDateTimeWithoutChangingTime() {
        LocalDateTime value = LocalDateTime.of(2026, 5, 1, 8, 30, 12);

        assertEquals(value, DateTimeRangeParser.parseStartTime("2026-05-01 08:30:12"));
        assertEquals(value, DateTimeRangeParser.parseEndTime("2026-05-01 08:30:12"));
    }

    @Test
    void shouldReturnNullForBlankText() {
        assertNull(DateTimeRangeParser.parseStartTime(null));
        assertNull(DateTimeRangeParser.parseEndTime(" "));
    }

    @Test
    void shouldRejectUnsupportedFormat() {
        BusinessException exception = assertThrows(BusinessException.class, () -> DateTimeRangeParser.parseStartTime("2026/05/01"));

        assertEquals(400, exception.getCode());
    }
}
