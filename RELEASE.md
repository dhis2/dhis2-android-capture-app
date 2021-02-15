<table>
<tbody>
<tr>
<td>
<img src="https://s3-eu-west-1.amazonaws.com/content.dhis2.org/dhis2-android/android-chrome-384x384.png" width="800">
</td>
<td>This is a patch version of the <strong>DHIS2 Android App</strong> It builds upon the last version including bug fixes that couldn't wait to the next version. It includes no functional improvements neither changes in the User Interface. It means that yours users can update without experiencing any change in the UI.
</td>
</tr>
<tr>
<td colspan="2" bgcolor="white">

## Bugs fixed
* [ANDROAPP-3605](https://jira.dhis2.org/browse/ANDROAPP-3605) Show option group' action fails when using the variable 'data element from previous even
* [ANDROAPP-3603](https://jira.dhis2.org/browse/ANDROAPP-3603) Crash generated on "new event" information/menu buttons
* [ANDROAPP-3602](https://jira.dhis2.org/browse/ANDROAPP-3602) Can't enable SMS module to sync by sms
* [ANDROAPP-3600](https://jira.dhis2.org/browse/ANDROAPP-3600) Crash when user can't login to a server
* [ANDROAPP-3595](https://jira.dhis2.org/browse/ANDROAPP-3595) [Sync] Old webapp values remain and app is blocked even when it has been disabled
* [ANDROAPP-3594](https://jira.dhis2.org/browse/ANDROAPP-3594) [Search] Relationships not showing up in map
* [ANDROAPP-3592](https://jira.dhis2.org/browse/ANDROAPP-3592) 'Next' button hides when creating a new event
* [ANDROAPP-3586](https://jira.dhis2.org/browse/ANDROAPP-3586) Inconsistent metadata should end the syncrhonization and not keep loading endelessly
* [ANDROAPP-3575](https://jira.dhis2.org/browse/ANDROAPP-3575) Using QR/Barcode in event form freezes the app
* [ANDROAPP-3571](https://jira.dhis2.org/browse/ANDROAPP-3571) [bug] TEI Search from attributes shows sync buttons, but they are not usable
* [ANDROAPP-3570](https://jira.dhis2.org/browse/ANDROAPP-3570) [bug] Relationship do not display properly the attributes
* [ANDROAPP-3566](https://jira.dhis2.org/browse/ANDROAPP-3566) [bug] Android does not respect Android Web Settings App for Reserved values
* [ANDROAPP-3561](https://jira.dhis2.org/browse/ANDROAPP-3561) Assign to me filter not available in tracker programs
* [ANDROAPP-3553](https://jira.dhis2.org/browse/ANDROAPP-3553) [Search] Tei image shows as "Null" in Search screen
* [ANDROAPP-3552](https://jira.dhis2.org/browse/ANDROAPP-3552) [Map] Layers are not applied properly when coming from tei dashboard
* [ANDROAPP-3551](https://jira.dhis2.org/browse/ANDROAPP-3551) [Map] Program map does not show all relationships
* [ANDROAPP-3538](https://jira.dhis2.org/browse/ANDROAPP-3538) Stage access is not set properly at event creation
* [ANDROAPP-3536](https://jira.dhis2.org/browse/ANDROAPP-3536) Filter does not apply correctly using the skipped event status
* [ANDROAPP-3535](https://jira.dhis2.org/browse/ANDROAPP-3535) Approved DataSets still editable on Android App
* [ANDROAPP-3534](https://jira.dhis2.org/browse/ANDROAPP-3534) Unable to type on text fields in DS
* [ANDROAPP-3533](https://jira.dhis2.org/browse/ANDROAPP-3533) Incorrect placement of the filter indicator
* [ANDROAPP-3529](https://jira.dhis2.org/browse/ANDROAPP-3529) Widget is not loading correct icon
* [ANDROAPP-3525](https://jira.dhis2.org/browse/ANDROAPP-3525) App randomly adds "%" sign to some fields after editing them
* [ANDROAPP-3522](https://jira.dhis2.org/browse/ANDROAPP-3522) Maps not rendering if tracked entity type has no icon
* [ANDROAPP-3520](https://jira.dhis2.org/browse/ANDROAPP-3520) [Bug] Increase/decrease column sizes buttons
* [ANDROAPP-3519](https://jira.dhis2.org/browse/ANDROAPP-3519) [Bug] Datasets alert dialog is cut and is difficult to click action buttons
* [ANDROAPP-3511](https://jira.dhis2.org/browse/ANDROAPP-3511) [bug] Aggregate form loads permentenly
* [ANDROAPP-3509](https://jira.dhis2.org/browse/ANDROAPP-3509) DHIS 2 showing incorrect period
* [ANDROAPP-3503](https://jira.dhis2.org/browse/ANDROAPP-3503) After report date entered in scheduled event go to stages not back to tracked entity profile
* [ANDROAPP-3501](https://jira.dhis2.org/browse/ANDROAPP-3501) Crash when creating a relationship and clicking on show on map
* [ANDROAPP-3498](https://jira.dhis2.org/browse/ANDROAPP-3498) DataEntryAdapter header crash
* [ANDROAPP-3496](https://jira.dhis2.org/browse/ANDROAPP-3496) [Bug] Date of enrollment filter From-To misbehavior
* [ANDROAPP-3492](https://jira.dhis2.org/browse/ANDROAPP-3492) Action: "make field mandatory" does not apply to render types matrix/sequential
* [ANDROAPP-3481](https://jira.dhis2.org/browse/ANDROAPP-3481) Crash invalid cast in data entry adapter
* [ANDROAPP-3480](https://jira.dhis2.org/browse/ANDROAPP-3480) Crash in custom text view when setting click listener
* [ANDROAPP-3477](https://jira.dhis2.org/browse/ANDROAPP-3477) Error at login: dialog with message Problem during Encryption
* [ANDROAPP-3476](https://jira.dhis2.org/browse/ANDROAPP-3476) Sync status dialog crash
* [ANDROAPP-3474](https://jira.dhis2.org/browse/ANDROAPP-3474) [Maps] Lateinit exception with mapLayerManager
* [ANDROAPP-3433](https://jira.dhis2.org/browse/ANDROAPP-3433) Valuetype Date & Time does not record seconds
* [ANDROAPP-3303](https://jira.dhis2.org/browse/ANDROAPP-3303) [bug] In landscape mode the hide search/filtering form doesn't work
* [ANDROAPP-3280](https://jira.dhis2.org/browse/ANDROAPP-3280) [bug] When using the Date filtering To-From the second time the user clicks on the filter the To date comes to today instead of keeping its previous date
* [ANDROAPP-2986](https://jira.dhis2.org/browse/ANDROAPP-2986) [Bug][Datasets] Long click for datasets in facility notes

You can find in Jira details on the [bugs fixed](https://jira.dhis2.org/issues/?filter=11970) in this version.

Remember to check the [documentation](https://www.dhis2.org/android-documentation) for detailed information of the features included in the App and how to configure DHIS2 to use it.

Please create a [Jira](https://jira.dhis2.org/secure/Dashboard.jspa) Issue if you find a bug or you want to propose a new functionality. \[Project: Android App for DHIS2 | Component: AndroidApp].

</td>
</tr>
</tbody>
</table>
