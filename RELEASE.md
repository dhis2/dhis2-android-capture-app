# Release notes - Android App for DHIS2 - 3.3.0

## Highlights of this release

- Redesigned login experience with improved usability and future support for two-factor authentication.
- Introduced configurable custom intents for seamless integration with external Android applications.
- Enhanced synchronization with support for syncing data based on selected working lists.

**Deprecation notice:** Support for Android 5.0 (API 21) and 5.1 (API 22) will end starting with version 3.4.0, while 3.3.x patch releases will continue to support these versions and receive critical security fixes if needed.

## Release Notes

### NEW FUNCTIONALITY AND WEB PARITY

#### Custom intents:
3.3.0 introduces custom intents, a new feature that enables integration between the DHIS2 Android Capture App and third-party Android applications. Through configuration in the Android Settings Web App, administrators can define how the Capture App launches external apps, sends data to them, and receives results automatically. It can be configured per tracked entity attribute or data element and support both data entry (tracker and event programs) and search workflows.

#### Sync by working list:
Synchronization can now be configured based on working lists defined in the Capture web app and managed through the Android Settings Web App (ASWA). Based on user sharing settings, the app will download data from the assigned working list or merge multiple lists when more than one is available.

### USER EXPERIENCE

#### Login redesign:
The login experience has been redesigned with a modern interface and improved usability. Beyond visual updates, this redesign refactors the authentication architecture to support OAuth-based two-factor authentication (2FA), ensuring the app is ready for upcoming security improvements in future releases.
Users will notice a cleaner layout  and faster access to frequently used servers.

As part of this redesign, the PIN verification screen (shown when reopening the app) has also been updated with the new visual style, ensuring a consistent and modern experience across the entire authentication flow.

#### Settings menu redesign:
The settings menu has been redesigned to provide a cleaner structure and improved navigation experience. This update aligns the screen with the overall app design updates introduced in previous versions.

#### Edge-to-edge review:
The update refines margins, padding, and component layouts to make full use of available screen space, especially on newer Android devices. Improvements are particularly noticeable in the TEI list, event list, and data-entry forms.

This review also enhances the experience on tablets and in landscape orientation, ensuring responsive layouts and alignment across all components.

### PERFORMANCE & MAINTENANCE

#### Migrate database to KMM:
While invisible to end users, this migration significantly improves maintainability, consistency, and performance of the app’s data layer. It also paves the way for potential future multi-platform clients that can share core logic with Android.

#### Reduce APK size with R8:
Through advanced code shrinking and resource optimization, the app’s footprint has been reduced without compromising features or stability. This change is particularly beneficial for deployments in low-bandwidth environments or with limited device storage.
