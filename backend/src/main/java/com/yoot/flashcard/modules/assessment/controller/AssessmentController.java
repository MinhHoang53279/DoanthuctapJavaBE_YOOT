package com.yoot.flashcard.modules.assessment.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/quizzes")
public class AssessmentController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success("Assessment module is ready", Map.of("module", "assessment"));
    }
}
