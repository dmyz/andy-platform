package net.junanw.upms.foundation.platform.auth.controller;

import net.junanw.upms.foundation.test.support.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import jakarta.servlet.Filter;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends IntegrationTestBase {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\\\"accessToken\\\":\\\"([^\\\"]+)\\\"");

    @org.springframework.beans.factory.annotation.Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(webApplicationContext.getBeansOfType(Filter.class).values().toArray(Filter[]::new))
                .build();
    }

    @Test
    void loginAndCurrentUserShouldUseBearerContract() throws Exception {
        String token = loginAs("admin");

        MvcResult currentUserResult = mockMvc.perform(get("/admin/auth/current-user")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andReturn();

        String body = currentUserResult.getResponse().getContentAsString();
        assertTrue(body.contains("system:navigation:view"));
        assertTrue(body.contains("/system"));
    }

    @Test
    void loginShouldReturnNewTokenPayload() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"grantType":"PASSWORD","username":"admin","password":"admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").isNumber())
                .andReturn();

        assertEquals("success", extractMessage(loginResult.getResponse().getContentAsString()));
    }

    @Test
    void logoutShouldInvalidateTokenImmediately() throws Exception {
        String token = loginAs("admin");

        mockMvc.perform(post("/admin/auth/logout")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data", nullValue()));

        mockMvc.perform(get("/admin/auth/current-user")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或会话失效"))
                .andExpect(jsonPath("$.data", nullValue()));
    }

    @Test
    void unauthenticatedLogoutShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/admin/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("未登录或会话失效"))
                .andExpect(jsonPath("$.data", nullValue()));
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

    private String extractMessage(String responseBody) {
        Pattern pattern = Pattern.compile("\\\"message\\\":\\\"([^\\\"]+)\\\"");
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new AssertionError("message not found in response: " + responseBody);
    }
}
