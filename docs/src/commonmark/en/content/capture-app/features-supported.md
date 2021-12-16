# Programs { #capture_app_programs }

## Programs in Android { #capture_app_programs_android }

### New Navigation Bar (NEW 2.4)

Use the new navigation bar to move from one screen to another.

#### Events

1. Details
2. Data Entry
3. Analytics
4. Notes

![](resources/images/capture-app-image141.jpg){ width=25%}

#### TEI Dashboard

1. Details
2. Analytics
3. Relationships
4. Notes
   
![](resources/images/capture-app-image142.jpg){ width=25%}

#### Event listing for event and tracker programs

  1. List view
  2. Map view

![](resources/images/capture-app-image143.jpg){ width=25%}
![](resources/images/capture-app-image144.jpg){ width=25%}

### Complete/Re-open event { #capture_app_programs_complete_reopen }


User must enter the event and then click the complete icon in the bottom right corner.

![](resources/images/capture-app-image37.jpg){ width=25%}
![](resources/images/capture-app-image74.png){ width=25%}

Two options will be presented:

1. Finish and Complete
2. Finish

![](resources/images/capture-app-image75.png){ width=25%}


> **Note**
>
> To verify if an event is completed look at the icon, it must be a green checked box.

> **Note**
>
> The app needs to take into consideration if the user has the correct authority (‘Uncomplete events’) to reopen a completed event.

### Field completion percentage { #capture_app_programs_field_percentage }


The percentage of data completed in each event is shown in the top right corner of an event when it is opened after first registration.

![](resources/images/capture-app-image80.png){ width=25%}

> **Note**
>

### Sections Navigation { #capture_app_programs_sections_nav }


The display for sections has been redesigned for a more simple user experience. In addition, the sections in enrollment form are now supported and are aligned with the design of the event sections.

![](resources/images/capture-app-image115.png){ width=25%}
![](resources/images/capture-app-image116.png){ width=25%}

### Error messages { #capture_app_programs_errors }


The app will now list the name of the mandatory fields missing when the user tries to complete an event or enrollment. The sections containing the missing fields will be highlighted to help the user find the missing fields.

![](resources/images/capture-app-image117.png){ width=25%}

The errors and warnings are also shown as an indicator next to the section name.

![](resources/images/capture-app-image145.png){ width=25%}

### Event Notes { #capture_app_programs_event_notes }


It is possible to add notes to events in single event programs and program stage events. Notes are available in a new tab at the data entry form.

![](resources/images/capture-app-image106.png){ width=25%}
![](resources/images/capture-app-image107.jpg){ width=25%}

### Maps { #capture_app_programs_maps }

#### Map Layers { #capture_app_programs_map_layers }

The possible layers to display in maps are:

- Show events (For programs without registration)
- Satellite view
- TEI coordinates (By default in programs with registration)
- Enrollment Coordinates (Only for programs with registration)
- Program Stage coordinates (Only for programs with registration)
- Relationships (Only for programs with registration)
- Heatmap (Only for programs with registration)
- Tracked Entity Attributes (Coordinates Value Type - Only for programs with registration)
- Data Elements (Coordinates Value Type)

The user can select one or more layers to be displayed.

The maps will display coordinates and polygons.

![](resources/images/capture-app-image125.png){ width=25%}
![](resources/images/capture-app-image146.png){ width=25%}
![](resources/images/capture-app-image136.png){ width=25%}

#### Map Carousel { #capture_app_programs_map_carousel }

On the map view of the program, a carousel of cards is displayed, one for each enrolled TEI (Tracker programs) or Event(Event programs).

* TEI cards on the carousel follow the same design as the TEI list view.

* When scrolling horizontally the carousel, the map zooms into the selected coordinates. If coordinates field is empty a message is shown instead.

![](resources/images/capture-app-image126.jpg){ width=25%} 
![](resources/images/capture-app-image133.png){ width=25%}

Each card displays Tracked Entity Attributes (for Tracker Programs) and Data Elements (for Event programs) configured as 'Display in list'.

![](resources/images/capture-app-image147.png){ width=25%}

#### Map Relationships { #capture_app_programs_map_relationships }

In tracker, the user can see relationships on a map by tapping the map icon on the relationships tab.

* An arrow is shown on the direction of the relationship.
* For bidirectional relationships, the arrow points both sides.
* Each relationship type displays a different color.
* If one or both TEIs have a polygon as coordinate, the line goes from (and to) the nearest point in the polygon of both TEIs.

![](resources/images/capture-app-image132.png){ width=25%}

#### Map Current Location (NEW 2.4)

If the user grants location permissions to the App, the map will show the current location represented as a blue color dot.

![](resources/images/capture-app-image148.png){ width=25%}

#### Map Navigation to Location (NEW 2.4)

If the TEI or event has coordinates a navigation icon will be displayed at the top right of the card. Click to open the location in a maps App.

![](resources/images/capture-app-image149.jpg){ width=25%}
![](resources/images/capture-app-image150.jpg){ width=25%}
![](resources/images/capture-app-image151.jpg){ width=25%}

### Working Lists (NEW 2.4)

Working lists are now compatible with the Android app. Once a list is selected, the filters will be blocked and not allowed to change until the user resets the search.

Working lists are available in Event and Tracker Programs.

![](resources/images/capture-app-image152.jpg){ width=25%}
![](resources/images/capture-app-image153.jpg){ width=25%}

### Program Indicators (NEW 2.4)

The new analytics tab now supports displaying text and key/value pair in feedback or indicator section.

![](resources/images/capture-app-image154.jpg){ width=25%}

### Legends (NEW 2.4)

Legends are now available in Android App. It will be shown next to value with the respective color and label.

![](resources/images/capture-app-image155.jpg){ width=25%}

## Program with registration in Android { #capture_app_programs_with_reg }

### TEI Card Design { #capture_app_programs_tei_design }

The new TEI Card design includes:

* Last update date
* Other enrollments in programs with displayFrontPageList
* At least **3 main attributes**. 
  * If there are more, there will be an arrow to display the full list (only attributes with displayInList option)
* Enrollment status label if **completed** or **cancelled**
* Overdue icon if an overdue event exists with the most recent overdue event date
* Card image (one of the following options):
  * Profile picture when available or
  * First letter of the first attribute or
  * Tracked Entity Type icon
  * A hyphen will display if non of the options are available

![](resources/images/capture-app-image124.png){ width=25%}

### Complete/Deactivate Enrollment { #capture_app_programs_complete_deactivate_enrollment }


To complete or deactivate an enrollment, click on three dot menu in the right upper corner and select "Complete" or "Deactivate".

![](resources/images/capture-app-image76.jpg){ width=25%}

### Reset Search Fields { #capture_app_programs_reset_search }


All tracker programs will take the user to the search screen. The search fields are used to look up for a specific entity, and the circled arrow to reset the search. All fields will be blank for the user to do a new search.

At first, the user is obligated to do a search. if there are no coincidences, the search button will change to an ‘Add’ button for the user to create a new enrollment.

![](resources/images/capture-app-image78.PNG){ width=25%}
![](resources/images/capture-app-image79.PNG){ width=25%}

### Search screen for all Tracked Entity Type { #capture_app_programs_search_screen }


User is able to search across all program of one tracked entity type (TET). In the Search screen there is a drop down which shows all the programs available for the active TET (active TET is defined by the selection of the program in the home screen). That drop down should also have an option with TET name. (for example: person)

When the user selects that option, the search fields available will only be the TET attributes (no program specific attributes).
Search restrictions do not apply, because they belong to the programs.

![](resources/images/capture-app-image44.png){ width=25%}
![](resources/images/capture-app-image22.png){ width=25%}

The search will return the found TEI's in the local database and also those in the Search OU of the user (when user is online). For those found online, the user will have to select them and the whole record will be downloaded.


> **Note**
>
>  When configuring search org. units, make sure that your capture org. units are contained in your search org. units, to do that capture org. units have to be selected as well as search org. units.

### TEI Dashboard across programs { #capture_app_programs_tei_dashboard }


User can see the TEI dashboard without any program by selecting the TEI in the list if the search was without program.

The dashboards will show the list of active enrollments.

![](resources/images/capture-app-image22.png){ width=25%}
![](resources/images/capture-app-image38.png){ width=25%}

### TEI enrollment history and new enrollment { #capture_app_programs_tei_history }


User is able to see the complete historical record of the TEI. Clicking on the top right corner menu, select "Program Enrollments" and a list of Active enrollments will be displayed, followed by a list of past enrollments (completed or cancelled), followed by the programs in which the TEI could be enrolled.
User can also return to the 'TEI Dashboard without any program' by selecting 'All enrollments'.

Users should be able to navigate to the different enrollments from the list.

![](resources/images/capture-app-image40.jpg){ width=25%}
![](resources/images/capture-app-image7.png){ width=25%}

### Delete TEI's & Enrollments { #capture_app_programs_delete_tei }


To delete a TEI or an enrollment, select In the TEI dashboard, the three dots menu.

Local TEI or Enrollment will be deleted from the database. Records that has been previously synced with the server will be marked for deletion if the user has the authority:

- F_ENROLLMENT_CASCADE_DELETE
- F_TEI_CASCADE_DELETE

They will show in the TEI search list, but will not be accessible.

> **Note**
>
> When users enter a TEI and while it is not synced to the server they will be able to delete the TEI and the enrollment even if they have not been asigned these authorities. This is by design and to allow users rolling back in case of having entered wrong data (TEI and/or enrollment) and thus preventing it reaching the server and requiring another user with higher privileges to fix the issue.

![](resources/images/capture-app-image86.jpg){ width=25%}

### Group view of Program stages in TEI Dashboard { #capture_app_programs_group_view }


The TEI Dashboard offers now the possibility to change the list of events from the chronological view to a stage grouping view. The stage grouping view will group and collapse the events per program stage. Each program stage group can be expanded by the user and the events will be displayed chronologically.

![](resources/images/capture-app-image108.png){ width=25%}
![](resources/images/capture-app-image109.jpg){ width=25%}

### Inherit Values { #capture_app_programs_inherit_values }

When creating a new TEI for a relationship, inherit any program attribute marked with the inherit check in web.

This means that any existing attributes in the first TEI should have pass to the new TEI and be shown in the registration form.

### Breaking the glass { #capture_app_programs_breaking_the_glass }

The ‘breaking the glass’ feature is not yet supported in DHIS2 Android Capture App. If the program is configured as ‘Protected’, the default behavior for Android will be the same as if the program is configured as ‘closed.’ This means that an Android user will not be able to read or edit enrollments of a TEI outside of their capture org units. TEIs registered in a Search OU will be returned by the TE Type search but if the program is closed or protected the user will not be allowed to see or create a new enrollment.
If Android users must be able to access TEI outside of their data capture org unit, the program should be configured with access level ‘Open.’

![](resources/images/capture-app-image137.jpg){ width=25%}

### Analytic charts (NEW 2.4)

It is possible to display the evolution in data elements as charts, values or tables. These data elements must be a numeric value type and configured in a repeatable stage.

1. Single value: It will display the newest value in the program.
   
![](resources/images/capture-app-image156.jpg){ width=25%}

2. Charts: It is possible to display the values as a line chart o as a bar chart.

![](resources/images/capture-app-image157.jpg){ width=25%}
![](resources/images/capture-app-image158.jpg){ width=25%}

The Nutrition Growth charts are shown according to the WHO standards.  This option will render a background image and apply the axis (0 to 5 monthly)according to the WHO model.

 ![](resources/images/capture-app-image159.jpg){ width=25%}
   
3. Tables: It will display the data elements or indicators in the rows and the periods in the columns.

![](resources/images/capture-app-image160.jpg){ width=25%} 

## Supported features Overview { #capture_app_programs_supported_features }


The following is a comprehensive list of all features available for Programs with and without registration in DHIS2, and notes on whether or not these have been implemented in the Android Capture app.

In the notes, ‘admin’ refers to someone who develops and configures a DHIS2 system, and ‘user’ refers to someone who uses apps to capture data, update it, and review reports.

|Legend|Description|
|:--:|:------|
|![](resources/icons/icon-complete.png)|Feature implemented|
|![](resources/icons/icon-incomplete.png)|Feature not implemented&nbsp;(will be ignored)|
|![](resources/icons/icon-na.png)|Not applicable|
|![](resources/icons/icon-wip.png)|Work in progress. Feature not completely implemented yet or with unexpected behaviour already reported.|

### Program { #capture_app_programs_supported_features_program }


|Feature|Description of feature|Program with registration|Program without registration|Notes on implementation|
|-|---|:-:|:-:|---|
|Data entry method for option sets|Enables an admin to choose how options will be displayed on-screen across the entire program (ie either as drop-down lists or as radio buttons)|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-incomplete.png)|This will be replaced by the new rendering options.|
|Combination of categories<br />(Attribute CatCombo)|Allows an admin to attach a Category (set of Options) to the Program, requiring users to categorize each enrolment. (This is called an Attribute Category Combination in DHIS 2.)|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Data approval workflow|If an admin selects a pre-configured Data Approval Workflow, this will be used to enforce an &lsquo;approval&rsquo; or &lsquo;acceptance and approval&rsquo; cascade, enabling users to sign-off and lock data.|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-incomplete.png)||
|Display front page list|If this option is ticked, the landing page displays a list of active enrolments once an Org Unit and Program have been chosen. (Attributes shown are those ticked as &lsquo;display in list&rsquo;.)|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|First stage appears on registration page|When this option is chosen, then during Program enrolment, the screen for the first Program Stage is also shown (enrolment and the first event are captured together on one screen).|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)| In Android, this is implemented by opening automatically the event after enrollment is completed, instead of adding the form to the same screen.|
|Completed events expiry days|Enables admins to lock data-entry a certain number of days after an event has been completed.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Expiry period type + expiry days|Enables admins to set a period (eg weekly, monthly), and to lock data-entry a certain number of days after the end of the period.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Allow future enrolment dates|If ticked, this enables a user to enter future Enrolment dates during enrolment in a Program; otherwise users are restricted to today or past dates.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Allow future incident dates|If ticked, this enables a user to enter future Incident dates during enrolment in a Program; otherwise users are restricted to today or past dates.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Only enrol once (per tracked entity instance lifetime)|If ticked, prevents a TEI (eg person) from being enrolled in this Program more than once.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Show incident date|If ticked, both Enrolment and Incident dates are shown to the user for data capture; otherwise, only the Enrolment date is shown/captured.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Description of incident date|Allows an admin to customize the label that is used for the incident date.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Description of enrolment date|Allows an admin to customize the label that is used for the enrollment date.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Capture coordinates (enrolment)|Enables users to capture geographical coordinates during enrolment in the program.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Capture Polygon (enrolment) |Enables users to capture locations (enclosed areas) during enrolment in the program.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|TEI Coordinates |Enables users to capture geographical coordinates for the TEI during the enrolment in the program.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Relationships: create and update|Enables users to create and update relationships.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Relationships - shortcut link to add a relative|This enables admins to add a link for one specific relationship to the Dashboard, enabling users to directly create a linked TEI (eg "child" patient).|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-na.png)||
|Attributes: display in list|This setting determines whether an Attribute can be viewed in lists such as search results, and whether it can be seen in the shortlist of Attributes shown under "Profile" in the Dashboard.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)|The first three attributes will be shown||
|Attributes: mandatory|This enables an admin to mark an Attribute as "mandatory";, meaning the enrolment can&rsquo;t be saved until a value is captured.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Attributes:  date in future|For date Attributes, this enables an admin to either prevent or allow future dates to be captured.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Registration form - default|The default data entry form simply lists all attributes defined for the TEI.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Registration form - custom|This enables an admin to define a custom layout (using HTML) for the registration form.|-|![](resources/icons/icon-na.png)|Custom layouts are not supported in the Android App||
|Program notifications|You can set up automated notifications for when program enrolments or completions occur, or at a set interval before/after incident or enrolment dates. These can be sent as internal DHIS 2 messages, emails or SMSs.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)|This functionality is executed on the server side, once data is received. Will not work when the app is working offline.||
|Activate/deactivate enrolment|Deactivating a TEI dashboard will cause the TEI to become &ldquo;read-only&rdquo;. This means you cannot enter data, enrol the TEI or edit the TEI profile.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Complete allowed only if validation passes|Select check box to enforce that an event created by this program is only completed when all validation rules have passed.|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-incomplete.png)||
|Org unit opening/closing dates|Enables an admin to set opening and closing dates for an Org Unit, which blocks users from adding or editing events outside of these dates.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Data sharing levels/Can capture data|Enables the user to add new event, edit data and delete events in the program.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Data sharing levels/Can view data|Enables the user to see list of events within the program.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Data sharing levels/No access|The user will not be able to see the program|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||

### Program stage { #capture_app_programs_supported_features_program_stage }


|Feature|Description of feature|Program with registration|Program without registration|Notes on implementation|
|-|---|:-:|:-:|---|
|Event form - default|The default data entry form simply lists all attributes belonging to a program registration|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Event form - section forms|Sections forms allow you to split existing forms into segments|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Event form - custom|Define a custom event form as a HTML page.|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-incomplete.png)|Custom layouts are not supported in the Android App.|
|Program stage notifications|You can set up automated notifications for when the program stage is completed, or at a set interval before/after scheduled event dates. These can be sent as internal DHIS 2 messages, emails or SMS messages.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)|This functionality is executed on the server side, once data is received. Will not work when the app is working offline.|
|Repeatable|If Repeatable Is ticked, this stage can be repeated during one program enrollment. If t is not, then the stage can only happen once during a program enrollment.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Repeatable|If Repeatable Is ticked, this stage can be repeated during one program enrollment. If t is not, then the stage can only happen once during a program enrollment.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Repeatable + Standard interval days|The system will suggest the due date as the calculation of the last event + standard interval dates.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Period type|Enables an admin to configure a set of periods (e.g. weeks or months) for each event in the program stage, instead of just a date. When creating events, users are then asked to choose a period (instead of a date) for each new event they create within that program stage.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Auto-generate event|If ticked, a "booking" is generated for this Program Stage upon enrolment, based on the "Scheduled days from start".|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Generate events based on enrolment date (not incident date)|Check on it for auto-generating due dates of events from program-stages of this program based on the enrollment date. If it is not checked, the due dates are generated based on incident date.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Open data entry form after enrolment + report date to use|If selected, once an enrolment is complete, an event&rsquo;s data entry form should open directly afterwards.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Ask user to complete program when stage is complete| If selected, upon completing the program stage the user should be asked to complete the program. (This setting is ignored if "Ask user to create new event" is also ticked.)|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Ask user to create new event when stage is complete|If selected, when the Program Stage is completed the user is prompted to book.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Hide due date|Only shows the actual date for events, hiding the due date.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-na.png)||
|Capture coordinates (event)/Feature Type-Point|Enables the user to capture geographical coordinates when each event is created ![](resources/icons/icon-incomplete.png) particularly useful in devices that have GPS (eg Android), as instead of having to type in coordinates, the user can automatically populate them with the press of a button.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Capture Polygon (event)/Feature Type-Polygon |Enables users to capture locations (enclosed areas) when each event is created. A Polygon must contain at least 4 points.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Description of report date|Allows an admin to customize the label that is used for the event&rsquo;s date.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Data elements-compulsory|This enables an admin to mark a data element as "compulsory", meaning an event can not be saved until a value is captured.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Data elements-allow provided elsewhere|On the form, this places a tick-box next to the selected data element, and enables previous data to be pulled into the data element.|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-na.png)||
|Data elements-display in reports|Displays the value of this data element into the single event without registration data entry function.|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-complete.png)||
|Data elements-date in future|For date Data Elements, this enables an admin to either prevent or allow future dates to be captured.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Data elements-render options as radio|Enables an admin to choose how options will be displayed on-screen for each Data Element (i.e. either as drop-down list or as radio buttons).|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-incomplete.png)||
|Block entry form after completed|Prevents all edits to events after they have been completed.|![](resources/icons/icon-complete.png)|![](resources/icons/icon-complete.png)||
|Event comments|Enables the user to add overall comments to an event. These comments are cumulative (new comments are added below existing comments).|![](resources/icons/icon-incomplete.png)|![](resources/icons/icon-na.png)||


### Program with registration: Tracked entity dashboard { #capture_app_programs_supported_features_program_with_reg }

|Feature|Description of feature|Status|Notes on implementation|
|-|---|:-:|---|
|Messaging|Enables users to send ad-hoc free-text messages to TEIs (e.g. patients) via SMS or email.|![](resources/icons/icon-incomplete.png)||
|Mark for follow-up (button with exclamation triangle)|Enables a user to mark a TEI (e.g. patient) as requiring follow-up.|![](resources/icons/icon-complete.png)||
|Display TEI audit history|Enables a user to see a history of all edits to Attributes for this TEI (e.g. patient).|-||
|Inline Program Indicators|If a program indicator "display in form" box is ticked, the indicator appears on the Tracker Capture dashboard, and is updated live as data capture occurs.|![](resources/icons/icon-complete.png)|||
|Delete events|Enables the user to delete an event.|![](resources/icons/icon-complete.png)||
|Schedule events|In the event generation dialogue, the user should also see the option to schedule an event. The process is like creating an event, but the user will be sent back to the TEI dashboard after the event is scheduled.|![](resources/icons/icon-complete.png)||
|Referral of patients|In the event generation dialogue, the user should also see the option to refer a patient. The process is like creating/scheduling an event, but the user can change the org unit and has to specify if is a one-time or permanent referral. One time will just create the event in the specified OU.|![](resources/icons/icon-complete.png)||
|Reset search fields|User is able to clean up the search fields by pressing on the rounded arrow icon on the top right corner of the search screen.|![](resources/icons/icon-complete.png)||
|Search screen for all TE Type|User is able to search across all program of one tracked entity type (TET). In the Search screen there is a drop down which shows all the programs available for the active TET (active TET is defined by the selection of the program in the home screen). That drop down should also have an option with TET name. (Person in our server). When the user selects that option, the search fields available will only be the TET attributes (no program specific attributes). All search restrictions do not apply, because they belong to the programs.|![](resources/icons/icon-complete.png)||
|TEI Dashboard without program|User can see the TEI dashboard without any program by selecting the TEI in the list if the search was without program. The dashboards will show the TET attributes in the details card followed by a list of active enrollments.|![](resources/icons/icon-complete.png)||
|TEI enrollment history and new enrollment|User is able to see the complete historical record of the TEI. By clicking on the top right corner icon they will see a list of Active enrolments, followed by a list of past enrolments (completed or cancelled), followed by the programs in which the TEI could be enrolled. Users should be able to navigate to the different enrolments from the list.|![](resources/icons/icon-complete.png)||
|Access level-Breaking the glass|If the program is configured with access level protected, and the user searches and finds tracked entity instances that is owned by organisation unit that the user does not have data capture authority for, the user is presented with the option of breaking the glass. The user will gove a reason for breaking the glass, then gain temporary ownership of the tracked entity instance.|![](resources/icons/icon-incomplete.png)||

### Program without registration: Single event program { #capture_app_programs_supported_features_program_without_reg }

|Feature|Description of feature|Status|Notes on implementation|
|-|---|:-:|---|
|Events listing (grid)|A listing of existing events that is displayed once a program is selected.|![](resources/icons/icon-complete.png)||
|Sort and filter events in grid|Allows the user to sort listed events, or to filter events based on keywords or specific ranges of dates/numbers.|![](resources/icons/icon-complete.png)|Events are sorted chronologically. The user can filter by period and organisation unit.||
|Edit events in grid|Allows the user to directly edit the data elements shown in the events listing/grid.|![](resources/icons/icon-incomplete.png)||
|View event audit history|Enables the user to see a history of all changes to the event&rsquo;s data elements.|![](resources/icons/icon-incomplete.png)||
|Show/hide columns (in event list/grid)|Enables the user to modify the data elements shown in the event listing/grid (applies to that user only).|![](resources/icons/icon-incomplete.png)||
|Field completion percentage|The percentage of data completed in each event is shown in the top right corner of an event when it is opened after first registration. The percentages should be adapted to the effects of the program rules in the forms.|![](resources/icons/icon-complete.png)|The percentage of completion does not take into account the not-supported value types in the forms.||
|Delete events|Enables the user to delete an event.|![](resources/icons/icon-complete.png)||
