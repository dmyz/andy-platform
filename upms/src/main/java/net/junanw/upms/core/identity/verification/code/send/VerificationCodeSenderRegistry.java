package net.junanw.upms.core.identity.verification.code.send;

import net.junanw.upms.core.identity.verification.model.VerificationTargetType;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 验证码发送器注册表。
 */
@Component
public class VerificationCodeSenderRegistry {

    /**
     * 按目标类型索引的发送器映射。
     */
    private final Map<VerificationTargetType, VerificationCodeSender> senders;

    public VerificationCodeSenderRegistry(List<VerificationCodeSender> senders) {
        EnumMap<VerificationTargetType, VerificationCodeSender> senderMap = new EnumMap<>(VerificationTargetType.class);
        senders.forEach(sender -> senderMap.put(sender.targetType(), sender));
        this.senders = Map.copyOf(senderMap);
    }

    /**
     * 获取指定目标类型的唯一发送器。
     */
    public VerificationCodeSender get(VerificationTargetType targetType) {
        VerificationCodeSender sender = senders.get(targetType);
        if (sender == null) {
            throw new BusinessException(400, "不支持的目标类型");
        }
        return sender;
    }
}
