Android Capture App for DHIS 2 (v2.5)
<table>
<tr> 
<td> 
<img src="https://s3-eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/android-chrome-384x384.png" width="800"> 
</td> 
<td>
The new <strong>DHIS2 Android App</strong> allows offline data capture across all DHIS2 data models. Data and metadata are automatically synchronized whenever there is internet access, always keeping the most relevant data for the logged user in the device.
The app is compatible and we support <strong>2.37</strong>, <strong>2.36</strong>, <strong>2.35</strong>.  And has no breaking changes with <strong>2.34</strong>, <strong>2.33</strong>, <strong>2.32</strong>, <strong>2.31</strong> and <strong>2.30</strong>.
</td>
</tr> 
<tr> 
<td colspan="2" bgcolor="white">

## Bugs fixed
<!-- Analytics -->
## LOCAL ANALYTICS

**Offline In-App Program/Dataset Analytics:** The Android app can now render analytics that have been created in the Data Visualizer app in DHIS2. Analytics to be displayed require configuration using the Android Settings Web App, where administrators will be able to select the charts and tables to be displayed for end users.
These visualizations can be rendered on the home screen of the App, on the dataset screen and at the programs level. All analytics are aggregated in the device using local data. The Analyticis feature is 100% functional offline.

The analytics supported in the Android App are:
- Pivot Tables
- Column Chart
- Line Chart
- Pie Chart
- Radar chart
- Single Value

All these visualizations can be organized and displayed in groups. Groups are also configured using the Android Settings Web App. For each visualization object, the user will be able to filter in the app by:

- Period: Daily, Weekly, Monthly, Yearly, This Quarter, Last Quarter, Last 4 Quarters and Quarter this year.
- OrgUnit: Select "All" to display all the org units available to the user or "Selection" to specify one or multiple org units.

[Jira](https://jira.dhis2.org/browse/ANDROAPP-2557) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Local+Analytics+-+Home.png) | [Screenshot 2](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Local+Analytics+-+Filtering.png) | [Screenshot 3](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Local+Analytics+-+Groups.png) | [Screenshot 4](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Local+Analytics+-+Android+Settings+Webapp.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/visual-configurations.html#local-analytics-new-25)


## DATA ENTRY USER EXPERIENCE

**Dataset redesign** The layout for datasets data entry has been redesigned for a more integrated user experience and clean user interface. [Jira](https://jira.dhis2.org/browse/ANDROAPP-4382) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Data+Sets+New+style.png)

**Export/Share QR and Bar codes:** Data elements or attributes type text can be configured as QR or barcodes. With the new export/share option, users will be able to display a bar or QR code in an image so that it can be shared it for printing, take a screenshot or show it on the screen for scanning.
[Jira](https://jira.dhis2.org/browse/ANDROAPP-3891) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Export+Share+QR+Code.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_render_qr)

**Improved render for icon-based data entry:** When program sections rendering type is used in combination with icons, a section with a single data element and associated Option Set renders the assigned icons next the options to simplify data entry. The layout and design of this screen has been redesigned and improved for a better user experience.
[Jira](https://jira.dhis2.org/browse/ANDROAPP-4027) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Visual+Data+Entry.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/visual-configurations.html#capture_app_visual_rendering_type)

**Personalized Calendar View:** In the DHIS2 Android Capture App users can switch date selection from spinner to calendar view. In this version, the app will remember the last visualization selected by the user and use it the next time the user needs to select a date.
[Jira](https://jira.dhis2.org/browse/ANDROAPP-2402) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Calendar.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/android-specific-features.html#personalized-calendar-view-new-25)

**Display reason for data non-editable:** Data can be blocked for many reasons in DHIS2, because of access restrictions or expiration among others. When an Event, TEI or Data Set are not editable the user will be able to find the reason in the "Details" section.  The possible reasons are:
- Event completion
- Enrollment completion
- Expired event
- Closed organisation unit
- Organisation unit out of capture scope
- No access to capture data in the program or data set
- No access to a category option in the program or data set
  [Jira](https://jira.dhis2.org/browse/ANDROAPP-3565) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Non+Editable+Data.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/android-specific-features.html#reason-for-non-editable-data-new-25)

**Adjust TEI Dashboard options to program configuration:** The options offered at the TEI dashboard will be tailored to the specific configuration of the program.
- Relationships tab will not be visible if the program relationships are not configured.
- Create event button will be hidden when the user cannot create more events based on tracker configuration.
- Indicators tab will be not be visible if the program has no program indicators configured.
- Organisation Unit filter will not be visible if the user has only one Organisation Unit configured.
  [Jira](https://jira.dhis2.org/browse/ANDROAPP-4097) | [Jira 2](https://jira.dhis2.org/browse/ANDROAPP-3129) | [Jira 3](https://jira.dhis2.org/browse/ANDROAPP-4099) | [Documentation](https://docs.dhis2.org/en/use/android-app/features-supported.html#tei-dashboard-navigation-panel-new-25)


## MAPS

**General Maps User experience:** After three versions since maps were included in the DHIS2 Android App, we have reviewed and improved the user experience based on community feedback.  
[Jira](https://jira.dhis2.org/browse/ANDROAPP-4024)

**Center to user postion:** If the user grants location permissions to the App, the map will show the current location represented as a blue color dot. The maps in the DHIS2 Android Capture App now include the possibility to center the map on the user location.
[Jira](https://jira.dhis2.org/browse/ANDROAPP-3583) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-User+position.png)

## TRACKER FEATURES

**Add support for Event - TEI relationships:** The app now allows users to add relationships from single events (Event programs) to TEIs. There is a new tab in the event dashboard, named relationships, that is active when it is configured in the server. This version does not allow relationships from TEIs to events or using events that belong to an enrollment. [Jira](https://jira.dhis2.org/browse/ANDROAPP-2275) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Event+TEI+Relationships.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/features-supported.html#event-tei-relationships-new-25)

**NEW filter for TEIs marked as follow-up:** In Tracker pograms, the 'Follow Up' filter allows the user to filter out the TEIs that have been marked as 'Follow-up'. TEIs can be marked to be followed up in the TEI Dashbaord.
[Jira](https://jira.dhis2.org/browse/ANDROAPP-3304) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.5/Android-2-5-Follow+Up+Filter.png) | [Documentation](https://docs.dhis2.org/en/use/android-app/android-specific-features.html#follow-up-new-25)

## OTHER FEATURES

**Interface language based on DHIS2 user language:** The language of the interface will correspond to the language set in the DHIS2 user configuration. If the language is not available in the app, it will pick the language of the device. If none of the language configurations are available, the app will default to English.
Translations set up in DHIS2 for metadata will also be shown according to the language in the user configuration.
[Jira](https://jira.dhis2.org/browse/ANDROAPP-2925) | [Documentation](https://docs.dhis2.org/en/use/android-app/visual-configurations.html#interface-language-new-25)

## MAINTENANCE

**Quality / Security / Performance:** You can find a list of issues related to quality, security and performance opening this [jira filter](https://jira.dhis2.org/issues/?filter=12204).

**Bug-fixing:** You can find a list of of the bugs fixed in this version opening this [jira filter](https://jira.dhis2.org/issues/?filter=12203).

## RELEASE INFO

|Release Information|Link|
| --- | --- |
|Download app from Google Play or Github |[Google Play](https://www.dhis2.org/app-store) - [Github](https://github.com/dhis2/dhis2-android-capture-app/releases)| 
|Documentation|[https://www.dhis2.org/android-documentation](https://docs.dhis2.org/en/full/use/dhis2-android-app.html)|
|Details about each feature on JIRA (requires login)|[2.5 Features ](https://jira.dhis2.org/issues/?filter=12300)|
|Overview of bugs fixed on JIRA (requires login)|[2.5 Bugs](https://jira.dhis2.org/issues/?filter=12203)|
|Demo instance (user/password)|[https://play.dhis2.org/demo/ ](https://play.dhis2.org/demo/) Credentials: android / Android123|
|DHIS 2 community|[https://community.dhis2.org Mobile Community ](https://community.dhis2.org/c/subcommunities/mobile/16)|
|Source code on Github|[https://github.com/dhis2/dhis2-android-capture-app ](https://github.com/dhis2/dhis2-android-capture-app)|
|Source code of SDK on Github |[https://github.com/dhis2/dhis2-android-sdk](https://github.com/dhis2/dhis2-android-sdk)| 

</td>
</tr>
</table>