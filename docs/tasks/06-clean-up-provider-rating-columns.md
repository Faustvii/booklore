# Task 06 — Clean Up Provider Rating Columns

**Priority:** P6  
**Status:** To Do  
**Scope:** Backend + Frontend

---

## Context

`BookMetadataEntity` currently has separate columns for ratings from each provider:
- `goodreads_rating`, `goodreads_review_count`
- `amazon_rating`, `amazon_review_count`
- `hardcover_rating`
- `lubimyczytac_rating`
- `ranobedb_rating`

Per the PRD, ratings come from embedded file metadata only. These provider-specific columns are populated solely by the metadata provider querying system (removed in Task 01). After Task 01 is complete, they will always be `null`.

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

- [ ] `BookMetadataEntity` has only one rating field (`rating`)
- [ ] New Flyway migration drops the columns cleanly
- [ ] Book detail page shows only the generic rating, no provider-specific breakdowns
- [ ] App starts without errors; compile clean
- [ ] All existing tests pass; remove or update tests that covered removed fields
