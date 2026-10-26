---
id: ios-project-setup
title: iOS Project Setup
sidebar_position: 7
---

The `composeApp` Kotlin Multiplatform module is consumed by the Xcode project in `iosApp/` as a
native Apple framework. The integration is direct: no CocoaPods or Swift Package Manager is
involved. Xcode calls Gradle on every build, so any change in `commonMain` or `iosMain` is picked up
automatically.

## How it works

1. **Kotlin/Native builds a framework.** The `iosArm64` and `iosSimulatorArm64` targets
   declared in `composeApp/build.gradle.kts` compile `commonMain` + `iosMain` into a static
   framework named `ComposeApp`:

    ```kotlin
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    ```

2. **Xcode invokes Gradle.** A Run Script build phase runs
   `:composeApp:embedAndSignAppleFrameworkForXcode`. The task reads the environment variables Xcode
   sets (`CONFIGURATION`, `SDK_NAME`, `ARCHS`) to select the matching target, for example
   `linkDebugFrameworkIosSimulatorArm64` for a Debug build on an Apple Silicon simulator, and writes
   the framework to `composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`.

3. **Xcode links the framework** using the search path and linker flags described below.

4. **Swift hosts the Compose UI.** `iosApp/iosApp/ContentView.swift` imports the framework and
   embeds the view controller exposed from `composeApp/src/iosMain`:

    ```kotlin
    // composeApp/src/iosMain/kotlin/org/dhis2/mobile/app/MainViewController.kt
    fun MainViewController() = ComposeUIViewController { App() }
    ```

    ```swift
    import ComposeApp

    struct ComposeView: UIViewControllerRepresentable {
        func makeUIViewController(context: Context) -> UIViewController {
            MainViewControllerKt.MainViewController()
        }
        func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
    }
    ```

## Xcode configuration

All settings below apply to the `iosApp` target, in both the Debug and Release configurations.

### Step 1: Add the Run Script build phase

1. **Open `iosApp/iosApp.xcodeproj` in Xcode.**
2. **Select the `iosApp` target and go to `Build Phases`.**
3. **Click `+` -> `New Run Script Phase` and add the script:**

    ```sh
    cd "$SRCROOT/.."
    export JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || echo "$JAVA_HOME")"
    ./gradlew :composeApp:embedAndSignAppleFrameworkForXcode
    ```

4. **Drag the phase to the top, above `Compile Sources`**, so the framework exists before Swift is
   compiled and linked.

The `JAVA_HOME` export is required because Xcode does not inherit your shell environment, so
Gradle would not find JDK 17 otherwise.

### Step 2: Set the Framework Search Paths

In `Build Settings`, set `Framework Search Paths` to:

```
$(SRCROOT)/../composeApp/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)
```

### Step 3: Set the Other Linker Flags

In `Build Settings`, set `Other Linker Flags` to:

```
$(inherited) -framework ComposeApp
```

The framework name must match `baseName` in `composeApp/build.gradle.kts`.

### Step 4: Disable User Script Sandboxing

In `Build Settings`, set `User Script Sandboxing` (`ENABLE_USER_SCRIPT_SANDBOXING`) to `NO` at
target level. With sandboxing enabled, the script cannot read or write outside Xcode's build
directory and the Gradle build fails.

## Running the app

- **Xcode:** select a simulator and press `⌘R`.
- **Android Studio:** with the Kotlin Multiplatform plugin installed, create an iOS run
  configuration pointing at the `iosApp` project.

There is no hot reload on iOS: every Kotlin change is recompiled into the framework as part of the
Xcode build. The first Kotlin/Native build takes several minutes; later builds are incremental.

## Troubleshooting

If a Kotlin change does not show up in the simulator:

1. **Check the Run Script step in the Xcode build log.** It should show
   `compileKotlinIosSimulatorArm64` and `linkDebugFramework…` running.
2. **Clean the build folder** with `Product` -> `Clean Build Folder` (`⇧⌘K`) and run again.
3. **Delete `composeApp/build/xcode-frameworks`** as a last resort to force a full framework
   rebuild.
