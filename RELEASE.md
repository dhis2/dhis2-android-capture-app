Android Capture App for DHIS 2 (v2.8)
<table>
<tr> 
<td> 
<img src="https://s3-eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/android-chrome-384x384.png" width="800"> 
</td> 
<td>
The new <strong>DHIS2 Android App</strong> allows offline data capture across all DHIS2 data models. Data and metadata are automatically synchronized whenever there is internet access, always keeping the most relevant data for the logged user in the device.
The app is compatible and we support <strong>2.40</strong>, <strong>2.39</strong>, <strong>2.38</strong>.  And has no breaking changes with <strong>2.37</strong>, <strong>2.36</strong>, <strong>2.35</strong> and <strong>2.34</strong>.
</td>
</tr> 
<tr> 
<td colspan="2" bgcolor="white">

## USER EXPERIENCE

**Resized dataset tables:** This feature allows users to resize the columns in datasets, making it easier to view and work with larger tables. Now it is possible to resize all columns by clicking on the header of the column. The resizing can be done through drag and drop. A maximum and minimum size limit is given for the columns that can be resized to ensure a good layout. Users can resize all columns at once by clicking on the top left of the table. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5153) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.8/release+feature+cards/Android-2-8-Resizing-tables.png)

**New sync error navigation:** This version of the app improves the navigation of sync errors on the user interface. The app will display the sync errors in a simple and clean manner allowing the user to navigate from the home screen to the exact field that is causing the error inside the event or data set. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5088) | Screenshot

**Collected handwritten signatures for image Data Elements:** This feature allows users to collect handwritten signatures from the device. The signatures will be saved as an image data elements in the app. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4986) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.8/release+feature+cards/Android-2-8-Handwritten-signatures.png)

**Made phone number and email Data Elements actionable in the form and allowed external actions for phone number, email and URL value types:** This feature enables users to take action on phone numbers, email addresses, and URLs directly from the app, such as dialing a phone number or opening a web page from the event or enrollment data entry form. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4291) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.8/release+feature+cards/Android-2-8-Action-buttons.png)

**Improved visual configuration and rendering of option sets:** This feature improves the way option sets are displayed and configured when configured visualy by enabling render option sets with any value type as Radio buttons or check box and supporting other value types for visual data entry. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4623) | [Jira2](https://dhis2.atlassian.net/browse/ANDROAPP-3370) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.8/release+feature+cards/Android-2-8-Rendering-options-configuration.png)

**Added loading banners when actions take a long time to improve user experience:** This feature provides visual annimations to users when they perform actions that take longer than usual to complete, such loading maps or searching, so that they know that the system is processing their request. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5012) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.8/release+feature+cards/Android-2-8-Loading-banners.png)

**Improved user experience for better functionality when offline:** This feature enhances the functionality of the app when it is used offline, ensuring that menus are disabled when the online actions are not available. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5032)

**Improved management of long texts:** This feature enables users to view long text in fields that are limited in size, such as a stage names or datasets headers. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5080) | [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5080)


## SUPPORT MOBILE IMPLEMENTATIONS

**Provided support for Android app version control from DHIS2 user interface:** This feature enables implementation administrators to manage and control the version of the Android app from the DHIS2 user web interface, making it easier to manage app updates and ensure compatibility with the DHIS2 system. Managers will be able to upload the desired version and users will get a prompt message to update when they are not in the last updated version. The management of versions is made through a new Web App.  [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-3288) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.8/release+feature+cards/Android-2-8-APK-Version.png)

**Removed the maximum number of offline accounts**: This feature removes the limitation on the number of offline accounts that users can create, allowing them to work with as many offline accounts as needed. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-5011)

## WEB PARITY

**Supported file value type:** This feature adds support for file value type in the Android app, allowing users to attach files to their data elements or attributes. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-1992) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.8/release+feature+cards/Android-2-8-File-value-type.png)

**Supported new tracker working lists (filter by DE):** This feature adds support for new tracker working lists that allow users to filter by data elements. The working lists need to be configured in web using the web Capture App. The Android app will download the working lists that are configured and saved on the server side. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-740)

## LMIS

**Integrated a new module for real stock monitoring use case:** This feature adds a new module for real stock monitoring use case to the app, enabling users to manage and monitor their stock levels in real-time. Integrating a module implies that the app will open a completely new and different user interface and experience to respond to a different data entry flow. To use this modules the program will need to be configured using the new web app for program use case configuration. [Jira](https://dhis2.atlassian.net/browse/ANDROAPP-4498) | [Screenshot](https://s3.eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/release+notes+2.8/release+feature+cards/Android-2-8-stock-management-tool.png)

## MAINTENANCE

**Bug fixing:** You can find the list of bugs fixed [here](https://dhis2.atlassian.net/issues/?filter=10402).

</td>
</tr>
</table>