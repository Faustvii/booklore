package org.booklore.service.metadata;

import org.booklore.model.entity.BookMetadataEntity;
import org.springframework.stereotype.Service;

@Service
public class RatingAggregationService {

    public Double computeAggregateRating(BookMetadataEntity metadata) {
        double weightedSum = 0;
        double weightTotal = 0;
        boolean hasRating = false;

        if (metadata.getHardcoverRating() != null && metadata.getHardcoverRating() > 0) {
            double weight = reviewWeight(metadata.getHardcoverReviewCount());
            weightedSum += metadata.getHardcoverRating() * weight;
            weightTotal += weight;
            hasRating = true;
        }
        if (metadata.getAmazonRating() != null && metadata.getAmazonRating() > 0) {
            double weight = reviewWeight(metadata.getAmazonReviewCount());
            weightedSum += metadata.getAmazonRating() * weight;
            weightTotal += weight;
            hasRating = true;
        }
        if (metadata.getGoodreadsRating() != null && metadata.getGoodreadsRating() > 0) {
            double weight = reviewWeight(metadata.getGoodreadsReviewCount());
            weightedSum += metadata.getGoodreadsRating() * weight;
            weightTotal += weight;
            hasRating = true;
        }

        if (!hasRating) {
            return null;
        }
        return weightedSum / weightTotal;
    }

    private double reviewWeight(Integer reviewCount) {
        return reviewCount != null && reviewCount > 0 ? reviewCount : 1;
    }
}
