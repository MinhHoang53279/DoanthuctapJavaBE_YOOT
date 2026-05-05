package com.yoot.flashcard.modules.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoot.flashcard.MongoIntegrationTestSupport;
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
class ContentControllerIntegrationTests extends MongoIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void learnerCanManagePrivateDeckAndFlashcards() throws Exception {
        JsonNode languages = performJson(get("/api/v1/languages")).get("data");
        JsonNode topics = performJson(get("/api/v1/topics")).get("data");
        Long englishId = findIdByField(languages, "code", "en");
        Long vietnameseId = findIdByField(languages, "code", "vi");
        Long dailyLifeId = findIdByField(topics, "name", "Daily Life");

        String accessToken = registerAndLogin();

        mockMvc.perform(post("/api/v1/decks")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Private Daily Vocabulary",
                                  "description": "Private deck for learner",
                                  "sourceLanguageId": %d,
                                  "targetLanguageId": %d,
                                  "topicId": %d,
                                  "visibility": "PRIVATE"
                                }
                                """.formatted(vietnameseId, englishId, dailyLifeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.visibility").value("PRIVATE"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));

        JsonNode deckList = performJson(get("/api/v1/decks")
                .header("Authorization", "Bearer " + accessToken));
        Long deckId = deckList.at("/data/items/0/id").asLong();
        assertThat(deckId).isPositive();

        mockMvc.perform(get("/api/v1/decks/{id}", deckId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/decks/{id}", deckId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Private Daily Vocabulary"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/decks/{id}", deckId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Private Daily Vocabulary",
                                  "description": "Updated private deck",
                                  "sourceLanguageId": %d,
                                  "targetLanguageId": %d,
                                  "topicId": %d,
                                  "visibility": "PRIVATE"
                                }
                                """.formatted(vietnameseId, englishId, dailyLifeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Private Daily Vocabulary"));

        JsonNode flashcard = performJson(post("/api/v1/decks/{deckId}/flashcards", deckId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "frontText": "apple",
                                  "backText": "qua tao",
                                  "pronunciation": "/ˈæp.əl/",
                                  "exampleSentence": "I eat an apple every day.",
                                  "note": "Noun",
                                  "difficultyLevel": "EASY",
                                  "cardOrder": 1
                                }
                                """), 201);
        Long flashcardId = flashcard.at("/data/id").asLong();
        assertThat(flashcard.at("/data/frontText").asText()).isEqualTo("apple");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/flashcards/{id}", flashcardId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "frontText": "green apple",
                                  "backText": "qua tao xanh",
                                  "difficultyLevel": "MEDIUM",
                                  "cardOrder": 2
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.frontText").value("green apple"))
                .andExpect(jsonPath("$.data.difficultyLevel").value("MEDIUM"));

        mockMvc.perform(get("/api/v1/decks/{deckId}/flashcards", deckId))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/decks/{deckId}/flashcards", deckId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].frontText").value("green apple"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/flashcards/{id}", flashcardId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/decks/{deckId}/flashcards", deckId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalItems").value(0));

        mockMvc.perform(post("/api/v1/decks")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Learner Public Deck",
                                  "visibility": "PUBLIC"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/decks/{id}", deckId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/decks/{id}", deckId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    private String registerAndLogin() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "content-learner@example.com",
                                  "username": "content_learner",
                                  "password": "Password@123",
                                  "fullName": "Content Learner"
                                }
                                """))
                .andExpect(status().isCreated());

        JsonNode login = performJson(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "usernameOrEmail": "content-learner@example.com",
                          "password": "Password@123"
                        }
                        """));
        return login.at("/data/accessToken").asText();
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
