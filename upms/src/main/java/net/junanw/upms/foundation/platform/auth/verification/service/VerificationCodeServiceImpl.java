package net.junanw.upms.foundation.platform.auth.verification.service;

import com.mybatisflex.core.query.QueryWrapper;
import net.junanw.upms.foundation.platform.auth.verification.model.IssuedVerificationCode;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.platform.auth.verification.persistence.VerificationCodeEntity;
import net.junanw.upms.foundation.platform.auth.verification.persistence.VerificationCodeMapper;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.foundation.shared.id.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static net.junanw.upms.foundation.platform.auth.verification.persistence.table.VerificationCodeEntityTableDef.VERIFICATION_CODE_ENTITY;

/**
 * 验证码服务默认实现。
 *
 * <p>负责生成验证码、失效旧码、校验最近一次验证码以及返回脱敏后的签发结果。
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    /** 验证码随机数生成器。 */
    private static final SecureRandom RANDOM = new SecureRandom();

    private final VerificationCodeMapper verificationCodeMapper;
    private final IdGenerator idGenerator;

    public VerificationCodeServiceImpl(VerificationCodeMapper verificationCodeMapper, IdGenerator idGenerator) {
        this.verificationCodeMapper = verificationCodeMapper;
        this.idGenerator = idGenerator;
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
        QueryWrapper oldCodesQuery = QueryWrapper.create()
                .where(VERIFICATION_CODE_ENTITY.TARGET_TYPE.eq(targetType))
                .and(VERIFICATION_CODE_ENTITY.TARGET_VALUE.eq(targetValue))
                .and(VERIFICATION_CODE_ENTITY.SCENE.eq(scene))
                .and(VERIFICATION_CODE_ENTITY.USED_FLAG.eq(false));
        List<VerificationCodeEntity> oldCodes = verificationCodeMapper.selectListByQuery(oldCodesQuery);

        LocalDateTime now = LocalDateTime.now();
        oldCodes.forEach(item -> {
            item.setUsedFlag(true);
            item.setUsedTime(now);
            verificationCodeMapper.update(item);
        });

        VerificationCodeEntity entity = new VerificationCodeEntity();
        entity.setId(idGenerator.nextId());
        entity.setTargetType(targetType);
        entity.setTargetValue(targetValue);
        entity.setScene(scene);
        entity.setVerificationCode(String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000)));
        entity.setExpireTime(now.plusSeconds(expireSeconds));
        entity.setUsedFlag(false);
        entity.setUsedTime(null);
        entity.setCreatedAt(now);
        verificationCodeMapper.insert(entity);

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
        QueryWrapper query = QueryWrapper.create()
                .where(VERIFICATION_CODE_ENTITY.TARGET_TYPE.eq(targetType))
                .and(VERIFICATION_CODE_ENTITY.TARGET_VALUE.eq(targetValue))
                .and(VERIFICATION_CODE_ENTITY.SCENE.eq(scene))
                .and(VERIFICATION_CODE_ENTITY.USED_FLAG.eq(false))
                .orderBy(VERIFICATION_CODE_ENTITY.CREATED_AT, false)
                .limit(1);
        VerificationCodeEntity entity = verificationCodeMapper.selectOneByQuery(query);

        if (entity == null) {
            throw new BusinessException(400, "验证码不存在或已失效");
        }
        if (entity.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "验证码已过期");
        }
        if (!entity.getVerificationCode().equals(String.valueOf(code))) {
            throw new BusinessException(400, "验证码错误");
        }
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
}
