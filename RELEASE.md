Android Capture App for DHIS 2 (v2.6.1) - Patch version
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
* [ANDROAPP-4792](https://jira.dhis2.org/browse/ANDROAPP-4792) [Tracker] TET coordinates are erased when clearing other fields
* [ANDROAPP-4791](https://jira.dhis2.org/browse/ANDROAPP-4791) [Tracker] Search field loses the underline when typing a value
* [ANDROAPP-4790](https://jira.dhis2.org/browse/ANDROAPP-4790) [Form] Unit interval dropdown doesn't save "0" and "1"
* [ANDROAPP-4775](https://jira.dhis2.org/browse/ANDROAPP-4775) wrong margin in event item icon
* [ANDROAPP-4763](https://jira.dhis2.org/browse/ANDROAPP-4763) One field's text is copied in other text fields using the "next" on the keyboard
* [ANDROAPP-4762](https://jira.dhis2.org/browse/ANDROAPP-4762) "unsynchronized" icon doesn't appear after editing an event or attribute
* [ANDROAPP-4755](https://jira.dhis2.org/browse/ANDROAPP-4755) Unique Attribute online check sometimes does not work
* [ANDROAPP-4753](https://jira.dhis2.org/browse/ANDROAPP-4753) [Crash] Break the glass crash
* [ANDROAPP-4752](https://jira.dhis2.org/browse/ANDROAPP-4752) App crashes when downloading a TEI
* [ANDROAPP-4750](https://jira.dhis2.org/browse/ANDROAPP-4750) Error when clicking on the TEI download arrow when the program is protected
* [ANDROAPP-4749](https://jira.dhis2.org/browse/ANDROAPP-4749) Login button is disabled after entering the password
* [ANDROAPP-4748](https://jira.dhis2.org/browse/ANDROAPP-4748) [Crash] LinkedHashMap ConcurrentModificationException
* [ANDROAPP-4745](https://jira.dhis2.org/browse/ANDROAPP-4745) QR CODE ERROR
* [ANDROAPP-4743](https://jira.dhis2.org/browse/ANDROAPP-4743) Dropdown for type Number/Integer are displayed as normal input
* [ANDROAPP-4742](https://jira.dhis2.org/browse/ANDROAPP-4742) [Form] Change input field focus makes loos the value
* [ANDROAPP-4741](https://jira.dhis2.org/browse/ANDROAPP-4741) TEI attributes required as mandatory by a rule that evaluates to false
* [ANDROAPP-4739](https://jira.dhis2.org/browse/ANDROAPP-4739) "Yes only" checkbox displays the "yes" label
* [ANDROAPP-4737](https://jira.dhis2.org/browse/ANDROAPP-4737) [Crash] RuntimeException TrackedEntityInstanceQueryCollectionRepository
* [ANDROAPP-4736](https://jira.dhis2.org/browse/ANDROAPP-4736) [Crash] FormView: could not find Fragment constructor
* [ANDROAPP-4719](https://jira.dhis2.org/browse/ANDROAPP-4719) Multiuser: Wrong view when closing and re-opening the app
* [ANDROAPP-4707](https://jira.dhis2.org/browse/ANDROAPP-4707) The shadow of the status icon above the program stage icon in the TEI dashboard's list of ungrouped events is too dark.
* [ANDROAPP-4706](https://jira.dhis2.org/browse/ANDROAPP-4706) Update Idling resoure for kotlin coroutines
* [ANDROAPP-4699](https://jira.dhis2.org/browse/ANDROAPP-4699) The bottom bar animation in the event view is a bit odd.
* [ANDROAPP-4687](https://jira.dhis2.org/browse/ANDROAPP-4687) [Dataset] Opened icon status on list not working when DataSetCompleteRegistration is deleted in the server
* [ANDROAPP-4686](https://jira.dhis2.org/browse/ANDROAPP-4686) "Open Location" icon should not appear if the TEI or event does not have coordinates
* [ANDROAPP-4683](https://jira.dhis2.org/browse/ANDROAPP-4683) Dataset granular sync is not triggered if error
* [ANDROAPP-4681](https://jira.dhis2.org/browse/ANDROAPP-4681) Generate events based on enrolment date - incorrect behavior
* [ANDROAPP-4673](https://jira.dhis2.org/browse/ANDROAPP-4673) App display bars in incorrect periods when filtering by years
* [ANDROAPP-4672](https://jira.dhis2.org/browse/ANDROAPP-4672) [Dataset] Totals in data set take decimal numbers as zeros
* [ANDROAPP-4643](https://jira.dhis2.org/browse/ANDROAPP-4643) App crashes when a PR is misconfigured with an empty value
* [ANDROAPP-4639](https://jira.dhis2.org/browse/ANDROAPP-4639) Validation rule shows "?" when empty category option
* [ANDROAPP-4629](https://jira.dhis2.org/browse/ANDROAPP-4629) Android should display a crash message (and not loading endlessly) when there is a problem with program rule values
* [ANDROAPP-4616](https://jira.dhis2.org/browse/ANDROAPP-4616) Reopening a dataset doesn't update the icon in the display list
* [ANDROAPP-4615](https://jira.dhis2.org/browse/ANDROAPP-4615) Menu (and other) items are not translated
* [ANDROAPP-4586](https://jira.dhis2.org/browse/ANDROAPP-4586) Applying a filter hides the cards in the map
* [ANDROAPP-4543](https://jira.dhis2.org/browse/ANDROAPP-4543) App becomes blank (cannot access TEIs) when changing the layout
* [ANDROAPP-4534](https://jira.dhis2.org/browse/ANDROAPP-4534) Remove TEI term from all menus and dialogs
* [ANDROAPP-4528](https://jira.dhis2.org/browse/ANDROAPP-4528) Event screen changes color after adding a relationship
* [ANDROAPP-4526](https://jira.dhis2.org/browse/ANDROAPP-4526) Focus in the input when clicking in the label or near the input.
* [ANDROAPP-4509](https://jira.dhis2.org/browse/ANDROAPP-4509) Error in synchronization when data set is not shared with the user
* [ANDROAPP-4460](https://jira.dhis2.org/browse/ANDROAPP-4460) App displays blank table when changing from Pie chart to table
* [ANDROAPP-4417](https://jira.dhis2.org/browse/ANDROAPP-4417) Dropdown lists open the keyboard (in landscape mode)
* [ANDROAPP-4406](https://jira.dhis2.org/browse/ANDROAPP-4406) The dashboard loses its color theme.
* [ANDROAPP-4080](https://jira.dhis2.org/browse/ANDROAPP-4080) Map cards and pin images not refreshing when coming back from dashboard
* [ANDROAPP-4056](https://jira.dhis2.org/browse/ANDROAPP-4056) Sync configuration now required to be selected twice
* [ANDROAPP-4046](https://jira.dhis2.org/browse/ANDROAPP-4046) Multiple cell selection
* [ANDROAPP-4001](https://jira.dhis2.org/browse/ANDROAPP-4001) DS - Options restrictions are not respected
* [ANDROAPP-3872](https://jira.dhis2.org/browse/ANDROAPP-3872) Organisation Unit incorrect label
* [ANDROAPP-3650](https://jira.dhis2.org/browse/ANDROAPP-3650) The dropdown menu should be separated from the right side of the screen 16dp
* [ANDROAPP-3581](https://jira.dhis2.org/browse/ANDROAPP-3581) App color not applying in TEI Dashboard
* This patch release updates the [Android SDK](https://github.com/dhis2/dhis2-android-sdk) to version develop.
    
You can find in Jira details on the [bugs fixed](https://jira.dhis2.org/issues/?filter=12376) in this version. 

Remember to check the [documentation](https://www.dhis2.org/android-documentation) for detailed 
information of the features included in the App and how to configure DHIS2 to use it. 

Please create a [Jira](https://jira.dhis2.org/secure/Dashboard.jspa) Issue if you find a bug or 
you want to propose a new functionality. [Project: Android App for DHIS2 | Component: 
AndroidApp].
</td>
</tr>
</table>