# Task 10 — Switch Library Scan Default to "Replace All Metadata"

**Priority:** P10
**Status:** Done
**Scope:** Backend + Frontend

---

## Why

A library rescan can refresh a book's metadata from its embedded file data in
one of two modes: `REPLACE_MISSING` ("Update Missing Metadata Only" — only
fills in fields Booklore doesn't already have) or `REPLACE_ALL` ("Replace All
Metadata" — fully resyncs from the file). `REPLACE_MISSING` is currently the
default and is labeled "(Recommended)". Since the embedded file is the source
of truth for metadata, a rescan should fully resync from it by default —
`REPLACE_ALL` should be the default and the recommended option going forward.

This is a default-flip, not a removal: both modes remain available and
user-selectable.

## What to Change

### Frontend
File: `booklore-ui/src/app/features/settings/task-management/task-management.component.ts`
- `selectedMetadataReplaceMode` (line ~74): change default from
  `MetadataReplaceMode.REPLACE_MISSING` to `MetadataReplaceMode.REPLACE_ALL`.
- `metadataReplaceOptions` (lines ~60-73): move the "(Recommended)" label
  from the `REPLACE_MISSING` entry to the `REPLACE_ALL` entry.
- Update the corresponding i18n description keys
  (`settingsTasks.metadataReplace.replaceMissingDesc` /
  `.replaceAllDesc`, all locales) if their copy references which mode is
  recommended.

### Backend
- Check `TaskCronConfigurationEntity` / `TaskCronService` (and any seeded/
  persisted default options for the `LIBRARY_RESCAN` task type) for a stored
  default `metadataReplaceMode` so scheduled (cron) scans — not just manually
  triggered ones — pick up the new default, not just the frontend's
  pre-selected value.

---

## Acceptance Criteria

- [x] Triggering a manual library rescan with no explicit selection uses
      `REPLACE_ALL`
- [x] Scheduled/cron-triggered rescans also use `REPLACE_ALL` by default
- [x] UI labels "(Recommended)" on `REPLACE_ALL`, not `REPLACE_MISSING`
- [x] `REPLACE_MISSING` remains selectable and functions unchanged
- [x] App starts without errors; compile clean
- [x] All existing tests pass; update any tests asserting the old default
