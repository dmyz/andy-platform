package net.junanw.upms.application.upms.audit.login.controller;

import net.junanw.upms.infrastructure.shared.api.ApiResponse;
import net.junanw.upms.infrastructure.shared.api.PageResponse;
import net.junanw.upms.core.security.audit.login.model.view.LoginAuditPageItem;
import net.junanw.upms.core.security.audit.login.service.LoginAuditService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAuditControllerTest {

    @Test
    void pageShouldUseDefaultPagingArguments() {
        CapturingLoginAuditService service = new CapturingLoginAuditService();
        LoginAuditController controller = new LoginAuditController(service);

        ApiResponse<PageResponse<LoginAuditPageItem>> response = controller.page("admin", "PASSWORD", "SUCCESS", null, null, null, null);

        assertEquals(1, service.pageNum);
        assertEquals(10, service.pageSize);
        assertEquals(1, response.data().pageNum());
        assertEquals(10, response.data().pageSize());
    }

    @Test
    void exportShouldBuildUtf8CsvResponse() {
        CapturingLoginAuditService service = new CapturingLoginAuditService();
        service.exportItems = List.of(new LoginAuditPageItem(
                "1",
                "admin",
                "超级管理员",
                "PASSWORD",
                "127.0.0.1",
                "Chrome",
                LocalDateTime.of(2026, 4, 2, 9, 30),
                "SUCCESS",
                null
        ));
        LoginAuditController controller = new LoginAuditController(service);

        byte[] body = controller.export("admin", "PASSWORD", "SUCCESS", null, null).getBody();
        String csv = new String(body, StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFF登录账号,用户姓名,登录方式,IP,浏览器信息,登录时间,登录结果,失败原因"));
        assertTrue(csv.contains("\"admin\",\"超级管理员\",\"PASSWORD\""));
        assertTrue(csv.contains("\"2026-04-02 09:30:00\""));
    }

    private static final class CapturingLoginAuditService implements LoginAuditService {

        private int pageNum;
        private int pageSize;
        private List<LoginAuditPageItem> exportItems = List.of();

        @Override
        public PageResponse<LoginAuditPageItem> page(String username, String loginType, String result, LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize) {
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            return PageResponse.of(List.of(), 0, pageNum, pageSize);
        }

        @Override
        public List<LoginAuditPageItem> export(String username, String loginType, String result, LocalDateTime startTime, LocalDateTime endTime) {
            return exportItems;
        }
    }
}
