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
        RatingAggregationService.RatingAggregate result = service.computeAggregateRating(metadata);
        assertThat(result.rating()).isEqualTo(4.0);
        assertThat(result.reviewCount()).isZero();
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
        double expectedRating = (5.0 * 100 + 1.0 * 1) / (100 + 1);
        RatingAggregationService.RatingAggregate result = service.computeAggregateRating(metadata);
        assertThat(result.rating()).isEqualTo(expectedRating);
        assertThat(result.reviewCount()).isEqualTo(101);
    }

    @Test
    void computeAggregateRating_treatsMissingReviewCountAsWeightOneForAveraging_butZeroForTotal() {
        BookMetadataEntity metadata = BookMetadataEntity.builder()
                .hardcoverRating(5.0)
                .hardcoverReviewCount(9)
                .amazonRating(1.0)
                .amazonReviewCount(null)
                .build();

        // (5*9 + 1*1) / (9 + 1) = 46/10 = 4.6 for the weighted average,
        // but the displayed total review count only counts real (present) review counts.
        RatingAggregationService.RatingAggregate result = service.computeAggregateRating(metadata);
        assertThat(result.rating()).isEqualTo(4.6);
        assertThat(result.reviewCount()).isEqualTo(9);
    }

    @Test
    void computeAggregateRating_usesOnlyPresentProviders() {
        BookMetadataEntity metadata = BookMetadataEntity.builder()
                .goodreadsRating(4.5)
                .goodreadsReviewCount(10)
                .build();

        RatingAggregationService.RatingAggregate result = service.computeAggregateRating(metadata);
        assertThat(result.rating()).isEqualTo(4.5);
        assertThat(result.reviewCount()).isEqualTo(10);
    }

    @Test
    void computeAggregateRating_sumsReviewCountsAcrossAllProviders() {
        BookMetadataEntity metadata = BookMetadataEntity.builder()
                .hardcoverRating(4.0).hardcoverReviewCount(1000)
                .amazonRating(4.5).amazonReviewCount(2000)
                .goodreadsRating(4.2).goodreadsReviewCount(1566)
                .build();

        RatingAggregationService.RatingAggregate result = service.computeAggregateRating(metadata);
        assertThat(result.reviewCount()).isEqualTo(4566);
    }
}
