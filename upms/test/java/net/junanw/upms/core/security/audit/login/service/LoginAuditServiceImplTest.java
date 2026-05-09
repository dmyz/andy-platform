package net.junanw.upms.core.security.audit.login.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.test.support.IntegrationTestBase;
import net.junanw.upms.core.security.audit.login.entity.LoginAuditEventEntity;
import net.junanw.upms.core.security.audit.login.model.view.LoginAuditPageItem;
import net.junanw.upms.core.security.audit.login.mapper.LoginAuditEventMapper;
import net.junanw.upms.core.identity.account.entity.AccountEntity;
import net.junanw.upms.core.identity.account.mapper.AccountMapper;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginAuditServiceImplTest extends IntegrationTestBase {

    @Autowired
    private LoginAuditService loginAuditService;

    @Autowired
    private LoginAuditEventMapper loginAuditEventMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AccountMapper accountMapper;

    @org.junit.jupiter.api.Test
    void pageShouldUseXbatisPagingForBaseFilters() {
        saveUser(910001L, "LOGINPAGE001", "登录分页用户", "TLOGIN001");
        saveUsernameAccount(910011L, 910001L, "audit-login-page-user");
        saveLoginEvent(910101L, 910001L, "audit-login-page-user", "PASSWORD", true, LocalDateTime.of(2026, 4, 4, 10, 0));
        saveLoginEvent(910102L, 910001L, "audit-login-page-user", "PASSWORD", true, LocalDateTime.of(2026, 4, 4, 9, 0));
        saveLoginEvent(910103L, 910001L, "audit-login-page-user", "PASSWORD", true, LocalDateTime.of(2026, 4, 4, 8, 0));
        saveLoginEvent(910104L, 910001L, "audit-login-page-user", "PASSWORD", false, LocalDateTime.of(2026, 4, 4, 7, 0));

        PageResponse<LoginAuditPageItem> page = loginAuditService.page("audit-login-page-user", "PASSWORD", "SUCCESS", null, null, 2, 1);

        assertEquals(3, page.total());
        assertEquals(2, page.pageNum());
        assertEquals(1, page.pageSize());
        assertEquals(1, page.list().size());
        assertEquals("910102", page.list().get(0).id());
    }

    @org.junit.jupiter.api.Test
    void pageShouldFilterByUsernameOrRealName() {
        saveUser(910002L, "LOGINNAME001", "真实姓名命中用户", "TLOGIN002");
        saveUsernameAccount(910021L, 910002L, "audit-login-name-user");
        saveLoginEvent(910201L, 910002L, "audit-login-name-user", "PASSWORD", true, LocalDateTime.of(2026, 4, 4, 11, 0));

        PageResponse<LoginAuditPageItem> usernamePage = loginAuditService.page("audit-login-name", null, null, null, null, 1, 10);
        PageResponse<LoginAuditPageItem> realNamePage = loginAuditService.page("真实姓名命中", null, null, null, null, 1, 10);

        assertEquals(1, usernamePage.total());
        assertEquals("audit-login-name-user", usernamePage.list().get(0).username());
        assertEquals(1, realNamePage.total());
        assertEquals("真实姓名命中用户", realNamePage.list().get(0).realName());
    }

    @org.junit.jupiter.api.Test
    void pageShouldMatchAnonymousRecordByAccountIdentifier() {
        saveLoginEvent(910301L, null, "anonymous-login-match", "PASSWORD", false, LocalDateTime.of(2026, 4, 4, 12, 0));

        PageResponse<LoginAuditPageItem> page = loginAuditService.page("anonymous-login-match", null, null, null, null, 1, 10);

        assertEquals(1, page.total());
        assertEquals("anonymous-login-match", page.list().get(0).username());
        assertEquals("FAIL", page.list().get(0).result());
    }


    @org.junit.jupiter.api.Test
    void exportShouldReuseAuditMapperFilters() {
        saveUser(910004L, "LOGINEXPORT001", "导出筛选用户", "TLOGIN004");
        saveUsernameAccount(910041L, 910004L, "audit-login-export-user");
        saveLoginEvent(910501L, 910004L, "audit-login-export-user", "PASSWORD", true, LocalDateTime.of(2026, 4, 4, 16, 0));
        saveLoginEvent(910502L, 910004L, "audit-login-export-user", "PASSWORD", false, LocalDateTime.of(2026, 4, 4, 15, 0));

        List<LoginAuditPageItem> items = loginAuditService.export("audit-login-export", "PASSWORD", "SUCCESS", null, null);

        assertEquals(1, items.size());
        assertEquals("910501", items.get(0).id());
    }

    @org.junit.jupiter.api.Test
    void pageShouldSortByEventTimeDesc() {
        saveUser(910003L, "LOGINSORT001", "排序验证用户", "TLOGIN003");
        saveUsernameAccount(910031L, 910003L, "audit-login-sort-user");
        saveLoginEvent(910401L, 910003L, "audit-login-sort-user", "PASSWORD", true, LocalDateTime.of(2026, 4, 4, 15, 0));
        saveLoginEvent(910402L, 910003L, "audit-login-sort-user", "PASSWORD", true, LocalDateTime.of(2026, 4, 4, 14, 0));
        saveLoginEvent(910403L, 910003L, "audit-login-sort-user", "PASSWORD", true, LocalDateTime.of(2026, 4, 4, 13, 0));

        PageResponse<LoginAuditPageItem> page = loginAuditService.page("audit-login-sort-user", null, null, null, null, 1, 2);

        assertEquals(3, page.total());
        assertEquals(2, page.list().size());
        assertEquals("910401", page.list().get(0).id());
        assertEquals("910402", page.list().get(1).id());
    }

    private void saveUser(Long id, String userCode, String displayName, String employeeNo) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setUserCode(userCode);
        entity.setDisplayName(displayName);
        entity.setGender("UNKNOWN");
        entity.setEmployeeNo(employeeNo);
        entity.setAvatarFileId(null);
        entity.setUserType("STAFF");
        entity.setSourceType("LOCAL");
        entity.setStatus("ACTIVE");
        entity.setPasswordResetRequired(false);
        entity.setLastLoginTime(null);
        entity.setLastLoginIp(null);
        entity.setRemark("test");
        entity.setDeleted(false);
        entity.setCreatorId(10001L);
        entity.setUpdaterId(10001L);
        userMapper.save(entity);
    }

    private void saveUsernameAccount(Long id, Long userId, String identifier) {
        AccountEntity entity = new AccountEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setAccountType("USERNAME");
        entity.setIdentifier(identifier);
        entity.setNormalizedIdentifier(identifier.toLowerCase());
        entity.setIsLoginEnabled(true);
        entity.setIsPrimary(true);
        entity.setVerifiedFlag(true);
        entity.setStatus("ACTIVE");
        entity.setLastUsedTime(null);
        entity.setCreatorId(10001L);
        entity.setUpdaterId(10001L);
        accountMapper.save(entity);
    }

    private void saveLoginEvent(Long id, Long userId, String accountIdentifier, String authType, boolean success, LocalDateTime eventTime) {
        LoginAuditEventEntity entity = new LoginAuditEventEntity();
        entity.setId(id);
        entity.setUserId(userId);
        entity.setAccountIdentifier(accountIdentifier);
        entity.setAuthType(authType);
        entity.setEventType(success ? "LOGIN_SUCCESS" : "LOGIN_FAIL");
        entity.setSuccessFlag(success);
        entity.setReasonCode(success ? null : "BAD_CREDENTIALS");
        entity.setIp("127.0.0.1");
        entity.setUserAgent("JUnit");
        entity.setSessionKey(null);
        entity.setTraceId(null);
        entity.setRequestId(null);
        entity.setEventTime(eventTime);
        loginAuditEventMapper.save(entity);
    }
}
