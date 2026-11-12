# Data set custom forms: types, support and PoC status

Analysis for [ANDROAPP-7972](https://dhis2.atlassian.net/browse/ANDROAPP-7972) (survey of real custom
forms), input for the epic [ANDROAPP-7720](https://dhis2.atlassian.net/browse/ANDROAPP-7720).

- **Test server:** <https://android.im.dhis2.org/custom-forms> (DHIS2 2.41.11): the core team's
  `custom-forms-41` DB plus the five `[7972]` test forms. Every form named below is there. Log in as
  `android` (org unit Ngelehun CHC), then **Settings > Sync configuration**: custom forms arrive with it,
  not with the Home refresh. The UG form also needs public data sharing (`rwrw----`) on its category
  options, or `android` cannot see it.
- **Build:** in `dhis2-android-sdk`, check out `spike/b2-data-entry-form` and run
  `./gradlew :core:publishToMavenLocal`; then `./gradlew assembleDhis2Debug` here. Debug builds skip the
  https rule (PoC only), so a local http server also works.
- **Real forms:** only two come from real implementations, **UG** (HMIS 105:10, 1,604 fields) and **SA**
  (COVID-19 daily totals, 573 KB). Everything else is demo or test content.
- **Old / New** in the tables: the old web Data Entry app (`dhis-web-dataentry`) and the new Data Entry
  app, which renders forms without JavaScript itself ("modern rendering") and runs the others in its
  Legacy Custom Forms plugin ([custom-forms.md](https://github.com/dhis2/aggregate-data-entry-app/blob/beta/docs/custom-forms.md),
  [custom-forms-legacy-plugin.md](https://github.com/dhis2/aggregate-data-entry-app/blob/beta/docs/custom-forms-legacy-plugin.md)).
- **Marks:** ✅ works (tested on the emulator) · ⚠️ partly · ⛔ not supported · ⏳ open · ? not verified.

## Where the code is

| Piece | Code | What it does |
| --- | --- | --- |
| Decision on open | `extension-host`: `ResolveCustomForm`, `CompatibilityRule.kt` (`NoFormScripts`, `LegacyApiScan`), `ExtensionHostConfig.legacyShim` | Custom form or native form, and why |
| Body seam | `aggregates`: `DataSetInstanceBody`, `DataSetTableViewModel` (`saveBodyValue`, `openBodyInput`) | The data set screen shows the form instead of its tables, saves its values and lends it its input |
| Glue | `app`: `CustomFormBodyProvider` | Joins the seam to `extension-host` |
| Renderer | `extension-web`: `WebViewCustomFormRenderer`, `CustomFormSession` (bridge messages), `CustomFormDataRepository`, `CustomFormPage` (picks the page) | Runs the form in the WebView |
| Pages and scripts | `extension-web/src/androidMain/assets/extension-web/`: `custom-form.html` + `custom-form-binding.js` (step 1); `custom-form-legacy.html` + `legacy-shim.js` + jQuery 3.2.1 (step 2) | What runs inside the WebView |
| Survey | `extension-host`: `CustomFormSurvey`, logged with tag `ExtensionHost` for every form that opens | What each form uses |

## Step 1: forms without scripts (ANDROAPP-8006)

- **Which forms:** no `<script>`, no `on*=` attribute and no `javascript:` link. They open on
  `custom-form.html`. 11 of the 18 forms on the test server, but neither real form (UG has a script, SA
  needs step 2).
- **What runs:** the form's HTML is drawn in the WebView; none of its scripts run. Only the app's
  **binding layer** (`custom-form-binding.js`, no library) runs: it finds the
  `{dataElement}-{categoryOptionCombo}-val` fields, shows their values, fills totals and indicators,
  keeps greyed fields disabled and adds the old app's classes (`entryfield`, `dataelementtotal`,
  `indicator`) so the form's CSS applies. Same approach as the new app's modern rendering.
- **Native does the rest:** saving (`SetDataValue`, as a table cell), validation and the rejection reason,
  refusing data elements outside the data set, read-only state, completion, validation rules and sync.
- **Input:** every field opens the data set screen's own input sheet, the one a table cell uses, with its
  keyboard, pickers, validation and actions; Next follows the form's field order, Done on the last. The
  only exception is the step 2 page, where form scripts listen to `change` on the old inputs: there only
  file, image, coordinate and org unit use the native input.
- **Forms with scripts:** with `ExtensionHostConfig.legacyShim` off (step 1 alone, and the ANDROAPP-8006
  commit), `NoFormScripts` sends them to the native form (`UNSUPPORTED_FEATURE`). Their scripts are not
  stripped: that would change what the form does (UG's tabs would stack, EPI Stock's messages would never
  show).
- **Open:** phone UX for wide forms (they scroll sideways), the form does not scroll to or highlight the
  field being edited, data value details (comment, history, follow-up).

| # | Type | Web Data Entry | PoC | Test on the server with |
| --- | --- | --- | --- | --- |
| 1 | HTML layout: tables, labels, fields | Old: ✅<br>New: ✅ modern rendering | ✅ | Morbidity, ART monthly summary (280 fields), Life-Saving Commodities, HIV Peadiatric, TB Facility, Staffing, `#1 Wedika Custom form 1` |
| 2 | HTML + CSS, fixed widths | Old: ✅<br>New: ⚠️ styles partly | ✅ The form's CSS applies. ⏳ Phone UX: ART is 800 px wide, EPI Stock 1100 px | Clinical Monitoring Checklist, Mortality < 5 years Narratives, ART |
| 3 | Totals and indicators, configured, no JS | Old: ✅<br>New: ✅ | ✅ Totals summed by the binding layer, indicators computed by the app | EPI Stock, UG (both have scripts, so step 2 page) |
| 4 | Attribute option combo | Old: ✅<br>New: ✅ | ✅ Passed by the host | ART (Implementing Partner, Project), EPI Stock (Project), UG (Nationality) |
| 5a | Number, text, long text, Yes/No, Yes-only, date, time | Old: ✅<br>New: ✅ | ✅ Native input | `[7972] Value types`, `[Custom] with different value type` |
| 5b | Option set | Old: ✅<br>New: ✅ | ✅ Native input; the form shows the option name | `[7972] Value types` |
| 5c | Multi-select (`MULTI_TEXT`) | Old: ?<br>New: ✅ | ✅ Native input; the form shows the option names | `[7972] Value types`, `[Custom] with different value type` |
| 5d | Age, date-time, email, phone, URL, percentage | Old: ✅<br>New: ? | ✅ Native input; invalid values rejected with the reason | `[7972] Value types` |
| 5e | File, image, coordinate, org unit | Old: ⚠️ file only<br>New: ? | ✅ Native input: file picker, camera, map picker, org unit tree; uploaded and synced | `[7972] Value types` |
| 6 | Greyed (disabled) fields | Old: ✅<br>New: ✅ | ✅ Stay disabled, never saved | `[7972] JS logic` (step 2 page) |

## Step 2: forms with scripts, the legacy shim (ANDROAPP-8007)

- **Why a shim:** custom forms were written to run inside the old Data Entry page, so their scripts expect
  what it had: jQuery, a global `dhis2` object (`dhis2.util.on`, the `dhis2.de.event.*` events,
  `dhis2.de.current*`, metadata such as `dhis2.de.dataElements`) and hidden elements such as
  `#selectedPeriodId`. The WebView has none of that.
- **What it is:** `legacy-shim.js` recreates the part forms use, loaded only on `custom-form-legacy.html`
  with jQuery 3.2.1. It adds to step 1: saves, validation and the binding layer stay the same; the shim
  translates between them and the form's scripts (a confirmed save becomes `dataValueSaved`). The new
  app's Legacy Custom Forms plugin is the same idea, much larger (about 100 `dhis2.de.*` names, jQuery
  UI, select2, Bootstrap, floatThead, the calendars).
- **When it runs:** `ExtensionHostConfig.legacyShim` on (the PoC on Android, from the ANDROAPP-8007
  commit). `CustomFormPage` then opens forms with scripts on the legacy page.
- **What still sends a form to the native form:** `LegacyApiScan` finds `dhis2.de.*` and `dhis2.util.*`
  names the shim does not provide. API calls, external files, legacy UI libraries and other `dhis2.*`
  globals are **not detected yet**: those forms open half-working. The form's own `/api/` calls are
  answered 501 (`ExtensionApiRouter`).
- **Why two layers:** step 1 ships without any of the shim, the code the Play review sees stays small, and
  the shim can grow name by name, from the survey, without touching saves.

| # | Type | Web Data Entry | PoC | Mobile possible? | Test on the server with |
| --- | --- | --- | --- | --- | --- |
| 7 | Events `formLoaded`, `dataValuesLoaded`, `formReady`, `dataValueSaved` | Old: ✅<br>New: ✅ plugin | ✅ | Yes | EPI Stock ("Form loaded successfully"), `[7972] JS logic` |
| 7b | Events `completed`, `uncompleted`, `validationSuccess/Error` | Old: ✅<br>New: ⛔ not fired | ⛔ Declared supported but never fired: fire them from the data set screen, or drop them | Yes | — |
| 8 | Self-contained logic: show/hide, JS validation, JS calculations, plain-JS tabs, `onclick=`/`onchange=` | Old: ✅<br>New: ✅ plugin | ✅ JS validation only warns, the value is saved anyway (as in the old app). UG's tab menu takes half the screen | Yes | `[7972] JS logic`, UG |
| 9 | Legacy metadata and helpers: `dhis2.de.dataElements`, `getCurrentOrganisationUnit`, `dhis2.de.cst.*`, `dhis2.period.*`, `dhis2.validation.*`… | Old: ✅<br>New: ✅ plugin | ⛔ `dhis2.de.*` names the shim lacks send the form to the native form (the scan is text-only: it also blocks guarded reads); `dhis2.period.*` and `dhis2.validation.*` are not scanned | Yes, name by name | `[7972] JS logic & metadata`, SA (native form) |
| 10 | Legacy UI libraries used without loading them: jQuery UI, Bootstrap, select2, floatThead, calendars | Old: ✅<br>New: ✅ plugin bundles them | ⛔ Not detected: opens unstyled, fields save. Bundle them offline, or detect and fall back | Yes | `[7972] Legacy UI libs` |
| 11 | External files by URL: `<script src>`, `<link>`, `<img>` | Old: ✅<br>New: ⛔ scripts; CSS and images ? | ⛔ Not detected: not loaded (a `data:` image works). Detect and fall back | No: offline and Play policy | `[7972] API & external` |
| 12 | Web API reads: other periods, dataStore, `/me`, analytics | Old: ✅<br>New: ✅ plugin, online | ⛔ Not detected: answered 501. Detect and fall back; later, offline answers from SDK data | Partly | `[7972] API & external`, SA |
| 13 | Web API writes outside the open instance | Old: ✅<br>New: ✅ plugin | ⛔ Detect and fall back | Not in 3.6.0 | SA |
| 14 | Overriding the old app's functions (`saveVal`, `loadDataValues`…) | Old: ✅<br>New: ⚠️ "patches will not work" | ⛔ Blocked by the scan | No | SA |
| 15 | Multi-org-unit entry | Old: ⚠️ section forms only<br>New: ⛔ dropped (DHIS2-21808) | ⛔ | Not for custom forms | — |

## Both steps

| Topic | PoC | Note |
| --- | --- | --- |
| Offline, read-only | ✅ | Forms open and save offline; a completed or locked instance is read-only (EPI Stock) |
| Data value details (comment, history, follow-up, audit) | ⏳ | Phone UX in ANDROAPP-8006; the native input sheet is the natural place |
| Form size | ✅ 573 KB (SA) and 1,604 fields (UG) | Input for ANDROSDK-2431 |
| Category option sharing | — | Not custom-form specific: without data sharing on its category options the data set is hidden and values are refused (UG on the test server) |
| Values shared between data sets | — | DHIS2 stores values per data element: a data element in two data sets shows the same value in both (`[7972] Value types` reuses demo data elements) |

## What each type means

1. **HTML layout.** A table with fields `{dataElement}-{categoryOptionCombo}-val`, typed in the Maintenance
   app editor. No script.
2. **HTML + CSS.** `<style>` blocks, inline styles and fixed widths (`<table style="width:1100px">`). On a
   phone, anything over about 400 px scrolls sideways.
3. **Totals and indicators.** `<input id="total{de}">` and `<input indicatorid="…">` cells that the app
   fills; the indicator must be in the data set. No script.
4. **Attribute option combo.** The data set's own category combination (Implementing Partner, Project,
   Nationality). The form reads it from `#attributeOptionComboId` or `dhis2.de.currentAttributeOptionComboId`.
5. **Value types.** The field's widget comes from the data element's value type and option set, not from
   the HTML. 5e needs a native picker (file, camera, map, org unit tree).
6. **Greyed fields.** `<input … disabled>` in the HTML: the field must never be editable.
7. **Event hooks.** `dhis2.util.on('dhis2.de.event.formReady', fn)`: code that runs when the form or a save
   finishes. 7b are the events for completion and validation, which the host has to send.
8. **Self-contained logic.** Scripts that touch only their own HTML: hide rows, check `referred <= seen`,
   compute a cell, switch tabs.
9. **Legacy metadata and helpers.** Scripts that read the old app's globals: `dhis2.de.dataElements[id].name`,
   `dhis2.de.getCurrentOrganisationUnit()`, the colour constants `dhis2.de.cst.*`, period and validation
   helpers.
10. **Legacy UI libraries.** Scripts that call `$('#tabs').tabs()`, `.select2()`, `.floatThead()`, Bootstrap
    `data-toggle`, without loading them: the old page had already loaded them.
11. **External files.** `<script src="https://cdn…">`, `<link rel="stylesheet" href="…">`, `<img src="…">`.
12. **API reads.** `$.get('../api/dataValues?…')`, `$.getJSON('../api/dataStore/…')`, `fetch('/api/me')`.
13. **API writes.** `$.ajax({ type: 'POST', url: 'api/dataValueSets' … })` for periods or data sets other
    than the one open.
14. **Overriding internals.** The form redefines the old app's functions to change how it loads or saves.
15. **Multi-org-unit.** One form for several org units: field ids `{orgUnit}-{de}-{coc}-val`.

## Open challenges

1. **The scan lets broken forms through (ANDROAPP-8007).** `LegacyApiScan` blocks only `dhis2.de.*` and
   `dhis2.util.*`. Forms with API calls (12), external files (11), legacy UI libraries (10) or
   `dhis2.period.*` / `dhis2.validation.*` (9) open and break: see `[7972] API & external` and
   `[7972] Legacy UI libs`. Either widen the scan (`CustomFormSurvey` already finds API paths and external
   scripts) or have the shim report them at runtime as `UNSUPPORTED_FEATURE`.
2. **Events nobody fires (ANDROAPP-8007).** `completed`, `uncompleted`, `validationSucces` and
   `validationError` are in `DEFAULT_SUPPORTED`, but nothing triggers them. Fire them from the data set
   screen, or drop them as the new web app does.
3. **Bundling the legacy UI libraries (ANDROAPP-8007).** jQuery UI, select2, Bootstrap, floatThead and the
   calendars: bundle them offline as the new web app does, or send those forms to the native form.
4. **Forms that read or write other periods.** SA reads previous days and writes future ones, so it stays on
   the native form, with cumulative fields typed by hand. The user docs of both items should list it as not
   supported.
5. **The survey is thin (ANDROAPP-7972).** Two real forms can't size types 5e and 10–12. It stays open until
   more come in.

## What to look for in more real forms

In order of how much each answer changes 3.6.0:

1. Forms that call the Web API, and which endpoints (row 12, 13). Today: SA only.
2. Every `dhis2.*` name used, including `dhis2.period` and `dhis2.validation` (row 9). This becomes the
   supported list.
3. Use of jQuery UI, select2, Bootstrap, floatThead or the calendars without loading them (row 10).
4. External scripts, stylesheets and images (row 11).
5. File, image, coordinate and org unit fields, and option sets, inside custom forms (row 5).
6. Completion or validation handlers (row 7b).
7. Any multi-org-unit custom form (row 15): one "none" from implementers confirms dropping it.
8. Widths and sizes, for phone UX and ANDROSDK-2431.

Where: the core extensibility team (DHIS2-19900) for the forms behind their plugin work, and HISP groups
for metadata exports of their `CUSTOM` data sets (form HTML included), recording the source of each.
