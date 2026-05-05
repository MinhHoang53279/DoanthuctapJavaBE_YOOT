package com.yoot.flashcard.common.config;

import com.yoot.flashcard.modules.content.entity.Language;
import com.yoot.flashcard.modules.content.entity.Topic;
import com.yoot.flashcard.modules.content.repository.LanguageRepository;
import com.yoot.flashcard.modules.content.repository.TopicRepository;
import com.yoot.flashcard.modules.identity.entity.Permission;
import com.yoot.flashcard.modules.identity.entity.Role;
import com.yoot.flashcard.modules.identity.repository.PermissionRepository;
import com.yoot.flashcard.modules.identity.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MongoSeedDataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;

    public MongoSeedDataInitializer(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            LanguageRepository languageRepository,
            TopicRepository topicRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, Permission> permissions = seedPermissions();
        seedRoles(permissions);
        seedLanguages();
        seedTopics();
    }

    private Map<String, Permission> seedPermissions() {
        Map<String, String> definitions = new LinkedHashMap<>();
        definitions.put("USER_READ", "Read users");
        definitions.put("USER_MANAGE_STATUS", "Lock and unlock users");
        definitions.put("DECK_CREATE", "Create decks");
        definitions.put("DECK_READ", "Read decks");
        definitions.put("DECK_UPDATE_OWN", "Update own decks");
        definitions.put("DECK_DELETE_OWN", "Delete own decks");
        definitions.put("DECK_APPROVE", "Approve public decks");
        definitions.put("FLASHCARD_CREATE", "Create flashcards");
        definitions.put("FLASHCARD_UPDATE_OWN", "Update own flashcards");
        definitions.put("LEARNING_REVIEW", "Submit learning reviews");
        definitions.put("ADMIN_DASHBOARD_READ", "Read admin dashboard");
        definitions.put("REPORT_READ", "Read content reports");
        definitions.put("REPORT_MANAGE", "Resolve or dismiss content reports");
        definitions.put("AUDIT_LOG_READ", "Read audit logs");

        Map<String, Permission> permissions = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : definitions.entrySet()) {
            Permission permission = permissionRepository.findByCode(entry.getKey())
                    .orElseGet(Permission::new);
            permission.setCode(entry.getKey());
            permission.setDescription(entry.getValue());
            permissions.put(entry.getKey(), permissionRepository.save(permission));
        }
        return permissions;
    }

    private void seedRoles(Map<String, Permission> permissions) {
        saveRole("LEARNER", "Learner role", permissions, Set.of(
                "DECK_CREATE",
                "DECK_READ",
                "DECK_UPDATE_OWN",
                "DECK_DELETE_OWN",
                "FLASHCARD_CREATE",
                "FLASHCARD_UPDATE_OWN",
                "LEARNING_REVIEW"
        ));
        saveRole("CONTENT_MANAGER", "Content manager role", permissions, Set.of(
                "DECK_CREATE",
                "DECK_READ",
                "DECK_UPDATE_OWN",
                "DECK_DELETE_OWN",
                "DECK_APPROVE",
                "FLASHCARD_CREATE",
                "FLASHCARD_UPDATE_OWN",
                "LEARNING_REVIEW"
        ));
        saveRole("ADMIN", "Administrator role", permissions, permissions.keySet());
        saveRole("SUPER_ADMIN", "System owner role", permissions, permissions.keySet());
    }

    private void saveRole(String name, String description, Map<String, Permission> permissions, Set<String> permissionCodes) {
        Role role = roleRepository.findByName(name).orElseGet(Role::new);
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissionCodes.stream()
                .map(permissions::get)
                .collect(Collectors.toSet()));
        roleRepository.save(role);
    }

    private void seedLanguages() {
        saveLanguage("en", "English");
        saveLanguage("vi", "Vietnamese");
        saveLanguage("ja", "Japanese");
        saveLanguage("ko", "Korean");
        saveLanguage("zh", "Chinese");
    }

    private void saveLanguage(String code, String name) {
        Language language = languageRepository.findByCode(code).orElseGet(Language::new);
        language.setCode(code);
        language.setName(name);
        language.setActive(true);
        languageRepository.save(language);
    }

    private void seedTopics() {
        saveTopic("Daily Life", "Common vocabulary for daily life");
        saveTopic("Travel", "Vocabulary for travel situations");
        saveTopic("Business", "Vocabulary for business communication");
        saveTopic("Grammar", "Grammar-focused study content");
        saveTopic("Exam Preparation", "Vocabulary for exams and certificates");
    }

    private void saveTopic(String name, String description) {
        Topic topic = topicRepository.findByName(name).orElseGet(Topic::new);
        topic.setName(name);
        topic.setDescription(description);
        topic.setActive(true);
        topicRepository.save(topic);
    }
}
