# Task 02 — Remove Provider-Sourced Book Reviews

**Priority:** P2  
**Status:** Done  
**Scope:** Backend + Frontend

---

## What to Remove

### Controllers
- `BookReviewController`

### Services
- `BookReviewService` (or equivalent — handles fetching and storing reviews from providers)

### Entities & Repositories
- `BookReviewEntity` / `PublicBookReviewEntity`
- `BookReviewRepository`

### DTOs
- `BookReview` DTO

### Database Migrations
- Add new migration to drop the `public_book_review` table

### Frontend
- Remove reviews tab/section from book detail page
- Remove any "Refresh Reviews" action
- Remove related API service calls and TypeScript types
- **Keep** the aggregated `rating` field display (star rating on book detail/cards) — this is sourced from embedded file metadata, not from reviews

---

## Acceptance Criteria

- [x] App starts without errors
- [x] No book detail page shows a reviews section
- [x] New Flyway migration drops the table cleanly
- [x] No dangling references to removed classes
- [x] All existing tests pass; remove or update tests that covered removed code
