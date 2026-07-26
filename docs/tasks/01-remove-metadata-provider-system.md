# Task 01 — Remove Metadata Provider Querying System

**Priority:** P1 (highest — also fixes active runtime bug)  
**Status:** To Do  
**Scope:** Backend + Frontend

---

## Why First

This is the largest removal and causes an active production bug: `getActiveTasks()` runs a `SELECT DISTINCT` on a JSON column which PostgreSQL rejects. Removing this system eliminates the bug and the most code.

---

## What to Remove

### Controllers
- `MetadataController`
- `MetadataTaskController`

### Services
- `MetadataRefreshService`
- `MetadataTaskService`
- `BookMetadataUpdater` (if solely used by refresh)
- All provider parsers under `service/metadata/parser/` (Amazon, GoodReads, Hardcover, ComicVine, Douban, Lubimyczytac, Ranobedb, Audible, etc.)

### Entities & Repositories
- `MetadataFetchJobEntity`
- `MetadataFetchProposalEntity`
- `MetadataFetchJobRepository`
- `MetadataFetchProposalRepository`

### DTOs / Mappers
- `FetchedProposal`, `MetadataFetchTask`, `MetadataTaskDetailsResponse`, `MetadataBatchProgressNotification`
- `FetchedProposalMapper`
- `MetadataRefreshRequest`, `MetadataRefreshOptions`, `FetchMetadataRequest`

### Enums
- `MetadataFetchTaskStatus`
- `FetchedMetadataProposalStatus`
- `MetadataProvider` (verify nothing else depends on this)
- `MetadataReplaceMode`

### Background Tasks
- `TempFetchedMetadataCleanupTask`
- Remove `REFRESH_LIBRARY_METADATA`, `CLEANUP_TEMP_METADATA`, `REFRESH_METADATA_MANUAL` from `TaskType` enum and task cron config

### Database Migrations
- Add new migration to drop `metadata_fetch_jobs` and `metadata_fetch_proposals` tables
- Remove any FK constraints referencing them

### Frontend
- Remove metadata fetch UI (bulk refresh, review/proposal workflow, task status panel)
- Remove any routes, components, and services related to metadata provider querying

---

## Acceptance Criteria

- [ ] App starts without errors
- [ ] No references to removed classes remain (compile clean)
- [ ] `getActiveTasks`-equivalent endpoint no longer exists
- [ ] Library scan still works and reads embedded metadata correctly
- [ ] New Flyway migration drops the two tables cleanly
- [ ] All existing tests pass; remove or update tests that covered removed code
