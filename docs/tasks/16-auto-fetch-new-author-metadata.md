# Task 16 — Auto-Fetch Metadata for Newly Detected Authors

**Priority:** P2
**Status:** Not Started
**Scope:** Backend + Frontend

---

## Why

When a library scan encounters a new author name embedded in a book file,
`BookCreatorService.addAuthorsToBook()` creates a bare `AuthorEntity` with
only a `name` — no photo, no bio. Fetching that metadata is currently a
fully manual, per-author "quick match" click in the author browser UI.
BookLore's stated role is a presentation layer, not a library-management
tool, so requiring a manual chore for every new author (a growing library
can add dozens per scan) works against that goal. The fetch logic already
exists end-to-end (`AuthorMetadataService.quickMatchAuthor` /
`autoMatchAuthors`) — it just isn't triggered automatically.

## What to Add

### Backend

- Track newly-created authors per scan:
  - `BookCreatorService.addAuthorsToBook()`
    (`booklore-api/src/main/java/org/booklore/service/book/BookCreatorService.java:142-153`)
    — when `authorRepository.save(AuthorEntity.builder().name(authorName).build())`
    creates a brand-new row (vs. `findByName` hit), collect that author's
    ID.
  - Same find-or-create pattern is duplicated in
    `BookMetadataUpdater.java:192-196` and `PhysicalBookService.java:131-138`
    — apply the same "new author" tracking there too so authors added
    outside a full scan (metadata refresh, manual/physical book add) are
    also covered.
  - Simplest approach: have all three call sites report newly-created
    author IDs back up to the scan task (`LibraryScanTask` /
    `LibraryRescanTask`, `booklore-api/src/main/java/org/booklore/task/tasks/`)
    which accumulates them across the scan.

- Fire auto-fetch after scan completes:
  - At the end of `LibraryScanTask`/`LibraryRescanTask` execution, if the
    setting (see below) is enabled and new author IDs were collected, call
    `AuthorMetadataService.autoMatchAuthors(newAuthorIds)`
    (`booklore-api/src/main/java/org/booklore/service/AuthorMetadataService.java:147-170`)
    — this already does reactive rate-limited (`Flux`/`concatMap`,
    250-750ms delay) `quickMatch` per author, respecting each author's
    `descriptionLocked`/`asinLocked`/`photoLocked` flags. No new matching
    logic needed — only the trigger wiring.
  - Follow the existing `Task`/`TaskService` pattern
    (`booklore-api/src/main/java/org/booklore/task/`) if this needs its
    own task/progress tracking (e.g. surfaced in task history like
    `BookRecommendationUpdaterTask`), or invoke inline as a
    post-scan step if a separate tracked task isn't necessary — decide
    based on how `LibraryScanTask` reports its own progress today.

- New setting:
  - Add an app/library setting (e.g. `autoFetchAuthorMetadata`, default
    `true`) following this repo's existing settings pattern (find via
    current app settings entity/service — same place other scan-related
    toggles live) to allow disabling automatic fetch.

### Frontend

- Add a toggle for the new setting in the relevant settings page
  (wherever other scan/library behavior toggles are surfaced), defaulted
  ON.
- No other UI changes required — existing per-author "quick match" button
  (`AuthorDetailComponent.quickMatch()`,
  `booklore-ui/src/app/features/author-browser/components/author-detail/author-detail.component.ts:137-159`)
  and bulk "auto-match selected" action
  (`AuthorBrowserComponent.autoMatchSelected()`,
  `booklore-ui/src/app/features/author-browser/components/author-browser/author-browser.component.ts:330-353`)
  stay as manual override/retry options — automation supplements them, does
  not replace them.

## Keep — Do Not Touch

- Only Audnexus (Audible) is used, matching the current single provider
  (`AudnexusAuthorParser`) — adding another author metadata provider is
  out of scope for this task.
- Existing manual match/quick-match/photo-search UI and endpoints stay
  fully intact and functional as manual fallback/override.
- `descriptionLocked`/`asinLocked`/`photoLocked` flags on `AuthorEntity`
  continue to be respected exactly as `AuthorMetadataService` already
  handles them — no changes to lock semantics.
- Rate-limiting behavior (250-750ms delay between provider calls in
  `autoMatchAuthors`) stays as-is — do not remove it to "speed up" bulk
  auto-fetch after large scans.

## Acceptance Criteria

- [ ] Authors newly created during a library scan (new file ingestion)
      automatically get a metadata/photo fetch attempt via Audnexus after
      the scan completes, when the new setting is enabled.
- [ ] Authors newly created via metadata refresh
      (`BookMetadataUpdater`) or manual/physical book add
      (`PhysicalBookService`) are also covered, not just full scans.
- [ ] New setting exists, defaults to ON, and disabling it stops
      automatic fetch (manual quick-match/auto-match still works
      regardless of the setting).
- [ ] Authors that already existed (matched by name) are never
      re-fetched by this automation — only genuinely new author rows.
- [ ] Existing manual match, quick-match, bulk auto-match, and photo
      search flows are unaffected.
- [ ] No compile errors.
- [ ] All existing tests pass.
