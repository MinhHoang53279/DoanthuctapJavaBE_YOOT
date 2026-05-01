package com.yoot.flashcard.modules.content.service;

import com.yoot.flashcard.common.exception.BusinessException;
import com.yoot.flashcard.common.security.SecurityUtils;
import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.content.entity.DeckStatus;
import com.yoot.flashcard.modules.content.entity.DeckVisibility;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ContentAccessService {

    public Long requireCurrentUserId() {
        return SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    public boolean canManageContent() {
        return SecurityUtils.hasAnyRole("ADMIN", "SUPER_ADMIN", "CONTENT_MANAGER");
    }

    public boolean canManageDeck(Deck deck) {
        return canManageContent() || SecurityUtils.currentUserId()
                .map(userId -> userId.equals(deck.getCreatedBy().getId()))
                .orElse(false);
    }

    public boolean canReadDeck(Deck deck) {
        return deck.getDeletedAt() == null && (
                deck.getVisibility() == DeckVisibility.PUBLIC && deck.getStatus() == DeckStatus.APPROVED
                        || canManageDeck(deck)
        );
    }

    public void requireDeckRead(Deck deck) {
        if (!canReadDeck(deck)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }

    public void requireDeckManage(Deck deck) {
        if (!canManageDeck(deck)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Forbidden");
        }
    }
}
