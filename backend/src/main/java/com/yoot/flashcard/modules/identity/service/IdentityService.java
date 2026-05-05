package com.yoot.flashcard.modules.identity.service;

import com.yoot.flashcard.common.exception.ResourceNotFoundException;
import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.modules.admin.service.AuditLogService;
import com.yoot.flashcard.modules.identity.dto.UpdateProfileRequest;
import com.yoot.flashcard.modules.identity.dto.UserDetailResponse;
import com.yoot.flashcard.modules.identity.dto.UserSummaryResponse;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.entity.UserProfile;
import com.yoot.flashcard.modules.identity.entity.UserStatus;
import com.yoot.flashcard.modules.identity.mapper.UserMapper;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class IdentityService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    private final MongoTemplate mongoTemplate;

    public IdentityService(
            UserRepository userRepository,
            UserMapper userMapper,
            AuditLogService auditLogService,
            MongoTemplate mongoTemplate
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.auditLogService = auditLogService;
        this.mongoTemplate = mongoTemplate;
    }

    public PageResponse<UserSummaryResponse> listUsers(int page, int size, String keyword, UserStatus status) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        String normalizedKeyword = normalizeKeyword(keyword);
        List<Criteria> criteria = new ArrayList<>();
        criteria.add(Criteria.where("deletedAt").is(null));
        if (status != null) {
            criteria.add(Criteria.where("status").is(status));
        }
        if (normalizedKeyword != null) {
            Pattern pattern = Pattern.compile(Pattern.quote(normalizedKeyword), Pattern.CASE_INSENSITIVE);
            criteria.add(new Criteria().orOperator(
                    Criteria.where("email").regex(pattern),
                    Criteria.where("username").regex(pattern)
            ));
        }

        Query query = new Query(new Criteria().andOperator(criteria.toArray(Criteria[]::new)));
        long total = mongoTemplate.count(query, User.class);
        List<User> items = mongoTemplate.find(query.with(pageable), User.class);
        Page<UserSummaryResponse> users = new PageImpl<>(items, pageable, total).map(userMapper::toSummary);
        return PageResponse.from(users);
    }

    public UserDetailResponse getUser(Long id) {
        User user = findUser(id);
        return userMapper.toDetail(user);
    }
    public UserDetailResponse updateStatus(Long id, UserStatus status) {
        User user = findUser(id);
        UserStatus previousStatus = user.getStatus();
        user.setStatus(status);
        User saved = userRepository.save(user);
        auditLogService.recordCurrentActor(
                "USER_STATUS_UPDATED",
                "USER",
                saved.getId(),
                "status=%s->%s".formatted(previousStatus, status)
        );
        return userMapper.toDetail(saved);
    }
    public UserDetailResponse updateProfile(Long id, UpdateProfileRequest request) {
        User user = findUser(id);
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            user.setProfile(profile);
        }

        profile.setFullName(request.fullName());
        profile.setAvatarUrl(request.avatarUrl());
        profile.setNativeLanguageCode(request.nativeLanguageCode());
        profile.setTargetLanguageCode(request.targetLanguageCode());
        profile.setTimezone(request.timezone());
        profile.setBio(request.bio());
        return userMapper.toDetail(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findWithRolesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
