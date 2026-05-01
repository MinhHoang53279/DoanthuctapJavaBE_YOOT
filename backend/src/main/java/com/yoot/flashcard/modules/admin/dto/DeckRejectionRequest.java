package com.yoot.flashcard.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeckRejectionRequest(
        @NotBlank @Size(max = 2000) String reason
) {
}
