# Task 03 — Remove File Ingestion (Upload, Bookdrop, Additional Files)

**Priority:** P3  
**Status:** Done  
**Scope:** Backend + Frontend

---

## What to Remove

### Controllers
- `FileUploadController` — book file upload via UI
- `BookdropFileController` — bookdrop watched-folder ingestion API
- Upload endpoint in `AdditionalFileController` (`POST /api/v1/books/{bookId}/files`) — keep download endpoint

### Services
- `FileUploadService` (verify it is not used by anything being kept)
- Bookdrop processing service / scheduled scanner
- Any bookdrop file tracking logic

### Entities & Repositories
- `BookdropFileEntity` / bookdrop tracking entity
- `BookdropFileRepository`

### Background Tasks
- `BOOKDROP_PERIODIC_SCANNING` task type and cron config entry

### Database Migrations
- Add new migration to drop the `bookdrop_file` table (or equivalent)

### Frontend
- Remove upload book UI (drag-and-drop, file picker)
- Remove bookdrop management UI
- Remove "Add additional file" button/form on book detail page
- Remove related API service calls and TypeScript types

---

## Notes

- The **download** endpoint on `AdditionalFileController` (`GET /api/v1/books/{bookId}/files/{fileId}/download`) should be **kept** — users can still download alternate formats that were found during library scan.
- Multi-format detection during library scan is **kept**; only the upload path is removed.

---

## Acceptance Criteria

- [x] No file upload UI exists anywhere in the frontend
- [x] No bookdrop UI or API endpoints exist
- [x] App starts without errors
- [x] Library scan still indexes multi-format books correctly
- [x] Download of alternate formats still works
- [x] New Flyway migration drops bookdrop table cleanly
- [x] All existing tests pass; remove or update tests that covered removed code
