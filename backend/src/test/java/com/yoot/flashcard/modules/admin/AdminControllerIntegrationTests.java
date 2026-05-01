package com.yoot.flashcard.modules.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoot.flashcard.modules.identity.entity.Role;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.repository.RoleRepository;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void adminCanModerateDeckManageReportsLockUserAndReadAuditLogs() throws Exception {
        TestUser admin = registerPromoteAndLogin("phase6_admin", "phase6-admin@example.com", "ADMIN");
        TestUser manager = registerPromoteAndLogin("phase6_manager", "phase6-manager@example.com", "CONTENT_MANAGER");
        TestUser learner = registerPromoteAndLogin("phase6_learner", "phase6-learner@example.com", "LEARNER");

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", bearer(learner.accessToken())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", bearer(admin.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").isNumber())
                .andExpect(jsonPath("$.data.totalDecks").isNumber())
                .andExpect(jsonPath("$.data.totalFlashcards").isNumber())
                .andExpect(jsonPath("$.data.totalStudySessions").isNumber());

        Long pendingDeckId = createPendingPublicDeck(manager.accessToken());
        mockMvc.perform(post("/api/v1/admin/decks/{id}/approve", pendingDeckId)
                        .header("Authorization", bearer(admin.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.approvedBy").value(admin.id()));

        JsonNode report = performJson(post("/api/v1/reports")
                .header("Authorization", bearer(learner.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "targetType": "DECK",
                          "targetId": %d,
                          "reason": "The deck needs admin review"
                        }
                        """.formatted(pendingDeckId)), 201);
        Long reportId = report.at("/data/id").asLong();

        JsonNode openReports = performJson(get("/api/v1/admin/reports")
                .header("Authorization", bearer(admin.accessToken()))
                .param("status", "OPEN"));
        assertThat(containsId(openReports.at("/data/items"), reportId)).isTrue();

        mockMvc.perform(patch("/api/v1/admin/reports/{id}/status", reportId)
                        .header("Authorization", bearer(admin.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "RESOLVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.resolvedAt").isNotEmpty());

        mockMvc.perform(post("/api/v1/admin/users/{id}/lock", learner.id())
                        .header("Authorization", bearer(admin.accessToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LOCKED"));

        mockMvc.perform(get("/api/v1/progress/me")
                        .header("Authorization", bearer(learner.accessToken())))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usernameOrEmail": "phase6-learner@example.com",
                                  "password": "Password@123"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        JsonNode deckAuditLogs = performJson(get("/api/v1/admin/audit-logs")
                .header("Authorization", bearer(admin.accessToken()))
                .param("action", "DECK_APPROVED")
                .param("resourceType", "DECK")
                .param("resourceId", pendingDeckId.toString()));
        assertThat(deckAuditLogs.at("/data/totalItems").asLong()).isGreaterThanOrEqualTo(1);

        JsonNode userAuditLogs = performJson(get("/api/v1/admin/audit-logs")
                .header("Authorization", bearer(admin.accessToken()))
                .param("action", "USER_LOCKED")
                .param("resourceType", "USER")
                .param("resourceId", learner.id().toString()));
        assertThat(userAuditLogs.at("/data/totalItems").asLong()).isGreaterThanOrEqualTo(1);
    }

    private Long createPendingPublicDeck(String accessToken) throws Exception {
        JsonNode deck = performJson(post("/api/v1/decks")
                .header("Authorization", bearer(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Phase 6 Public Deck",
                          "description": "Deck for admin approval flow",
                          "visibility": "PUBLIC"
                        }
                        """), 201);

        Long deckId = deck.at("/data/id").asLong();
        mockMvc.perform(post("/api/v1/decks/{id}/submit-review", deckId)
                        .header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
        return deckId;
    }

    private TestUser registerPromoteAndLogin(String username, String email, String roleName) throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "username": "%s",
                                  "password": "Password@123",
                                  "fullName": "%s"
                                }
                                """.formatted(email, username, username)))
                .andExpect(status().isCreated());

        User registeredUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        User user = userRepository.findWithRolesById(registeredUser.getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.saveAndFlush(user);

        JsonNode login = performJson(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "usernameOrEmail": "%s",
                          "password": "Password@123"
                        }
                        """.formatted(email)));
        return new TestUser(user.getId(), login.at("/data/accessToken").asText());
    }

    private JsonNode performJson(org.springframework.test.web.servlet.RequestBuilder requestBuilder) throws Exception {
        return performJson(requestBuilder, 200);
    }

    private JsonNode performJson(org.springframework.test.web.servlet.RequestBuilder requestBuilder, int expectedStatus) throws Exception {
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response);
    }

    private boolean containsId(JsonNode nodes, Long id) {
        for (JsonNode node : nodes) {
            if (node.get("id").asLong() == id) {
                return true;
            }
        }
        return false;
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private record TestUser(Long id, String accessToken) {
    }
}
