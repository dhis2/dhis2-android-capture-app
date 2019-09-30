# Program Indicators

The following is a comprehensive list of all Program indicator variables available in DHIS2, and notes on whether or not these have been implemented in the Android Capture app.

Any issues around using a particular feature with Android are highlighted with an exclamation mark.

|legend|description|
|:--:|:------|
|✓|Component implemented|
|&ndash;|Component not implemented (rule fails) |
|n/a|Not applicable|
|![](resources/images/image3_icon.png)|Work in progress. Feature not completely implemented yet or with unexpected behaviour already reported.|

## Variables to use in a program indicator expression or filter
| Variable type| Description of variable type|Status|Notes on implementation|
|-|---|:-:|---|
|Event Date<br/>event_date|The date of when the event took place.|![](resources/images/image3_icon.png)||
|Creation Date\*<br/>creation_date|The date of when an event or enrollment was created in the system.|![](resources/images/image3_icon.png)||
|Due Date<br/>due_date|The date of when an event is due.|![](resources/images/image3_icon.png)||
|Sync Date\*<br/>sync_date|The date of when the event or enrollment was last synchronized with the Android app.|![](resources/images/image3_icon.png)||
|Incident Date<br/>incident_date|The date of the incidence of the event.|✓||
|Enrollment Date (not visible on UI)<br/>enrollment_date|The date of when the tracked entity instance was enrolled in the program.|✓||
|Enrollment Status<br/>enrollment_status|Can be used to include or exclude enrollments in certain statuses.|&ndash;||
|Current Date<br/>current_date|The current date.|✓||
|Completed Date|The date the event is completed.|![](resources/images/image3_icon.png)||
|Value Count<br/>value_count|The number of non-null values in the expression part of the event.|✓||
|Zero or positive value count<br/>zero_pos_value_count|The number of numeric positive values in the expression part of the event.|✓||
|Event Count<br/>event_count|The count of events (useful in combination with filters).|✓||
|Enrollment Count<br/>enrollment_count|The count of enrollments (useful in combination with filters).|n/a|Indicators in the Android App are calculated in the domain of one TEI enrollment. Value always 1.||
|TEI Count<br/>tei_count|The count of tracked entity instances (useful in combination with filters).|n/a|Indicators in the Android App are calculated in the domain of one TEI enrollment. Value always 1.||
|Program Stage Name<br/>program_stage_name|Can be used in filters for including only certain program stages in a filter for tracker programs.|![](resources/images/image3_icon.png)||
|Program Stage ID<br/>program_stage_id|Can be used in filters for including only certain program stages in a filter for tracker programs.|![](resources/images/image3_icon.png)||
|Reporting Period Start<br/>reporting_period_start|Can be used in filters or expressions for comparing any date to the first date in each reporting period.|n/a|Indicators in the Android App are calculated in the domain of one TEI enrollment.||
|Reporting Period End<br/>reporting_period_end|Can be used in filters or expressions for comparing any date to the last inclusive date in each reporting period.|n/a|Indicators in the Android App are calculated in the domain of one TEI enrollment.||

[Documentation Reference](https://www.google.com/url?q=https://docs.dhis2.org/master/en/user/html/configure_program_indicator.html%23program_indicator_functions_variables_operators&sa=D&ust=1557433016643000)
\* DHIS 2 v2.32
