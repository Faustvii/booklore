package org.booklore.service.library;

import org.booklore.model.entity.BookEntity;
import org.booklore.repository.AnnotationRepository;
import org.booklore.repository.BookAdditionalFileRepository;
import org.booklore.repository.BookMarkRepository;
import org.booklore.repository.BookNoteRepository;
import org.booklore.repository.BookNoteV2Repository;
import org.booklore.repository.BookRepository;
import org.booklore.repository.PdfAnnotationRepository;
import org.booklore.repository.ReadingSessionRepository;
import org.booklore.repository.UserBookFileProgressRepository;
import org.booklore.repository.UserBookProgressRepository;
import org.booklore.service.NotificationService;
import org.booklore.util.FileService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookDeletionServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private BookAdditionalFileRepository bookAdditionalFileRepository;
    @Mock private FileService fileService;
    @Mock private NotificationService notificationService;
    @Mock private AnnotationRepository annotationRepository;
    @Mock private BookMarkRepository bookMarkRepository;
    @Mock private BookNoteRepository bookNoteRepository;
    @Mock private BookNoteV2Repository bookNoteV2Repository;
    @Mock private PdfAnnotationRepository pdfAnnotationRepository;
    @Mock private ReadingSessionRepository readingSessionRepository;
    @Mock private UserBookProgressRepository userBookProgressRepository;
    @Mock private UserBookFileProgressRepository userBookFileProgressRepository;
    @Mock private EntityManager entityManager;

    private BookDeletionService bookDeletionService;

    @BeforeEach
    void setUp() {
        bookDeletionService = new BookDeletionService(
                bookRepository,
                bookAdditionalFileRepository,
                fileService,
                notificationService,
                annotationRepository,
                bookMarkRepository,
                bookNoteRepository,
                bookNoteV2Repository,
                pdfAnnotationRepository,
                readingSessionRepository,
                userBookProgressRepository,
                userBookFileProgressRepository,
                entityManager
        );
    }

    @Test
    void deleteRemovedBooks_shouldPurgeAllDependentTables_beforeDeletingBooks() {
        List<Long> bookIds = List.of(1L, 2L);
        BookEntity book1 = new BookEntity();
        book1.setId(1L);
        BookEntity book2 = new BookEntity();
        book2.setId(2L);
        when(bookRepository.findAllById(bookIds)).thenReturn(List.of(book1, book2));
        when(fileService.getImagesFolder(anyLong())).thenReturn("/tmp/does-not-exist-" + System.nanoTime());
        when(fileService.getBookMetadataBackupPath(anyLong())).thenReturn("/tmp/does-not-exist-backup-" + System.nanoTime());

        bookDeletionService.deleteRemovedBooks(bookIds);

        // Every dependent table lacking ON DELETE CASCADE (or JPA cascade coverage) must be
        // cleared for these book ids before the book rows themselves are deleted - otherwise
        // the delete fails with a foreign key violation (e.g. user_book_progress).
        InOrder inOrder = inOrder(annotationRepository, bookMarkRepository, bookNoteRepository,
                bookNoteV2Repository, pdfAnnotationRepository, readingSessionRepository,
                userBookFileProgressRepository, userBookProgressRepository, bookRepository);

        inOrder.verify(annotationRepository).deleteByBookIdIn(bookIds);
        inOrder.verify(bookMarkRepository).deleteByBookIdIn(bookIds);
        inOrder.verify(bookNoteRepository).deleteByBookIdIn(bookIds);
        inOrder.verify(bookNoteV2Repository).deleteByBookIdIn(bookIds);
        inOrder.verify(pdfAnnotationRepository).deleteByBookIdIn(bookIds);
        inOrder.verify(readingSessionRepository).deleteByBookIdIn(bookIds);
        inOrder.verify(userBookFileProgressRepository).deleteByBookIdIn(bookIds);
        inOrder.verify(userBookProgressRepository).deleteByBookIdIn(bookIds);
        inOrder.verify(bookRepository).deleteAll(List.of(book1, book2));
    }

    @Test
    void deleteRemovedBooks_shouldDoNothing_whenBookIdsEmpty() {
        bookDeletionService.deleteRemovedBooks(List.of());

        verifyNoInteractions(annotationRepository, bookMarkRepository, bookNoteRepository,
                bookNoteV2Repository, pdfAnnotationRepository, readingSessionRepository,
                userBookFileProgressRepository, userBookProgressRepository, bookRepository,
                notificationService);
    }
}
