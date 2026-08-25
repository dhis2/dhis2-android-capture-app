# Instrumented (device) tests

Compose Testing + Espresso, Robot pattern, run on the BrowserStack device matrix.

**Where they live:** `src/androidTest/java` — in `app`, `commons`, `compose-table` and
`form`. Those four are the only modules with instrumented tests today.

`commonskmm`, `login` and `sync` declare an `androidDeviceTest` source set in their
`build.gradle.kts`, but **no test has been written in one yet**. If you are adding the
first device test to a KMP module, that is the source set to use — expect to wire up
the harness (`BaseTest`, MockWebServer, fixtures) from scratch, none of it is there.

For host (JVM) unit tests, see [unit-testing.md](unit-testing.md).

---

## Critical Rule: No Hard-Coded Delays

ViewModels use `launchUseCase { }` which wraps `CoroutineTracker`. Espresso's
`IdlingResource` automatically waits for tracked coroutines. `Thread.sleep()` and
hard-coded timeouts are **forbidden**.

```kotlin
// ✅ CORRECT — IdlingResource waits automatically
@Test
fun shouldLoadData() {
    exampleRobot(composeTestRule) {
        clickLoadButton()
        verifyDataDisplayed()  // no delay needed
    }
}

// ❌ WRONG
@Test
fun shouldLoadData() {
    clickLoadButton()
    Thread.sleep(2000)  // FORBIDDEN
    verifyDataDisplayed()
}
```

## UI Tests: Robot Pattern

All UI tests go in `src/androidTest/`. Always use the Robot pattern. Tests extend
`BaseTest`, which provides `mockWebServerRobot` — a helper that stubs HTTP responses from the
DHIS2 server so tests run fully offline against a local `MockWebServer`. Register stubs
**before** launching the robot body.

```kotlin
fun exampleRobot(rule: ComposeTestRule, body: ExampleRobot.() -> Unit) =
    ExampleRobot(rule).apply { body() }

class ExampleRobot(val rule: ComposeTestRule) : BaseRobot() {
    fun typeUsername(username: String) {
        rule.waitUntilExactlyOneExists(hasTestTag(USERNAME_TAG), TIMEOUT)
        rule.onNodeWithTag(USERNAME_TAG).performClick()
        rule.onAllNodesWithTag("INPUT_TEXT_FIELD")[0].performTextInput(username)
    }

    fun clickSubmitButton() {
        rule.waitUntilExactlyOneExists(hasTestTag(SUBMIT_TAG), TIMEOUT)
        rule.onNodeWithTag(SUBMIT_TAG).performClick()
    }
}

class ExampleTest : BaseTest() {
    @get:Rule val rule = createComposeRule()

    @Test
    fun shouldPerformSuccessfulAction() {
        // Stub the network response before any UI interaction
        mockWebServerRobot.addResponse(GET, "/api/endpoint", MOCK_RESPONSE, 200)
        exampleRobot(rule) {
            typeUsername("user")
            clickSubmitButton()
            verifySuccessMessageDisplayed()
        }
        // Call cleanDatabase() after any test that writes to the local DB —
        // it clears all DHIS2 SDK tables so state doesn't leak into the next test.
        cleanDatabase()
    }
}
```

### What belongs in a robot, and which robot

- **One robot per screen or dialog.** A dialog reached from several screens gets its
  own robot rather than duplicated methods in each caller's robot —
  `OrgUnitSelectorRobot` is the existing example.
- **Waiting for a screen belongs to that screen's robot**, not the test. If a step
  navigates to a new Activity, expose e.g. `waitForFormToOpen()` on the destination's
  robot; the test then reads as intent, and the reason for the wait is documented once
  instead of repeated at every call site.
- **Keep framework plumbing out of the test class.** Reaching into
  `supportFragmentManager` / `ActivityLifecycleMonitorRegistry` from a `@Test` bypasses
  the pattern; assert on what the dialog renders through its robot instead.

## Test Tags

Export constants from the screen file. Format: `{SCREEN}_{COMPONENT}_TAG`.

```kotlin
const val LOGIN_BUTTON_TAG = "LOGIN_BUTTON_TAG"
const val USERNAME_INPUT_TAG = "USERNAME_INPUT_TAG"

@Composable
fun LoginScreen() {
    InputField(modifier = Modifier.testTag(USERNAME_INPUT_TAG))
    Button(
        onClick = { /* submit */ },
        modifier = Modifier.testTag(LOGIN_BUTTON_TAG),
    ) {
        Text("Log in")
    }
}
```

### Never assume a test tag exists — verify it is emitted first

A matcher built on a tag that the UI never renders fails silently: it just
times out with no hint that the tag was the problem. Before you write
`hasTestTag("FOO")`, confirm `FOO` is actually set on a node — grep the screen
(and the design-system component source) for `testTag("FOO")`, or dump the tree
with `composeTestRule.onRoot().printToLog("TREE")` and read what's really there.

This bites hardest with tags that come from the design-system library rather
than app code (e.g. a list-card item tag). If you can't confirm a tag is
emitted, match on **confirmable text or semantics** instead — text you can see
on screen is always safer than a tag you're guessing at.

### Prefix matchers: check no longer tag shares the prefix

When matching on a tag *prefix* rather than an exact tag, verify that no longer tag
starts with the same string. `OrgBottomSheet` declares both `ORG_TREE_ITEM_` and
`ORG_TREE_ITEM_CHECKBOX_`, so a `startsWith("ORG_TREE_ITEM_")` matcher also selects
every checkbox. Harmless for an existence check, wrong if you meant "a tree row".

### Merged vs unmerged semantics tree

Any node with `mergeDescendants = true` collapses its whole subtree in the **merged**
tree — the default for every query. `Modifier.clickable` sets it, so a clickable
`LazyColumn` or a design-system list card merges everything inside it.

| Symptom | Cause | Fix |
| --- | --- | --- |
| `onAllNodesWithTag(X)` → 0 nodes, but X is on screen | tag absorbed by a merging ancestor | add `useUnmergedTree = true` |
| `performScrollTo()` → "no parent layout with a Scroll SemanticsAction" | matched the scroller, not the child | query unmerged, or scroll the container with `performScrollToNode` |
| a child-counting assertion passes suspiciously | nodes inside a merged subtree report `children == []` — a vacuous pass | read the merged node's aggregated text instead |

**What makes this look self-contradictory:** *clickable* descendants (radio buttons,
icon buttons) set their own `mergeDescendants` and survive merging, so some merged
lookups work on the same screen where others return nothing.

**`waitUntilAtLeastOneExists` always searches the merged tree** — it has no unmerged
option. For a tag that exists only unmerged, spell the wait out:

```kotlin
composeTestRule.waitUntil(TIMEOUT) {
    composeTestRule.onAllNodesWithTag(TAG, useUnmergedTree = true)
        .fetchSemanticsNodes().isNotEmpty()
}
```

An unmerged node carries only its **own** text, so to find a field by its label you
must recurse the subtree — the label often sits several levels below the tagged node.
`BaseRobot` provides `texts()` (own text, merged nodes) and `subtreeTexts()`
(recursive, unmerged) for exactly this split.

## DHIS2 Design System Inputs

DHIS2 input components are composite. Click the wrapper to focus, then target the
inner field. Use `performTextInput()`, never `performTextReplacement()`.

The inner-field tag depends on the component. Most text-style inputs follow the
pattern `INPUT_<COMPONENT_NAME>_FIELD` — e.g. `InputText` uses `INPUT_TEXT_FIELD`,
`InputEmail` uses `INPUT_EMAIL_FIELD`, `InputNumber` uses `INPUT_NUMBER_FIELD`,
`InputPhoneNumber` uses `INPUT_PHONE_NUMBER_FIELD`, and so on. Non-text inputs
(checkboxes, dropdowns, dialogs, pickers, org-unit, coordinate, etc.) use their
own tag schemes.

To find the exact testTag for any design-system component, check the API docs:
<https://dhis2.github.io/dhis2-mobile-ui/api/-mobile%20-u-i/org.hisp.dhis.mobile.ui.designsystem.component/index.html>
— or open the component source in
`../dhis2-mobile-ui/designsystem/src/commonMain/kotlin/org/hisp/dhis/mobile/ui/designsystem/component/<Component>.kt`
and grep for `testTag(`.

```kotlin
// ✅ CORRECT — InputText example
rule.onNodeWithTag(USERNAME_TAG).performClick()
rule.onAllNodesWithTag("INPUT_TEXT_FIELD")[0].performTextInput(username)

// ❌ WRONG
rule.onNodeWithTag(USERNAME_TAG).performTextReplacement(username)
```

## Instrumented Test State: Fixtures & Assertions

### Assert through the UI, not the SDK

In an instrumented test, verify what the **user sees** — visible text, tags,
semantics — not the SDK's internal state. Reaching into
`D2Manager.getD2()…blockingGet()` to check a status couples the test to the
database layer instead of the screen, and no other test in this codebase does
it. If the UI shows "Event completed", assert on that; don't probe
`event.status()`.

```kotlin
// ✅ CORRECT — assert what's on screen
programEventsRobot(composeTestRule) {
    checkEventIsComplete(eventDate)
}

// ❌ WRONG — probing SDK state from an instrumented test
val status = D2Manager.getD2().eventModule().events().uid(uid).blockingGet()?.status()
assertTrue(status == EventStatus.COMPLETED)
```

The SDK is still fine for **seeding** a fixture (see below) — the rule is about
*assertions*: check the UI, not the database.

### Seed fixtures at runtime — don't hardcode demo UIDs

The test DB is a snapshot; a specific demo event UID like `"ohAH6BXIMad"` can
disappear or change the moment the snapshot is regenerated, breaking the test
for reasons unrelated to the app. Instead, **create the fixture you need at the
start of the test** via the SDK, in an intent helper, and use the UID it
returns:

```kotlin
// In EventIntents.kt — create a fresh event, return its UID + display date
fun createFreshFlowAEvent(): FreshFlowAEvent {
    val uid = d2.eventModule().events().blockingAdd(
        EventCreateProjection.builder()
            .program(FLOW_A_PROGRAM_UID)      // anchor to the stable program…
            .programStage(FLOW_A_STAGE_UID)
            .organisationUnit(FLOW_A_ORG_UNIT_UID)
            .build(),
    )
    d2.eventModule().events().uid(uid).setEventDate(now)
    return FreshFlowAEvent(uid, displayDate)  // …generate the fragile event yourself
}
```

You still depend on the **program** existing (a big, stable structural object),
but you generate the **event** (the fragile row) yourself — so a DB refresh
can't pull the rug out. Prefer this over hardcoded demo UIDs for any test that
needs a specific event/enrollment to act on.

### Tests run isolated per class — but state leaks within one run

CI runs each test class in its own instrumentation process, so each class
starts from the fresh DB snapshot. But within a **single** `connectedAndroidTest`
invocation that spans multiple classes, SDK writes persist across tests — a
fixture one test seeds (or a status it changes) is still there for the next
test. Two consequences:

- A multi-class local run can fail a later test that a single-class run passes
  (stale state, not a real bug). Reproduce CI by running one class at a time.
- When a seeded fixture coexists with demo data, clean up with
  `cleanDatabase()` where the next test needs a pristine list.

## Write for the CI device matrix — including landscape

Tests run on the BrowserStack device matrix (multiple devices **and
orientations**), not just your local emulator. A test that passes locally in
portrait can fail on CI in landscape — almost always because landscape has far
less vertical height, so a node that was on-screen in portrait is now scrolled
out of the viewport. Compose reports such a node as **present but not
displayed**, so `assertIsDisplayed()` fails (and `performClick()` may miss)
even though the element exists and the app is fine.

Make assertions orientation-independent:

- **Scroll the target into view before asserting or clicking.** Call
  `performScrollTo()` (Compose) / `scrollTo()` (Espresso) on the node first.

  ```kotlin
  // ✅ robust in any orientation — bring it on-screen, then assert
  composeTestRule.onNodeWithText(orgUnit).performScrollTo().assertIsDisplayed()

  // ❌ portrait-only — fails in landscape when the node is below the fold
  composeTestRule.onNodeWithText(orgUnit).assertIsDisplayed()
  ```

- **`performScrollTo()` only works if the node has a Compose scroll ancestor**
  (`verticalScroll`, `LazyColumn`, …). It is not a free safety net: with no such
  ancestor it throws *"no parent layout with a Scroll SemanticsAction"* on **every**
  device, portrait included. `BottomSheetDialogContent` has no scroll container — the
  sheet's drag is View-level `BottomSheetBehavior`, invisible to Compose semantics — so
  nothing inside a bottom sheet can be scrolled to. Check the component before
  applying the rule above; where there is no scroller, use `assertExists()`.
- **Match the assertion to the claim.** "Did this screen/dialog open" is an
  *existence* claim → `assertExists()`. "Can the user see or act on this" is a
  *visibility* claim → `assertIsDisplayed()`, scrolled into view first.
- **When you only need to prove a node is in the tree** (not that it's
  visible right now), use `assertExists()` instead of `assertIsDisplayed()`.
- **Don't assume layout positions.** Toolbars, FABs, and bottom sheets reflow
  in landscape; the soft keyboard can also go fullscreen (extract mode) and
  cover the form. Target nodes by tag/text and scroll to them rather than
  relying on where they sit in portrait.
- **"Green locally" ≠ "green on CI".** Don't declare a flow done on a local
  portrait run alone — the matrix exercises orientations your emulator didn't.


## Common mistakes — instrumented

- Using `Thread.sleep()` or any hard-coded delays
- Using `performTextReplacement()` on DHIS2 design system components
- Not exporting test tag constants from screen files
- Not extending `BaseRobot` for robot classes
- Not cleaning up after tests (`cleanDatabase()`, clear preferences)
- Testing implementation details instead of user flows
- Probing SDK state (`D2Manager…blockingGet()`) to assert in an instrumented
  test instead of checking what's on screen
- Building a matcher on a test tag you haven't confirmed is emitted (especially
  design-system tags) — verify or match on text instead
- Matching a tag *prefix* without checking whether a longer tag shares it
- Querying the merged tree for a tag inside a merging container (returns 0 nodes),
  or using `waitUntilAtLeastOneExists` for an unmerged-only tag
- Adding `performScrollTo()` to a node with no Compose scroll ancestor — it throws
  rather than hardening the assertion
- Hardcoding demo fixture UIDs instead of seeding the fixture at runtime via
  the SDK
- Asserting `assertIsDisplayed()` / clicking without `performScrollTo()` first —
  fails in landscape on the CI matrix when the node is below the fold
