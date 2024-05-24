<table>
<tr>
<td>
The new <strong>DHIS2 Android App</strong> allows offline data capture across all DHIS2 data models. Data and metadata are automatically synchronized whenever there is internet access, always keeping the most relevant data for the logged user in the device.
The app is compatible and we support <strong>41</strong>, <strong>40</strong>, <strong>2.39</strong>.
</td>
</tr> 
<tr> 
<td colspan="2" bgcolor="white">

**Cross product**

[**Support for customized Tracker terminology**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas?selectedIssue=ROADMAP-201)**:** Some DHIS2 terminology is not familiar for the end users. For this reason, we are gradually enabling the possibility to customize it to each particular use case. In this version, the term "event" and "enrollment" are customizable. The admin user will be able to configure it for each program using the Maintenance App, and the Android Capture App will display the customized term instead of the generic one.

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_customized_terminology>

[**Multiselect value type:**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-72\&issueViewSection=comments) DHIS2 already supports the introduction of multiple options for data elements for data aggregation. In this version it will also support it for individual data. The Android App will support both aggregated and individual multi select data elements from this version.

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/value-types-supported.html#capture_app_value_types>

[**Custom Icons**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-207): DHIS2 now supports uploading custom Icons to be used in addition to the built in Icon library. This is useful for use cases not related to health or that require very specific iconography. The Android App will render the custom icons that need to be uploaded and configured using the Maintenance DHIS2 Web App.

**Documentation link:**

<https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_icon_lib>

**User Experience**

[**Improvements in forms layout**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas?selectedIssue=ROADMAP-204)**:** When creating an event or an enrollment, there are a number of fields that are not data elements or attributes, for example, event date, org unit, coordinates, enrollment date, category combinations. Those elements are referred to as event/enrollment details and in previous versions they were displayed in different screens separated from the data elements or attributes. They were difficult to find when users wanted to edit or consult them. In this version the details are displayed inside the form, as the first opened section for completion. Once they are filled in, for example when the user reopens the event or enrollment form, the details section will be visible and easily available, but collapsed to leave more space for the data collection. 

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_common_features_newEdit_event>

[**Improve TEI search user experience**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas?selectedIssue=ROADMAP-203)**:** The search form has been improved to provide a cleaner look and a more intuitive user experience. The buttons have been made more explicit for differencing search from creation. In addition the flow for searching TEIs using attributes rendered as bar / QR codes has been made more agile. If there is only one result and the attribute is unique, the app will open the TEI Dashboard directly. If there are multiple results, the app will display all the cards on the TEI list (this is equal to the current workflow), and if there are no results, the app will display the create button and allow the user to “search outside the program” if the configuration allows it.

**Documentation link:**

<https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_unique_qrBar_search>

[**Improve TEI dashboard user experience**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas?selectedIssue=ROADMAP-205)**:** In the previous version of the application, the TEI header part of the dashboard was improved. In continuation to that effort, the bottom part, where all program stages are displayed, has been redesigned in this version. The changes include a fresh and more clean look of the list of events, with more space and less -not critical- information displayed. In addition, the button for creating new events has been moved to the top (in timeline view).

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/program-features.htm#capture_app_programs_TEI_Dashboard_program_stages>

[**New inputs for value types:**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-293) ****The inputs for all value types have been gradually redesigned from the 2.9 version of the app. The signature input field as well as the complete legend description are included now to improve user experience at data entry. The new input fields are now displayed by default and admin users are able to opt-out to use the old forms through the Android Settings web app.

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_signature>

[https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture\_app\_visual\_legends\_descri2ptions](https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_legends_descriptions)

<https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_input_fields>

[**Improvements in tracker programs data entry flow:**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-294) ****Several improvements have been made in the tracker programs user flow. An informative dialog has been added for confirmation when the user deletes a TEIs. The dialog for scheduling events after compilation has also been redesigned and improved. The selection of org. Units when the user only has access to one org. Unit for data collection has been removed and pre-filled, and lastly, the program rule “Hide program stage” behavior has been aligned with Capture web.

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_common_features_schedule_after_completion>

<https://docs.dhis2.org/en/use/android-app/program-features.html#capture_app_programs_delete_tei>

[**Improve App behavior when working in offline mode:**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-295) ****Some parts of the Android App are not responsive when there is no connection available (i.e. sync buttons…). The behavior is now improved and the App will inform the user that actions are not started because there is no internet connection available when buttons that require connection are tapped.

**Documentation link:** NA

**New functionality and Web Parity**

[**Line listing analytics**](https://dhis2.atlassian.net/browse/ROADMAP-206): This version of the Android App includes the possibility to render and display line listing as part of the offline analytics functionalities. The line list has to be created using the Line Listing DHIS2 web App, and then configured to be displayed in Android using the Android Settings Web App (ASWA), as any other offline analytics in Android. In this case Line lists can be displayed in the home screen, and event or tracker programs (as they do not really apply to aggregated data, they are not displayed in Datasets). Users will be able to search by period, Org. unit, or any of the columns added in the Line List.

There are some limitations to the line lists to be displayed in the Android App. The Org. units and Periods must be relative, not fixed. And there is a maximum number of columns of 15. 

Android Local Analytics are built using local data, and wil, update instantly as more data gets collected (or downloaded) in the device. The App will display a maximum of 500 rows and will inform the user when the limit is reached.

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_event_visualizations>

\


[**Configurable basemap layer:**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-209\&issueViewSection=deliver) DHIS2 supports the configuration of custom map layers for the Maps Web App. From this version of the DHIS2 Android App, those custom layers will be downloaded and rendered in the Maps. The custom layers will be presented as additional layers to the default ones.

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/program-features.html#map-layers>

**Implementation Support**

[**Import/Export App database:**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-210) ****As part of troubleshooting, some errors can be hard to replicate and can lead to data loss because of being unable to sync. With this functionality the end user will be able to export the local database and share it with an admin who will be able to import it for troubleshooting, being able to replicate the exact environment (database, device, configuration). The exported database is encrypted and the administrator will require the user credentials to be able to access the database.

**Documentation link:** <https://docs.dhis2.org/en/use/android-app/settings.html>

[**Improve end-user config error feedback:**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-296) ****In some cases configuration errors leave empty screens in the Android Capture app, either because of empty forms or because of lack of access. From this version of the Android App the app will display explicit and understandable errors to the user, who will be able to effectively communicate with the administrator to fix the problem.

**Documentation link: NA**

 

**Maintenance/Performance**

[**Improve app navigation performance for high number of TEIs:**](https://dhis2.atlassian.net/jira/polaris/projects/ROADMAP/ideas/view/4066207?selectedIssue=ROADMAP-208) ****Implementations are more and more demanding in terms of offline need of individual records. This version of the app has been reviewed to optimize performance when there are big numbers of TEIs downloaded locally. ****

**Documentation link: NA**

You can find in Jira details on the [new features](https://dhis2.atlassian.net/issues/?filter=10640) and [bugs fixed](https://dhis2.atlassian.net/issues/?filter=10641) in this version.

Remember to check the [documentation](https://www.dhis2.org/android-documentation) for detailed information of the features included in the App and how to configure DHIS2 to use it.

Please create a [Jira](https://dhis2.atlassian.net) Issue if you find a bug or you want to propose a new functionality. [Project: Android App for DHIS2 | Component: 
AndroidApp].
</td>
</tr>
</table>