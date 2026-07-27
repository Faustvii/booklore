package org.booklore.service.metadata;

import org.booklore.model.entity.BookMetadataEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RatingAggregationServiceTest {

    private final RatingAggregationService service = new RatingAggregationService();

    @Test
    void computeAggregateRating_returnsNull_whenAllProvidersAbsent() {
        BookMetadataEntity metadata = BookMetadataEntity.builder().build();

        assertThat(service.computeAggregateRating(metadata)).isNull();
    }

    @Test
    void computeAggregateRating_ignoresZeroOrNegativeRatings() {
        BookMetadataEntity metadata = BookMetadataEntity.builder()
                .hardcoverRating(0.0)
                .amazonRating(-1.0)
                .goodreadsRating(null)
                .build();

        assertThat(service.computeAggregateRating(metadata)).isNull();
    }

    @Test
    void computeAggregateRating_simpleAverage_whenNoReviewCounts() {
        BookMetadataEntity metadata = BookMetadataEntity.builder()
                .hardcoverRating(4.0)
                .amazonRating(5.0)
                .goodreadsRating(3.0)
                .build();

        // No review counts -> each provider contributes weight 1 -> plain average
        assertThat(service.computeAggregateRating(metadata)).isEqualTo(4.0);
    }

    @Test
    void computeAggregateRating_weightedByReviewCount() {
        BookMetadataEntity metadata = BookMetadataEntity.builder()
                .hardcoverRating(5.0)
                .hardcoverReviewCount(100)
                .amazonRating(1.0)
                .amazonReviewCount(1)
                .build();

        // (5*100 + 1*1) / (100 + 1) = 501/101
        double expected = (5.0 * 100 + 1.0 * 1) / (100 + 1);
        assertThat(service.computeAggregateRating(metadata)).isEqualTo(expected);
    }

    @Test
    void computeAggregateRating_treatsMissingReviewCountAsWeightOne() {
        BookMetadataEntity metadata = BookMetadataEntity.builder()
                .hardcoverRating(5.0)
                .hardcoverReviewCount(9)
                .amazonRating(1.0)
                .amazonReviewCount(null)
                .build();

        // (5*9 + 1*1) / (9 + 1) = 46/10 = 4.6
        assertThat(service.computeAggregateRating(metadata)).isEqualTo(4.6);
    }

    @Test
    void computeAggregateRating_usesOnlyPresentProviders() {
        BookMetadataEntity metadata = BookMetadataEntity.builder()
                .goodreadsRating(4.5)
                .goodreadsReviewCount(10)
                .build();

        assertThat(service.computeAggregateRating(metadata)).isEqualTo(4.5);
    }
}
