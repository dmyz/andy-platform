package net.junanw.upms.foundation.platform.auth.application.password;

import net.junanw.upms.foundation.platform.auth.application.code.target.VerificationTargetProfileHandler;
import net.junanw.upms.foundation.platform.auth.application.code.target.VerificationTargetProfileHandlerRegistry;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialEntity;
import net.junanw.upms.foundation.platform.auth.application.password.credential.PasswordCredentialRepository;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationScene;
import net.junanw.upms.foundation.platform.auth.verification.model.VerificationTargetType;
import net.junanw.upms.foundation.platform.auth.query.model.UserProfileSnapshot;
import net.junanw.upms.foundation.platform.auth.verification.service.VerificationCodeService;
import net.junanw.upms.foundation.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResetPasswordProcessorTest {

    @Test
    void shouldVerifyCodeLoadProfileAndUpdatePassword() {
        VerificationTargetProfileHandler handler = mock(VerificationTargetProfileHandler.class);
        VerificationCodeService verificationCodeService = mock(VerificationCodeService.class);
        PasswordCredentialRepository passwordCredentialRepository = mock(PasswordCredentialRepository.class);
        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
        when(handler.targetType()).thenReturn(VerificationTargetType.MOBILE);
        ResetPasswordProcessor processor = new ResetPasswordProcessorImpl(
                new VerificationTargetProfileHandlerRegistry(List.of(handler)),
                verificationCodeService,
                passwordCredentialRepository,
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
        when(passwordCredentialRepository.findByUserId(1L)).thenReturn(Optional.of(credential));

        processor.reset(new ResetPasswordCommand(VerificationTargetType.MOBILE, "13800138000", "123456", "new-secret"));

        verify(verificationCodeService).verifyLatest(VerificationTargetType.MOBILE, "13800138000", VerificationScene.RESET_PASSWORD, "123456");
        ArgumentCaptor<PasswordCredentialEntity> credentialCaptor = ArgumentCaptor.forClass(PasswordCredentialEntity.class);
        verify(passwordCredentialUpdater).updatePassword(eq(1L), credentialCaptor.capture(), eq("new-secret"), eq(false));
        assertEquals(1L, credentialCaptor.getValue().getUserId());
    }

    @Test
    void shouldFailWhenCredentialMissing() {
        VerificationTargetProfileHandler handler = mock(VerificationTargetProfileHandler.class);
        VerificationCodeService verificationCodeService = mock(VerificationCodeService.class);
        PasswordCredentialRepository passwordCredentialRepository = mock(PasswordCredentialRepository.class);
        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
        when(handler.targetType()).thenReturn(VerificationTargetType.EMAIL);
        ResetPasswordProcessor processor = new ResetPasswordProcessorImpl(
                new VerificationTargetProfileHandlerRegistry(List.of(handler)),
                verificationCodeService,
                passwordCredentialRepository,
                passwordCredentialUpdater
        );

        UserProfileSnapshot snapshot = new UserProfileSnapshot(
                2L, "demo", "演示", null, "demo@example.com", null, null,
                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
        );
        when(handler.normalizeRequiredTarget("demo@example.com")).thenReturn("demo@example.com");
        when(handler.requireProfile("demo@example.com")).thenReturn(snapshot);
        when(passwordCredentialRepository.findByUserId(2L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> processor.reset(
                new ResetPasswordCommand(VerificationTargetType.EMAIL, "demo@example.com", "654321", "new-secret")
        ));

        assertEquals(404, exception.getCode());
        assertEquals("密码凭证不存在", exception.getMessage());
        verify(verificationCodeService).verifyLatest(VerificationTargetType.EMAIL, "demo@example.com", VerificationScene.RESET_PASSWORD, "654321");
        verify(passwordCredentialUpdater, never()).updatePassword(anyLong(), any(), anyString(), anyBoolean());
    }
}
