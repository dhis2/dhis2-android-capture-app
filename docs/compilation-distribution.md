---
id: compilation-distribution
title: Compiling and distributing
sidebar_position: 4
---

## Compiling APKs

Run the following gradle commands to build and generate the available APK types:

- Release apk (both with and without play services)
  ```bash
  gradlew assembleRelease
  ```
- Training apk
    ```bash
    gradlew assembleDhis2Training
  ```
- Debug apk
  ```bash
  gradlew assembleDebug
  ```

You can also go to the right side of the screen and click on the “gradle” icon, then click on
“dhis2-capture-app” > “app” > “build” > “assembleDebug” (or any other).
After building the APK will be generated in the path project/app/build/outputs/apk

When it comes to building release APK, “assembleRelease” you will generate a release
APK but it will not be signed, so in this case we recommend you go to “Build” that can easily be
found on top of the screen, then “Generate signed/bundle APK” and you will be asked to load a
keystore and type your password. Finally, after filling the passwords and clicking next the APK will
be generated in the same path mentioned previously project/app/build/outputs/apk.

## Distribution via MDM

At this moment you should have the DHIS2 Mobile App compiled with a different applicationId and
signed with your certificates. So it can be distributed via your Mobile Device Management under the
Enterprise Application (or similar) section.
