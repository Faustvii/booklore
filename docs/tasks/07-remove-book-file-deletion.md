# Task 07 — Remove Book/File Deletion

**Priority:** P7
**Status:** Not started
**Scope:** Backend + Frontend

---

## Why

`BookService.deleteBooks` (`booklore-api/src/main/java/org/booklore/service/book/BookService.java:373-433`)
physically deletes the book's file(s) from disk before removing the DB rows —
including recursive directory deletion for folder-based audiobooks, cleanup
of now-empty parent directories up to the library root, and deletion of
sidecar files. This is a direct violation of the PRD's Core Principle 1:
"Booklore ... never writes to, moves, or deletes files."

Booklore already has a sanctioned mechanism for a book leaving the index: the
filesystem watcher (`BookFilePersistenceService.deleteBookFile`) and library
rescans detect when a file has been removed from disk *outside* Booklore and
clean up the corresponding DB rows automatically. A manual "delete" action in
the UI is redundant with this and actively dangerous (it's the one place
Booklore still deletes user files).

## What to Remove

### Controller / Service
- `BookController.deleteBooks` (bulk delete endpoint) and
  `BookService.deleteBooks`.
- Now-dead helpers: `BookService.deleteDirectoryRecursively`,
  `BookService.deleteEmptyParentDirsUpToLibraryFolders`.
- Verify no other caller needs `sidecarMetadataWriter.deleteSidecarFiles(...)`
  before removing that call site.

### Permissions
- `canDeleteBook` / `CAN_DELETE_BOOK` permission end-to-end: `UserPermission`
  enum entry, DTO/entity fields, OIDC group → role mapping, user
  provisioning, admin permissions UI checkbox. Same pattern Task 04 used for
  `CAN_MOVE_ORGANIZE_FILES` — add a new Flyway migration dropping the
  corresponding `user_permissions` column.

### Frontend
- Delete actions/menu items in `book-browser.component.ts`,
  `book-card.component.ts`, `series-page.component.ts`.
- `deleteBook(s)` calls and related types in `book.service.ts`.
- Remove unused i18n keys for the delete confirmation dialog(s) across all
  locale files.

---

## Known Dependency — Resolve, Don't Silently Break

`duplicate-merger.component.ts` (the "Duplicate Merger" feature for
resolving detected duplicate books) calls `bookService.deleteBooks(...)` as
its core resolution action. Once file deletion is removed, this feature
needs an explicit decision during implementation:
- Turn it into a read-only "possible duplicates" report (no action), or
- Find another resolution action that doesn't require deleting a file.

Do not remove file deletion and leave Duplicate Merger silently broken —
handle it as part of this task.

---

## Acceptance Criteria

- [ ] No code path deletes a book's file(s) from disk
- [ ] "Delete Books" endpoint and UI actions no longer exist
- [ ] `CAN_DELETE_BOOK` permission removed end-to-end, including a Flyway
      migration dropping the column
- [ ] Duplicate Merger feature has an explicit, working (non-broken) behavior
      after this change
- [ ] Books removed from disk outside Booklore are still cleaned up
      automatically via the watcher/rescan path (unchanged, verify still works)
- [ ] App starts without errors; compile clean
- [ ] All existing tests pass; remove or update tests that covered removed code
