# Task 14 — Add Series Position to Generated Covers

**Priority:** P3
**Status:** Not Started
**Scope:** Backend

---

## Why

Generated covers currently render only title and author. Books in the same
series often share near-identical title styling (e.g. "Aethers Guard",
"Aethers Blessing", "Aethers Victory", "Aethers Sadness") and look
indistinguishable in grid/thumbnail views — there's no visual cue for
reading order. `BookMetadata` already stores `seriesName`, `seriesNumber`,
and `seriesTotal`, but `BookCoverService` discards this data when building
covers. Surfacing series position on the generated cover itself removes the
ambiguity without requiring the user to open each book.

## What to Add

### Backend

- `CoverImageGenerator.java`
  (`booklore-api/src/main/java/org/booklore/service/metadata/CoverImageGenerator.java`)
  - Reuse the existing (currently unused) subtitle render slot —
    `generateCover(title, author, subtitle)` (line ~42) and
    `renderSubtitle(...)` (line ~485) — as the series-position slot instead
    of adding a new parameter/layout path.
  - Format: `"Book N"` (e.g. `"Book 3"`), built from `seriesNumber`. Round /
    format fractional numbers (e.g. `2.5`) sensibly (e.g. `"Book 2.5"`).
  - No series text rendered when `seriesNumber` is null (2-arg
    `generateCover(title, author)` call path stays as-is for such books).
  - Apply to `generateSquareCover(...)` too (audiobook covers), reusing the
    same formatted string — add the equivalent optional param there since it
    currently has no subtitle-style slot.

- `BookCoverService.java`
  (`booklore-api/src/main/java/org/booklore/service/metadata/BookCoverService.java`)
  - `generateCustomCover(long bookId)` (line ~58): read
    `bookEntity.getMetadata().getSeriesNumber()`, build `"Book N"` string
    (or `null`/empty when absent), pass as the subtitle/series arg to
    `coverImageGenerator.generateCover(title, author, seriesText)`.
  - `generateCustomAudiobookCover(long bookId)` (line ~108): same lookup,
    pass into updated `generateSquareCover(...)`.
  - `processBulkCustomCoverGeneration(...)` (line ~307-338): same lookup for
    bulk regeneration path.
  - Add a small private helper (e.g. `formatSeriesPosition(Float
    seriesNumber)`) shared by all three call sites instead of duplicating
    formatting logic.

## Keep — Do Not Touch

- `seriesName` / `seriesTotal` are NOT rendered — only `seriesNumber`, per
  chosen format ("Book N"). Do not add series name text to covers in this
  task.
- No new DB columns/migrations — all needed fields already exist on
  `BookMetadataEntity`.
- No frontend changes — cover generation is backend-only; UI already
  displays whatever image the API returns.
- Do not change `renderTitle`/`renderAuthor` layout math — only reuse the
  existing subtitle slot for portrait covers, and mirror it minimally for
  square covers.

## Acceptance Criteria

- [ ] `CoverImageGenerator.generateCover(title, author, seriesText)` renders
      `"Book N"` in the existing subtitle slot when `seriesText` is
      non-null/non-empty.
- [ ] `CoverImageGenerator.generateSquareCover(...)` accepts and renders the
      same optional series text.
- [ ] `BookCoverService.generateCustomCover`,
      `generateCustomAudiobookCover`, and
      `processBulkCustomCoverGeneration` all pass series position through
      when `seriesNumber` is present on the book's metadata.
- [ ] Books with no `seriesNumber` render covers identical to current
      behavior (no blank/empty subtitle artifact).
- [ ] Fractional series numbers (e.g. `2.5`) render as `"Book 2.5"`, whole
      numbers (e.g. `3.0`) render as `"Book 3"` (no trailing `.0`).
- [ ] No compile errors.
- [ ] All existing tests pass.
