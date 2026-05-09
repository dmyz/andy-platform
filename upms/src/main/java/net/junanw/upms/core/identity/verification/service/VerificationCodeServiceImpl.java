package net.junanw.upms.core.identity.verification.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.verification.model.IssuedVerificationCode;
import net.junanw.upms.core.identity.verification.model.VerificationTargetType;
import net.junanw.upms.core.identity.verification.entity.VerificationCodeEntity;
import net.junanw.upms.core.identity.verification.mapper.VerificationCodeMapper;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.security.SecurityRateLimiter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;


/**
 * 验证码服务默认实现。
 *
 * <p>负责生成验证码、失效旧码、校验最近一次验证码以及返回脱敏后的签发结果。
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    /** 验证码随机数生成器。 */
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration SEND_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration SEND_WINDOW = Duration.ofMinutes(10);
    private static final Duration VERIFY_FAILURE_WINDOW = Duration.ofMinutes(10);
    private static final Duration VERIFY_LOCK_DURATION = Duration.ofMinutes(10);
    private static final long SEND_THRESHOLD = 5L;
    private static final long VERIFY_FAILURE_THRESHOLD = 5L;

    private final VerificationCodeMapper verificationCodeMapper;
    private final SecurityRateLimiter securityRateLimiter;

    public VerificationCodeServiceImpl(
            VerificationCodeMapper verificationCodeMapper,
            SecurityRateLimiter securityRateLimiter
    ) {
        this.verificationCodeMapper = verificationCodeMapper;
        this.securityRateLimiter = securityRateLimiter;
    }

    /**
     * 签发新验证码。
     *
     * <p>在生成新验证码前，会先把同目标、同场景下尚未使用的旧验证码全部标记为已使用，
     * 确保“最近一次验证码”语义始终明确。
     *
     * @param targetType 目标类型
     * @param targetValue 目标值
     * @param scene 业务场景
     * @param expireSeconds 过期秒数
     * @return 签发结果摘要
     */
    @Override
    @Transactional
    public IssuedVerificationCode issue(String targetType, String targetValue, String scene, int expireSeconds) {
        String rateLimitKey = buildRateLimitKey(targetType, targetValue, scene);
        securityRateLimiter.checkCooldown("verification-code-send", rateLimitKey, SEND_COOLDOWN, "验证码发送过于频繁");
        securityRateLimiter.checkLimit("verification-code-send", rateLimitKey, SEND_THRESHOLD, SEND_WINDOW, "验证码发送次数过多，请稍后再试");

        List<VerificationCodeEntity> oldCodes = QueryChain.of(verificationCodeMapper)
                .eq(VerificationCodeEntity::getTargetType, targetType)
                .eq(VerificationCodeEntity::getTargetValue, targetValue)
                .eq(VerificationCodeEntity::getScene, scene)
                .eq(VerificationCodeEntity::getUsedFlag, false)
                .list();

        LocalDateTime now = LocalDateTime.now();
        oldCodes.forEach(item -> {
            item.setUsedFlag(true);
            item.setUsedTime(now);
            verificationCodeMapper.update(item);
        });

        VerificationCodeEntity entity = new VerificationCodeEntity();
        entity.setTargetType(targetType);
        entity.setTargetValue(targetValue);
        entity.setScene(scene);
        entity.setVerificationCode(String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000)));
        entity.setExpireTime(now.plusSeconds(expireSeconds));
        entity.setUsedFlag(false);
        entity.setUsedTime(null);
        entity.setCreatedAt(now);
        verificationCodeMapper.save(entity);

        return new IssuedVerificationCode(targetType, scene, maskTarget(targetType, targetValue), expireSeconds);
    }

    /**
     * 校验最近一次验证码。
     *
     * <p>仅允许使用最近一条未消费验证码，并在校验通过后立即把该验证码标记为已使用，
     * 防止重复消费。
     *
     * @param targetType 目标类型
     * @param targetValue 目标值
     * @param scene 业务场景
     * @param code 用户输入验证码
     */
    @Override
    @Transactional
    public void verifyLatest(String targetType, String targetValue, String scene, String code) {
        if (targetValue == null) {
            throw new BusinessException(400, "当前账号未绑定对应联系方式");
        }
        String rateLimitKey = buildRateLimitKey(targetType, targetValue, scene);
        securityRateLimiter.rejectIfLocked("verification-code-verify", rateLimitKey, "验证码错误次数过多，请稍后再试");
        VerificationCodeEntity entity = QueryChain.of(verificationCodeMapper)
                .eq(VerificationCodeEntity::getTargetType, targetType)
                .eq(VerificationCodeEntity::getTargetValue, targetValue)
                .eq(VerificationCodeEntity::getScene, scene)
                .eq(VerificationCodeEntity::getUsedFlag, false)
                .orderByDesc(VerificationCodeEntity::getCreatedAt)
                .get();

        if (entity == null) {
            throw new BusinessException(400, "验证码不存在或已失效");
        }
        if (entity.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "验证码已过期");
        }
        if (!entity.getVerificationCode().equals(String.valueOf(code))) {
            securityRateLimiter.recordFailure(
                    "verification-code-verify",
                    rateLimitKey,
                    VERIFY_FAILURE_THRESHOLD,
                    VERIFY_FAILURE_WINDOW,
                    VERIFY_LOCK_DURATION,
                    "验证码错误次数过多，请稍后再试"
            );
            throw new BusinessException(400, "验证码错误");
        }
        securityRateLimiter.clear("verification-code-verify", rateLimitKey);
        entity.setUsedFlag(true);
        entity.setUsedTime(LocalDateTime.now());
        verificationCodeMapper.update(entity);
    }

    /**
     * 对验证码接收目标做脱敏展示。
     *
     * @param targetType 目标类型
     * @param target 原始目标值
     * @return 脱敏后的目标值
     */
    private String maskTarget(String targetType, String target) {
        if (target == null || target.isBlank()) {
            return "";
        }
        VerificationTargetType resolvedType = VerificationTargetType.from(targetType);
        if (resolvedType == VerificationTargetType.MOBILE && target.length() >= 7) {
            return target.substring(0, 3) + "****" + target.substring(target.length() - 4);
        }
        if (resolvedType == VerificationTargetType.EMAIL) {
            int atIndex = target.indexOf('@');
            if (atIndex > 1) {
                return target.substring(0, 1) + "***" + target.substring(atIndex - 1);
            }
        }
        return "***";
    }

    /**
     * 构建目标和场景组合限流键。
     */
    private String buildRateLimitKey(String targetType, String targetValue, String scene) {
        return String.join(
                ":",
                targetType == null ? "unknown" : targetType,
                targetValue == null ? "blank" : targetValue,
                scene == null ? "default" : scene
        );
    }
}
