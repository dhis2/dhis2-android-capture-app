---
id: creating-custom-usecases
title: Creating Custom Use Cases
sidebar_position: 6
---

To create custom use cases in our DHIS2 Android Capture App, follow the steps below:

## Step 1: Create a New Module

The best way to implement a new use case is by creating a new Multiplatform library module. This
module will encapsulate the functionality and can be easily maintained and tested.

1. **Open your project in Android Studio.**
2. **Go to `File` -> `New` -> `New Module`.**
3. **Select `Kotlin Multiplatform shared module` and click `Next`.**
4. **Name your module according to the pattern `customcase-usecase` (e.g., `stock-usecase`).**
5. **Click `Finish`.**

## Step 2: Add Dependencies

In your newly created module, add the `commonskmm` module as a dependency to access the **design
system**, and `commons` for the **DHIS2 SDK**, and other common tools.

1. **Open `build.gradle.kts` file of your new module.**
2. **Add the following line to the dependencies sections:**

    ```kotlin
    commonMain { 
        dependencies {
            implementation(project(":commonskmm"))
        }
   }
   androidMain.dependencies {
        implementation(project(":commons"))
   }
    ```

### Configuration Adjustments

Depending on your Gradle version, you might need to make some configuration adjustments in the
`build.gradle` file. Additionally, if you are using the version catalog, you will need to adjust the
configuration accordingly.

This allows you to use resources and tools provided in the `commons` modules.

## Step 3: Implement the Use Case

Now, you can start implementing your use case within the new module. Structure your code following
the best practices and utilize the resources from the `commons` module.

### Recommended Architecture

We recommend using the MVVM (Model-View-ViewModel) architecture with Kotlin language and Jetpack
Compose. This approach ensures a clean separation of concerns and enhances code maintainability. For
more details, refer to the
official [Android Architecture Guidelines](https://developer.android.com/topic/architecture).

### Example Module Structure

Here is an example of the module structure:

- `src/androidMain`: Contains Java/Kotlin source files that needs the android framework as well as
  the AndroidManifest and android resources
- `src/commonMain`: Contains Kotlin source files that can be shared between platforms.
- `src/desktopMain`: Contains source files that needs the desktop framework

It is recommended to include the kotlin multiplatform resource library to share resources.

## Step 4: Using the Custom Use Case

To use your custom use case, add your new module as a dependency in the `app` module and call the
entry point (which can be an Activity or a Composable) from where you would like to start using it.

1. **Open `build.gradle.kts` file of the `app` module.**
2. **Add the following line to the dependencies section:**

    ```kotlin
    dependencies {
        implementation(project(":usecase-module"))
    }
    ```

3. **Call the entry point in your code:**

    ```kotlin
    // Example: Calling an Activity
    startActivity(Intent(this, YourUseCaseActivity::class.java))

    // Example: Calling a Composable
    setContent {
        YourUseCaseComposable()
    }
    ```

## Benefits

This modular approach helps to avoid conflicts when updating your project with our new developments.
By isolating custom use cases in separate modules, you can independently maintain and update your
custom functionalities without interfering with the main project components.