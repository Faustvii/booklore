# Task 05 — Remove Sidecar Writing

**Priority:** P5  
**Status:** Done  
**Scope:** Backend + Frontend

---

## What to Remove

### Controllers
- Write endpoints in `SidecarController` — remove any `POST`, `PUT`, `PATCH`, `DELETE` mappings
- Keep `GET` (read) endpoints

### Services
- Sidecar write/generate logic in `SidecarService` (or equivalent)
- Keep sidecar read/parse logic used during library scan

### Frontend
- Remove any "Export sidecar", "Save to sidecar", or "Write metadata to file" actions
- Remove related API service calls and TypeScript types

---

## Notes

- **Sidecar reading during library scan is kept.** Only the ability to write `.opf` files from Booklore is removed.
- After removing manual metadata editing (part of Task 01), the only remaining trigger for sidecar writes should be gone anyway — but the write endpoints should still be explicitly removed.

---

## Acceptance Criteria

- [x] No sidecar write endpoints exist (`POST`/`PUT`/`PATCH`/`DELETE` on `/api/v1/sidecar/**`)
- [x] Sidecar read during library scan still works correctly
- [x] App starts without errors
- [x] All existing tests pass; remove or update tests that covered removed code

## Implementation Notes

- `SidecarController` now only exposes the two `GET` endpoints (sidecar content, sync status). The `export`, `import`, `export-all`, and `import-all` endpoints and their `SidecarService` methods were deleted.
- `SidecarMetadataWriter` was stripped down to `deleteSidecarFiles(...)` / `moveSidecarFiles(...)`, which are unrelated file-management helpers still used by `BookService` (book deletion) and `FileMoveService`/`BookMetadataUpdater` (auto-organize) — not the removed writing feature. `writeSidecarMetadata(...)` and its enablement checks (`isWriteOnUpdateEnabled`, `isWriteOnScanEnabled`) were deleted, along with their call sites in `BookMetadataUpdater` (auto-write on metadata update) and `AbstractFileProcessor` (auto-write on scan) — the latter required updating the constructor of `AbstractFileProcessor` and all 7 format-specific processor subclasses to drop the now-unused `SidecarMetadataWriter` dependency.
- The `SidecarSettings` DTO (`enabled`/`writeOnUpdate`/`writeOnScan`/`includeCoverFile`) only ever gated writing, so it was removed entirely from `MetadataPersistenceSettings` on both backend and frontend, along with the "Sidecar" section of the metadata persistence settings UI.
- The sidecar viewer tab keeps its read-only sync-status/content view; only the Export/Import buttons and their service calls were removed.
- Removed unused i18n keys (`exportBtn`, `exportTooltip`, `importBtn`, `importTooltip`, `toast.*` under `metadata.sidecar`, and the sidecar settings keys under `settingsMeta.persistence`) across all 19 locale files, and corrected the now-stale "use the Export button" wording in `noSidecarDescription`.
- Verified with the full backend test suite and a clean Angular production build.
