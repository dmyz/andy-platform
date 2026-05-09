package net.junanw.upms.infrastructure.shared.security;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Argon2id 密码编码器实现
 * <p>
 * 使用 Argon2id 算法进行密码哈希，提供抗侧信道攻击和抗 GPU 破解能力。
 * 编码格式：$argon2id$v=19$m=32768,t=2,p=1$salt$hash
 * </p>
 *
 * @author Claude
 * @since 1.0.0
 */
@Component
public class Argon2idPasswordEncoder implements PasswordEncoder {

    /**
     * 盐值长度（字节）
     */
    private static final int SALT_LENGTH = 16;

    /**
     * 哈希输出长度（字节）
     */
    private static final int HASH_LENGTH = 32;

    /**
     * 内存成本（KB）
     */
    private final int memoryCost;

    /**
     * 时间成本（迭代次数）
     */
    private final int timeCost;

    /**
     * 并行度
     */
    private final int parallelism;

    /**
     * 安全随机数生成器
     */
    private final SecureRandom secureRandom;

    /**
     * 使用默认参数构造编码器
     * <p>
     * 默认参数：内存成本 32768 KB (64 MB)，时间成本 2 次迭代，并行度 1
     * </p>
     */
    public Argon2idPasswordEncoder() {
        this(32768, 2, 1);
    }

    /**
     * 使用自定义参数构造编码器
     *
     * @param memoryCost  内存成本（KB）
     * @param timeCost    时间成本（迭代次数）
     * @param parallelism 并行度
     */
    public Argon2idPasswordEncoder(int memoryCost, int timeCost, int parallelism) {
        this.memoryCost = memoryCost;
        this.timeCost = timeCost;
        this.parallelism = parallelism;
        this.secureRandom = new SecureRandom();
    }

    /**
     * 对原始密码进行编码
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码字符串
     */
    @Override
    public String encode(CharSequence rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        byte[] hash = hashPassword(rawPassword.toString(), salt);

        return formatHash(salt, hash);
    }

    /**
     * 验证原始密码与编码密码是否匹配
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 编码后的密码
     * @return 是否匹配
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length != 6) {
                return false;
            }

            // 验证算法标识
            if (!"argon2id".equals(parts[1])) {
                return false;
            }

            // 解析参数
            String[] params = parts[3].split(",");
            int m = Integer.parseInt(params[0].substring(2));
            int t = Integer.parseInt(params[1].substring(2));
            int p = Integer.parseInt(params[2].substring(2));

            // 解码盐值和哈希
            byte[] salt = Base64.getDecoder().decode(parts[4]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[5]);

            // 使用相同参数计算哈希
            byte[] actualHash = hashPassword(rawPassword.toString(), salt, m, t, p);

            // 常量时间比较
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 计算密码哈希
     *
     * @param password 原始密码
     * @param salt     盐值
     * @return 哈希值
     */
    private byte[] hashPassword(String password, byte[] salt) {
        return hashPassword(password, salt, memoryCost, timeCost, parallelism);
    }

    /**
     * 使用指定参数计算密码哈希
     *
     * @param password    原始密码
     * @param salt        盐值
     * @param memoryCost  内存成本
     * @param timeCost    时间成本
     * @param parallelism 并行度
     * @return 哈希值
     */
    private byte[] hashPassword(String password, byte[] salt, int memoryCost, int timeCost, int parallelism) {
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(memoryCost)
                .withIterations(timeCost)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build();

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);

        byte[] hash = new byte[HASH_LENGTH];
        generator.generateBytes(password.toCharArray(), hash);

        return hash;
    }

    /**
     * 格式化哈希输出
     *
     * @param salt 盐值
     * @param hash 哈希值
     * @return 格式化的哈希字符串
     */
    private String formatHash(byte[] salt, byte[] hash) {
        String saltEncoded = Base64.getEncoder().withoutPadding().encodeToString(salt);
        String hashEncoded = Base64.getEncoder().withoutPadding().encodeToString(hash);

        return String.format("$argon2id$v=19$m=%d,t=%d,p=%d$%s$%s",
                memoryCost, timeCost, parallelism, saltEncoded, hashEncoded);
    }

    /**
     * 常量时间比较两个字节数组
     * <p>
     * 防止时序攻击
     * </p>
     *
     * @param a 数组 a
     * @param b 数组 b
     * @return 是否相等
     */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }

        return result == 0;
    }

    public static void main(String[] args) {
        Argon2idPasswordEncoder argon2idPasswordEncoder = new Argon2idPasswordEncoder();
        String admin = argon2idPasswordEncoder.encode("admin");
        System.out.println("密码 = " + admin);
    }

}
