package net.junanw.upms.application.upms.identity.navigation.controller;

import cn.xbatis.core.sql.executor.chain.QueryChain;
import jakarta.servlet.Filter;
import net.junanw.upms.infrastructure.test.support.IntegrationTestBase;
import net.junanw.upms.core.identity.navigation.entity.NavigationEntity;
import net.junanw.upms.core.identity.navigation.mapper.NavigationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

class NavigationControllerIntegrationTest extends IntegrationTestBase {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\\\"accessToken\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\":\\\"([^\\\"]+)\\\"");

    @Autowired
    private NavigationMapper navigationMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(webApplicationContext.getBeansOfType(Filter.class).values().toArray(Filter[]::new))
                .build();
    }

    @Test
    void treeAndDetailShouldExposeSeededNavigation() throws Exception {
        String token = loginAs("admin");
        NavigationEntity systemNavigation = requireNavigation("system_navigation");

        mockMvc.perform(get("/admin/navigation/tree")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data..routePath", hasItem("/system/navigation")))
                .andExpect(jsonPath("$.data..routePath", hasItem("/system/permission-module")));

        mockMvc.perform(get("/admin/navigation/{id}", systemNavigation.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("导航管理"))
                .andExpect(jsonPath("$.data.routePath").value("/system/navigation"));
    }

    @Test
    void createUpdateAssignPermissionsAndDeleteShouldUseXbatisFlow() throws Exception {
        String token = loginAs("admin");
        NavigationEntity systemRoot = requireNavigation("system");
        String routePath = "/system/navigation-it-" + System.nanoTime();

        MvcResult createResult = mockMvc.perform(post("/admin/navigation")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId":"%s",
                                  "name":"导航集成测试",
                                  "type":"PAGE",
                                  "routePath":"%s",
                                  "componentPath":"views/system/navigation/integration.vue",
                                  "externalUrl":"",
                                  "icon":"catalog",
                                  "sortOrder":99,
                                  "visible":true,
                                  "status":1
                                }
                                """.formatted(systemRoot.getId(), routePath)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.routePath").value(routePath))
                .andReturn();
        String navigationId = extractId(createResult.getResponse().getContentAsString());
        assertFalse(navigationId.isBlank());

        mockMvc.perform(post("/admin/navigation/{id}/permissions", navigationId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"permissionCodes":["system:navigation:view","system:permission:view"]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/admin/navigation/{id}/permissions", navigationId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data..code", hasItems("system:navigation:view", "system:permission:view")));

        mockMvc.perform(put("/admin/navigation/{id}", navigationId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "parentId":"%s",
                                  "name":"导航集成测试-已更新",
                                  "type":"PAGE",
                                  "routePath":"%s",
                                  "componentPath":"views/system/navigation/integration-updated.vue",
                                  "externalUrl":"",
                                  "icon":"secured",
                                  "sortOrder":100,
                                  "visible":true,
                                  "status":1
                                }
                                """.formatted(systemRoot.getId(), routePath)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("导航集成测试-已更新"))
                .andExpect(jsonPath("$.data.icon").value("secured"));

        mockMvc.perform(delete("/admin/navigation/{id}", navigationId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        NavigationEntity entity = QueryChain.of(navigationMapper)
                .eq(NavigationEntity::getId, Long.valueOf(navigationId))
                .eq(NavigationEntity::getDeleted, false)
                .get();
        assertTrue(entity == null);
    }

    private NavigationEntity requireNavigation(String navCode) {
        NavigationEntity entity = QueryChain.of(navigationMapper)
                .eq(NavigationEntity::getNavCode, navCode)
                .eq(NavigationEntity::getDeleted, false)
                .get();
        if (entity == null) {
            throw new AssertionError("missing navigation: " + navCode);
        }
        return entity;
    }

    private String loginAs(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"grantType":"PASSWORD","username":"%s","password":"admin"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andReturn();
        return extractAccessToken(result.getResponse().getContentAsString());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String extractAccessToken(String responseBody) {
        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new AssertionError("token not found in response: " + responseBody);
    }

    private String extractId(String responseBody) {
        Matcher matcher = ID_PATTERN.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new AssertionError("id not found in response: " + responseBody);
    }
}
