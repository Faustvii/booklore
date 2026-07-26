# Task 05 — Remove Sidecar Writing

**Priority:** P5  
**Status:** To Do  
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

- [ ] No sidecar write endpoints exist (`POST`/`PUT`/`PATCH`/`DELETE` on `/api/v1/sidecar/**`)
- [ ] Sidecar read during library scan still works correctly
- [ ] App starts without errors
- [ ] All existing tests pass; remove or update tests that covered removed code
