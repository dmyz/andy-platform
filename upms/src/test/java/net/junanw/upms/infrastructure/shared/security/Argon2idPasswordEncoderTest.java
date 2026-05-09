package net.junanw.upms.infrastructure.shared.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Argon2id 密码编码器测试
 *
 * @author Claude
 * @since 1.0.0
 */
class Argon2idPasswordEncoderTest {

    private Argon2idPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new Argon2idPasswordEncoder();
    }

    /**
     * 测试密码编码格式
     */
    @Test
    void testEncodeFormat() {
        String rawPassword = "MySecurePassword123!";
        String encoded = encoder.encode(rawPassword);

        assertNotNull(encoded);
        assertTrue(encoded.startsWith("$argon2id$v=19$"));
        assertTrue(encoded.contains("m=32768,t=2,p=1"));

        // 验证格式：$argon2id$v=19$m=32768,t=2,p=1$salt$hash
        String[] parts = encoded.split("\\$");
        assertEquals(6, parts.length);
        assertEquals("", parts[0]); // 第一个 $ 前为空
        assertEquals("argon2id", parts[1]);
        assertEquals("v=19", parts[2]);
        assertEquals("m=32768,t=2,p=1", parts[3]);
        assertFalse(parts[4].isEmpty()); // salt
        assertFalse(parts[5].isEmpty()); // hash
    }

    /**
     * 测试相同密码生成不同哈希（因为盐值不同）
     */
    @Test
    void testEncodeDifferentSalts() {
        String rawPassword = "password123";
        String encoded1 = encoder.encode(rawPassword);
        String encoded2 = encoder.encode(rawPassword);

        assertNotEquals(encoded1, encoded2);
    }

    /**
     * 测试密码匹配成功
     */
    @Test
    void testMatchesSuccess() {
        String rawPassword = "MySecurePassword123!";
        String encoded = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encoded));
    }

    /**
     * 测试密码匹配失败
     */
    @Test
    void testMatchesFailure() {
        String rawPassword = "MySecurePassword123!";
        String wrongPassword = "WrongPassword456!";
        String encoded = encoder.encode(rawPassword);

        assertFalse(encoder.matches(wrongPassword, encoded));
    }

    /**
     * 测试空密码处理
     */
    @Test
    void testNullPassword() {
        String encoded = encoder.encode("password");

        assertFalse(encoder.matches(null, encoded));
        assertFalse(encoder.matches("password", null));
        assertFalse(encoder.matches(null, null));
    }

    /**
     * 测试空字符串密码
     */
    @Test
    void testEmptyPassword() {
        String encoded = encoder.encode("");

        assertTrue(encoder.matches("", encoded));
        assertFalse(encoder.matches("notEmpty", encoded));
    }

    /**
     * 测试无效编码格式
     */
    @Test
    void testInvalidEncodedFormat() {
        assertFalse(encoder.matches("password", "invalid-format"));
        assertFalse(encoder.matches("password", "$argon2id$invalid"));
        assertFalse(encoder.matches("password", "$bcrypt$v=19$m=65536,t=3,p=4$salt$hash"));
    }

    /**
     * 测试自定义参数编码器
     */
    @Test
    void testCustomParameters() {
        Argon2idPasswordEncoder customEncoder = new Argon2idPasswordEncoder(32768, 2, 2);
        String rawPassword = "customPassword";
        String encoded = customEncoder.encode(rawPassword);

        assertTrue(encoded.contains("m=32768,t=2,p=2"));
        assertTrue(customEncoder.matches(rawPassword, encoded));
    }

    /**
     * 测试长密码
     */
    @Test
    void testLongPassword() {
        String longPassword = "a".repeat(1000);
        String encoded = encoder.encode(longPassword);

        assertTrue(encoder.matches(longPassword, encoded));
        assertFalse(encoder.matches(longPassword + "b", encoded));
    }

    /**
     * 测试特殊字符密码
     */
    @Test
    void testSpecialCharacters() {
        String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        String encoded = encoder.encode(specialPassword);

        assertTrue(encoder.matches(specialPassword, encoded));
    }

    /**
     * 测试 Unicode 字符密码
     */
    @Test
    void testUnicodePassword() {
        String unicodePassword = "密码123🔐";
        String encoded = encoder.encode(unicodePassword);

        assertTrue(encoder.matches(unicodePassword, encoded));
        assertFalse(encoder.matches("密码123", encoded));
    }

    /**
     * 测试编码性能（确保在合理时间内完成）
     */
    @Test
    void testEncodePerformance() {
        String password = "performanceTest";
        long startTime = System.currentTimeMillis();

        encoder.encode(password);

        long duration = System.currentTimeMillis() - startTime;

        // 编码应在 1 秒内完成（实际通常在 100-300ms）
        assertTrue(duration < 1000, "编码耗时过长: " + duration + "ms");
    }

    /**
     * 测试验证性能
     */
    @Test
    void testMatchesPerformance() {
        String password = "performanceTest";
        String encoded = encoder.encode(password);

        long startTime = System.currentTimeMillis();

        encoder.matches(password, encoded);

        long duration = System.currentTimeMillis() - startTime;

        // 验证应在 1 秒内完成
        assertTrue(duration < 1000, "验证耗时过长: " + duration + "ms");
    }

    /**
     * 测试并发安全性
     */
    @Test
    void testConcurrentEncoding() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                String password = "password" + index;
                String encoded = encoder.encode(password);
                results[index] = encoder.matches(password, encoded);
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (boolean result : results) {
            assertTrue(result);
        }
    }
}
