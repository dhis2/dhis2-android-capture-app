# Mobile extensibility: reference branch (`poc/extensibility`)

PoC code for the extensibility initiative, on top of the 3.6.0 spikes. **Not to be merged.**
Each commit matches one task of the epic
[ANDROAPP-7720](https://dhis2.atlassian.net/browse/ANDROAPP-7720) (data set custom forms), so it
can be read as the starting point for that task: start from `develop` and copy what helps.

Background: [Mobile extensibility: PoCs, findings and options](https://dhis2.atlassian.net/wiki/spaces/MOB/pages/1737490434/Mobile+extensibility+PoCs+findings+and+options).

## Commits and tasks

| Commit | Task | What it holds |
| --- | --- | --- |
| `[ANDROAPP-7920] spike: extension-host module` | ANDROAPP-7920 | `extension-host`: models, `ResolveCustomForm`, https rule, HTML scan, `CustomFormRenderer` and reporter contracts |
| `[ANDROAPP-7966] spike: Sentry reporting…` | ANDROAPP-7966 | `SentryExtensionFailureReporter` |
| `[ANDROAPP-7967] spike: data set body seam…` | ANDROAPP-7967 | `DataSetInstanceBodyProvider` / `DataSetInstanceBody` in `aggregates`, `CustomFormBodyProvider` in `app` |
| `[ANDROAPP-7920] spike: unit tests green against the SDK spike` | — | Two test fixes so `./run_tests.sh` is green |
| `[ANDROAPP-7973] poc: WebView runtime (extension-web)` | ANDROAPP-7973 | `extension-web`: `hostWebView`, the `dhis2Native` bridge, `api-shim.js`, `ExtensionApiRouter` |
| `poc: custom forms: legacy shim, fields and saves` | Custom forms (no ticket yet) | `WebViewCustomFormRenderer`, `CustomFormSession`, `custom-form.html/js` |
| `[ANDROAPP-7972] poc: custom forms: survey tooling` | ANDROAPP-7972 | `surveyCustomForm`, logged for every form that opens |
| `poc: next: form field plugins (not 3.6.0)` | Next release | `plugin` module, form placement, bundle download, dataStore sync; see [web-plugins.md](web-plugins.md) |

Tasks with nothing to reuse here: phone UX, file/image/coordinate/org unit fields, multi-org-unit
forms, Play listing, core zip endpoint.

## How it fits together

```text
DataSetTableViewModel ──► DataSetInstanceBodyProvider (aggregates, the seam)
                              │ bound in app: CustomFormBodyProvider
                              ▼
                      ResolveCustomForm (extension-host)
                        SDK DataEntryForm ─► https? ─► device WebView? ─► HTML scan
                              │ form                       │ failure ─► reporter (Sentry) ─► native form
                              ▼
                      WebViewCustomFormRenderer (extension-web)
                        hostWebView ─► custom-form.html ─► bridge ─► CustomFormSession
                          reads:  CustomFormDataRepository (this instance only)
                          writes: the seam's saveValue ─► SetDataValue
```

## Build and run

1. SDK: check out `spike/b2-data-entry-form` in `dhis2-android-sdk` and run
   `./gradlew :core:publishToMavenLocal` (the app builds against `1.16.0-SNAPSHOT`).
2. App: `./gradlew assembleDhis2Debug`. In a worktree, export `ANDROID_HOME` first.
3. Server: a local DHIS2 2.43 with the Sierra Leone demo database (9 custom forms). From the
   emulator it is `http://10.0.2.2:8080`. Debug builds skip the https rule (spike only).
4. Custom forms arrive with **Settings > Sync configuration**, not with the Home refresh.
5. Logcat tag `ExtensionHost` shows the survey of each opened form and every failure.

Tests: `./run_tests.sh` is green (1,901 tests).

## Validated (12 Nov 2026, emulator, DHIS2 2.43 on localhost)

| Check | Result |
| --- | --- |
| SDK stores the forms | 9 forms, 158,745 chars in total, largest 52,376 |
| EPI Stock (JavaScript) opens through the seam, stored values shown, its `formReady` handler runs | OK |
| A value saves through `SetDataValue`; the form's `dataValueSaved` handler runs | OK |
| The value reaches the server after sync; the form reloads after sync | OK |
| Airplane mode: opens and saves | OK |
| Completed instance is read-only; editable after re-opening | OK |
| Plain HTML form (ART monthly summary, 280 fields) | Renders; wider than the screen |
| Data set without custom form | Native tables, no WebView |
| Form that never finishes loading | `RENDER_TIMEOUT` reported, native form, message shown |
| Another form opened right after that timeout | Renders in a new renderer process |
| Form field plugin (Matrix Select) on the shared runtime | Renders with stored values |

## Findings for the tasks

- **A stuck form blocks every later WebView.** The app's WebViews share one renderer process, so
  a form looping in a script kept it busy after the fallback and the next form or plugin stayed
  blank. The PoC terminates the renderer on `RENDER_TIMEOUT` (in the custom forms commit; it
  belongs to ANDROAPP-7973).
- **Blank screen while loading.** Nothing is shown during the up to 10 s before the timeout; a
  loading indicator is needed, and the timeout value needs deciding. Part of the custom forms
  task (legacy shim, fields and saves).
- **Wide forms.** EPI Stock is 1100 px and ART 800 px wide: they scroll sideways, with zoom.
  (Phone UX)
- **Form changes arrive with the configuration sync**, as any metadata; the Home refresh only
  syncs data. Expected; to mention in the user docs.
- **`:form` tests did not compile against the SDK spike**: `blockingProcessAndAdd` now has `File`
  and `Path` overloads. `develop` will need the same fix when it takes that SDK change.
- **A flaky unit test on `develop`.** `DataSetDetailViewModelTest` used the real `Dispatchers.IO`
  and failed whichever test ran next, at random. Fixed here; worth its own PR to `develop`.
- **One origin.** Host pages and content share an origin; isolation and CSP are not done.
  (WebView runtime)
- **API router.** Custom forms get 501 for every `/api/` call. The router still holds the plugin
  endpoints in one class; the real one registers endpoints per content type. (WebView runtime)

## Reuse by native modules

`poc/plugin-system` (native modules, unscheduled) is not in this branch. When that track starts it
can reuse `extension-host` (the `Extension` / `ExtensionKind` / `ExtensionTarget` model, the
resolve-and-fall-back flow, `FailureReason` with the Sentry reporter, and `CompatibilityRule` for
the host/SDK version range) and the data set body seam, which replaces its
`DATA_SET_INSTANCE_CONTENT` slot. It cannot reuse `extension-web`, the https rule or the HTML scan.
For that to hold, `extension-host` must stay free of WebView and HTML specifics.
