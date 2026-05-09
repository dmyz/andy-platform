package net.junanw.upms.infrastructure.shared.web;

import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 日期时间范围查询参数解析器。
 *
 * <p>兼容前端日期选择器常用的日期格式和完整日期时间格式。
 */
public final class DateTimeRangeParser {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ERROR_MESSAGE = "时间参数格式错误，请使用 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss";

    private DateTimeRangeParser() {
    }

    /**
     * 解析范围开始时间。
     *
     * @param value 日期或日期时间文本
     * @return 开始时间，空文本返回 {@code null}
     */
    public static LocalDateTime parseStartTime(String value) {
        return parse(value, false);
    }

    /**
     * 解析范围结束时间。
     *
     * @param value 日期或日期时间文本
     * @return 结束时间，空文本返回 {@code null}
     */
    public static LocalDateTime parseEndTime(String value) {
        return parse(value, true);
    }

    /** 按范围边界语义解析时间文本。 */
    private static LocalDateTime parse(String value, boolean endOfDay) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        try {
            if (text.length() == 10) {
                LocalDate date = LocalDate.parse(text, DATE_FORMATTER);
                return endOfDay ? date.atTime(LocalTime.MAX) : date.atStartOfDay();
            }
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BusinessException(400, ERROR_MESSAGE);
        }
    }
}
