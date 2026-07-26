package org.booklore.model.enums;

import lombok.Getter;

public enum TaskType {
    LIBRARY_RESCAN(
            false,
            true,
            true,
            false,
            "Library Rescan",
            "Re-reads book information (title, author, cover, etc.) from your library files and updates the Booklore database."
    ),
    UPDATE_BOOK_RECOMMENDATIONS(
            false,
            true,
            true,
            false,
            "Update Book Recommendations",
            "Analyzes your library to generate personalized book recommendations based on the books you own."
    ),
    CLEANUP_DELETED_BOOKS(
            false,
            false,
            true,
            false,
            "Cleanup Deleted Books",
            "Permanently removes database entries for books you previously deleted from your libraries."
    ),
    SYNC_LIBRARY_FILES(
            false,
            false,
            true,
            false,
            "Sync Library Files",
            "Scans your library folders to detect new books and removes entries for files that no longer exist."
    ),
    BOOKDROP_PERIODIC_SCANNING(
            false,
            false,
            true,
            false,
            "Bookdrop Periodic Scanning",
            "Scans the bookdrop ingest folder for newly added files and queues them for bookdrop processing."
    );

    @Getter
    private final boolean parallel;

    @Getter
    private final boolean async;

    @Getter
    private final boolean cronSupported;

    @Getter
    private final boolean hiddenFromUI;

    @Getter
    private final String name;

    @Getter
    private final String description;

    TaskType(boolean parallel, boolean async, boolean cronSupported, boolean hiddenFromUI, String name, String description) {
        this.parallel = parallel;
        this.async = async;
        this.cronSupported = cronSupported;
        this.hiddenFromUI = hiddenFromUI;
        this.name = name;
        this.description = description;
    }
}