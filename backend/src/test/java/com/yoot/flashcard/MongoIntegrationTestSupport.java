package com.yoot.flashcard;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class MongoIntegrationTestSupport {

    private static final String DEFAULT_TEST_DATABASE = "flashcard_platform_test_" + Long.toHexString(System.nanoTime());
    private static final String DEFAULT_TEST_URI = "mongodb://localhost:27017/" + DEFAULT_TEST_DATABASE;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MongoIntegrationTestSupport::mongoTestUri);
    }

    private static String mongoTestUri() {
        String configuredUri = System.getenv("MONGODB_TEST_URI");
        if (configuredUri == null || configuredUri.isBlank()) {
            return DEFAULT_TEST_URI;
        }
        return configuredUri;
    }
}
