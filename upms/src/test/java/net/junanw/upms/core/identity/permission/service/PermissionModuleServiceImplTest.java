package net.junanw.upms.core.identity.permission.service;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.permission.entity.PermissionEntity;
import net.junanw.upms.core.identity.permission.entity.PermissionModuleEntity;
import net.junanw.upms.core.identity.permission.mapper.PermissionMapper;
import net.junanw.upms.core.identity.permission.mapper.PermissionModuleMapper;
import net.junanw.upms.core.identity.permission.model.request.PermissionModuleSaveRequest;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PermissionModuleServiceImplTest {

    @Test
    void optionsShouldMapActiveModulesToFrontendOptions() {
        PermissionModuleMapper permissionModuleMapper = mock(PermissionModuleMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        PermissionModuleService service = new PermissionModuleServiceImpl(permissionModuleMapper, permissionMapper);
        PermissionModuleEntity userModule = module(1L, "user", "用户管理", null, 20, "ACTIVE");
        PermissionModuleEntity profileModule = module(2L, "profile", "个人中心", null, 30, "ACTIVE");

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PermissionModuleEntity> chain = spy(new QueryChain<>(permissionModuleMapper));
            queryChainMock.when(() -> QueryChain.of(permissionModuleMapper)).thenReturn(chain);
            doReturn(List.of(userModule, profileModule)).when(chain).list();

            var options = service.options();

            assertEquals(1, options.size());
            assertEquals("用户管理", options.getFirst().label());
            assertEquals("user", options.getFirst().value());
            assertEquals(20, options.getFirst().sortOrder());
        }
    }

    @Test
    void validateActiveModuleShouldRejectProfileModule() {
        PermissionModuleMapper permissionModuleMapper = mock(PermissionModuleMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        PermissionModuleService service = new PermissionModuleServiceImpl(permissionModuleMapper, permissionMapper);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.validateActiveModule("profile"));

        assertEquals(400, exception.getCode());
        assertEquals("个人中心为登录后公共能力，无需配置权限模块", exception.getMessage());
    }

    @Test
    void createShouldRejectDuplicateModuleCode() {
        PermissionModuleMapper permissionModuleMapper = mock(PermissionModuleMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        PermissionModuleService service = new PermissionModuleServiceImpl(permissionModuleMapper, permissionMapper);
        PermissionModuleEntity existing = module(1L, "user", "用户管理", null, 20, "ACTIVE");

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PermissionModuleEntity> chain = spy(new QueryChain<>(permissionModuleMapper));
            queryChainMock.when(() -> QueryChain.of(permissionModuleMapper)).thenReturn(chain);
            doReturn(existing).when(chain).get();

            BusinessException exception = assertThrows(BusinessException.class, () -> service.create(new PermissionModuleSaveRequest(
                    "用户管理", "USER", null, 20, 1, null
            )));

            assertEquals(400, exception.getCode());
            assertEquals("权限模块编码已存在", exception.getMessage());
            verify(permissionModuleMapper, never()).save(any(PermissionModuleEntity.class));
        }
    }

    @Test
    void deleteShouldRejectModuleWithChildren() {
        PermissionModuleMapper permissionModuleMapper = mock(PermissionModuleMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        PermissionModuleService service = new PermissionModuleServiceImpl(permissionModuleMapper, permissionMapper);
        PermissionModuleEntity systemModule = module(1L, "system", "系统管理", null, 10, "ACTIVE");

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PermissionModuleEntity> requireChain = spy(new QueryChain<>(permissionModuleMapper));
            QueryChain<PermissionModuleEntity> childChain = spy(new QueryChain<>(permissionModuleMapper));
            queryChainMock.when(() -> QueryChain.of(permissionModuleMapper)).thenReturn(requireChain, childChain);
            doReturn(systemModule).when(requireChain).get();
            doReturn(1).when(childChain).count();

            BusinessException exception = assertThrows(BusinessException.class, () -> service.delete("1"));

            assertEquals(400, exception.getCode());
            assertEquals("存在子权限模块，不能删除", exception.getMessage());
            verify(permissionModuleMapper, never()).deleteById(anyLong());
        }
    }

    @Test
    void deleteShouldRejectModuleReferencedByPermission() {
        PermissionModuleMapper permissionModuleMapper = mock(PermissionModuleMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        PermissionModuleService service = new PermissionModuleServiceImpl(permissionModuleMapper, permissionMapper);
        PermissionModuleEntity userModule = module(1L, "user", "用户管理", null, 20, "ACTIVE");

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PermissionModuleEntity> requireChain = spy(new QueryChain<>(permissionModuleMapper));
            QueryChain<PermissionModuleEntity> childChain = spy(new QueryChain<>(permissionModuleMapper));
            QueryChain<PermissionEntity> permissionChain = spy(new QueryChain<>(permissionMapper));
            queryChainMock.when(() -> QueryChain.of(permissionModuleMapper)).thenReturn(requireChain, childChain);
            queryChainMock.when(() -> QueryChain.of(permissionMapper)).thenReturn(permissionChain);
            doReturn(userModule).when(requireChain).get();
            doReturn(0).when(childChain).count();
            doReturn(1).when(permissionChain).count();

            BusinessException exception = assertThrows(BusinessException.class, () -> service.delete("1"));

            assertEquals(400, exception.getCode());
            assertEquals("权限模块已被权限定义引用，不能删除", exception.getMessage());
            verify(permissionModuleMapper, never()).deleteById(anyLong());
        }
    }

    @Test
    void updateShouldRejectParentDescendantCycle() {
        PermissionModuleMapper permissionModuleMapper = mock(PermissionModuleMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        PermissionModuleService service = new PermissionModuleServiceImpl(permissionModuleMapper, permissionMapper);
        PermissionModuleEntity systemModule = module(1L, "system", "系统管理", null, 10, "ACTIVE");
        PermissionModuleEntity userModule = module(2L, "user", "用户管理", "system", 20, "ACTIVE");

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PermissionModuleEntity> requireChain = spy(new QueryChain<>(permissionModuleMapper));
            QueryChain<PermissionModuleEntity> duplicateChain = spy(new QueryChain<>(permissionModuleMapper));
            QueryChain<PermissionModuleEntity> parentChain = spy(new QueryChain<>(permissionModuleMapper));
            queryChainMock.when(() -> QueryChain.of(permissionModuleMapper)).thenReturn(requireChain, duplicateChain, parentChain);
            doReturn(systemModule).when(requireChain).get();
            doReturn(systemModule).when(duplicateChain).get();
            doReturn(userModule).when(parentChain).get();

            BusinessException exception = assertThrows(BusinessException.class, () -> service.update("1", new PermissionModuleSaveRequest(
                    "系统管理", "system", "user", 10, 1, null
            )));

            assertEquals(400, exception.getCode());
            assertEquals("父模块不能选择当前模块的下级模块", exception.getMessage());
            verify(permissionModuleMapper, never()).update(any(PermissionModuleEntity.class));
        }
    }

    private PermissionModuleEntity module(Long id, String code, String name, String parentCode, Integer sortOrder, String status) {
        PermissionModuleEntity entity = new PermissionModuleEntity();
        entity.setId(id);
        entity.setModuleCode(code);
        entity.setModuleName(name);
        entity.setParentCode(parentCode);
        entity.setSortOrder(sortOrder);
        entity.setStatus(status);
        entity.setDeleted(false);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }
}
