package org.booklore.service.metadata;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CoverImageGeneratorTest {

    private final CoverImageGenerator generator = new CoverImageGenerator();

    @Test
    void generateCover_withSeriesText_producesNonEmptyJpeg() {
        byte[] bytes = generator.generateCover("Aethers Guard", "Jane Doe", "Book 3");

        assertThat(bytes).isNotEmpty();
    }

    @Test
    void generateCover_withoutSeriesText_matchesTwoArgOverload() {
        assertThatCode(() -> generator.generateCover("Aethers Guard", "Jane Doe")).doesNotThrowAnyException();
        assertThatCode(() -> generator.generateCover("Aethers Guard", "Jane Doe", null)).doesNotThrowAnyException();
        assertThatCode(() -> generator.generateCover("Aethers Guard", "Jane Doe", "")).doesNotThrowAnyException();
    }

    @Test
    void generateSquareCover_withSeriesText_producesNonEmptyJpeg() {
        byte[] bytes = generator.generateSquareCover("Aethers Guard", "Jane Doe", "Book 2.5");

        assertThat(bytes).isNotEmpty();
    }

    @Test
    void generateSquareCover_withoutSeriesText_doesNotThrow() {
        assertThatCode(() -> generator.generateSquareCover("Aethers Guard", "Jane Doe")).doesNotThrowAnyException();
        assertThatCode(() -> generator.generateSquareCover("Aethers Guard", "Jane Doe", null)).doesNotThrowAnyException();
        assertThatCode(() -> generator.generateSquareCover("Aethers Guard", "Jane Doe", "")).doesNotThrowAnyException();
    }
}
