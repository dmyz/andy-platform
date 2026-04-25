package net.junanw.upms.support.audit.operation.controller;

import net.junanw.upms.foundation.shared.api.ApiResponse;
import net.junanw.upms.foundation.shared.api.PageResponse;
import net.junanw.upms.support.audit.operation.model.view.OperationAuditDetailView;
import net.junanw.upms.support.audit.operation.model.view.OperationAuditPageItem;
import net.junanw.upms.support.audit.operation.service.OperationAuditService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationAuditControllerTest {

    @Test
    void pageShouldUseDefaultPagingArguments() {
        CapturingOperationAuditService service = new CapturingOperationAuditService();
        OperationAuditController controller = new OperationAuditController(service);

        ApiResponse<PageResponse<OperationAuditPageItem>> response = controller.page("管理员", "用户", "CREATE", "SUCCESS", null, null, null, null);

        assertEquals(1, service.pageNum);
        assertEquals(10, service.pageSize);
        assertEquals(1, response.data().pageNum());
        assertEquals(10, response.data().pageSize());
    }

    @Test
    void exportShouldBuildUtf8CsvResponse() {
        CapturingOperationAuditService service = new CapturingOperationAuditService();
        service.exportItems = List.of(new OperationAuditPageItem(
                "1",
                "超级管理员",
                "用户管理",
                "CREATE",
                "/admin/user",
                126L,
                LocalDateTime.of(2026, 4, 2, 10, 15),
                "SUCCESS"
        ));
        OperationAuditController controller = new OperationAuditController(service);

        byte[] body = controller.export("超级管理员", "用户", "CREATE", "SUCCESS", null, null).getBody();
        String csv = new String(body, StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFF操作人,模块名称,操作类型,请求地址,耗时(ms),操作时间,操作结果"));
        assertTrue(csv.contains("\"超级管理员\",\"用户管理\",\"CREATE\",\"/admin/user\",126"));
        assertTrue(csv.contains("\"2026-04-02 10:15:00\""));
    }

    private static final class CapturingOperationAuditService implements OperationAuditService {

        private int pageNum;
        private int pageSize;
        private List<OperationAuditPageItem> exportItems = List.of();

        @Override
        public PageResponse<OperationAuditPageItem> page(String operatorName, String moduleName, String actionType, String result, LocalDateTime startTime, LocalDateTime endTime, int pageNum, int pageSize) {
            this.pageNum = pageNum;
            this.pageSize = pageSize;
            return PageResponse.of(List.of(), 0, pageNum, pageSize);
        }

        @Override
        public OperationAuditDetailView detail(String id) {
            return null;
        }

        @Override
        public List<OperationAuditPageItem> export(String operatorName, String moduleName, String actionType, String result, LocalDateTime startTime, LocalDateTime endTime) {
            return exportItems;
        }
    }
}
