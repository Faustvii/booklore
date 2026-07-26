# Booklore — Product Requirements Document

**Status:** Draft  
**Last updated:** 2026-07-26

---

## Vision

Booklore is a self-hosted, read-only front-end for existing book and comic libraries. Users point it at pre-existing directories of files; Booklore scans them, reads embedded metadata from the files themselves, and presents a clean reading and organisation interface.

Booklore does **not** manage the library. It does not upload, move, rename, or modify files. It does not query external metadata providers. The library is owned and maintained by the user outside of Booklore.

---

## Core Principles

1. **Read the library, don't manage it.** Booklore scans files and reads embedded metadata. It never writes to, moves, or deletes files.
2. **No outbound metadata provider calls.** Booklore does not query Amazon, GoodReads, Hardcover, ComicVine, or any other provider for metadata or ratings. All metadata comes from the files.
3. **No file ingestion.** Users cannot upload books through the UI, drop them into a watched folder, or attach additional format files through Booklore.
4. **User data lives in Booklore.** Reading progress, shelves, annotations, notes, and sync state are stored in Booklore's database and are independent of the source files.

---

## What Booklore Does

### Library

- Point Booklore at one or more directories. It scans them and indexes the books found.
- Metadata is read from embedded file metadata (EPUB OPF, PDF metadata, ID3/audio tags, CBX metadata) and `.opf` sidecar files (read-only).
- Multiple formats of the same title (e.g. both `.epub` and `.azw3` present on disk) are detected and grouped under a single book entry.
- Ratings are read from embedded file/sidecar metadata only. No provider calls.
- Library rescans can be triggered manually or on a schedule via the task system.

### Reading

- Built-in readers for EPUB, PDF, CBX/comics, and audiobooks.
- Reader preferences per format (fonts, themes, layout, scroll mode, etc.).
- Custom user-uploaded fonts for the EPUB reader.
- In-reader highlights and annotations stored in Booklore's DB.
- Reader bookmarks stored in Booklore's DB.

### Organisation

- **Shelves** — manual, user-curated collections.
- **Magic shelves** — dynamic, filter-based collections (auto-populated by rules).
- **Read status** — unread, reading, finished per user per book.
- **Custom icons** — user-uploaded icons for shelves and libraries.

### User data

- Per-user reading sessions and statistics (time read, books finished, streaks).
- Personal book notes (freeform text per book, per user).
- In-reader annotations and highlights.

### Device & protocol integrations

| Integration | Direction | Notes |
|---|---|---|
| Kobo sync | Two-way progress sync | Full Kobo sync protocol |
| KOReader sync | Two-way progress sync | KOReader sync protocol |
| Hardcover | Outbound progress sync | Per-user API key, opt-in |
| OPDS | Read-only library feed | Standard OPDS protocol |
| Komga-compatible API | Read-only library API | For Komga-compatible reader apps (Panels, Mihon, etc.) |

### Delivery

- Send books to Kindle or any email address via configured SMTP provider.

### Admin

- User management (local accounts + OIDC provisioning).
- OIDC group → Booklore role mapping.
- Content restrictions per user (age rating / tag filtering).
- Audit log of user actions.
- App-wide settings.

### Background tasks

- Periodic and on-demand library scans (detect new, changed, and removed files).
- Cleanup tasks (temp files, expired data).

---

## What Booklore Does Not Do

| Feature | Decision |
|---|---|
| Upload book files | Removed |
| Bookdrop (watched folder ingestion) | Removed |
| Upload additional/alternate format files | Removed |
| Move or rename files between libraries | Removed |
| Query metadata providers (Amazon, GoodReads, ComicVine, etc.) | Removed |
| Manual metadata editing in UI | Removed |
| Write `.opf` sidecar files | Removed |
| Fetch ratings from providers | Removed — ratings from embedded metadata only |
| Metadata fetch jobs, review/proposal workflow | Removed |
| Provider-sourced public book reviews | Removed |

---

## Metadata Strategy

Booklore reads the following metadata sources during scan, in priority order:

1. `.opf` sidecar file alongside the book file (read-only)
2. Metadata embedded inside the book file (EPUB package OPF, PDF XMP/DocInfo, CBX `ComicInfo.xml`, audio ID3/Vorbis tags)

Booklore stores a copy of this metadata in its database to support search, filtering, and display performance. It does **not** write back to the source files or sidecars.

Ratings displayed in Booklore come from the embedded metadata only (e.g. Calibre's `calibre:rating` custom column, ComicInfo `CommunityRating`, etc.).

---

## Out of Scope (Future Consideration)

- Ratings-only refresh from a single trusted provider (e.g. Hardcover) as an optional, admin-enabled feature — deferred, not planned.

---

## Decisions

- **Komga-compatible API maintenance:** Best-effort for now. Booklore will not commit to tracking every Komga API change; compatibility is maintained on a reasonable basis.
- **Content restriction tags:** Sourced from embedded file metadata only. Admins cannot define custom restriction tags independent of what is present in the scanned files.
