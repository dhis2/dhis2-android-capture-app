# DHIS2 Plugin Host assets

The Capture App ships these files to give DHIS2 web plugins a parent page and
a post-robot counterpart when they run inside a WebView.

| File | Purpose |
| --- | --- |
| `bridge.html` | Generic, plugin-agnostic parent page. Hosts an iframe for the plugin, implements the `getPropsFromParent` / `updated` post-robot protocol, and forwards `setFieldValue` / `setContextFieldValue` calls to Kotlin via `window.Android`. |
| `post-robot.min.js` | Vendored post-robot library. **Must be supplied manually** — see below. |

## Vendoring post-robot

- Library: [post-robot](https://github.com/krakenjs/post-robot)
- Version pinned: **10.0.46**
- Why this exact version: `@dhis2/app-runtime@3.x` bundles post-robot 10.0.46
  inside every built DHIS2 plugin. Confirmed by inspecting a built plugin
  bundle (the `post_robot_10_0_46__` namespace string). Matching the exact
  version avoids envelope-format drift between parent and plugin.

### How to install

```bash
curl -fsSL -o form/src/main/assets/dhis2-plugin-host/post-robot.min.js \
  https://unpkg.com/post-robot@10.0.46/dist/post-robot.min.js
```

Or copy from a local node_modules that already has `@dhis2/app-runtime`
installed.

### When to update

Whenever `@dhis2/app-runtime` bumps its post-robot pin. Check a freshly built
plugin bundle for the `post_robot_<major>_<minor>_<patch>__` namespace string
to confirm the new version before changing the pin here.
