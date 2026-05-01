package com.yoot.flashcard.modules.learning.entity;

public enum ReviewRating {
    AGAIN(0),
    HARD(3),
    GOOD(4),
    EASY(5);

    private final int score;

    ReviewRating(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}
