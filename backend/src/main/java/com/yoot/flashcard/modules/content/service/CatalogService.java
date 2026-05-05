package com.yoot.flashcard.modules.content.service;

import com.yoot.flashcard.common.exception.ConflictException;
import com.yoot.flashcard.modules.content.dto.LanguageRequest;
import com.yoot.flashcard.modules.content.dto.LanguageResponse;
import com.yoot.flashcard.modules.content.dto.TagRequest;
import com.yoot.flashcard.modules.content.dto.TagResponse;
import com.yoot.flashcard.modules.content.dto.TopicRequest;
import com.yoot.flashcard.modules.content.dto.TopicResponse;
import com.yoot.flashcard.modules.content.entity.Language;
import com.yoot.flashcard.modules.content.entity.Tag;
import com.yoot.flashcard.modules.content.entity.Topic;
import com.yoot.flashcard.modules.content.mapper.ContentMapper;
import com.yoot.flashcard.modules.content.repository.LanguageRepository;
import com.yoot.flashcard.modules.content.repository.TagRepository;
import com.yoot.flashcard.modules.content.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;
    private final ContentMapper contentMapper;

    public CatalogService(
            LanguageRepository languageRepository,
            TopicRepository topicRepository,
            TagRepository tagRepository,
            ContentMapper contentMapper
    ) {
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
        this.tagRepository = tagRepository;
        this.contentMapper = contentMapper;
    }
    public List<LanguageResponse> listLanguages() {
        return languageRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(contentMapper::toLanguage)
                .toList();
    }
    public LanguageResponse createLanguage(LanguageRequest request) {
        String code = request.code().trim().toLowerCase();
        if (languageRepository.existsByCode(code)) {
            throw new ConflictException("Language code already exists");
        }

        Language language = new Language();
        language.setCode(code);
        language.setName(request.name().trim());
        return contentMapper.toLanguage(languageRepository.save(language));
    }
    public List<TopicResponse> listTopics() {
        return topicRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(contentMapper::toTopic)
                .toList();
    }
    public TopicResponse createTopic(TopicRequest request) {
        String name = request.name().trim();
        if (topicRepository.existsByName(name)) {
            throw new ConflictException("Topic already exists");
        }

        Topic topic = new Topic();
        topic.setName(name);
        topic.setDescription(request.description());
        return contentMapper.toTopic(topicRepository.save(topic));
    }
    public List<TagResponse> listTags() {
        return tagRepository.findAllByOrderByNameAsc().stream()
                .map(contentMapper::toTag)
                .toList();
    }
    public TagResponse createTag(TagRequest request) {
        String name = request.name().trim();
        if (tagRepository.existsByName(name)) {
            throw new ConflictException("Tag already exists");
        }

        Tag tag = new Tag();
        tag.setName(name);
        return contentMapper.toTag(tagRepository.save(tag));
    }
}
