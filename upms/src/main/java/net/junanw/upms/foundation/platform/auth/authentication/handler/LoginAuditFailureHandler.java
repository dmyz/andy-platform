package net.junanw.upms.foundation.platform.auth.authentication.handler;

import net.junanw.upms.foundation.platform.auth.authentication.LoginAuthentication;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import net.junanw.upms.support.audit.login.model.LoginAuditEventType;
import net.junanw.upms.support.audit.login.service.LoginAuditRecord;
import net.junanw.upms.support.audit.login.service.LoginAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录失败审计处理器。
 */
@Component
public class LoginAuditFailureHandler implements LoginAuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(LoginAuditFailureHandler.class);
    /**
     * 登录审计服务。
     */
    private final LoginAuditService loginAuditService;

    public LoginAuditFailureHandler(LoginAuditService loginAuditService) {
        this.loginAuditService = loginAuditService;
    }

    @Override
    public void onFailure(LoginAuthentication authentication, BusinessException exception) {
        // 登录失败时只记录审计，不吞掉原异常。
        try {
            loginAuditService.record(new LoginAuditRecord(
                    authentication.accountIdentifier(),
                    authentication.grantType().name(),
                    LoginAuditEventType.LOGIN_FAIL.name(),
                    false,
                    exception.getMessage(),
                    authentication.details().remoteAddr(),
                    authentication.details().userAgent(),
                    null,
                    authentication.details().traceId(),
                    authentication.details().requestId(),
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("登录失败时只记录审计", e);
        }
    }
}
