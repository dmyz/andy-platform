package net.junanw.upms.core.security.audit.operation.service;

import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.test.support.IntegrationTestBase;
import net.junanw.upms.core.security.audit.operation.entity.OperationAuditEventEntity;
import net.junanw.upms.core.security.audit.operation.model.view.OperationAuditPageItem;
import net.junanw.upms.core.security.audit.operation.mapper.OperationAuditEventMapper;
import net.junanw.upms.core.identity.user.entity.UserEntity;
import net.junanw.upms.core.identity.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationAuditServiceImplTest extends IntegrationTestBase {

    @Autowired
    private OperationAuditService operationAuditService;

    @Autowired
    private OperationAuditEventMapper operationAuditEventMapper;

    @Autowired
    private UserMapper userMapper;

    @org.junit.jupiter.api.Test
    void pageShouldUseXbatisPagingForOperationAudit() {
        saveUser(920001L, "OPPAGE001", "操作分页用户", "TOP001");
        saveOperationEvent(920101L, 920001L, "user", "UPDATE", "SUCCESS", "/admin/user/1", LocalDateTime.of(2026, 4, 4, 10, 0));
        saveOperationEvent(920102L, 920001L, "user", "UPDATE", "SUCCESS", "/admin/user/2", LocalDateTime.of(2026, 4, 4, 9, 0));
        saveOperationEvent(920103L, 920001L, "user", "UPDATE", "SUCCESS", "/admin/user/3", LocalDateTime.of(2026, 4, 4, 8, 0));
        saveOperationEvent(920104L, 920001L, "user", "DELETE", "SUCCESS", "/admin/user/4", LocalDateTime.of(2026, 4, 4, 7, 0));

        PageResponse<OperationAuditPageItem> page = operationAuditService.page("操作分页用户", "用户管理", "UPDATE", "SUCCESS", null, null, 2, 1);

        assertEquals(3, page.total());
        assertEquals(2, page.pageNum());
        assertEquals(1, page.pageSize());
        assertEquals(1, page.list().size());
        assertEquals("920102", page.list().get(0).id());
    }

    @org.junit.jupiter.api.Test
    void pageShouldFilterByOperatorDisplayName() {
        saveUser(920002L, "OPNAME001", "操作姓名匹配用户", "TOP002");
        saveOperationEvent(920201L, 920002L, "role", "CREATE", "SUCCESS", "/admin/role", LocalDateTime.of(2026, 4, 4, 11, 0));

        PageResponse<OperationAuditPageItem> page = operationAuditService.page("姓名匹配", null, null, null, null, null, 1, 10);

        assertEquals(1, page.total());
        assertEquals("操作姓名匹配用户", page.list().get(0).operatorName());
    }

    @org.junit.jupiter.api.Test
    void pageShouldFilterByResolvedModuleCode() {
        saveUser(920003L, "OPMODULE001", "模块映射用户", "TOP003");
        saveOperationEvent(920301L, 920003L, "setting", "UPDATE", "SUCCESS", "/admin/setting", LocalDateTime.of(2026, 4, 4, 12, 0));
        saveOperationEvent(920302L, 920003L, "user", "UPDATE", "SUCCESS", "/admin/user", LocalDateTime.of(2026, 4, 4, 11, 0));

        PageResponse<OperationAuditPageItem> page = operationAuditService.page("模块映射用户", "系统配置", "UPDATE", "SUCCESS", null, null, 1, 10);

        assertEquals(1, page.total());
        assertEquals("系统配置", page.list().get(0).moduleName());
    }


    @org.junit.jupiter.api.Test
    void exportShouldReuseAuditMapperFilters() {
        saveUser(920005L, "OPEXPORT001", "导出筛选操作人", "TOP005");
        saveOperationEvent(920501L, 920005L, "setting", "UPDATE", "SUCCESS", "/admin/setting/1", LocalDateTime.of(2026, 4, 4, 16, 0));
        saveOperationEvent(920502L, 920005L, "setting", "DELETE", "SUCCESS", "/admin/setting/2", LocalDateTime.of(2026, 4, 4, 15, 0));

        List<OperationAuditPageItem> items = operationAuditService.export("导出筛选", "系统配置", "UPDATE", "SUCCESS", null, null);

        assertEquals(1, items.size());
        assertEquals("920501", items.get(0).id());
    }

    @org.junit.jupiter.api.Test
    void pageShouldReturnStableTotalAcrossPages() {
        saveUser(920004L, "OPTOTAL001", "总数稳定用户", "TOP004");
        saveOperationEvent(920401L, 920004L, "file", "UPDATE", "SUCCESS", "/admin/file/1", LocalDateTime.of(2026, 4, 4, 15, 0));
        saveOperationEvent(920402L, 920004L, "file", "UPDATE", "SUCCESS", "/admin/file/2", LocalDateTime.of(2026, 4, 4, 14, 0));
        saveOperationEvent(920403L, 920004L, "file", "UPDATE", "SUCCESS", "/admin/file/3", LocalDateTime.of(2026, 4, 4, 13, 0));

        PageResponse<OperationAuditPageItem> firstPage = operationAuditService.page("总数稳定用户", "文件管理", "UPDATE", "SUCCESS", null, null, 1, 1);
        PageResponse<OperationAuditPageItem> secondPage = operationAuditService.page("总数稳定用户", "文件管理", "UPDATE", "SUCCESS", null, null, 2, 1);

        assertEquals(3, firstPage.total());
        assertEquals(3, secondPage.total());
        assertEquals("920401", firstPage.list().get(0).id());
        assertEquals("920402", secondPage.list().get(0).id());
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

    private void saveOperationEvent(Long id, Long actorUserId, String moduleCode, String actionCode, String resultStatus, String requestUri, LocalDateTime eventTime) {
        OperationAuditEventEntity entity = new OperationAuditEventEntity();
        entity.setId(id);
        entity.setActorUserId(actorUserId);
        entity.setModuleCode(moduleCode);
        entity.setActionCode(actionCode);
        entity.setTargetType("USER");
        entity.setTargetId(1L);
        entity.setRequestMethod("POST");
        entity.setRequestUri(requestUri);
        entity.setRequestSummary("{}");
        entity.setResponseSummary("{\"code\":0}");
        entity.setResultStatus(resultStatus);
        entity.setDurationMs(10);
        entity.setIp("127.0.0.1");
        entity.setTraceId(null);
        entity.setRequestId(null);
        entity.setEventTime(eventTime);
        operationAuditEventMapper.save(entity);
    }
}
