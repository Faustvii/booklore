# Task 04 — Remove File Move Between Libraries

**Priority:** P4  
**Status:** Done  
**Scope:** Backend + Frontend

---

## What to Remove

### Controllers
- `FileMoveController`

### Services
- `FileMoveService` (or equivalent — physically moves files on disk between library paths)

### Frontend
- Remove "Move to library" action from book detail / context menus
- Remove related API service calls and TypeScript types

---

## Acceptance Criteria

- [x] No "move file" or "move to library" action exists in the UI
- [x] App starts without errors
- [x] No dangling references to removed classes
- [x] All existing tests pass; remove or update tests that covered removed code

## Implementation Notes

- `FileMoveController` and the bulk cross-library `FileMoveService.bulkMoveFiles(...)` (plus `FileMoveRequest`) were deleted. `FileMoveService` itself was kept but stripped down to `moveSingleFile(...)`, which is unrelated shared infrastructure used by metadata-driven auto-organize (`MetadataManagementService`, `BookMetadataUpdater`) — not the removed library-move feature. `FileMoveHelper` and `FileMoveResult` were untouched for the same reason.
- The `canMoveOrganizeFiles` / `CAN_MOVE_ORGANIZE_FILES` user permission (a persisted `user_permissions` column) only ever gated the removed UI actions, so it was removed end-to-end: `UserPermission` enum entry, DTO/entity fields, OIDC group mapping, user provisioning, admin permissions UI, and a new Flyway migration (`V6__Remove_file_move.sql`) dropping `permission_move_organize_files`.
- Removed unused i18n keys (`browser.tooltip.organizeFiles`, `card.menu.organizeFile`, `seriesPage.tooltip.organizeFiles`, `viewer.menuOrganizeFiles`, `perms.moveOrganize`) across all 19 locale files.
- Verified with the full backend test suite (3064 tests, 0 failures) and a clean Angular production build.
