package net.junanw.upms.foundation.shared.id;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ID 生成器。
 *
 * <p>为需要显式分配主键的业务对象生成递增趋势的长整型 ID。
 */
@Component
public class IdGenerator {

    private static final long EPOCH = 1_700_000_000_000L;
    private final AtomicInteger sequence = new AtomicInteger(0);

    /**
     * 生成下一个 ID。
     *
     * @return 长整型 ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis() - EPOCH;
        int current = sequence.updateAndGet(value -> (value + 1) & 0x0FFF);
        return (timestamp << 12) | current;
    }
}
