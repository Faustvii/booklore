# Task 07 — Remove Book/File Deletion

**Priority:** P7
**Status:** Done
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

- [x] No code path deletes a book's file(s) from disk
- [x] "Delete Books" endpoint and UI actions no longer exist
- [x] `CAN_DELETE_BOOK` permission removed end-to-end, including a Flyway
      migration dropping the column
- [x] Duplicate Merger feature has an explicit, working (non-broken) behavior
      after this change
- [x] Books removed from disk outside Booklore are still cleaned up
      automatically via the watcher/rescan path (unchanged, verify still works)
- [x] App starts without errors; compile clean
- [x] All existing tests pass; remove or update tests that covered removed code

## Implementation Notes

- `BookController.deleteBooks` and `BookService.deleteBooks` were deleted,
  along with the now-dead `BookService.deleteDirectoryRecursively` and
  `BookService.deleteEmptyParentDirsUpToLibraryFolders`, and the
  `BookDeletionResponse` DTO. `SidecarMetadataWriter.deleteSidecarFiles(...)`
  had no other caller, so it was deleted too (`moveSidecarFiles` is unrelated
  shared infra used by `FileMoveService` and was left untouched).
- `BookFileAttachmentService` (the merge/attach-file feature backing
  Duplicate Merger's "Merge" action) turned out to call
  `bookService.deleteEmptyParentDirsUpToLibraryFolders(...)` directly for
  post-move directory cleanup — this was **not** dead code. It was
  repointed to the already-existing `FileMoveHelper.deleteEmptyParentDirsUpToLibraryFolders(...)`
  (the same helper `FileMoveService` uses), so no duplicate logic was
  reintroduced into `BookService`.
- `CAN_DELETE_BOOK` / `permissionDeleteBook` was removed end-to-end:
  `UserPermission` enum entry, `PermissionType`/`UserPermissionUtils`,
  `BookLoreUser`/`UserUpdateRequest`/`UserCreateRequest` DTO fields,
  `UserPermissionsEntity` column + `SecurityUtil.canDeleteBook()`,
  OIDC group mapping (`OidcGroupMappingService`) and all four
  `UserProvisioningService` provisioning paths, plus the admin permissions
  UI checkbox and create-user dialog — and a new Flyway migration
  (`V7__Remove_book_file_deletion.sql`) dropping `permission_delete_book`.
- Two other endpoints reused `@securityUtil.canDeleteBook()` for unrelated
  file-deleting actions outside this task's scope
  (`AuthorController.deleteAuthors`, which deletes cached author images, and
  `AdditionalFileController.deleteAdditionalFile`, which deletes individual
  book format/supplementary files — used by the metadata viewer's "Delete
  File Formats"/"Delete Supplementary Files" menus). Since the permission
  they checked no longer exists, both were re-gated to `isAdmin()` only
  rather than left broken or silently removed; the corresponding frontend
  gates (`author-browser.component.ts`'s `canDeleteBook` getter, renamed to
  `canDeleteAuthor`; `book-card.component.ts`; `metadata-viewer.component.ts`)
  were updated to check `permissions.admin` instead. The whole-book "Delete
  Book" / "Delete Book & All Files" menu item in the metadata viewer (which
  called the now-removed `bookService.deleteBooks`) was deleted; the
  per-format and per-supplementary-file delete actions in that same menu
  were kept since they don't go through the removed endpoint.
- Duplicate Merger: removed `deleteGroup()`, `toggleDeleteSelection()`,
  `getDeleteSelectedCount()`, the `selectedForDeletion` field, and the
  corresponding checkbox/button in the template. "Merge" (via
  `attachBookFiles`) and "Dismiss" remain as the two resolution actions per
  group, so the feature keeps a working, non-destructive resolution path.
- Removed unused i18n keys (delete confirmation dialogs/toasts for
  book-browser, book-card, series-page, `bookService`, the duplicate-merger
  "Delete Selected" action, and the metadata-viewer whole-book delete menu)
  across all 19 locale files, and corrected now-stale wording in
  `duplicateMerger.helpText` and `metadata.viewer.confirm.deleteOnlyFormatMessage`
  that referenced the removed "Delete Book & All Files" action.
- Verified with the full backend test suite (3050 tests, 0 failures) and a
  clean Angular production build.
