package net.junanw.upms.infrastructure.security.satoken;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token Web MVC 拦截器配置。
 *
 * <p>负责把登录校验挂到管理端受保护接口上，同时保留登录、发码与重置密码等匿名入口。
 */
@Configuration
public class SaTokenWebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 登录拦截器。
     *
     * @param registry MVC 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> StpUtil.checkLogin()))
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/auth/login",
                        "/admin/auth/code/**",
                        "/admin/auth/captcha",
                        "/admin/auth/password/reset"
                );
    }
}
