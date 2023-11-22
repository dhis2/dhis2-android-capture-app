# DHIS2 Android App version 2.9 Release Notes
<table>
<tr> 
<td> 
<img src="https://s3-eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/android-chrome-384x384.png" width="800"> 
</td> 
<td>
The new <strong>DHIS2 Android App</strong> allows offline data capture across all DHIS2 data models. Data and metadata are automatically synchronized whenever there is internet access, always keeping the most relevant data for the logged user in the device.
The app is compatible and we support <strong>2.38</strong>, <strong>2.39</strong>, <strong>40</strong>.  And has no breaking changes with <strong>2.37</strong>, <strong>2.36</strong>, <strong>2.35</strong> and <strong>2.34</strong>. 
</td>
</tr> 
<tr> 
<td colspan="2" bgcolor="white">
## USER EXPERIENCE

**Disable referral in tracker programs:** When users add events in a tracker program, the DHIS2 Android Capture app offers three options: Add (for new events), Schedule (for planning future evetns) and Refer (for referrals or transfers). As this third option is not used in many implementations, this new feature enables the admin user to remove that option from the menu to simplify the user experience. The referral option can be hidden using the Android Settings Web App for all programs or for each specific program.   [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4445) | [Documentation App](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_referrals) | [Documentation Webapp](https://docs.dhis2.org/en/use/android-app/settings-configuration.html#capture_app_android_settings_webapp_appearance_program) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-Disable-referrals.png)

**Skip home screen if users only have access to one program:** The home screen of the DHIS2 Android App shows the list of programs and datasets available for the user. The first thing a user must do when using the app is to select the program or dataset to work with. In some implementations, users have access to only one program or dataset. To reduce the number of clicks and streamline the process of data entry, the App will now skip the home screen in the cases where the user has access to only one program or dataset, and will instead open directly to the program or dataset screen with the event, TEI or dataset list. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5148) | [Documentation](https://docs.dhis2.org/en/use/android-app/android-specific-features.html#capture_app_home) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-Skip-home-screen.png)

**Display program stage description:** The description for program stage sections was not available to the end user in previous versions of the App. To provide more context and information at the moment of data collection, the description has now been brought to the user interface and will be displayed below the section name. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5151) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_data_entry_form_program_stage_description) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-Program-stage-description.png)

**Disable collapsible sections in forms:** Stage sections in the Android App are displayed with collapsible menus that enable the user to open one section at a time. The purpose of this accordion-like implementation is to help the user navigate very long forms. However, some implementations would prefer to list the sections one after the other. This new version of the application enables the admin user to decide if the sections should appear in extended mode. This configuration is made through the Android Settings Web App and will display the sections one after the other with the section name acting as a separator. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5393) | [Documentation App](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_data_entry_form_collapsible_sections) | [Documentation Webapp](https://docs.dhis2.org/en/use/android-app/settings-configuration.html#capture_app_android_settings_webapp_appearance_program) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-Not-collapse-sections.png)

**Move working lists under the search bar:** The working lists have been moved from the filters section to the main program screen. In earlier versions, the user had to open the filters to be able to see and select a working list. From this version, the working lists are always visible under the search bar, facilitating their use for filtering out Tracked Entity Instances. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5453) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_working_lists) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-Working-list-under-search-bar.png)

**New design for Dataset, Event and TEI cards:** Cards are used for listing datasets, events and TEIs. The new design offers a cleaner and more intuitive layout, replacing the use of colored icons by descriptive text when relevant. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5485) | [Documentation datasets](https://docs.dhis2.org/en/use/android-app/datasets-features.html#capture_app_datsets_cards_design) | [Documentation events](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_cards_design) | [Documentation TEI](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_tei_design) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-New-cards-design.png)

**Implement changes in TEI Dashboard details:** The TEI Dashboard has been redesigned for both portrait and landscape view. The new design offers a cleaner and more intuitive layout, replacing the use of colored icons by text when relevant and moving some secondary actions to the hidden menus. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4019) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-TEI-dashboard.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_tei_design)

**Data entry forms - New inputs per value type:** The inputs for all value types have been redesigned. Tappable areas and texts have been increased and selection modes are improved to offer a cleaner and more intuitive user experience. By default, the Android App will display the previous forms. Admin users are able to opt-in to use the new forms through the Android Settings Web App. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5408) | [Documentation App](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_data_entry_form_new_inputs) | [Documentation Webapp](https://docs.dhis2.org/en/use/android-app/settings-configuration.html#capture_app_android_settings_webapp_appearance_program) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-New-inputs.png)

**[EXPERIMENTAL] TEI Header:** The TEI Header is a title that can be added to the TEI cards and dashboards in the app. The title helps identify a TEI by displaying a summary of key information. It is formed by a concatenation of Tracked Entity Attributes and fixed text. The title is configured through a Program Indicator in the Maintenance app and is assigned to the tracker program in the Android Settings web app. This feature is experimental, and depending on feedback and adoption it will be refined and incorporated in the web Capture app. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5402) | [Documentation App](https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_tei_header) | [Documentation Webapp](https://docs.dhis2.org/en/use/android-app/settings-configuration.html#capture_app_android_settings_webapp_appearance_program_specific) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-TEI-Header.png)

**Other improvements for User Experience**
- Smaller improvements focussing on user experience like a new org unit selector [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4566) | | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-Org-unit-selector.png), or adding a loading spinner during the deletion of big databases. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4768) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.9/release+feature+cards/Android-2-9-loading-when-deleting-data.png)

## MAINTENANCE

**Bug fixing:** You can find the list of bugs fixed [here](https://dhis2.atlassian.net/issues/?filter=10510).

You can find in Jira details on the [new features](https://dhis2.atlassian.net/issues/?filter=10513) in this version.

Remember to check the [documentation](https://www.dhis2.org/android-documentation) for detailed 
information of the features included in the App and how to configure DHIS2 to use it. 

Please create a [Jira](https://dhis2.atlassian.net) Issue if you find a bug or
you want to propose a new functionality. [Project: Android App for DHIS2 | Component: 
AndroidApp].
</td>
</tr>
</table>