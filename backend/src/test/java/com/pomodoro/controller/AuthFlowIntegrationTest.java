package com.pomodoro.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pomodoro.mapper.UserMapper;
import com.pomodoro.model.UserRow;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthFlowIntegrationTest.NoOpTransactionConfig.class)
class AuthFlowIntegrationTest {

    private static final String CSRF_COOKIE = "XSRF-TOKEN";
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserMapper userMapper;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void registerCreatesUserWithHashedPassword() throws Exception {
        when(userMapper.insert(any(UserRow.class))).thenAnswer(invocation -> {
            UserRow user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        Csrf csrf = fetchCsrf();

        mockMvc.perform(post("/api/auth/register")
                        .cookie(csrf.cookie())
                        .header(CSRF_HEADER, csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"));

        ArgumentCaptor<UserRow> captor = ArgumentCaptor.forClass(UserRow.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", captor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void registerReturns409ForDuplicateUsername() throws Exception {
        when(userMapper.insert(any(UserRow.class)))
                .thenThrow(new DuplicateKeyException("duplicate username"));
        Csrf csrf = fetchCsrf();

        mockMvc.perform(post("/api/auth/register")
                        .cookie(csrf.cookie())
                        .header(CSRF_HEADER, csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    void registerRejectsInvalidBody() throws Exception {
        Csrf csrf = fetchCsrf();

        mockMvc.perform(post("/api/auth/register")
                        .cookie(csrf.cookie())
                        .header(CSRF_HEADER, csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"","password":"secret123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("用户名不能为空"));
    }

    @Test
    void loginReturns401ForWrongPassword() throws Exception {
        when(userMapper.findByUsername("alice")).thenReturn(userRow("alice", "secret123"));
        Csrf csrf = fetchCsrf();

        mockMvc.perform(post("/api/auth/login")
                        .cookie(csrf.cookie())
                        .header(CSRF_HEADER, csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    }

    @Test
    void loginKeepsSessionUntilLogout() throws Exception {
        when(userMapper.findByUsername("alice")).thenReturn(userRow("alice", "secret123"));
        Csrf csrf = fetchCsrf();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .cookie(csrf.cookie())
                        .header(CSRF_HEADER, csrf.token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("alice"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));

        mockMvc.perform(post("/api/auth/logout")
                        .session(session)
                        .cookie(csrf.cookie())
                        .header(CSRF_HEADER, csrf.token()))
                .andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未登录"));
    }

    @Test
    void protectedEndpointWithoutLoginReturns401() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("未登录"));
    }

    @Test
    void writeRequestWithoutCsrfTokenReturns403() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"alice","password":"secret123"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("权限不足或 CSRF 校验失败"));
    }

    private Csrf fetchCsrf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value(CSRF_HEADER))
                .andReturn();

        Cookie cookie = result.getResponse().getCookie(CSRF_COOKIE);
        assertThat(cookie).isNotNull();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new Csrf(cookie, body.get("token").asText());
    }

    private UserRow userRow(String username, String rawPassword) {
        UserRow user = new UserRow();
        user.setId(1L);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return user;
    }

    private record Csrf(Cookie cookie, String token) {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class NoOpTransactionConfig {

        @Bean
        PlatformTransactionManager transactionManager() {
            return new NoOpTransactionManager();
        }
    }

    static class NoOpTransactionManager implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }
}
