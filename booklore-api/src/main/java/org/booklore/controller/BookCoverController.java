package org.booklore.controller;

import org.booklore.config.security.annotation.CheckBookAccess;
import org.booklore.model.dto.request.BulkBookIdsRequest;
import org.booklore.service.metadata.BookCoverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@AllArgsConstructor
@Tag(name = "Book Metadata", description = "Endpoints for managing book metadata, covers, and metadata operations")
public class BookCoverController {

    private final BookCoverService bookCoverService;

    @Operation(summary = "Regenerate audiobook cover for a book", description = "Regenerate audiobook cover for a specific book by extracting from the audiobook file. Requires metadata edit permission or admin.")
    @ApiResponse(responseCode = "204", description = "Audiobook cover regenerated successfully")
    @PostMapping("/{bookId}/regenerate-audiobook-cover")
    @PreAuthorize("@securityUtil.canEditMetadata() or @securityUtil.isAdmin()")
    @CheckBookAccess(bookIdParam = "bookId")
    public void regenerateAudiobookCover(@Parameter(description = "ID of the book") @PathVariable Long bookId) {
        bookCoverService.regenerateAudiobookCover(bookId);
    }

    @Operation(summary = "Generate custom audiobook cover for a book", description = "Generate a custom audiobook cover for a specific book based on its metadata. Requires metadata edit permission or admin.")
    @ApiResponse(responseCode = "204", description = "Custom audiobook cover generated successfully")
    @PostMapping("/{bookId}/generate-custom-audiobook-cover")
    @PreAuthorize("@securityUtil.canEditMetadata() or @securityUtil.isAdmin()")
    @CheckBookAccess(bookIdParam = "bookId")
    public void generateCustomAudiobookCover(@Parameter(description = "ID of the book") @PathVariable Long bookId) {
        bookCoverService.generateCustomAudiobookCover(bookId);
    }

    @Operation(summary = "Regenerate all covers", description = "Regenerate covers for all books. Requires metadata edit permission or admin.")
    @ApiResponse(responseCode = "204", description = "Covers regenerated successfully")
    @PostMapping("/regenerate-covers")
    @PreAuthorize("@securityUtil.canBulkRegenerateCover() or @securityUtil.isAdmin()")
    public void regenerateCovers(@RequestParam(defaultValue = "false") boolean missingOnly) {
        bookCoverService.regenerateCovers(missingOnly);
    }

    @Operation(summary = "Regenerate cover for a book", description = "Regenerate cover for a specific book. Requires metadata edit permission or admin.")
    @ApiResponse(responseCode = "204", description = "Cover regenerated successfully")
    @PostMapping("/{bookId}/regenerate-cover")
    @PreAuthorize("@securityUtil.canEditMetadata() or @securityUtil.isAdmin()")
    @CheckBookAccess(bookIdParam = "bookId")
    public void regenerateCovers(@Parameter(description = "ID of the book") @PathVariable Long bookId) {
        bookCoverService.regenerateCover(bookId);
    }

    @Operation(summary = "Generate custom cover for a book", description = "Generate a custom cover for a specific book based on its metadata. Requires metadata edit permission or admin.")
    @ApiResponse(responseCode = "204", description = "Custom cover generated successfully")
    @PostMapping("/{bookId}/generate-custom-cover")
    @PreAuthorize("@securityUtil.canEditMetadata() or @securityUtil.isAdmin()")
    @CheckBookAccess(bookIdParam = "bookId")
    public void generateCustomCover(@Parameter(description = "ID of the book") @PathVariable Long bookId) {
        bookCoverService.generateCustomCover(bookId);
    }

    @Operation(summary = "Regenerate covers for selected books", description = "Regenerate covers for a list of books. Requires metadata edit permission or admin.")
    @ApiResponse(responseCode = "204", description = "Cover regeneration started successfully")
    @PostMapping("/bulk-regenerate-covers")
    @PreAuthorize("@securityUtil.canBulkRegenerateCover() or @securityUtil.isAdmin()")
    public void regenerateCoversForBooks(@Parameter(description = "List of book IDs") @Validated @RequestBody BulkBookIdsRequest request) {
        bookCoverService.regenerateCoversForBooks(request.getBookIds());
    }

    @Operation(summary = "Generate custom covers for selected books", description = "Generate custom covers for a list of books. Requires metadata edit permission or admin.")
    @ApiResponse(responseCode = "204", description = "Custom cover generation started successfully")
    @PostMapping("/bulk-generate-custom-covers")
    @PreAuthorize("@securityUtil.canBulkRegenerateCover() or @securityUtil.isAdmin()")
    public void generateCustomCoversForBooks(@Parameter(description = "List of book IDs") @Validated @RequestBody BulkBookIdsRequest request) {
        bookCoverService.generateCustomCoversForBooks(request.getBookIds());
    }
}
