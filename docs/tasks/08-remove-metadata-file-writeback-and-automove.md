# Task 08 — Remove Metadata File Write-back and Auto-Move

**Priority:** P8
**Status:** Done
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

- [x] No code path writes metadata into an EPUB/PDF/CBX/audiobook file as a
      side effect of a metadata edit (single-book, bulk, or Metadata Manager)
- [x] No code path renames/moves a file as a side effect of a metadata edit
- [x] No code path auto-converts CBR/CB7 → CBZ
- [x] `MetadataPersistenceSettings` no longer has `saveToOriginalFile`,
      `moveFilesToLibraryPattern`, or `convertCbrCb7ToCbz` fields
- [x] Settings UI no longer shows these toggles
- [x] Editing metadata still correctly updates Booklore's DB and UI
- [x] App starts without errors; compile clean
- [x] All existing tests pass; remove or update tests that covered removed code

## Implementation Notes

- `EpubMetadataWriter`, `CbxMetadataWriter`, `PdfMetadataWriter`,
  `AudiobookMetadataWriter`, `MetadataWriterFactory`, and the `MetadataWriter`
  interface were deleted outright. Two writer-only helpers with no remaining
  callers went with them: `MetadataCopyHelper` and `BookLoreSchema` (the
  latter was already dead — an unused XMP schema helper referenced only by
  its own test).
- `BookCoverService` also used `MetadataWriterFactory` to embed *cover images*
  into the source EPUB/PDF/CBX/audiobook file on every cover
  generate/upload/URL-set operation (`writeCoverToBookFile` /
  `writeAudiobookCoverToFile`), gated only by `convertCbrCb7ToCbz` — not by
  `saveToOriginalFile`. This wasn't explicitly named in the task, but it's the
  same category of file write-back and depends entirely on the writer
  infrastructure being removed, so it was cut too: covers are now DB/thumbnail
  only, never embedded back into the source file. `BookCoverService` keeps its
  DB-facing behavior (thumbnail generation, cover hash, notifications)
  unchanged; the upload/URL endpoints themselves are unaffected by this task
  (their removal is Task 09's job).
- `MetadataPersistenceSettings` is kept as an empty `@Data @Builder` shell
  (mirroring how `SidecarSettings` was fully deleted in Task 05 while its
  *parent* `MetadataPersistenceSettings` stayed, since it still had other
  fields at the time) — `AppSettingKey.METADATA_PERSISTENCE_SETTINGS`,
  `AppSettings.metadataPersistenceSettings`, and the frontend
  `MetadataPersistenceSettings` model interface are all still wired through,
  just with nothing left to configure.
- `FileMoveService` (and its `FileMoveResult` DTO) had no callers left once
  the auto-move call sites in `BookMetadataUpdater` and
  `MetadataManagementService` were removed, so — per this task's own
  instruction — it was deleted rather than kept, unlike Task 04 where it was
  retained specifically *because* those two callers still existed.
  `FileMoveHelper` (used separately by `BookFileAttachmentService`) was left
  untouched.
- `SidecarMetadataWriter.moveSidecarFiles(...)` was `FileMoveService`'s only
  caller; once `FileMoveService` was deleted, the entire
  `SidecarMetadataWriter` class had zero remaining references anywhere
  (`deleteSidecarFiles` had already been removed in an earlier task), so it
  was deleted too.
- `MetadataChangeDetector.hasValueChangesForFileWrite(...)` and the
  `includedInFileWrite` flag threaded through every `FieldDescriptor`/
  `CollectionFieldDescriptor` entry existed solely to decide whether a field
  change should trigger a file write. With file writes gone, this whole
  concept was removed, along with its dedicated test coverage in
  `MetadataChangeDetectorTest`.
- Two frontend components outside the settings page — `book-file-attacher`
  and `duplicate-merger` — read `metadataPersistenceSettings.moveFilesToLibraryPattern`
  purely to seed the *default* value of an unrelated, still-functional
  "move files" checkbox (their own explicit per-operation choice, backed by
  `BookFileAttachmentService`/`FileMoveHelper`, not by anything removed here).
  That default-seeding subscription (and the now-unused `AppSettingsService`
  injection in each) was removed; the checkbox now just defaults to
  unchecked instead of mirroring the deleted global setting.
- **Follow-up fix:** the first pass missed stale UI copy that still implied
  fields get written to the file. The metadata editor showed a per-field
  "written to physical file" icon/tooltip on ~40 fields, driven by a leftover
  `embeddable-fields.config.ts` module (`isFieldEmbeddable`/
  `hasMetadataWriter`, plus the component's `isEmbeddable()`/`hasWriter()`
  wrappers — `hasWriter()` was already unused in the template). The Metadata
  Manager page also still warned that operations "will modify all relevant
  files" if `'Write to file'` is enabled, with matching processing-time notes
  on the merge/rename/delete dialogs. All of it — the config module, the
  component methods, the `embeddable-indicator` CSS, and the four i18n keys
  (`editor.writtenToFileTooltip`, `manager.writeToFileWarning`,
  `manager.processingNote`, `manager.renameProcessingNote`, all locales) — was
  removed. `editor.fetchFromFileTooltip` (reading metadata embedded in a file,
  a read-only operation that still works) was left untouched.
- Verified with the full backend test suite (2908 tests, 0 failures) and a
  clean Angular production build.
