package net.junanw.upms.core.identity.password;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import net.junanw.upms.core.identity.password.entity.PasswordCredentialEntity;
import net.junanw.upms.core.identity.password.mapper.PasswordCredentialMapper;
import net.junanw.upms.core.identity.profile.model.UserProfileSnapshot;
import net.junanw.upms.core.identity.verification.code.target.VerificationTargetProfileHandler;
import net.junanw.upms.core.identity.verification.code.target.VerificationTargetProfileHandlerRegistry;
import net.junanw.upms.core.identity.verification.model.VerificationScene;
import net.junanw.upms.core.identity.verification.model.VerificationTargetType;
import net.junanw.upms.core.identity.verification.service.VerificationCodeService;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResetPasswordProcessorTest {

    @Test
    void shouldVerifyCodeLoadProfileAndUpdatePassword() {
        VerificationTargetProfileHandler handler = mock(VerificationTargetProfileHandler.class);
        VerificationCodeService verificationCodeService = mock(VerificationCodeService.class);
        PasswordCredentialMapper passwordCredentialMapper = mock(PasswordCredentialMapper.class);
        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
        when(handler.targetType()).thenReturn(VerificationTargetType.MOBILE);
        ResetPasswordProcessor processor = new ResetPasswordProcessorImpl(
                new VerificationTargetProfileHandlerRegistry(List.of(handler)),
                verificationCodeService,
                passwordCredentialMapper,
                passwordCredentialUpdater
        );

        PasswordCredentialEntity credential = new PasswordCredentialEntity();
        credential.setUserId(1L);
        credential.setPasswordHash("old");
        credential.setPasswordAlgo("BCRYPT");
        credential.setTemporaryFlag(false);
        credential.setFailedCount(0);
        credential.setCreatedAt(LocalDateTime.now());
        credential.setUpdatedAt(LocalDateTime.now());
        UserProfileSnapshot snapshot = new UserProfileSnapshot(
                1L, "admin", "管理员", "13800138000", "admin@example.com", null, null,
                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
        );
        when(handler.normalizeRequiredTarget("13800138000")).thenReturn("13800138000");
        when(handler.requireProfile("13800138000")).thenReturn(snapshot);

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PasswordCredentialEntity> mockChain = spy(new QueryChain<>(passwordCredentialMapper));
            queryChainMock.when(() -> QueryChain.of(passwordCredentialMapper)).thenReturn(mockChain);
            doReturn(credential).when(mockChain).get();

            processor.reset(new ResetPasswordCommand(VerificationTargetType.MOBILE, "13800138000", "123456", "new-secret"));

            verify(verificationCodeService).verifyLatest(VerificationTargetType.MOBILE, "13800138000", VerificationScene.RESET_PASSWORD, "123456");
            ArgumentCaptor<PasswordCredentialEntity> credentialCaptor = ArgumentCaptor.forClass(PasswordCredentialEntity.class);
            verify(passwordCredentialUpdater).updatePassword(eq(1L), credentialCaptor.capture(), eq("new-secret"), eq(false));
            assertEquals(1L, credentialCaptor.getValue().getUserId());
        }
    }

    @Test
    void shouldFailWhenCredentialMissing() {
        VerificationTargetProfileHandler handler = mock(VerificationTargetProfileHandler.class);
        VerificationCodeService verificationCodeService = mock(VerificationCodeService.class);
        PasswordCredentialMapper passwordCredentialMapper = mock(PasswordCredentialMapper.class);
        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
        when(handler.targetType()).thenReturn(VerificationTargetType.EMAIL);
        ResetPasswordProcessor processor = new ResetPasswordProcessorImpl(
                new VerificationTargetProfileHandlerRegistry(List.of(handler)),
                verificationCodeService,
                passwordCredentialMapper,
                passwordCredentialUpdater
        );

        UserProfileSnapshot snapshot = new UserProfileSnapshot(
                2L, "demo", "演示", null, "demo@example.com", null, null,
                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
        );
        when(handler.normalizeRequiredTarget("demo@example.com")).thenReturn("demo@example.com");
        when(handler.requireProfile("demo@example.com")).thenReturn(snapshot);

        try (MockedStatic<QueryChain> queryChainMock = mockStatic(QueryChain.class)) {
            QueryChain<PasswordCredentialEntity> mockChain = spy(new QueryChain<>(passwordCredentialMapper));
            queryChainMock.when(() -> QueryChain.of(passwordCredentialMapper)).thenReturn(mockChain);
            doReturn(null).when(mockChain).get();

            BusinessException exception = assertThrows(BusinessException.class, () -> processor.reset(
                    new ResetPasswordCommand(VerificationTargetType.EMAIL, "demo@example.com", "654321", "new-secret")
            ));

            assertEquals(404, exception.getCode());
            assertEquals("密码凭证不存在", exception.getMessage());
            verify(verificationCodeService).verifyLatest(VerificationTargetType.EMAIL, "demo@example.com", VerificationScene.RESET_PASSWORD, "654321");
            verify(passwordCredentialUpdater, never()).updatePassword(anyLong(), any(), anyString(), anyBoolean());
        }
    }
}
