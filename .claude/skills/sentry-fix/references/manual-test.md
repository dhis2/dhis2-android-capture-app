# Manual test section for Sentry-fix Jira tickets

The `## How to test manually` section is for a field tester holding a **release
APK**: no adb, no debug build, no logcat, no developer options. Every step is UI
navigation using the labels the tester actually sees on screen.

## Shape

1. **Preconditions** — the user and metadata needed (e.g. "a user with access
   to at least one tracker program"). Never a hardcoded demo UID.
2. **Numbered steps** — the shortest path to the crash site, naming visible
   labels ("tap the program name in the toolbar and choose *All Persons*").
   Include any state the crash needs (search form closed, offline, rotated…).
3. **Expected** — one observable outcome on the fixed build (an icon is
   absent, a dialog opens, a message shows), followed by what the shipped build
   did instead ("before the fix, tapping it closes the app").
4. **Variants** — repeat the check where the code path differs: landscape when
   layout or configurator code branches on orientation, offline when the code
   branches on connectivity.
5. **Regression step** — the normal path still works (e.g. with a program
   selected the sync icon shows and opens the sync dialog).

## Self-check (do this before writing the section)

Walk the steps twice against the code read in Steps 2–6:

- **Against the fixed code**: every step is reachable and the Expected outcome
  holds.
- **Against the shipped code** (the release tag from Step 1): the same steps
  reach the crash or the wrong behavior.

If the fixed build would fail the test, or the shipped build would pass it,
rewrite the steps. Don't promise what the steps can't show — "the app does not
crash" is only meaningful if a step would have crashed before.
