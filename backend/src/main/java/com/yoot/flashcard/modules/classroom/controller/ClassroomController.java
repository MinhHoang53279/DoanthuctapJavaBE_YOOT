package com.yoot.flashcard.modules.classroom.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/classes")
public class ClassroomController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success("Classroom module is ready", Map.of("module", "classroom"));
    }
}
