# Task 08 — Remove Metadata File Write-back and Auto-Move

**Priority:** P8
**Status:** Not started
**Scope:** Backend + Frontend

---

## Why

The metadata editor is being kept (see Task 11 for its eventual, deliberately
deferred removal), but it currently has two settings-gated side effects that
mutate files on disk when a user edits metadata — both violate the PRD's
Core Principle 1 ("never writes to, moves, or deletes files"):

1. **Write metadata back into the original file** — when enabled, editing a
   book's metadata rewrites the EPUB OPF / CBX `ComicInfo.xml` / PDF metadata
   / audiobook tags of the source file.
2. **Auto-move/rename on metadata update** — when enabled, editing metadata
   (including via the standalone Metadata Manager's merge/rename/delete
   operations) renames and relocates the file according to the library's
   naming pattern.

Both are off by default but fully wired, user-facing settings in
`MetadataPersistenceSettings` — not dead code. Removing them is what makes it
accurate to say "the metadata editor doesn't touch your files."

## What to Remove

### `MetadataPersistenceSettings`
File: `booklore-api/src/main/java/org/booklore/model/dto/settings/MetadataPersistenceSettings.java`
- `saveToOriginalFile` (per-format `enabled`/`maxFileSizeInMb` for epub/pdf/
  cbx/audiobook)
- `moveFilesToLibraryPattern`
- `convertCbrCb7ToCbz` (auto-converts CBR/CB7 → CBZ on disk; used in
  `BookCoverService`)

### Writers
- `service/metadata/writer/EpubMetadataWriter.java`
- `service/metadata/writer/CbxMetadataWriter.java`
- `service/metadata/writer/PdfMetadataWriter.java`
- `service/metadata/writer/AudiobookMetadataWriter.java`
- `MetadataWriterFactory` and its call site in
  `BookMetadataUpdater.setBookMetadata(...)`

### Auto-move call sites
- `BookMetadataUpdater.setBookMetadata(...)` — the
  `fileMoveService.moveSingleFile(book)` call gated by
  `moveFilesToLibraryPattern`.
- `MetadataManagementService.writeMetadataToFile(...)` — same pattern, called
  from every `consolidateXxx`/`deleteXxx` method (authors, categories, moods,
  tags, series, publishers, languages).
- After removing both call sites, check whether `FileMoveService` and
  `FileMoveService.moveSingleFile(...)` have any remaining callers. If none,
  delete the method/class; if it's still referenced elsewhere, leave it and
  note why in the implementation notes (per the pattern in Task 04/05's
  implementation notes).

### CBR/CB7 conversion
- `convertCbrCb7ToCbz` usage in `BookCoverService`.

### Frontend
- The "Write metadata directly into original files" and "Auto-Move Files on
  Metadata Update" sections of
  `metadata-persistence-settings-component.ts`/`.html`.
- Related i18n keys under `settingsMeta.persistence` in
  `settings-metadata.json` (all locales), including the network-storage
  warning text describing these features.

---

## Notes

- **Keep** the metadata editor writing to Booklore's own DB
  (`BookMetadataUpdater` updating `BookMetadataEntity` fields via the normal
  `PUT /api/v1/books/{bookId}/metadata` flow) — this task only removes the
  disk-mutation side effects, not DB-only editing itself.
- `SidecarSettings` was already removed in Task 05; `MetadataPersistenceSettings`
  should end up containing none of the three fields above once this is done.

---

## Acceptance Criteria

- [ ] No code path writes metadata into an EPUB/PDF/CBX/audiobook file as a
      side effect of a metadata edit (single-book, bulk, or Metadata Manager)
- [ ] No code path renames/moves a file as a side effect of a metadata edit
- [ ] No code path auto-converts CBR/CB7 → CBZ
- [ ] `MetadataPersistenceSettings` no longer has `saveToOriginalFile`,
      `moveFilesToLibraryPattern`, or `convertCbrCb7ToCbz` fields
- [ ] Settings UI no longer shows these toggles
- [ ] Editing metadata still correctly updates Booklore's DB and UI
- [ ] App starts without errors; compile clean
- [ ] All existing tests pass; remove or update tests that covered removed code
