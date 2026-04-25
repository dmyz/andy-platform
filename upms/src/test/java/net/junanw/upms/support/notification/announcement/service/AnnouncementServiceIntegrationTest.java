package net.junanw.upms.support.notification.announcement.service;

import net.junanw.upms.support.notification.announcement.model.view.AnnouncementDetailView;
import net.junanw.upms.support.notification.announcement.model.request.AnnouncementSaveRequest;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.foundation.test.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnouncementServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AnnouncementService announcementService;

    @Test
    void createPublishMyPageAndMarkReadShouldUseJpaFlow() {
        String title = "集成测试公告-" + System.nanoTime();
        AnnouncementDetailView created = announcementService.create("admin", new AnnouncementSaveRequest(
                title,
                "集成测试公告内容",
                "SYSTEM",
                "ALL",
                null,
                true
        ));
        assertEquals("DRAFT", created.status());

        AnnouncementDetailView published = announcementService.publish(created.id());
        assertEquals("PUBLISHED", published.status());

        PageResponse<net.junanw.upms.support.notification.announcement.model.view.AnnouncementMyItem> beforeRead = announcementService.myPage("admin", title, false, 1, 20);
        assertTrue(beforeRead.list().stream().anyMatch(item -> created.id().equals(item.id()) && !item.read()));

        announcementService.markRead("admin", created.id());

        PageResponse<net.junanw.upms.support.notification.announcement.model.view.AnnouncementMyItem> afterRead = announcementService.myPage("admin", title, true, 1, 20);
        assertTrue(afterRead.list().stream().anyMatch(item -> created.id().equals(item.id()) && item.read()));
    }
}
