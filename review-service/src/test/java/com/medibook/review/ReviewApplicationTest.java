package com.medibook.review;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class ReviewApplicationTest {

    @Test
    void main_runsWithoutFailure() {
        ReviewServiceApplication app = new ReviewServiceApplication();
        assertDoesNotThrow(() -> {});
    }
}
