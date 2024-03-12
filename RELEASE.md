Android Capture App for DHIS 2 (v2.9.1) - Patch version
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
* [ANDROAPP-5895](https://dhis2.atlassian.net/browse/ANDROAPP-5895) Correct misalignment when entering text for inputShell
* [ANDROAPP-5885](https://dhis2.atlassian.net/browse/ANDROAPP-5885) [Data set] indicators don't update until the user moves to a different cell
* [ANDROAPP-5881](https://dhis2.atlassian.net/browse/ANDROAPP-5881) Input with virtual keyboard not working correctly
* [ANDROAPP-5872](https://dhis2.atlassian.net/browse/ANDROAPP-5872) [LMIS] Search in stock management is not updating the list
* [ANDROAPP-5871](https://dhis2.atlassian.net/browse/ANDROAPP-5871) App Not Responding when local db is encrypted
* [ANDROAPP-5856](https://dhis2.atlassian.net/browse/ANDROAPP-5856) ANR ReadableStoreImpl.addObjectsToCollection in DataSetDetailRepositoryImpl
* [ANDROAPP-5825](https://dhis2.atlassian.net/browse/ANDROAPP-5825) Input date value changes on click in schedule new event screen
* [ANDROAPP-5821](https://dhis2.atlassian.net/browse/ANDROAPP-5821) NoSuchElementException: List is empty.
* [ANDROAPP-5807](https://dhis2.atlassian.net/browse/ANDROAPP-5807) Crash when parsing value to input in InputDateTime
* [ANDROAPP-5804](https://dhis2.atlassian.net/browse/ANDROAPP-5804) Incorrect label on bar codes, QR codes and GS1
* [ANDROAPP-5803](https://dhis2.atlassian.net/browse/ANDROAPP-5803) On schedule event due date incorrect when last previous event does not have a report date
* [ANDROAPP-5788](https://dhis2.atlassian.net/browse/ANDROAPP-5788) Keyboard hides helper text if the selected field is near the bottom of the screen
* [ANDROAPP-5773](https://dhis2.atlassian.net/browse/ANDROAPP-5773) Analytics are not being displayed as tables.
* [ANDROAPP-5770](https://dhis2.atlassian.net/browse/ANDROAPP-5770) IllegalStateException: Expected BringIntoViewRequester to not be used before parents are placed.
* [ANDROAPP-5769](https://dhis2.atlassian.net/browse/ANDROAPP-5769) ApplicationNotResponding: ANR for at least 5000 ms.
* [ANDROAPP-5767](https://dhis2.atlassian.net/browse/ANDROAPP-5767) RuntimeException in teidashboardActivity Sentry issue
* [ANDROAPP-5764](https://dhis2.atlassian.net/browse/ANDROAPP-5764) BottomSheetDialog shows barcode expanded with old form 
* [ANDROAPP-5749](https://dhis2.atlassian.net/browse/ANDROAPP-5749) Incorrect behavior when tapping on Next on sections that are too long
* [ANDROAPP-5746](https://dhis2.atlassian.net/browse/ANDROAPP-5746) Exception when trying to add a file from downloads directory
* [ANDROAPP-5743](https://dhis2.atlassian.net/browse/ANDROAPP-5743) [Bug?] Calculated variables save integer values with ".0"
* [ANDROAPP-5742](https://dhis2.atlassian.net/browse/ANDROAPP-5742) [Local Analytics] App isn't plotting all the points (per event), only the first one.
* [ANDROAPP-5741](https://dhis2.atlassian.net/browse/ANDROAPP-5741) [Local Analytics] App crashes if charts are empty
* [ANDROAPP-5740](https://dhis2.atlassian.net/browse/ANDROAPP-5740) Display error correctly when date or time is incomplete
* [ANDROAPP-5726](https://dhis2.atlassian.net/browse/ANDROAPP-5726) RTS workflow needs to allow for translating the 3 transaction types
* [ANDROAPP-5716](https://dhis2.atlassian.net/browse/ANDROAPP-5716) Filters not responsive to rapid changes when there are many programs
* [ANDROAPP-5710](https://dhis2.atlassian.net/browse/ANDROAPP-5710) Keyboard not showing for certain fields
* [ANDROAPP-5704](https://dhis2.atlassian.net/browse/ANDROAPP-5704) Overdue date in patient line list follows inconsistent format
* [ANDROAPP-5700](https://dhis2.atlassian.net/browse/ANDROAPP-5700) User can select out-of-scope OUs on the enrollment form
* [ANDROAPP-5698](https://dhis2.atlassian.net/browse/ANDROAPP-5698) Incorrect list of points in a polygon
* [ANDROAPP-5663](https://dhis2.atlassian.net/browse/ANDROAPP-5663) Tei dashboard event list scrolling
* [ANDROAPP-5662](https://dhis2.atlassian.net/browse/ANDROAPP-5662) Search button is behind the nav bar
* [ANDROAPP-5630](https://dhis2.atlassian.net/browse/ANDROAPP-5630) Due date in Tracker program does not follow standard interval days
* [ANDROAPP-5606](https://dhis2.atlassian.net/browse/ANDROAPP-5606) Active filter counter mismatched with workinglist's filters
* [ANDROAPP-5604](https://dhis2.atlassian.net/browse/ANDROAPP-5604) App crashes when one attempts to synchronise TEI and events imported via QR code
* [ANDROAPP-5570](https://dhis2.atlassian.net/browse/ANDROAPP-5570) Changes to enrollment date not respected by program rules
* [ANDROAPP-5567](https://dhis2.atlassian.net/browse/ANDROAPP-5567) DHIS2-RTS Capture app limited to 60 TEIs
* [ANDROAPP-5484](https://dhis2.atlassian.net/browse/ANDROAPP-5484) Images block creating relationships (Capture Android)
* [ANDROAPP-5294](https://dhis2.atlassian.net/browse/ANDROAPP-5294) Filter by ACCESSIBLE org units
* [ANDROAPP-5261](https://dhis2.atlassian.net/browse/ANDROAPP-5261) The animation of the input bottom bar is not smooth.
* [ANDROAPP-5249](https://dhis2.atlassian.net/browse/ANDROAPP-5249) Resizing for all columns difficult use
* [ANDROAPP-5130](https://dhis2.atlassian.net/browse/ANDROAPP-5130) Follow-up clicks can be skipped by the app when entering data into tables.
* This patch release updates the [Android SDK](https://github.com/dhis2/dhis2-android-sdk) to version 1.9.1-20240109.100903-15.
    
You can find in Jira details on the [bugs fixed](https://dhis2.atlassian.net/issues/?filter=10554) in this version.

Remember to check the [documentation](https://www.dhis2.org/android-documentation) for detailed 
information of the features included in the App and how to configure DHIS2 to use it. 

Please create a [Jira](https://dhis2.atlassian.net) Issue if you find a bug or
you want to propose a new functionality. [Project: Android App for DHIS2 | Component: 
AndroidApp].
</td>
</tr>
</table>