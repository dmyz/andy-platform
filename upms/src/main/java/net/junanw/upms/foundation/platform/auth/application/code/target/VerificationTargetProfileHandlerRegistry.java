package net.junanw.upms.foundation.platform.auth.application.code.target;

import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 验证目标档案处理器注册表。
 */
@Component
public class VerificationTargetProfileHandlerRegistry {

    /**
     * 按目标类型索引的处理器映射。
     */
    private final Map<VerificationTargetType, VerificationTargetProfileHandler> handlers;

    public VerificationTargetProfileHandlerRegistry(List<VerificationTargetProfileHandler> handlers) {
        EnumMap<VerificationTargetType, VerificationTargetProfileHandler> handlerMap = new EnumMap<>(VerificationTargetType.class);
        handlers.forEach(handler -> handlerMap.put(handler.targetType(), handler));
        this.handlers = Map.copyOf(handlerMap);
    }

    /**
     * 获取指定目标类型的处理器。
     */
    public VerificationTargetProfileHandler get(VerificationTargetType targetType) {
        VerificationTargetProfileHandler handler = handlers.get(targetType);
        if (handler == null) {
            throw new BusinessException(400, "不支持的目标类型");
        }
        return handler;
    }
}
