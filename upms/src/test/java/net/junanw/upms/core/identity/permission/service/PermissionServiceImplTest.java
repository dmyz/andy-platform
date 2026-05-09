package net.junanw.upms.core.identity.permission.service;

import net.junanw.upms.core.identity.permission.mapper.PermissionMapper;
import net.junanw.upms.core.identity.permission.entity.PermissionEntity;
import net.junanw.upms.core.identity.permission.model.request.PermissionSaveRequest;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PermissionServiceImplTest {

    @Test
    void createShouldRejectInactivePermissionModuleBeforeSaving() {
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        UserContextService userContextService = mock(UserContextService.class);
        PermissionModuleService permissionModuleService = mock(PermissionModuleService.class);
        PermissionService service = new PermissionServiceImpl(permissionMapper, userContextService, permissionModuleService);
        doThrow(new BusinessException(400, "权限模块已停用: user"))
                .when(permissionModuleService).validateActiveModule("user");

        BusinessException exception = assertThrows(BusinessException.class, () -> service.create(new PermissionSaveRequest(
                "新增用户",
                "system:user:create",
                "BUTTON",
                "USER",
                "CREATE",
                "user",
                1,
                null
        )));

        assertEquals(400, exception.getCode());
        assertEquals("权限模块已停用: user", exception.getMessage());
        verify(permissionMapper, never()).save(any(PermissionEntity.class));
    }
}
