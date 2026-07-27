package org.booklore.service.metadata;

import org.booklore.model.MetadataUpdateContext;
import org.booklore.model.MetadataUpdateWrapper;
import org.booklore.model.dto.BookMetadata;
import org.booklore.model.entity.BookEntity;
import org.booklore.model.entity.BookMetadataEntity;
import org.booklore.repository.AuthorRepository;
import org.booklore.repository.BookRepository;
import org.booklore.repository.CategoryRepository;
import org.booklore.repository.ComicCharacterRepository;
import org.booklore.repository.ComicCreatorRepository;
import org.booklore.repository.ComicLocationRepository;
import org.booklore.repository.ComicMetadataRepository;
import org.booklore.repository.ComicTeamRepository;
import org.booklore.repository.MoodRepository;
import org.booklore.repository.TagRepository;
import org.booklore.service.author.AuthorAutoFetchService;
import org.booklore.service.author.NewAuthorTrackingContext;
import org.booklore.util.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookMetadataUpdaterRatingTest {

    @Mock private AuthorRepository authorRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private MoodRepository moodRepository;
    @Mock private TagRepository tagRepository;
    @Mock private BookRepository bookRepository;
    @Mock private ComicMetadataRepository comicMetadataRepository;
    @Mock private ComicCharacterRepository comicCharacterRepository;
    @Mock private ComicTeamRepository comicTeamRepository;
    @Mock private ComicLocationRepository comicLocationRepository;
    @Mock private ComicCreatorRepository comicCreatorRepository;
    @Mock private FileService fileService;
    @Mock private MetadataMatchService metadataMatchService;
    @Mock private NewAuthorTrackingContext newAuthorTrackingContext;
    @Mock private AuthorAutoFetchService authorAutoFetchService;

    private BookMetadataUpdater bookMetadataUpdater;

    @BeforeEach
    void setUp() {
        bookMetadataUpdater = new BookMetadataUpdater(
                authorRepository, categoryRepository, moodRepository, tagRepository, bookRepository,
                comicMetadataRepository, comicCharacterRepository, comicTeamRepository, comicLocationRepository,
                comicCreatorRepository, fileService, metadataMatchService, newAuthorTrackingContext,
                authorAutoFetchService, new RatingAggregationService()
        );
        when(newAuthorTrackingContext.begin()).thenReturn(true);
    }

    private BookEntity newBookEntity() {
        BookMetadataEntity metadataEntity = BookMetadataEntity.builder().title("Existing Title").build();
        BookEntity bookEntity = BookEntity.builder().id(1L).metadata(metadataEntity).build();
        metadataEntity.setBook(bookEntity);
        return bookEntity;
    }

    private MetadataUpdateContext context(BookEntity bookEntity, BookMetadata newMetadata) {
        return MetadataUpdateContext.builder()
                .bookEntity(bookEntity)
                .metadataUpdateWrapper(MetadataUpdateWrapper.builder().metadata(newMetadata).build())
                .build();
    }

    @Test
    void setBookMetadata_computesWeightedAggregate_whenNoExplicitRatingSupplied() {
        BookEntity bookEntity = newBookEntity();
        BookMetadata newMetadata = new BookMetadata();
        newMetadata.setHardcoverRating(5.0);
        newMetadata.setHardcoverReviewCount(100);
        newMetadata.setAmazonRating(1.0);
        newMetadata.setAmazonReviewCount(1);

        bookMetadataUpdater.setBookMetadata(context(bookEntity, newMetadata));

        double expected = (5.0 * 100 + 1.0 * 1) / (100 + 1);
        assertThat(bookEntity.getMetadata().getRating()).isEqualTo(expected);
    }

    @Test
    void setBookMetadata_explicitRatingWinsOverAggregate() {
        BookEntity bookEntity = newBookEntity();
        BookMetadata newMetadata = new BookMetadata();
        newMetadata.setHardcoverRating(1.0);
        newMetadata.setAmazonRating(1.0);
        newMetadata.setGoodreadsRating(1.0);
        newMetadata.setRating(4.7); // e.g. extracted from an embedded booklore:rating tag

        bookMetadataUpdater.setBookMetadata(context(bookEntity, newMetadata));

        assertThat(bookEntity.getMetadata().getRating()).isEqualTo(4.7);
    }

    @Test
    void setBookMetadata_recomputesAggregate_whenProviderRatingsChangeOnSubsequentUpdate() {
        BookEntity bookEntity = newBookEntity();

        BookMetadata firstUpdate = new BookMetadata();
        firstUpdate.setHardcoverRating(2.0);
        bookMetadataUpdater.setBookMetadata(context(bookEntity, firstUpdate));
        assertThat(bookEntity.getMetadata().getRating()).isEqualTo(2.0);

        BookMetadata secondUpdate = new BookMetadata();
        secondUpdate.setHardcoverRating(4.0);
        bookMetadataUpdater.setBookMetadata(context(bookEntity, secondUpdate));
        assertThat(bookEntity.getMetadata().getRating()).isEqualTo(4.0);
    }

    @Test
    void setBookMetadata_leavesRatingUnset_whenNoProviderRatingsPresent() {
        BookEntity bookEntity = newBookEntity();
        BookMetadata newMetadata = new BookMetadata();
        newMetadata.setTitle("A different title so the update isn't skipped as a no-op");

        bookMetadataUpdater.setBookMetadata(context(bookEntity, newMetadata));

        assertThat(bookEntity.getMetadata().getRating()).isNull();
    }
}
