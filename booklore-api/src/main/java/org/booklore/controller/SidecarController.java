package org.booklore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.booklore.config.security.annotation.CheckBookAccess;
import org.booklore.model.dto.sidecar.SidecarMetadata;
import org.booklore.model.enums.SidecarSyncStatus;
import org.booklore.service.metadata.sidecar.SidecarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Tag(name = "Sidecar Metadata", description = "Endpoints for managing sidecar JSON metadata files")
@RequestMapping("/api/v1")
@RestController
@AllArgsConstructor
public class SidecarController {

    private final SidecarService sidecarService;

    @Operation(summary = "Get sidecar content", description = "Get the content of the sidecar JSON file for a book")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sidecar content returned successfully"),
            @ApiResponse(responseCode = "404", description = "Book or sidecar file not found")
    })
    @CheckBookAccess(bookIdParam = "bookId")
    @GetMapping("/books/{bookId}/sidecar")
    public ResponseEntity<SidecarMetadata> getSidecarContent(@Parameter(description = "Book ID") @PathVariable Long bookId) {
        Optional<SidecarMetadata> sidecar = sidecarService.getSidecarContent(bookId);
        return sidecar.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get sidecar sync status", description = "Get the synchronization status between database and sidecar file")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync status returned successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @CheckBookAccess(bookIdParam = "bookId")
    @GetMapping("/books/{bookId}/sidecar/status")
    public ResponseEntity<Map<String, SidecarSyncStatus>> getSyncStatus(@Parameter(description = "Book ID") @PathVariable Long bookId) {
        SidecarSyncStatus status = sidecarService.getSyncStatus(bookId);
        return ResponseEntity.ok(Map.of("status", status));
    }
}
