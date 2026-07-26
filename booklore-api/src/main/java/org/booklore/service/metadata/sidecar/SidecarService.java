package org.booklore.service.metadata.sidecar;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.booklore.exception.ApiError;
import org.booklore.model.dto.sidecar.SidecarMetadata;
import org.booklore.model.entity.BookEntity;
import org.booklore.model.enums.SidecarSyncStatus;
import org.booklore.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class SidecarService {

    private final BookRepository bookRepository;
    private final SidecarMetadataReader sidecarReader;

    public Optional<SidecarMetadata> getSidecarContent(Long bookId) {
        BookEntity book = bookRepository.findByIdWithBookFiles(bookId)
                .orElseThrow(() -> ApiError.BOOK_NOT_FOUND.createException(bookId));

        Path bookPath = book.getFullFilePath();
        if (bookPath == null) {
            return Optional.empty();
        }

        return sidecarReader.readSidecarMetadata(bookPath);
    }

    public SidecarSyncStatus getSyncStatus(Long bookId) {
        BookEntity book = bookRepository.findByIdWithBookFiles(bookId)
                .orElseThrow(() -> ApiError.BOOK_NOT_FOUND.createException(bookId));

        return sidecarReader.getSyncStatus(book);
    }
}
