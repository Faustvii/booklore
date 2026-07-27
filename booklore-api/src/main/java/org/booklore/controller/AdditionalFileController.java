package org.booklore.controller;

import org.booklore.config.security.annotation.CheckBookAccess;
import org.booklore.model.dto.BookFile;
import org.booklore.model.dto.request.DetachBookFileRequest;
import org.booklore.model.dto.response.DetachBookFileResponse;
import org.booklore.service.book.BookFileDetachmentService;
import org.booklore.service.file.AdditionalFileService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping("/api/v1/books/{bookId}/files")
@RestController
@AllArgsConstructor
public class AdditionalFileController {

    private final AdditionalFileService additionalFileService;
    private final BookFileDetachmentService bookFileDetachmentService;

    @GetMapping
    @CheckBookAccess(bookIdParam = "bookId")
    public ResponseEntity<List<BookFile>> getAdditionalFiles(@PathVariable Long bookId) {
        List<BookFile> files = additionalFileService.getAdditionalFilesByBookId(bookId);
        return ResponseEntity.ok(files);
    }

    @GetMapping(params = "isBook")
    @CheckBookAccess(bookIdParam = "bookId")
    public ResponseEntity<List<BookFile>> getFilesByIsBook(
            @PathVariable Long bookId,
            @RequestParam boolean isBook) {
        List<BookFile> files = additionalFileService.getAdditionalFilesByBookIdAndIsBook(bookId, isBook);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{fileId}/download")
    @CheckBookAccess(bookIdParam = "bookId")
    public ResponseEntity<Resource> downloadAdditionalFile(
            @PathVariable Long bookId,
            @PathVariable Long fileId) throws IOException {
        return additionalFileService.downloadAdditionalFile(fileId);
    }

    @PostMapping("/{fileId}/detach")
    @CheckBookAccess(bookIdParam = "bookId")
    @PreAuthorize("@securityUtil.canManageLibrary() or @securityUtil.isAdmin()")
    public ResponseEntity<DetachBookFileResponse> detachFile(
            @PathVariable Long bookId,
            @PathVariable Long fileId,
            @RequestBody DetachBookFileRequest request) {
        return ResponseEntity.ok(bookFileDetachmentService.detachBookFile(bookId, fileId, request.copyMetadata()));
    }
}
