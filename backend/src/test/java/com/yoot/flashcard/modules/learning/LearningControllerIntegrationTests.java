package com.yoot.flashcard.modules.learning;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LearningControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void learnerCanReviewCardAndProgressUpdates() throws Exception {
        String accessToken = registerAndLogin();
        Long deckId = createPrivateDeck(accessToken);
        Long flashcardId = createFlashcard(accessToken, deckId);

        JsonNode session = performJson(post("/api/v1/study-sessions/start")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "deckId": %d,
                          "limit": 20
                        }
                        """.formatted(deckId)));
        Long sessionId = session.at("/data/sessionId").asLong();
        assertThat(session.at("/data/cards/0/flashcardId").asLong()).isEqualTo(flashcardId);

        mockMvc.perform(post("/api/v1/reviews/{flashcardId}", flashcardId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studySessionId": %d,
                                  "rating": "GOOD",
                                  "responseTimeMs": 1200
                                }
                                """.formatted(sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.masteryLevel").value("LEARNING"))
                .andExpect(jsonPath("$.data.intervalDays").value(1));

        mockMvc.perform(get("/api/v1/progress/decks/{deckId}", deckId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.learnedCards").value(1))
                .andExpect(jsonPath("$.data.completionRate").value(100.0));

        mockMvc.perform(get("/api/v1/progress/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStreakDays").value(1))
                .andExpect(jsonPath("$.data.bestStreakDays").value(1));

        mockMvc.perform(post("/api/v1/study-sessions/{id}/finish", sessionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    private String registerAndLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "learning-learner@example.com",
                                  "username": "learning_learner",
                                  "password": "Password@123",
                                  "fullName": "Learning Learner"
                                }
                                """))
                .andExpect(status().isCreated());

        JsonNode login = performJson(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "usernameOrEmail": "learning-learner@example.com",
                          "password": "Password@123"
                        }
                        """));
        return login.at("/data/accessToken").asText();
    }

    private Long createPrivateDeck(String accessToken) throws Exception {
        JsonNode languages = performJson(get("/api/v1/languages")).get("data");
        JsonNode topics = performJson(get("/api/v1/topics")).get("data");
        Long englishId = findIdByField(languages, "code", "en");
        Long vietnameseId = findIdByField(languages, "code", "vi");
        Long topicId = findIdByField(topics, "name", "Daily Life");

        JsonNode deck = performJson(post("/api/v1/decks")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Learning Daily Vocabulary",
                          "sourceLanguageId": %d,
                          "targetLanguageId": %d,
                          "topicId": %d,
                          "visibility": "PRIVATE"
                        }
                        """.formatted(vietnameseId, englishId, topicId)), 201);
        return deck.at("/data/id").asLong();
    }

    private Long createFlashcard(String accessToken, Long deckId) throws Exception {
        JsonNode flashcard = performJson(post("/api/v1/decks/{deckId}/flashcards", deckId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "frontText": "book",
                          "backText": "quyen sach",
                          "difficultyLevel": "EASY",
                          "cardOrder": 1
                        }
                        """), 201);
        return flashcard.at("/data/id").asLong();
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

    private Long findIdByField(JsonNode nodes, String field, String value) {
        for (JsonNode node : nodes) {
            if (value.equals(node.get(field).asText())) {
                return node.get("id").asLong();
            }
        }
        throw new IllegalStateException("Missing seeded value: " + value);
    }
}
