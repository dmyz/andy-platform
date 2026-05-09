package net.junanw.upms.infrastructure.shared.security;

import jakarta.annotation.PostConstruct;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * 本地敏感词服务。
 *
 * <p>启动时从 classpath 资源加载词库，业务提交时只返回统一拦截原因，不暴露具体命中词。
 */
@Component
public class SensitiveWordService {

    private static final String RESOURCE_LOCATION = "classpath:security/sensitive-words.txt";

    private final ResourceLoader resourceLoader;
    private volatile List<String> words = List.of();

    @Autowired
    public SensitiveWordService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    SensitiveWordService(Collection<String> words) {
        this.resourceLoader = null;
        this.words = normalize(words);
    }

    /**
     * 初始化本地词库。
     */
    @PostConstruct
    public void init() {
        if (resourceLoader == null) {
            return;
        }
        Resource resource = resourceLoader.getResource(RESOURCE_LOCATION);
        if (!resource.exists()) {
            words = List.of();
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            words = normalize(reader.lines().toList());
        }
        catch (IOException exception) {
            throw new IllegalStateException("加载敏感词库失败", exception);
        }
    }

    /**
     * 校验文本是否包含敏感词。
     *
     * @param fieldName 字段名称
     * @param value 文本
     */
    public void rejectIfPresent(String fieldName, String value) {
        if (value == null || value.isBlank() || words.isEmpty()) {
            return;
        }
        String normalizedValue = value.toLowerCase(Locale.ROOT);
        for (String word : words) {
            if (normalizedValue.contains(word)) {
                throw new BusinessException(400, fieldName + "包含不允许的词汇");
            }
        }
    }

    /**
     * 归一化词库内容。
     *
     * @param source 原始词库
     * @return 可匹配词列表
     */
    private List<String> normalize(Collection<String> source) {
        List<String> normalized = new ArrayList<>();
        for (String item : source) {
            if (item == null) {
                continue;
            }
            String word = item.trim();
            if (word.isBlank() || word.startsWith("#")) {
                continue;
            }
            normalized.add(word.toLowerCase(Locale.ROOT));
        }
        return List.copyOf(normalized);
    }
}
