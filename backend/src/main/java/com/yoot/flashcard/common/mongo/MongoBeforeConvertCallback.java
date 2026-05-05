package com.yoot.flashcard.common.mongo;

import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Component
public class MongoBeforeConvertCallback implements BeforeConvertCallback<Object> {

    private final MongoSequenceGenerator sequenceGenerator;

    public MongoBeforeConvertCallback(MongoSequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Object onBeforeConvert(Object entity, String collection) {
        assignId(entity);
        applyTimestamps(entity);
        return entity;
    }

    private void assignId(Object entity) {
        SequencedDocument sequencedDocument = entity.getClass().getAnnotation(SequencedDocument.class);
        if (sequencedDocument == null) {
            return;
        }

        Field idField = ReflectionUtils.findField(entity.getClass(), "id");
        if (idField == null) {
            return;
        }
        ReflectionUtils.makeAccessible(idField);
        Object currentValue = ReflectionUtils.getField(idField, entity);
        if (currentValue == null) {
            ReflectionUtils.setField(idField, entity, sequenceGenerator.next(sequencedDocument.value()));
        }
    }

    private void applyTimestamps(Object entity) {
        LocalDateTime now = LocalDateTime.now();
        Field createdAt = ReflectionUtils.findField(entity.getClass(), "createdAt");
        if (createdAt != null) {
            ReflectionUtils.makeAccessible(createdAt);
            if (ReflectionUtils.getField(createdAt, entity) == null) {
                ReflectionUtils.setField(createdAt, entity, now);
            }
        }

        Field updatedAt = ReflectionUtils.findField(entity.getClass(), "updatedAt");
        if (updatedAt != null) {
            ReflectionUtils.makeAccessible(updatedAt);
            ReflectionUtils.setField(updatedAt, entity, now);
        }
    }
}
