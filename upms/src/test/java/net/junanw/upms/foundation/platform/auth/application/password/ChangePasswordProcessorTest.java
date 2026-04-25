package net.junanw.upms.foundation.platform.auth.application.password;

class ChangePasswordProcessorTest {

//    @Test
//    void shouldUpdatePasswordWhenOldPasswordMatches() {
//        UserContextService userContextService = mock(UserContextService.class);
//        PasswordCredentialRepository passwordCredentialRepository = mock(PasswordCredentialRepository.class);
//        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
//        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
//        ChangePasswordProcessor processor = new ChangePasswordProcessorImpl(
//                userContextService,
//                passwordCredentialRepository,
//                passwordEncoder,
//                passwordCredentialUpdater
//        );
//
//        UserProfileSnapshot snapshot = new UserProfileSnapshot(
//                1L, "admin", "管理员", null, null, null, null,
//                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
//        );
//        PasswordCredentialEntity credential = new PasswordCredentialEntity();
//        credential.setUserId(1L);
//        credential.setPasswordHash("hashed-old");
//        credential.setPasswordAlgo("BCRYPT");
//        credential.setTemporaryFlag(false);
//        credential.setFailedCount(0);
//        credential.setCreatedAt(LocalDateTime.now());
//        credential.setUpdatedAt(LocalDateTime.now());
//        when(userContextService.requireByUsername("admin")).thenReturn(snapshot);
//        when(passwordCredentialRepository.findByUserId(1L)).thenReturn(Optional.of(credential));
//        when(passwordEncoder.matches("old-secret", "hashed-old")).thenReturn(true);
//
//        processor.change(new ChangePasswordCommand("admin", "old-secret", "new-secret"));
//
//        ArgumentCaptor<PasswordCredentialEntity> credentialCaptor = ArgumentCaptor.forClass(PasswordCredentialEntity.class);
//        verify(passwordCredentialUpdater).updatePassword(eq(1L), credentialCaptor.capture(), eq("new-secret"), eq(false));
//        assertEquals(1L, credentialCaptor.getValue().getUserId());
//    }
//
//    @Test
//    void shouldFailWhenCredentialMissing() {
//        UserContextService userContextService = mock(UserContextService.class);
//        PasswordCredentialRepository passwordCredentialRepository = mock(PasswordCredentialRepository.class);
//        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
//        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
//        ChangePasswordProcessor processor = new ChangePasswordProcessorImpl(
//                userContextService,
//                passwordCredentialRepository,
//                passwordEncoder,
//                passwordCredentialUpdater
//        );
//
//        UserProfileSnapshot snapshot = new UserProfileSnapshot(
//                2L, "demo", "演示", null, null, null, null,
//                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
//        );
//        when(userContextService.requireByUsername("demo")).thenReturn(snapshot);
//        when(passwordCredentialRepository.findByUserId(2L)).thenReturn(Optional.empty());
//
//        BusinessException exception = assertThrows(BusinessException.class,
//                () -> processor.change(new ChangePasswordCommand("demo", "old-secret", "new-secret")));
//
//        assertEquals(404, exception.getCode());
//        assertEquals("密码凭证不存在", exception.getMessage());
//        verify(passwordCredentialUpdater, never()).updatePassword(anyLong(), any(), anyString(), anyBoolean());
//    }
//
//    @Test
//    void shouldFailWhenOldPasswordDoesNotMatch() {
//        UserContextService userContextService = mock(UserContextService.class);
//        PasswordCredentialRepository passwordCredentialRepository = mock(PasswordCredentialRepository.class);
//        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
//        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
//        ChangePasswordProcessor processor = new ChangePasswordProcessorImpl(
//                userContextService,
//                passwordCredentialRepository,
//                passwordEncoder,
//                passwordCredentialUpdater
//        );
//
//        UserProfileSnapshot snapshot = new UserProfileSnapshot(
//                3L, "tester", "测试", null, null, null, null,
//                null, null, null, null, null, null, 1, false, null, List.of(), List.of()
//        );
//        PasswordCredentialEntity credential = new PasswordCredentialEntity();
//        credential.setUserId(3L);
//        credential.setPasswordHash("hashed-old");
//        credential.setPasswordAlgo("BCRYPT");
//        credential.setTemporaryFlag(false);
//        credential.setFailedCount(0);
//        credential.setCreatedAt(LocalDateTime.now());
//        credential.setUpdatedAt(LocalDateTime.now());
//        when(userContextService.requireByUsername("tester")).thenReturn(snapshot);
//        when(passwordCredentialRepository.findByUserId(3L)).thenReturn(Optional.of(credential));
//        when(passwordEncoder.matches("wrong-old", "hashed-old")).thenReturn(false);
//
//        BusinessException exception = assertThrows(BusinessException.class,
//                () -> processor.change(new ChangePasswordCommand("tester", "wrong-old", "new-secret")));
//
//        assertEquals(400, exception.getCode());
//        assertEquals("原密码不正确", exception.getMessage());
//        verify(passwordCredentialUpdater, never()).updatePassword(anyLong(), any(), anyString(), anyBoolean());
//    }
//
//    @Test
//    void shouldPropagateUserLookupFailure() {
//        UserContextService userContextService = mock(UserContextService.class);
//        PasswordCredentialRepository passwordCredentialRepository = mock(PasswordCredentialRepository.class);
//        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
//        PasswordCredentialUpdater passwordCredentialUpdater = mock(PasswordCredentialUpdater.class);
//        ChangePasswordProcessor processor = new ChangePasswordProcessorImpl(
//                userContextService,
//                passwordCredentialRepository,
//                passwordEncoder,
//                passwordCredentialUpdater
//        );
//        when(userContextService.requireByUsername("ghost"))
//                .thenThrow(new BusinessException(404, "用户不存在"));
//
//        BusinessException exception = assertThrows(BusinessException.class,
//                () -> processor.change(new ChangePasswordCommand("ghost", "old-secret", "new-secret")));
//
//        assertEquals(404, exception.getCode());
//        assertEquals("用户不存在", exception.getMessage());
//        verify(passwordCredentialRepository, never()).findByUserId(anyLong());
//        verify(passwordCredentialUpdater, never()).updatePassword(anyLong(), any(), anyString(), anyBoolean());
//    }
}
