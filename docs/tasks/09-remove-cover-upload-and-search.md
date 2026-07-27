# Task 09 — Remove Manual Cover Upload and Cover Search

**Priority:** P9
**Status:** Done
**Scope:** Backend + Frontend

---

## Why

Covers can currently be set three different ways: **generated** locally from
the book's own embedded art, **uploaded** manually by the user, or **searched**
via a DuckDuckGo image search restricted to `site:amazon.com OR
site:goodreads.com`. The last one is an outbound provider call — the same
category of feature already removed elsewhere in the PRD's "no outbound
provider calls" principle — and manual upload lets a user attach an image
with no relationship to the book's own file. Generation is being kept (see
`docs/PRD.md` Decisions): it only derives from the book's own embedded art or
a placeholder, and doesn't call out to the network or touch the source file.

## What to Remove

### Backend — `BookCoverController`
- `POST /{bookId}/metadata/cover/upload`
- `POST /{bookId}/metadata/cover/from-url`
- `POST /{bookId}/metadata/audiobook-cover/upload`
- `POST /{bookId}/metadata/audiobook-cover/from-url`
- `POST /bulk-upload-cover`
- `POST /{bookId}/metadata/covers` (cover search)

### Backend — `BookCoverService`
- `updateCoverFromFile`, `updateCoverFromUrl`,
  `updateAudiobookCoverFromFile`, `updateAudiobookCoverFromUrl`,
  `updateCoverFromFileForBooks`

### Backend — `DuckDuckGoCoverService`
- Remove `getCovers(CoverFetchRequest)` (the `site:amazon.com OR
  site:goodreads.com` search) and its `BookCoverProvider` interface wiring.
- **Do not** remove the class or `searchImages(String)` — that generic
  method (no site restriction) backs `AuthorController`'s author-photo
  search (`GET /{authorId}/search-photos`), which is being kept.
- If `BookCoverProvider` has no other implementors after this, evaluate
  whether the interface itself should be removed too.

### Frontend
- `cover-search.component.ts`/`.html` and its entry points from the metadata
  editor / book context menu.
- Manual upload UI (file picker / "cover from URL" field) in the metadata
  editor.
- Related API service calls, TypeScript types, and i18n keys.

---

## Keep — Do Not Touch

- Cover generation: `generateCustomCover`, `generateCustomAudiobookCover`,
  `regenerateCover`, `regenerateAudiobookCover`, and their bulk variants
  (`regenerateCovers`, `generateCustomCoversForBooks`,
  `regenerateCoversForBooks`), plus the `bulk-regenerate-covers` /
  `bulk-generate-custom-covers` endpoints.
- `DuckDuckGoCoverService.searchImages(...)` and the author-photo search
  feature it backs.
- The entire author metadata system (`AuthorController`,
  `AuthorMetadataService`, `AudnexusAuthorParser`) — kept per
  `docs/PRD.md` Decisions.

---

## Acceptance Criteria

- [x] No endpoint or UI allows uploading a cover image file or setting a
      cover from an arbitrary URL
- [x] No endpoint or UI allows searching for a cover via DuckDuckGo/Amazon/
      GoodReads
- [x] Cover generation/regeneration (single and bulk) still works unchanged
- [x] Author photo search still works unchanged
- [x] App starts without errors; compile clean
- [x] All existing tests pass; remove or update tests that covered removed code

## Implementation Notes

- Removed `BookCoverController`'s upload/from-url/bulk-upload/search endpoints,
  the corresponding `BookCoverService` methods, and `DuckDuckGoCoverService
  .getCovers(...)` plus the now-unused `BookCoverProvider` interface,
  `CoverFetchRequest`, and `CoverUrlRequest`. `DuckDuckGoCoverService
  .searchImages(...)` (author photo search) and all cover generation methods
  were left untouched.
- Deleted the frontend `cover-search` component and `book-cover.service.ts`,
  and removed the upload/search buttons from the metadata editor and the
  bulk-metadata-update dialog, along with their now-dead service methods and
  i18n keys (across all locales).
- Collateral finding: `metadata-picker.component.ts`'s Save flow used
  `uploadAudiobookCoverFromUrl` to apply an Audible-fetched audiobook cover
  for dual-format (ebook+audiobook) books — a different feature from manual
  upload/search, but dependent on the removed endpoint. Per user decision,
  this apply-on-save behavior was dropped rather than kept alive via a new
  endpoint; the audiobook cover lock/preview UI for the current (already
  saved) cover is unaffected.
- Backend compiles clean, all existing tests pass (with removed/updated
  tests for the deleted code paths). Frontend (`ng build`) compiles clean.
