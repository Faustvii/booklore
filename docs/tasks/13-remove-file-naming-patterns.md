# Task 13 — Remove File Naming Patterns

**Priority:** P13
**Status:** Not started
**Scope:** Backend + Frontend

---

## Why

File naming patterns are a feature for managing physical library files — renaming and organizing files on disk using metadata placeholders. Since Booklore no longer manages physical library files (file write-back, auto-move, and file deletion were all removed or deprecated), file naming patterns have no purpose. The pattern engine, its UI, the backend endpoint, the database column, and the supporting utilities are all dead code that should be removed.

This is a full removal: the `file_naming_pattern` column on the `library` table, the API endpoint, the settings panel, the pattern resolver utility, and all related i18n strings should be deleted.

## What to Remove

### Backend — Remove entirely
- `booklore-api/src/main/java/org/booklore/model/entity/LibraryEntity.java`:
  - Remove `fileNamingPattern` field and its `@Column(name = "file_naming_pattern")` annotation
- `booklore-api/src/main/java/org/booklore/model/dto/Library.java`:
  - Remove `fileNamingPattern` field
- `booklore-api/src/main/java/org/booklore/controller/LibraryController.java`:
  - Remove `setFileNamingPattern()` method (lines ~115-127) including the `PATCH /{libraryId}/file-naming-pattern` endpoint
- `booklore-api/src/main/java/org/booklore/service/library/LibraryService.java`:
  - Remove `setFileNamingPattern()` method
  - Remove `AuditAction.NAMING_PATTERN_CHANGED` usage in `setFileNamingPattern()`
- `booklore-api/src/main/java/org/booklore/service/file/FileMovingHelper.java`:
  - Remove `getFileNamingPattern()` method (line ~23)
  - Remove fallback pattern `{currentFilename}` if it is only used here
- `booklore-api/src/main/java/org/booklore/service/file/FileMoveHelper.java`:
  - Remove `getFileNamingPattern()` method (line ~153)
  - Remove fallback pattern usage if it is only used here
- `booklore-api/src/main/java/org/booklore/service/book/BookFileAttachmentService.java`:
  - Remove `fileNamingPattern` local variable and `PathPatternResolver.resolvePattern()` calls that use it (lines ~163-164, 235)
- `booklore-api/src/main/java/org/booklore/util/PathPatternResolver.java` — entire file
- `booklore-api/src/main/java/org/booklore/model/enums/AuditAction.java`:
  - Remove `NAMING_PATTERN_CHANGED` enum value
- `booklore-api/src/main/java/org/booklore/audit/` — verify no remaining references to `NAMING_PATTERN_CHANGED`
- `booklore-api/src/main/resources/db/migration/V1__Baseline.sql`:
  - Remove `file_naming_pattern varchar(255)` from the `library` table `CREATE TABLE` statement
- `booklore-api/src/main/resources/db/migration/` — check for any newer migration files that reference `file_naming_pattern` and remove them too
- `booklore-api/src/test/java/` — remove any tests asserting `fileNamingPattern`, `file_naming_pattern`, `setFileNamingPattern`, `getFileNamingPattern`, or `NAMING_PATTERN_CHANGED`

### Frontend — Remove entirely
- `booklore-ui/src/app/features/settings/file-naming-pattern/file-naming-pattern.component.ts` — entire file
- `booklore-ui/src/app/features/settings/file-naming-pattern/file-naming-pattern.component.html` — entire file
- `booklore-ui/src/app/features/settings/file-naming-pattern/file-naming-pattern.component.scss` — entire file
- `booklore-ui/src/app/features/settings/file-naming-pattern/file-naming-pattern.component.spec.ts` — entire file (if exists)
- `booklore-ui/src/app/features/settings/settings.component.ts`:
  - Remove `FileNamingPatternComponent` import
  - Remove `NamingPattern = 'naming-pattern'` enum entry
  - Remove `FileNamingPatternComponent` from `imports` array
- `booklore-ui/src/app/features/settings/settings.component.html`:
  - Remove the `p-tab` item for `SettingsTab.NamingPattern` (lines ~32-35)
  - Remove the `p-tabpanel` for `SettingsTab.NamingPattern` (lines ~96-98)
- `booklore-ui/src/app/features/book/service/library.service.ts`:
  - Remove `updateLibraryFileNamingPattern()` method (line ~139)
- `booklore-ui/src/app/shared/components/external-doc-link/external-doc-link.component.ts`:
  - Remove `'fileNamePatterns'` from the `DocType` union type
  - Remove `fileNamePatterns` entry from `DOC_URLS`
- `booklore-ui/src/app/shared/util/pattern-resolver.ts` — entire file
- `booklore-ui/src/app/shared/styles/_settings-shared.scss`:
  - Remove the benchmark comment referencing `file-naming-pattern` component (line ~3)
- `booklore-ui/src/i18n/en/settings-naming.json` — entire file (remove)
- `booklore-ui/src/i18n/en/settings.json`:
  - Remove `patterns` key from settings navigation
- All other locale files under `booklore-ui/src/i18n/`:
  - Remove `settings-naming.json` files
  - Remove `patterns` key from respective `settings.json` files

---

## Keep — Do Not Touch

- `FileMovingHelper` file-moving logic that does not depend on `getFileNamingPattern()` — keep the file if it has other responsibilities
- `BookFileAttachmentService` methods that use `resolvePattern` for non-naming-pattern purposes (if any)
- The `external-doc-link` component itself — only remove the `fileNamePatterns` doc type entry

---

## Acceptance Criteria

- [ ] `file_naming_pattern` column removed from `library` table (via V1 migration or a follow-up migration)
- [ ] `LibraryEntity.fileNamingPattern` field removed
- [ ] `Library.fileNamingPattern` DTO field removed
- [ ] `LibraryController.setFileNamingPattern()` endpoint removed
- [ ] `LibraryService.setFileNamingPattern()` method removed
- [ ] `AuditAction.NAMING_PATTERN_CHANGED` removed
- [ ] `PathPatternResolver.java` removed
- [ ] `getFileNamingPattern()` removed from `FileMovingHelper` and `FileMoveHelper`
- [ ] `BookFileAttachmentService` no longer references `fileNamingPattern` or `PathPatternResolver.resolvePattern()`
- [ ] `FileNamingPatternComponent` and all 3 files (ts, html, scss, spec) removed
- [ ] `SettingsTab.NamingPattern` enum entry removed from `settings.component.ts`
- [ ] `FileNamingPatternComponent` removed from imports and template in `settings.component.ts` and `settings.component.html`
- [ ] `updateLibraryFileNamingPattern()` removed from `library.service.ts`
- [ ] `fileNamePatterns` doc type removed from `external-doc-link.component.ts`
- [ ] `pattern-resolver.ts` removed from frontend
- [ ] All `settings-naming.json` locale files removed
- [ ] `patterns` key removed from all `settings.json` locale files
- [ ] No compile errors or missing imports after removal
- [ ] All existing tests pass