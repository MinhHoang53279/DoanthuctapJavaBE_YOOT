package com.yoot.flashcard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlashcardApplicationTests extends MongoIntegrationTestSupport {

    @Test
    void contextLoads() {
    }
}
