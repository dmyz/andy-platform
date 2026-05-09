package net.junanw.upms.core.identity.password;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.password.entity.PasswordCredentialEntity;
import net.junanw.upms.core.identity.password.mapper.PasswordCredentialMapper;
import net.junanw.upms.core.identity.profile.context.UserContextService;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.shared.security.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ChangePasswordProcessorTest {

    @Test
    void shouldUpdatePasswordWhenOldPasswordMatches() {
        UserContextService userContextService = mock(UserContextService.class);
        PasswordCredentialMapper passwordCredentialMapper = mock(PasswordCredentialMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
        ChangePasswordProcessor processor = new ChangePasswordProcessorImpl(
                userContextService,
                passwordCredentialMapper,
                passwordEncoder,
                passwordCredentialUpdater
        );

        UserProfileSnapshot snapshot = new UserProfileSnapshot(
                1L, "admin", "管理员", null, null, null, null,
                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
        );
        PasswordCredentialEntity credential = new PasswordCredentialEntity();
        credential.setUserId(1L);
        credential.setPasswordHash("hashed-old");
        credential.setPasswordAlgo("BCRYPT");
        credential.setTemporaryFlag(false);
        credential.setFailedCount(0);
        credential.setCreatedAt(LocalDateTime.now());
        credential.setUpdatedAt(LocalDateTime.now());
        when(userContextService.requireByUsername("admin")).thenReturn(snapshot);

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PasswordCredentialEntity> mockChain = spy(new QueryChain<>(passwordCredentialMapper));
            queryChainMock.when(() -> QueryChain.of(passwordCredentialMapper)).thenReturn(mockChain);
            doReturn(credential).when(mockChain).get();
            when(passwordEncoder.matches("old-secret", "hashed-old")).thenReturn(true);

            processor.change(new ChangePasswordCommand("admin", "old-secret", "new-secret"));

            ArgumentCaptor<PasswordCredentialEntity> credentialCaptor = ArgumentCaptor.forClass(PasswordCredentialEntity.class);
            verify(passwordCredentialUpdater).updatePassword(eq(1L), credentialCaptor.capture(), eq("new-secret"), eq(false));
            assertEquals(1L, credentialCaptor.getValue().getUserId());
        }
    }
//
    @Test
    void shouldFailWhenCredentialMissing() {
        UserContextService userContextService = mock(UserContextService.class);
        PasswordCredentialMapper passwordCredentialMapper = mock(PasswordCredentialMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
        ChangePasswordProcessor processor = new ChangePasswordProcessorImpl(
                userContextService,
                passwordCredentialMapper,
                passwordEncoder,
                passwordCredentialUpdater
        );

        UserProfileSnapshot snapshot = new UserProfileSnapshot(
                2L, "demo", "演示", null, null, null, null,
                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
        );
        when(userContextService.requireByUsername("demo")).thenReturn(snapshot);

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PasswordCredentialEntity> mockChain = spy(new QueryChain<>(passwordCredentialMapper));
            queryChainMock.when(() -> QueryChain.of(passwordCredentialMapper)).thenReturn(mockChain);
            doReturn(null).when(mockChain).get();

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> processor.change(new ChangePasswordCommand("demo", "old-secret", "new-secret")));

            assertEquals(404, exception.getCode());
            assertEquals("密码凭证不存在", exception.getMessage());
            verify(passwordCredentialUpdater, never()).updatePassword(anyLong(), any(), anyString(), anyBoolean());
        }
    }

    @Test
    void shouldFailWhenOldPasswordDoesNotMatch() {
        UserContextService userContextService = mock(UserContextService.class);
        PasswordCredentialMapper passwordCredentialMapper = mock(PasswordCredentialMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
        ChangePasswordProcessor processor = new ChangePasswordProcessorImpl(
                userContextService,
                passwordCredentialMapper,
                passwordEncoder,
                passwordCredentialUpdater
        );

        UserProfileSnapshot snapshot = new UserProfileSnapshot(
                3L, "tester", "测试", null, null, null, null,
                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
        );
        PasswordCredentialEntity credential = new PasswordCredentialEntity();
        credential.setUserId(3L);
        credential.setPasswordHash("hashed-old");
        credential.setPasswordAlgo("BCRYPT");
        credential.setTemporaryFlag(false);
        credential.setFailedCount(0);
        credential.setCreatedAt(LocalDateTime.now());
        credential.setUpdatedAt(LocalDateTime.now());
        when(userContextService.requireByUsername("tester")).thenReturn(snapshot);

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PasswordCredentialEntity> mockChain = spy(new QueryChain<>(passwordCredentialMapper));
            queryChainMock.when(() -> QueryChain.of(passwordCredentialMapper)).thenReturn(mockChain);
            doReturn(credential).when(mockChain).get();
            when(passwordEncoder.matches("wrong-old", "hashed-old")).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> processor.change(new ChangePasswordCommand("tester", "wrong-old", "new-secret")));

            assertEquals(400, exception.getCode());
            assertEquals("原密码不正确", exception.getMessage());
            verify(passwordCredentialUpdater, never()).updatePassword(anyLong(), any(), anyString(), anyBoolean());
        }
    }

    @Test
    void shouldPropagateUserLookupFailure() {
        UserContextService userContextService = mock(UserContextService.class);
        PasswordCredentialMapper passwordCredentialMapper = mock(PasswordCredentialMapper.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
        ChangePasswordProcessor processor = new ChangePasswordProcessorImpl(
                userContextService,
                passwordCredentialMapper,
                passwordEncoder,
                passwordCredentialUpdater
        );
        when(userContextService.requireByUsername("ghost"))
                .thenThrow(new BusinessException(404, "用户不存在"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> processor.change(new ChangePasswordCommand("ghost", "old-secret", "new-secret")));

        assertEquals(404, exception.getCode());
        assertEquals("用户不存在", exception.getMessage());
        verify(passwordCredentialUpdater, never()).updatePassword(anyLong(), any(), anyString(), anyBoolean());
    }
}
