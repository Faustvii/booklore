# Task 09 — Remove Manual Cover Upload and Cover Search

**Priority:** P9
**Status:** Not started
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

- [ ] No endpoint or UI allows uploading a cover image file or setting a
      cover from an arbitrary URL
- [ ] No endpoint or UI allows searching for a cover via DuckDuckGo/Amazon/
      GoodReads
- [ ] Cover generation/regeneration (single and bulk) still works unchanged
- [ ] Author photo search still works unchanged
- [ ] App starts without errors; compile clean
- [ ] All existing tests pass; remove or update tests that covered removed code
