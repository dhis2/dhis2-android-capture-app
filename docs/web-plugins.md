# Web plugins (PoC)

> Status: proof of concept on `poc/web-plugins` (ANDROAPP-7835). It replaces the dex-based
> approach, which Google Play policy does not allow (ANDROAPP-7830).

The app runs **unmodified DHIS2 Capture form field plugins**: the same App Hub apps, placed with the
same configuration the web Capture app uses. They keep working **offline**.

## Configuration: the web Capture app's own

There is no Android-specific plugin configuration. Placement is read from the `capture` dataStore
namespace, which the **Tracker Plugin Configurator** writes:

- `capture/dataEntryForms` is keyed by form: a program stage for event forms, a program for
  registration forms.
- A `plugin` element gives:
  - `pluginSource`: `{server}/api/apps/{key}/plugin.html`, an app installed on the server.
  - `fieldMap`: the fields the plugin may use. `IdFromApp` is the field uid; `IdFromPlugin` is the
    alias the plugin knows it by.

Writing the `capture` namespace needs the `F_CAPTURE_DATASTORE_UPDATE` authority.

## How a plugin gets onto the device (`LoadPlugins`, on opening Home)

```
capture/dataEntryForms ─► placements ─► app keys from pluginSource
for each key:
  server /api/apps?key=…            installed name + version (+ app_hub_id when the author set one)
  already installed that version?   ─► use it, no network
  App Hub /api/v2/apps…             by app_hub_id, else by exact name; pick the installed version
  download the zip                  SDK client, external request, no DHIS2 credentials sent
  verify                            entry module script (content-hashed) == the one the server serves
  extract                           filesDir/web-plugins/{key}-{version}/
offline: cached config + last installed bundle
```

`plugin.html` cannot be compared, because the server rewrites it to inject its base URL. The script
it loads is served byte-identical.

## How it renders (`FormFieldPluginSlot` in `Form`)

- A form is resolved to its `dataEntryForms` key: an event to its program stage, an enrollment to its
  program.
- The plugins configured there render **after the form's sections**. The configured layout (section
  order, fields the plugin replaces) is **not applied yet**, so mapped fields also stay as native
  fields.
- Props match the web Capture app. `values`, `errors`, `warnings` and `fieldsMetadata` are keyed by
  alias; option sets carry `options[{id, code, value, text, name}]`.
- `setFieldValue({fieldId: alias})` maps back to the uid and saves through `FormIntent.OnSave`, so the
  form validates it and program rules run. Writes to fields outside the field map are refused.
- Transport: the WebView loads only local content (`WebViewAssetLoader`). Every `/api/` call is
  answered by `PluginApiRouter` from the SDK, over the `dhis2Native` message bridge, through
  `api-shim.js`. See below.

## Verified on a device (DHIS2 2.43, Sierra Leone demo, airplane mode)

**Matrix Select 1.0.2** from the App Hub, on "First antenatal care visit" (4 allergy fields sharing
the "Yes NoneKnown" option set):
1. The plugin is resolved by name on the App Hub (no `app_hub_id`), verified against the server, and
   installed.
2. Offline, it renders the matrix from SDK metadata.
3. Selections are saved as `TO_POST`, and the native fields show them.
4. After sync, the server has the values.

## Web API available offline (for plugins that fetch their own data)

| Endpoint | Methods |
|---|---|
| `me`, `system/info`, `userSettings`, `systemSettings` | GET |
| `dataStore/{namespace}` | GET (keys) |
| `dataStore/{namespace}/{key}` | GET, PUT, POST, DELETE |
| `dataValueSets` | POST (`dataSet` required; option combos only defaulted from default category combos) |

Anything else answers `501` and is logged. Enrollment plugins such as the WHO Growth Chart need
tracker and metadata endpoints added here.

## Known limits

- The `dataEntryForms` layout is not respected yet: plugins render after the native form.
- Only form field plugins; enrollment plugins (`enrollment*Layout` keys) are not read yet.
- Plugins uploaded manually, without an App Hub release, cannot be fetched: the server has no
  endpoint that serves an installed app's bundle.
- **SDK:** DHIS2 2.43 returns import conflicts with `objects`, which SDK `1.16.0-20261002` cannot
  parse, so one rejected data value aborts the whole data sync.
- A swipe that starts on a plugin does not scroll the screen around it.

## Code

- `plugin/`
  - `data/`: capture config, server apps, App Hub, bundle verifier and installer, field metadata,
    form context
  - `domain/LoadPlugins`
  - `registry/`
  - `web/`: `FormFieldPluginSlot`, `WebPlugin`, `PluginBridgeSession`, props
  - `api/PluginApiRouter`
  - `assets/dhis2-plugin-host/`: `bridge.html`, `api-shim.js`, post-robot 10.0.46
- `form/.../ui/Form.kt`: the slot, and field states and writes
- `sync/`: data sync uploads the server dataStore
