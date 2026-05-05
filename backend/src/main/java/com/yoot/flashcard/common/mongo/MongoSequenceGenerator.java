package com.yoot.flashcard.common.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.stereotype.Component;

@Component
public class MongoSequenceGenerator {

    private static final String SEQUENCE_COLLECTION = "database_sequences";

    private final MongoDatabaseFactory mongoDatabaseFactory;

    public MongoSequenceGenerator(MongoDatabaseFactory mongoDatabaseFactory) {
        this.mongoDatabaseFactory = mongoDatabaseFactory;
    }

    public long next(String sequenceName) {
        MongoCollection<Document> sequences = mongoDatabaseFactory.getMongoDatabase()
                .getCollection(SEQUENCE_COLLECTION);
        Document sequence = sequences.findOneAndUpdate(
                Filters.eq("_id", sequenceName),
                Updates.inc("seq", 1L),
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
        );
        if (sequence == null) {
            throw new IllegalStateException("Could not generate MongoDB sequence: " + sequenceName);
        }
        Number value = sequence.get("seq", Number.class);
        if (value == null) {
            throw new IllegalStateException("MongoDB sequence has no value: " + sequenceName);
        }
        return value.longValue();
    }
}
