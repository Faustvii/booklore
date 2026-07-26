# Task 04 — Remove File Move Between Libraries

**Priority:** P4  
**Status:** To Do  
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

- [ ] No "move file" or "move to library" action exists in the UI
- [ ] App starts without errors
- [ ] No dangling references to removed classes
- [ ] All existing tests pass; remove or update tests that covered removed code
