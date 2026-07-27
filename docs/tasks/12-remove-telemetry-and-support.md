# Task 12 — Remove Telemetry and Support

**Priority:** P12
**Status:** Done
**Scope:** Backend + Frontend

---

## Why

Telemetry and the support button are non-essential to Booklore's core function as a self-hosted library manager. Sending anonymous usage data to an external telemetry server and promoting third-party support/ donation platforms are both out of scope for a privacy-focused, offline-capable tool. The PRD's "What Booklore Does Not Do" should be reflected in the codebase by fully removing the telemetry pipeline and all UI support affordances.

The support button on the topbar has already been removed from the HTML template (see `ui-support.patch`), and telemetry sending has already been hard-disabled via `telemetry.patch` (both `checkAndRunTelemetry()` and `checkAndRunPing()` stubbed out, `isTelemetryEnabled()` returns `false`). These patches are not clean — they leave dead code, unused fields, and stale i18n strings in place. This task cleans up all remnants end-to-end.

## What to Remove

### Backend — Remove entirely
- `booklore-api/src/main/java/org/booklore/service/TelemetryService.java` — entire file
- `booklore-api/src/main/java/org/booklore/model/dto/BookloreTelemetry.java` — entire file
- `booklore-api/src/main/java/org/booklore/crons/CronService.java`:
  - Remove `telemetryService` and `restClient` constructor parameters and fields
  - Remove `sendTelemetryData()` method (public)
  - Remove `sendPing()` method (public)
  - Remove `checkAndRunTelemetry()` method (private)
  - Remove `checkAndRunPing()` method (private)
  - Remove `isTelemetryEnabled()` method (private)
  - Remove constants `LAST_TELEMETRY_KEY`, `LAST_PING_KEY`, `LAST_PING_APP_VERSION_KEY`
  - Remove `import` statements for `BookloreTelemetry`, `TelemetryService`, `InstallationPing`
  - Remove `telemetry` / `ping` scheduled task logic if still present from original patch state
- `booklore-api/src/main/java/org/booklore/config/AppProperties.java`:
  - Remove `Telemetry` inner class
  - Remove `telemetry` field
- `booklore-api/src/main/java/org/booklore/model/dto/settings/AppSettings.java`:
  - Remove `telemetryEnabled` field
- `booklore-api/src/main/java/org/booklore/model/dto/settings/AppSettingKey.java`:
  - Remove `TELEMETRY_ENABLED` enum entry
- `booklore-api/src/main/java/org/booklore/service/appsettings/AppSettingService.java`:
  - Remove line that loads `TELEMETRY_ENABLED` default into `AppSettings` builder
- `booklore-api/src/main/resources/application.yaml`:
  - Remove `telemetry:` section (lines 21-22)

### Backend — Tests
- `booklore-api/src/test/java/org/booklore/crons/CronServiceTest.java`:
  - Remove all telemetry/ping-related test methods (`sendTelemetryData_*`, `sendPing_*`, `checkAndRunTelemetry_*`, `checkAndRunPing_*`)
  - Remove `TelemetryService` and `BookloreTelemetry` mocks/imports
  - Remove `restClient` mock if no longer needed by remaining tests
  - Remove `appProperties.getTelemetry()` usages

### Frontend — Remove entirely
- `booklore-ui/src/app/shared/components/github-support-dialog/github-support-dialog.ts` — entire file
- `booklore-ui/src/app/shared/components/github-support-dialog/github-support-dialog.html` — entire file
- `booklore-ui/src/app/shared/components/github-support-dialog/github-support-dialog.scss` — entire file
- `booklore-ui/src/app/shared/components/github-support-dialog/github-support-dialog.spec.ts` — entire file (if exists)
- `booklore-ui/src/app/shared/layout/component/layout-topbar/app.topbar.component.ts`:
  - Remove `SUPPORT_ANIMATION_KEY` import from `global-preferences.component`
  - Remove `supportAnimationEnabled` property
  - Remove `storage` event listener for `SUPPORT_ANIMATION_KEY`
  - Remove `openGithubSupportDialog()` method
- `booklore-ui/src/app/features/settings/global-preferences/global-preferences.component.ts`:
  - Remove `SUPPORT_ANIMATION_KEY` export
  - Remove `supportButtonAnimation` property
  - Remove `enableTelemetry` from `toggles` object and model
  - Remove telemetry toggle initialization from settings load
  - Remove `onSupportAnimationChange()` method
  - Remove telemetry setting save logic (`enableTelemetry: AppSettingKey.TELEMETRY_ENABLED`)
- `booklore-ui/src/app/features/settings/global-preferences/global-preferences.component.html`:
  - Remove the Telemetry Setting Section (lines ~186-207)
  - Remove the appearance support button animation toggle (lines ~26-34)
- `booklore-ui/src/app/shared/components/external-doc-link/external-doc-link.component.ts`:
  - Remove `'telemetry'` from the `DocType` union type
  - Remove `telemetry` entry from `DOC_URLS`
- `booklore-ui/src/app/shared/services/dialog-launcher.service.ts`:
  - Remove `GithubSupportDialog` import
  - Remove `openGithubSupportDialog()` method
- `booklore-ui/src/app/shared/model/app-settings.model.ts`:
  - Remove `telemetryEnabled: boolean` field
  - Remove `TELEMETRY_ENABLED` from `AppSettingKey` enum
- `booklore-ui/src/app/shared/layout/component/layout-topbar/app.topbar.component.scss`:
  - Remove all heart/support button animation styles (`.heart-button`, `.heart-button-static`, `.heart-ripple`, `.heart-orbit`, `.heart-icon`, `.heart-ripple-delayed`, `@keyframes heart-float`, `@keyframes heartbeat`)
- `booklore-ui/src/i18n/en/settings-application.json`:
  - Remove `appearance.supportButtonAnimation` and `appearance.supportButtonAnimationDesc`
  - Remove `telemetry` section entirely
- `booklore-ui/src/i18n/en/shared.json`:
  - Remove `supportDialog` section entirely
- `booklore-ui/src/i18n/en/layout.json`:
  - Remove `supportBookLore` key
- All other locale files under `booklore-ui/src/i18n/`: apply equivalent removals for `telemetry`, `appearance.supportButtonAnimation`, `appearance.supportButtonAnimationDesc`, `shared.supportDialog`, and `layout.supportBookLore` keys

### Related cleanup
- Verify `booklore-ui/src/app/app.routes.ts` has no telemetry or support routes (check also)
- Remove any remaining `@opentelemetry/api` dependency from `booklore-ui/package.json` if it exists only for telemetry (check — it's a transitive dev dependency and may need to stay)

---

## Keep — Do Not Touch

- `ReadingSessionController` recording telemetry from the e-reader client (KOReader/Kobo) — this is about receiving reading session data from the device, not the Booklore outbound telemetry system
- `supportsDualCovers()` methods — these are about book cover rendering, not the support button
- Font format support checks — unrelated to the support button

---

## Acceptance Criteria

- [x] `TelemetryService` and `BookloreTelemetry` classes removed from backend
- [x] `CronService` no longer contains `sendTelemetryData()`, `sendPing()`, `checkAndRunTelemetry()`, `checkAndRunPing()`, or `isTelemetryEnabled()`
- [x] `AppProperties` no longer has a `Telemetry` inner class or `telemetry` field
- [x] `AppSettings` no longer has `telemetryEnabled` field
- [x] `AppSettingKey` no longer has `TELEMETRY_ENABLED` entry
- [x] `application.yaml` no longer references `telemetry`
- [x] Backend tests pass with all telemetry/ping tests removed
- [x] `GithubSupportDialog` component (ts, html, scss) removed entirely
- [x] `app.topbar.component.ts` no longer references `SUPPORT_ANIMATION_KEY`, `supportAnimationEnabled`, or `openGithubSupportDialog()`
- [x] `global-preferences.component.ts` no longer has `SUPPORT_ANIMATION_KEY`, `supportButtonAnimation`, or telemetry toggle logic
- [x] `global-preferences.component.html` no longer has telemetry section or support button animation toggle
- [x] `external-doc-link.component.ts` no longer has `telemetry` in `DocType` union or `DOC_URLS`
- [x] `dialog-launcher.service.ts` no longer references `GithubSupportDialog`
- [x] `app-settings.model.ts` no longer has `telemetryEnabled` or `TELEMETRY_ENABLED`
- [x] `app.topbar.component.scss` no longer has heart/support button animation styles
- [x] All locale JSON files: `telemetry`, `appearance.supportButtonAnimation*`, `shared.supportDialog`, and `layout.supportBookLore` keys removed
- [x] App starts without errors; compile clean
- [x] All existing tests pass
