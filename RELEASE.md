
## NEW FUNCTIONALITY AND WEB PARITY

**New Capture Coordinates process:** The 3.1 version introduces a list of new features designed to enhance the capture coordinates process. These improvements aim to provide greater accuracy, flexibility, and control over location data capture.
- **Accuracy:** The capture coordinates process now includes a feature that displays the precision of the captured location. This allows users to see how accurate their location data is in real-time. This parameter can also be restricted using the Android Settings WebApp.
- **Search Functionality:** A new search functionality has been added, allowing users to look up specific locations by name or address. Users are also able to navigate through the map and perform area searches to discover other locations within a specified region.
- **Block Manual Capture:** Using the Android Settings Web App, administrators now have the option to block manual location capture. When this setting is enabled, users can only capture the current location and cannot manually select or search a different one. This ensures that location data remains consistent and accurate.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-6330) | [Card1](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-disabled-manual-capture.png) | [Card2](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-map-accuracy.png) | [Card3](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-map-search.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_map_accuracy)

**Improve transfers flow:** Significant enhancements to the transfer flow, aimed at making the process more user-friendly and transparent. The transfer button has been moved to a more accessible location within the three dot menu in the TEI Dashboard, ensuring that users can easily find and initiate transfers without unnecessary navigation. It also has introduced new dialogs throughout the transfer process. These dialogs provide clear, step-by-step guidance, ensuring that users understand each part of the process.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-6228) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_transfers)

**New relationship section:** Major updates have been made in the relationship tabs,  enhancing both functionality and user experience. Relationship cards have been updated with the new design to offer a more intuitive and visually appealing experience. The new design emphasizes clarity and usability, making it easier to view and manage relationships at a glance.

To prevent accidental deletions and enhance user control, a new confirmation dialog also has been added when deleting a relationship. This dialog will prompt users to confirm their action, ensuring that relationships are only deleted intentionally.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-6362) | [Card1](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-relationship-sections.png) | [Card2](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-new-relationship-cards.png) | [Card3](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-relationship-deletion.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_relationships)

**Sort of unique attributes in the search screen:** Aimed at aligning it with the web instance for a more consistent user experience, this version of the Android app, by default, sorts the unique attributes (QR, barcode) at the top of the list of searchable attributes. Users can quickly and easily find the attributes for a more exact search.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-6039) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_unique_qrBar_search)

**Support of biometric dialog:** An enhancement to the biometric authentication feature has been made in 3.1.0. When there is only one account configured, the user can configure biometric authentication (fingerprint or face ID).

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4676) | [Documentation](https://docs.dhis2.org/en/use/android-app/android-specific-features.html#capture_app_generic_biometrics_login)

**Line Listing improvements:** This version of the Android App introduces support for the Category Option Dimension in line listings. This enhancement enables users to apply category options directly within line listings to filter data according to precise criteria, improving data exploration and decision-making processes. This feature greatly enhances the versatility and utility of line listings, empowering users to perform more sophisticated reporting.

Additionally, it has been improved the text alignment within the Line Listing tables to support left alignment. This enhancement ensures better readability and a cleaner presentation of data, making it easier for users to review and analyze their information quickly.

[Jira1](https://dhis2.atlassian.net/browse/ANDROAPP-6353) | [Jira2](https://dhis2.atlassian.net/browse/ANDROAPP-6121) | [Documentation](https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_event_visualizations)

## USER EXPERIENCE

**Responsive Home Screen:** In this Android App version a new dynamic home screen that adapts to the number of programs available has been implemented. This update replaces the old static list that didn’t adjust to the screen, providing a more responsive and user-friendly interface.The responsive design makes better use of screen real estate, providing a more engaging and functional home screen layout.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5394) | [Card](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-responsive-home-screen.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/android-specific-features.html#capture_app_home)

**Scheduled events dialog:** As a continuation of the new schedule dialog introduced in the version 3.0, a new  intuitive and user-friendly schedule dialog has been implemented to enhance the overall user experience, making it easier to book, reschedule, or cancel events.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-6229) | [Card1](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-schedule-new.png) | [Card2](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-enter-cancel-reschedule.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_scheduling)

**Improve menus and navigation bar:** A revamped of the menus and navigation bar has been made to be more user-friendly and accessible. It includes a cleaner, more modern look that improves readability and usability. These updates are designed to provide a more efficient and enjoyable user experience.

[Jira1](https://dhis2.atlassian.net/browse/ANDROAPP-6036) | [Jira2](https://dhis2.atlassian.net/browse/ANDROAPP-6113) | [Card1](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-menu.png) | [Card2](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+3.1/release+cards/Android-3-1-navigation-bar.png) | [Documentation
](https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_menu_bars_update)
## CROSS PRODUCT

**Support for customized Tracker terminology:** Some DHIS2 terminology is not familiar for the end users. For this reason, we are gradually enabling the possibility to customize it to each particular use case. In this version, the term "event" (program label context) is customizable. The admin user will be able to configure it for each program using the Maintenance App, and the Android Capture App will display the customized term instead of the generic one.

[Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5947) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_customized_terminology)

---

##### **DETAILS**
You can find the list of all new features and all bugs fixed in 3.1.0 [here.](https://dhis2.atlassian.net/projects/ANDROAPP/versions/10851/tab/release-report-all-issues)
