# Task 11 — Remove Manual Metadata Editing UI

**Priority:** P11 (lowest — file last unless a future task depends on it)
**Status:** Not started
**Scope:** Backend + Frontend

---

## Why

The PRD's "What Booklore Does Not Do" table lists "Manual metadata editing in
UI — Removed," but the full editor (single book, bulk, multi-book, and the
standalone Metadata Manager) is still present. This is deliberately
deprioritized: once Task 08 removes the file-write/auto-move side effects,
manual editing only touches Booklore's own DB, which is an acceptable interim
state. This task is the eventual full removal, to be scheduled last unless
something else in the backlog turns out to depend on it first.

**Depends on:** Task 08 (remove metadata file write-back and auto-move)
should land first.

## What to Remove

### Single-book editor
- `booklore-ui/.../metadata/component/book-metadata-center/metadata-editor/metadata-editor.component.ts`/`.html`
- `booklore-ui/.../metadata/component/book-metadata-center/book-metadata-center.component.ts`/`.html`
- `BookController.updateMetadata` (`PUT /api/v1/books/{bookId}/metadata`) and
  `BookMetadataUpdater.setBookMetadata(...)`

### Bulk / multi-book editing
- `bulk-metadata-update-component.ts`/`.html`
- `multi-book-metadata-editor-component.ts`/`.html`
- `PUT /api/v1/books/bulk-edit-metadata` (`BookMetadataService.bulkUpdateMetadata`)
- `PUT /api/v1/books/metadata/toggle-all-lock`
- `PUT /api/v1/books/metadata/toggle-field-locks`

### Standalone Metadata Manager
- `metadata-manager.component.ts`/`.html`, its `/metadata-manager` route
  (`app.routes.ts`), and its topbar nav entry
  (`layout-topbar/app.topbar.component.ts`)
- `POST /api/v1/books/metadata/manage/consolidate`
- `POST /api/v1/books/metadata/manage/delete`
- `MetadataManagementService`

### Related cleanup (fold in here if not done earlier)
- `CAN_BULK_AUTO_FETCH_METADATA` / `CAN_BULK_CUSTOM_FETCH_METADATA`
  permissions — leftover from Task 01, no controller currently uses them.
  Remove end-to-end (enum, DTO/entity, admin UI checkboxes).
- Orphaned `providers` section in `settings-metadata.json` (all locales) —
  leftover translation block from the removed provider-settings UI, no
  component references it.
- `POST /api/v1/books/metadata/recalculate-match-scores` /
  `MetadataMatchService` — verify nothing else calls this once editing is
  gone; remove if dead.
- Provider-ID lock/edit fields on the metadata editor (ASIN, Goodreads/
  Hardcover/Comicvine/Google/Lubimyczytac/Ranobedb/Audible IDs and ratings)
  go away naturally with the editor itself; the underlying `BookMetadataEntity`
  columns stay (per Task 06 — they're populated from embedded file metadata).

---

## Keep — Do Not Touch

- Cover generation/regeneration (Task 09) and author bio/photo lookup — both
  are independent of the metadata editor and explicitly kept per
  `docs/PRD.md` Decisions.
- Reading and displaying metadata (book detail pages, cards, filters) —
  unaffected; only the editing surface is removed.

---

## Acceptance Criteria

- [ ] No UI exists to manually edit a book's metadata (single, bulk, or via
      Metadata Manager)
- [ ] No endpoint accepts a manual metadata update from the UI
- [ ] Metadata Manager route, page, and nav entry are gone
- [ ] Orphaned permissions and i18n `providers` block cleaned up
- [ ] Library scan still correctly populates all metadata fields (this
      becomes the *only* way metadata enters Booklore's DB)
- [ ] App starts without errors; compile clean
- [ ] All existing tests pass; remove or update tests that covered removed code
