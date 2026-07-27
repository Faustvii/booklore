# Task 06 — Clean Up Provider Rating Columns

**Priority:** P6  
**Status:** Done — closed, no change made (premise was false, see below)  
**Scope:** Backend + Frontend

---

## Context

`BookMetadataEntity` currently has separate columns for ratings from each provider:
- `goodreads_rating`, `goodreads_review_count`
- `amazon_rating`, `amazon_review_count`
- `hardcover_rating`, `hardcover_review_count`
- `lubimyczytac_rating`
- `ranobedb_rating`
- `audible_rating`, `audible_review_count`

**This task's original premise was incorrect and should not be acted on as written.** It assumed these columns are populated solely by the external metadata-provider querying system (`service/metadata/parser/`, removed in Task 01), and would therefore always be `null` afterward.

In reality there is a second, still-active code path: `service/metadata/extractor/` reads these ratings straight out of embedded file metadata (EPUB/PDF/AZW3/MOBI/FB2/CBX), independent of any network call:
- Calibre custom-column tags (`#goodreads_rating`, `#amazon_rating`, etc.) via `calibre:user_metadata:#...` OPF entries.
- BookLore's own namespaced tags (`booklore:goodreads_rating`, `booklore:amazon_rating`, etc. — see `BookLoreMetadata.NS_PREFIX`), which `PdfMetadataWriter` also writes back out to files — a full round trip.
- `SidecarMetadataMapper` additionally maps sidecar-file ratings onto the same fields.

So these columns are real, user-facing data (ratings embedded in people's own files), not dead columns left over from the removed provider-querying system. Dropping them as originally proposed would silently discard that data.

If per-provider ratings are genuinely out of scope for the product going forward, this task needs to be rescoped as "stop reading/writing provider-specific ratings from embedded file metadata, and migrate/drop the columns" — a materially larger change than originally described, touching every format processor and extractor/writer. Do not proceed on the original plan without that decision being made explicitly.

---

## What to Remove

### Entity
- Remove provider-specific rating fields from `BookMetadataEntity`:
  - `goodreadsRating`, `goodreadsReviewCount`
  - `amazonRating`, `amazonReviewCount`
  - `hardcoverRating`
  - `lubimyczytacRating`
  - `ranobedbRating`
- Keep the generic `rating` field — this is populated from embedded file metadata

### DTOs
- Remove corresponding fields from `BookMetadata` DTO and any other DTOs that expose these columns
- Update MapStruct mappers accordingly

### Database Migrations
- Add new migration to drop the provider-specific rating columns from `book_metadata` (or equivalent table)
- Use `ALTER TABLE ... DROP COLUMN IF EXISTS` for each column

### Frontend
- Remove any display of provider-specific ratings (GoodReads stars, Amazon stars, etc.) from book detail pages
- Keep display of the single generic `rating` field

---

## Acceptance Criteria

N/A — task closed without changes; original acceptance criteria don't apply since the provider-specific rating columns are kept.

- ~~`BookMetadataEntity` has only one rating field (`rating`)~~
- ~~New Flyway migration drops the columns cleanly~~
- ~~Book detail page shows only the generic rating, no provider-specific breakdowns~~
- ~~App starts without errors; compile clean~~
- ~~All existing tests pass; remove or update tests that covered removed fields~~
