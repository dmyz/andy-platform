package net.junanw.upms.infrastructure.shared.id;

import cn.xbatis.core.incrementer.IdWorker;

/**
 * ID 生成器。
 *
 * <p>为需要显式分配主键的业务对象生成递增趋势的长整型 ID。
 */
public final class IdGenerator {

    /**
     * 工具类禁止实例化。
     */
    private IdGenerator() {
    }

    /**
     * 生成下一个 ID。
     *
     * @return 长整型 ID
     */
    public static long nextId() {
        return IdWorker.INSTANCE.nextId();
    }

}
