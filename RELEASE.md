Android Capture App for DHIS 2 (v2.8.2) - Patch version
<table>
<tr> 
<td> 
<img src="https://s3-eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/android-chrome-384x384.png" width="800"> 
</td> 
<td>
This is a patch version of the <strong>DHIS2 Android App</strong> It builds upon the last version including bug fixes that couldn't wait to the next version. 
It includes no functional improvements neither changes in the User Interface. It means that yours users can update without experiencing any change in the UI. 
</td>
</tr> 
<tr> 
<td colspan="2" bgcolor="white">

## Bugs fixed
* [ANDROAPP-5463](https://dhis2.atlassian.net/browse/ANDROAPP-5463) Wrong password in already authenticated account throws invalid url
* [ANDROAPP-5452](https://dhis2.atlassian.net/browse/ANDROAPP-5452) Notifications not displaying on devices with android 13 
* [ANDROAPP-5426](https://dhis2.atlassian.net/browse/ANDROAPP-5426) DataSet tables not saving text values
* [ANDROAPP-5425](https://dhis2.atlassian.net/browse/ANDROAPP-5425) App crashing when opening orgUnit field in event creation on android 5 and 6 devices
* [ANDROAPP-5424](https://dhis2.atlassian.net/browse/ANDROAPP-5424) App not functional in Android 5.0 and 6.0 devices due to expression-parser library
* [ANDROAPP-5403](https://dhis2.atlassian.net/browse/ANDROAPP-5403) The app displays the keyboard over the selecting menu when the user has multiple apps to use the email or phone number
* [ANDROAPP-5401](https://dhis2.atlassian.net/browse/ANDROAPP-5401) Infinite loading when applying date filters in tracker program
* [ANDROAPP-5399](https://dhis2.atlassian.net/browse/ANDROAPP-5399) Improve recomposition on input field in tables
* [ANDROAPP-5396](https://dhis2.atlassian.net/browse/ANDROAPP-5396) SDK BC: handle d2ErrorCode SERVER_CONNECTION_ERROR
* [ANDROAPP-5389](https://dhis2.atlassian.net/browse/ANDROAPP-5389) Validation Strategy - Errors
* [ANDROAPP-5385](https://dhis2.atlassian.net/browse/ANDROAPP-5385) Program dashboard: filter by EventDate includes all EventStatus as side effect
* [ANDROAPP-5384](https://dhis2.atlassian.net/browse/ANDROAPP-5384) Persist dataset column size when adjusted by the user and revert to default
* [ANDROAPP-5380](https://dhis2.atlassian.net/browse/ANDROAPP-5380) Sync button crashes app after rotating device in search screen
* [ANDROAPP-5377](https://dhis2.atlassian.net/browse/ANDROAPP-5377) A TEI enrolled in many programs display to many icons blocking the TEI info card
* [ANDROAPP-5376](https://dhis2.atlassian.net/browse/ANDROAPP-5376) Login button doesn't work and doesn't present any error
* [ANDROAPP-5375](https://dhis2.atlassian.net/browse/ANDROAPP-5375) App crashes with some icons
* [ANDROAPP-5370](https://dhis2.atlassian.net/browse/ANDROAPP-5370) Navigation button does not open in some programs.
* [ANDROAPP-5369](https://dhis2.atlassian.net/browse/ANDROAPP-5369) NullPointerException: Attempt to invoke virtual method 'java.lang.String java.lang.String.replaceAll(java.lang.String, ...
* [ANDROAPP-5368](https://dhis2.atlassian.net/browse/ANDROAPP-5368) Org Unit value type opens the hierarchy incorrectly
* [ANDROAPP-5363](https://dhis2.atlassian.net/browse/ANDROAPP-5363)   Wrong label displayed while navigating an error or warning
* [ANDROAPP-5348](https://dhis2.atlassian.net/browse/ANDROAPP-5348) Errors in program rules are not shown after they have been displayed once despite the program rule being reexecuted
* [ANDROAPP-5343](https://dhis2.atlassian.net/browse/ANDROAPP-5343) Sync flow backwards
* [ANDROAPP-5342](https://dhis2.atlassian.net/browse/ANDROAPP-5342) Form actionable icons launch action from stored value
* [ANDROAPP-5340](https://dhis2.atlassian.net/browse/ANDROAPP-5340) Store image and files before value type validation
* [ANDROAPP-5335](https://dhis2.atlassian.net/browse/ANDROAPP-5335) In TEI dashboard filters appears items related to TEI
* [ANDROAPP-5334](https://dhis2.atlassian.net/browse/ANDROAPP-5334) "All enrollments" cards show incident date even when not configured
* [ANDROAPP-5330](https://dhis2.atlassian.net/browse/ANDROAPP-5330) App crash when deleting quantities in "Review" stage
* [ANDROAPP-5329](https://dhis2.atlassian.net/browse/ANDROAPP-5329) The selected cell is hidden in RTStock program (and datasets) table if the first cell is selected after scroll
* [ANDROAPP-5328](https://dhis2.atlassian.net/browse/ANDROAPP-5328) Default language not respected (or inconsistent) when changing between servers.
* [ANDROAPP-5323](https://dhis2.atlassian.net/browse/ANDROAPP-5323) IllegalStateException: Attempting to launch an unregistered ActivityResultLauncher with contract androidx.activity.resul...
* [ANDROAPP-5255](https://dhis2.atlassian.net/browse/ANDROAPP-5255) [LANDSCAPE] Loading bar never hides in overview screen
* [ANDROAPP-5253](https://dhis2.atlassian.net/browse/ANDROAPP-5253) Event status filter doesn't remove checkmarks after the reset
* [ANDROAPP-4710](https://dhis2.atlassian.net/browse/ANDROAPP-4710) Validation Strategy - Mandatory Fields
* [ANDROAPP-4322](https://dhis2.atlassian.net/browse/ANDROAPP-4322) Analytics legends don't show event's exact date
* [ANDROAPP-3106](https://dhis2.atlassian.net/browse/ANDROAPP-3106) [Bug]Error when searching with comma char in the values
* This patch release updates the [Android SDK](https://github.com/dhis2/dhis2-android-sdk) to version 1.8.2.
    
You can find in Jira details on the [bugs fixed](https://dhis2.atlassian.net/issues/?filter=10461) in this version.

Remember to check the [documentation](https://www.dhis2.org/android-documentation) for detailed 
information of the features included in the App and how to configure DHIS2 to use it. 

Please create a [Jira](https://dhis2.atlassian.net) Issue if you find a bug or
you want to propose a new functionality. [Project: Android App for DHIS2 | Component: 
AndroidApp].
</td>
</tr>
</table>