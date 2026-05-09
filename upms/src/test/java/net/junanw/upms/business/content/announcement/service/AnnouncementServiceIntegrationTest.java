package net.junanw.upms.business.content.announcement.service;

import net.junanw.upms.business.content.announcement.model.view.AnnouncementDetailView;
import net.junanw.upms.business.content.announcement.model.request.AnnouncementSaveRequest;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.infrastructure.shared.exception.BusinessException;
import net.junanw.upms.infrastructure.test.support.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnouncementServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AnnouncementService announcementService;

    @Test
    void createPublishMyPageAndMarkReadShouldUseXbatisFlow() {
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

        PageResponse<net.junanw.upms.business.content.announcement.model.view.AnnouncementMyItem> beforeRead = announcementService.myPage("admin", title, false, 1, 20);
        assertTrue(beforeRead.list().stream().anyMatch(item -> created.id().equals(item.id()) && !item.read()));

        announcementService.markRead("admin", created.id());

        PageResponse<net.junanw.upms.business.content.announcement.model.view.AnnouncementMyItem> afterRead = announcementService.myPage("admin", title, true, 1, 20);
        assertTrue(afterRead.list().stream().anyMatch(item -> created.id().equals(item.id()) && item.read()));
    }

    @Test
    void createShouldEscapeXssPayloadAndRejectSensitiveWord() {
        AnnouncementDetailView created = announcementService.create("admin", new AnnouncementSaveRequest(
                "XSS集成测试-" + System.nanoTime(),
                "<script>alert('x')</script>",
                "SYSTEM",
                "ALL",
                null,
                false
        ));

        assertEquals("&lt;script&gt;alert(&#x27;x&#x27;)&lt;&#x2F;script&gt;", created.content());
        assertThrows(BusinessException.class, () -> announcementService.create("admin", new AnnouncementSaveRequest(
                "blocked-example",
                "正常内容",
                "SYSTEM",
                "ALL",
                null,
                false
        )));
    }
}
