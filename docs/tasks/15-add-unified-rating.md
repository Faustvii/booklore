# Task 15 — Add Unified/Aggregated Rating Field

**Priority:** P2
**Status:** Not Started
**Scope:** Backend + Frontend

---

## Why

Books currently expose separate per-provider ratings (`amazonRating`,
`goodreadsRating`, `hardcoverRating`, `audibleRating`,
`lubimyczytacRating`, `ranobedbRating`) with no single "main" number to sort
or judge a book by — users have to arbitrarily pick one provider's field.
`OpdsBookService.calculateRating()` already proves the concept (averages
hardcover/amazon/goodreads for OPDS feed sorting) but is transient,
unpersisted, and excludes review-count weighting. A persisted, weighted,
unified rating gives one trustworthy "main" field for sorting/browsing
while every individual provider field and UI element stays exactly as-is.

## What to Add

### Backend

- `BookMetadataEntity.java`
  (`booklore-api/src/main/java/org/booklore/model/entity/BookMetadataEntity.java`)
  - The existing bare `rating` column (line ~65) becomes the unified
    field — no new column. Add a DB migration only if needed for any
    supporting index (not expected).
  - `BookMetadata.java` DTO: bare `rating` field (line ~68) — no change
    needed, already present and exposed.

- New computation logic (new method, e.g. `RatingAggregationService` or a
  method on `BookMetadataUpdater`):
  - Provider set: `hardcoverRating`, `amazonRating`, `goodreadsRating` —
    same subset as `OpdsBookService.calculateRating()`
    (`booklore-api/src/main/java/org/booklore/service/opds/OpdsBookService.java:621-651`),
    only including ratings `> 0`.
  - Weighting: weighted average using each provider's review count
    (`hardcoverReviewCount`, `amazonReviewCount`, `goodreadsReviewCount`)
    when available; fall back to simple (unweighted) average across
    providers missing a review count.
  - Formula: `sum(rating_i * max(reviewCount_i, 1)) / sum(max(reviewCount_i, 1))`
    — providers with no review count contribute with weight 1.
  - If all three provider ratings are absent, leave `rating` unset (no
    computed value).

- Precedence over embedded-file rating:
  - `rating` can already be populated from embedded file metadata (e.g.
    PDF `booklore:rating` tag, see
    `booklore-api/src/main/java/org/booklore/service/metadata/extractor/PdfMetadataExtractor.java`).
    Metadata extraction runs before the new aggregation step in the
    update pipeline (`BookMetadataUpdater.java`) — if `rating` is already
    non-null after extraction, treat it as authoritative and do **not**
    overwrite it with the computed aggregate. Only compute/populate
    `rating` when it is still null at that point.
  - No new `ratingLocked` flag needed for this — the null-check above is
    the only guard required (embedded-source values always win by running
    first; no per-provider locks apply to this derived field).

- Recompute trigger: hook into `BookMetadataUpdater.java`
  (`booklore-api/src/main/java/org/booklore/service/metadata/BookMetadataUpdater.java`,
  around the lock-flag application logic at lines ~132-146 / ~630-654) so
  the aggregate recomputes whenever any of the three input provider
  ratings changes via metadata update/sidecar/scan.

- `RuleField.java`
  (`booklore-api/src/main/java/org/booklore/model/dto/RuleField.java`) —
  add a `RATING` entry so the unified field is usable in Magic Shelf
  rules, following the existing `AMAZON_RATING`/`GOODREADS_RATING`
  pattern; wire it into
  `BookRuleEvaluatorService.java:777-790` (getter mapping) and `:949-954`
  (numeric-field list).

### Frontend

- `book.model.ts`
  (`booklore-ui/src/app/features/book/model/book.model.ts`) — `rating`
  field already present (mirrors DTO); no change needed structurally, but
  confirm it's surfaced as "the" rating wherever a single-number rating is
  shown (e.g. book cards/grid).
- `BookSorter.ts`
  (`booklore-ui/src/app/features/book/components/book-browser/sorting/BookSorter.ts`)
  — add/promote `rating` as the default/primary "Rating" sort option
  (distinct from the existing per-provider sort entries, which stay).
- `sort.service.ts`
  (`booklore-ui/src/app/features/book/service/sort.service.ts:79-88`) —
  `rating` extractor already present in `fieldExtractors`; verify it maps
  to the persisted unified field once backend lands.
- No changes to `metadata-viewer.component.html` /
  `metadata-editor.component.html` — individual provider rating badges
  and edit fields stay exactly as they are today.

## Keep — Do Not Touch

- All individual provider rating fields, their lock booleans, and their
  UI display/edit elements stay untouched — this task only adds an
  aggregate on top.
- `audibleRating`, `lubimyczytacRating`, `ranobedbRating` are **not**
  included in the aggregate in this task (matches existing
  `OpdsBookService` precedent) — adding them is a follow-on, not required
  here.
- `personalRating` (user's own rating) is separate and not part of this
  aggregate.
- `OpdsBookService.calculateRating()` can be left as-is or later
  refactored to reuse the new persisted field — not required for this
  task's acceptance criteria.

## Acceptance Criteria

- [ ] `rating` on `BookMetadataEntity` is populated with a weighted
      average of hardcover/amazon/goodreads ratings (weighted by review
      count where available) whenever at least one of those three is
      present and `rating` wasn't already set by embedded-file extraction.
- [ ] `rating` is left untouched when already populated from embedded
      file metadata (e.g. `booklore:rating` PDF tag) — extraction always
      wins over computation.
- [ ] Recomputation happens automatically on metadata update/scan/sidecar
      sync when input provider ratings change.
- [ ] `rating` is sortable and filterable (Magic Shelf `RATING` rule
      field) same as other numeric fields.
- [ ] All existing per-provider rating fields, locks, and UI remain fully
      functional and unchanged.
- [ ] No compile errors.
- [ ] All existing tests pass.
