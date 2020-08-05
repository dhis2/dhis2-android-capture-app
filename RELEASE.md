Android Capture App for DHIS 2 (v2.1.1) - Patch version

<table>
<tbody>
<tr>
<td>
<img src="https://s3-eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/android-chrome-384x384.png" width="800">
</td>
<td>The new <strong>DHIS2 Android App</strong> allows offline data capture across all DHIS2 data models. Data and metadata are automatically synchronized whenever there is internet access, always keeping the most relevant data for the logged user in the device.

The app is compatible and we support <strong>2.34</strong>, <strong>2.33</strong>, <strong>2.32</strong>.  And has no breaking changes with <strong>2.31</strong>, <strong>2.30</strong> and <strong>2.29</strong>.
</td>
</tr>
<tr>
<td colspan="2" bgcolor="white">

## New features
### Data sets
#### Validation rules
The validation step has been integrated in the save and complete flow.
***
### Listing, filtering and sorting of events
#### Sorting events and TEIs
The app now supports sorting of lists of events and TEIs. Sorting is integrated with the filters menu and the users will be allowed to sort the list by one chosen parameter either in ascending or descending order.
#### Enrollment status filter
In the Program Search screen, the user is able to filter the TEI list by Enrollment Status. This filter does not allow multiselection.
#### Enrollment date filter
In the Program Search screen, the user is able to filter the TEI list by Enrollment Date (in addition to event date, which was already available).
***
### Maps
#### Satellite view
The user will be able to change the map background to satellite view. Satellite view is available as an option in the map layer dialog. When selected, it will replace the default background image.
#### Event layer in tracker programs
When opening the map view in tracker programs, the program stages with coordinates will be available as layers.
#### Display relationships in maps
tracker programs, the Relationships between TEIs with coordinates will be available as layers.
#### Carousel navigation in maps
A carousel with TEIs, Events or Relationships cards has been added to the map view. The carrousel and the map will respond to the user selection in both directions.
***
### Generic features
- Preselect previous organisation unit when entering events
- Disable grammar spelling in option fields
- Change color of long text fields in forms
- Make category option searchable when there are more than 15 options
***
### Tracker features
- Inherit values when creating new TEI for relationship
***
### User experience and user interface
- Redesigned events and tei cards
- Improve data set screen
- Disable horizontal swipe in data sets
- New icons for event status and sync state
- Improved settings for sync parameters via the new Android Setting DHIS2 web app
***
### Quality, security and performance
- Database encryption
- Expanded error log
- Home screen initialization
***
## Other additions
In addition, the <strong>Android SDK</strong> has been updated to <strong>v1.2.0</strong>
The <strong>rule engine</strong> has been updated to <strong>v2.0.6</strong>
You can find in Jira details on the [new features](https://jira.dhis2.org/issues/?filter=11877) and [bugs fixed](https://jira.dhis2.org/issues/?filter=11878) in this version.

Remember to check the [documentation](https://www.dhis2.org/android-documentation) for detailed information of the features included in the App and how to configure DHIS2 to use it.

Please create a [Jira](https://jira.dhis2.org/secure/Dashboard.jspa) Issue if you find a bug or you want to propose a new functionality. \[Project: Android App for DHIS2 | Component: AndroidApp].

</td>
</tr>
</tbody>
</table>