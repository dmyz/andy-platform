package net.junanw.upms.core.identity.session;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthSessionActivityFilter extends OncePerRequestFilter {

    private final AuthOnlineSessionService authOnlineSessionService;

    public AuthSessionActivityFilter(AuthOnlineSessionService authOnlineSessionService) {
        this.authOnlineSessionService = authOnlineSessionService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/admin/")
                || "/admin/auth/login".equals(uri)
                || uri.startsWith("/admin/auth/code/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
        try {
            if (StpUtil.isLogin()) {
                authOnlineSessionService.touch(StpUtil.getTokenValue());
            }
        }
        catch (SaTokenContextException ignored) {
            // MockMvc 场景可能没有 Sa-Token 线程上下文，此时只跳过会话活跃时间刷新。
        }
    }
}
