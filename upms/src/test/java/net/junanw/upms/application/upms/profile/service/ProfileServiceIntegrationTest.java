package net.junanw.upms.application.upms.profile.service;

import net.junanw.upms.application.upms.profile.model.view.ProfileMeView;
import net.junanw.upms.application.upms.profile.model.request.ProfileUpdateRequest;
import net.junanw.upms.infrastructure.test.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProfileServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProfileService profileService;

    @Test
    void meShouldReturnXbatisBackedProfile() {
        ProfileMeView me = profileService.me("admin");

        assertEquals("admin", me.username());
        assertNotNull(me.realName());
        assertFalse(me.realName().isBlank());
    }

    @Test
    void updateMeShouldPersistWithinTransaction() {
        ProfileMeView updated = profileService.updateMe("admin", new ProfileUpdateRequest(
                "管理员-集成测试",
                "EMP-IT",
                "MALE",
                "技术中心",
                "测试岗位",
                "集成测试备注"
        ));

        assertEquals("管理员-集成测试", updated.realName());
        assertEquals("EMP-IT", updated.employeeNo());
        assertEquals("MALE", updated.gender());
        assertEquals("测试岗位", updated.positionName());
        assertEquals("集成测试备注", updated.remark());
    }
}
