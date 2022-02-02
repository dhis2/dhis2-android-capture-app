# Program Indicators { #capture_app_program_ind }

The following is a comprehensive list of all Program indicator variables available in DHIS2, and notes on whether or not these have been implemented in the Android Capture app.

Any issues around using a particular feature with Android are highlighted with an exclamation mark.

|legend|description|
|:--:|:------|
|![](resources/images/../../admin/icon-complete.png)|Component implemented|
|![](resources/images/../../admin/icon-incomplete.png)|Component not implemented (rule fails) |
|![](resources/images/../../admin/icon-na.png)|Not applicable|
|![](resources/images/../../admin/icon-wip.png)|Work in progress. Feature not completely implemented yet or with unexpected behaviour already reported.|

## Common functions to use in a program indicator expression or filter { #capture_app_program_ind_common_functions }

 Function| Description |Status|Notes on implementation|
|-|-----|:-:|-----|
|if|Evaluates the boolean expression and if true returns the true expression value, if false returns the false expression value. The arguments must follow the rules for any indicator expression.|![](resources/images/../../admin/icon-complete.png)||
|isNull|Returns true if the element value is missing (null), otherwise false.|![](resources/images/../../admin/icon-complete.png)||
|isNotNull|Returns true if the element value is not missing (not null), otherwise false.|![](resources/images/../../admin/icon-complete.png)||
|firstNonNull|RReturns the value of the first element that is not missing (not null). Can be provided any number of arguments. Any argument may also be a numeric or string literal, which will be returned if all the previous objects have missing values.|![](resources/images/../../admin/icon-complete.png)||
|greatest|Returns the greatest (highest) value of the expressions given. Can be provided any number of arguments.|![](resources/images/../../admin/icon-complete.png)||
|least|Returns the least (lowest) value of the expressions given. Can be provided any number of arguments.|![](resources/images/../../admin/icon-complete.png)||

## (d2) Functions to use in a program indicator expression or filter { #capture_app_program_ind_d2_functions }

 Function| Description |Status|Notes on implementation|
|-|-----|:-:|-----|
|addDays|Produces a date based on the first argument date, adding the second argument number of days.|![](resources/images/../../admin/icon-complete.png)||
|ceil|Rounds the input argument up to the nearest whole number.|![](resources/images/../../admin/icon-complete.png)||
|condition|Evaluates the boolean expression and if true returns the true expression value, if false returns the false expression value. The conditional expression must be quoted. The true-expr and false-expr arguments must follow the rules of any program indicator expression (including functions).|![](resources/images/../../admin/icon-complete.png)||
|count|Counts the number of data values that has been collected for the given program stage and data element in the course of the enrollment. The argument data element is supplied with the #{programStage.dataElement} syntax.|![](resources/images/../../admin/icon-complete.png)||
|countIfCondition|Counts the number of data values that matches the given condition criteria for the given program stage and data element in the course of the enrollment. The argument data element is supplied with the #{programStage.dataElement} syntax. The condition is supplied as a expression in single quotes.|![](resources/images/../../admin/icon-complete.png)||
|countIfValue|Counts the number of data values that matches the given literal value for the given program stage and data element in the course of the enrollment. The argument data element is supplied with the #{programStage.dataElement} syntax. The value can be a hard coded text or number.|![](resources/images/../../admin/icon-complete.png)||
|countIfZeroPos|Counts the number of values that is zero or positive entered for the source field in the argument.|![](resources/images/../../admin/icon-complete.png)||
|daysBetween|Produces the number of days between two data elements/attributes of type date.|![](resources/images/../../admin/icon-complete.png)||
|floor|Rounds the input argument down to the nearest whole number.|![](resources/images/../../admin/icon-complete.png)||
|hasUserRole|Returns true if current user has this role otherwise false.|![](resources/images/../../admin/icon-complete.png)||
|hasValue|Returns true if the data element/attribute has a value.|![](resources/images/../../admin/icon-complete.png)||
|inOrgUnitGroup|Evaluates whether the current organisation unit is in the argument group. The argument can be defined with either ID or organisation unit group code.|![](resources/images/../../admin/icon-complete.png)||
|left|Evaluates to the left part of a text, num-chars from the first character.|![](resources/images/../../admin/icon-complete.png)||
|length|Find the length of a string.|![](resources/images/../../admin/icon-complete.png)||
|minutesBetween|Produces the number of minutes between two data elements/attributes of type “date and time”.|![](resources/images/../../admin/icon-complete.png)||
|modulus|Produces the modulus when dividing the first with the second argument.|![](resources/images/../../admin/icon-complete.png)||
|monthsBetween|Produces the number of full months between the first and second argument.|![](resources/images/../../admin/icon-complete.png)||
|oizp|Returns one if the expression is zero or positive, otherwise returns zero.|![](resources/images/../../admin/icon-complete.png)||
|relationshipCount|Produces the number of relationships of the given type that is connected to the enrollment or event. When no type is given, all types are counted.|![](resources/images/../../admin/icon-incomplete.png)||
|right|	Evaluates to the right part of a text, num-chars from the last character.|![](resources/images/../../admin/icon-complete.png)||
|round|	Rounds the input argument to the nearest whole number.|![](resources/images/../../admin/icon-complete.png)||
|split|RSplit the text by delimiter, and keep the nth element(0 is the first).|![](resources/images/../../admin/icon-complete.png)||
|substring|Evaluates to the part of a string specified by the start and end character number.|![](resources/images/../../admin/icon-complete.png)||
|validatePatten|Evaluates to true if the input text is an exact match with the supplied regular expression pattern.|![](resources/images/../../admin/icon-complete.png)||
|weeksBetween|Produces the number of full weeks between two data elements/attributes of type date.|![](resources/images/../../admin/icon-complete.png)||
|yearsBetween|Produces the number of years between the first and second argument.|![](resources/images/../../admin/icon-complete.png)||
|zing|Returns zero if the expression is negative, otherwise returns the expression value.|![](resources/images/../../admin/icon-complete.png)||
|zpvc|Returns the number of numeric zero and positive values among the given object arguments.|![](resources/images/../../admin/icon-complete.png)||

## Variables to use in a program indicator expression or filter { #capture_app_program_ind_variables }


| Variable type| Description of variable type|Status|Notes on implementation|
|-|-----|:-:|-----|
|Event Date <br />event_date|The date of when the event took place.|![](resources/images/../../admin/icon-complete.png)||
|Creation Date* <br />creation_date|The date of when an event or enrollment was created in the system.|![](resources/images/../../admin/icon-complete.png)||
|Due Date<br />due_date|The date of when an event is due.|![](resources/images/../../admin/icon-complete.png)||
|Sync Date\*<br />sync_date|The date of when the event or enrollment was last synchronized with the Android app.|![](resources/images/../../admin/icon-incomplete.png)||
|Incident Date<br />incident_date|The date of the incidence of the event.|![](resources/images/../../admin/icon-complete.png)||
|Enrollment Date (not visible on UI)<br />enrollment_date|The date of when the tracked entity instance was enrolled in the program.|![](resources/images/../../admin/icon-complete.png)||
|Enrollment Status<br />enrollment_status|Can be used to include or exclude enrollments in certain statuses.|![](resources/images/../../admin/icon-complete.png)||
|Current Date<br />current_date|The current date.|![](resources/images/../../admin/icon-complete.png)||
|Completed Date|The date the event is completed.|![](resources/images/../../admin/icon-complete.png)||
|Value Count<br />value_count|The number of non-null values in the expression part of the event.|![](resources/images/../../admin/icon-complete.png)||
|Zero or positive value count<br />zero_pos_value_count|The number of numeric positive values in the expression part of the event.|![](resources/images/../../admin/icon-complete.png)||
|Event Count<br />event_count|The count of events (useful in combination with filters).|![](resources/images/../../admin/icon-complete.png)||
|Enrollment Count<br />enrollment_count|The count of enrollments (useful in combination with filters).|![](resources/images/../../admin/icon-complete.png)|Indicators in the Android App are calculated in the domain of one TEI enrollment. Value always 1.||
|TEI Count<br />tei_count|The count of tracked entity instances (useful in combination with filters).|![](resources/images/../../admin/icon-na.png)|Indicators in the Android App are calculated in the domain of one TEI enrollment. Value always 1.||
|Program Stage Name<br />program_stage_name|Can be used in filters for including only certain program stages in a filter for tracker programs.|![](resources/images/../../admin/icon-incomplete.png)||
|Program Stage ID<br />program_stage_id|Can be used in filters for including only certain program stages in a filter for tracker programs.|![](resources/images/../../admin/icon-incomplete.png)||
|Reporting Period Start<br />reporting_period_start|Can be used in filters or expressions for comparing any date to the first date in each reporting period.|![](resources/images/../../admin/icon-na.png)|Indicators in the Android App are calculated in the domain of one TEI enrollment.||
|Reporting Period End<br />reporting_period_end|Can be used in filters or expressions for comparing any date to the last inclusive date in each reporting period.|![](resources/images/../../admin/icon-na.png)|Indicators in the Android App are calculated in the domain of one TEI enrollment.||
|Organisation Unit Count<br />organisationunit_count|.|![](resources/images/../../admin/icon-na.png)||

[Documentation Reference](https://docs.dhis2.org/master/en/user/html/configure_program_indicator.html%23program_indicator_functions_variables_operators&sa=D&ust=1557433016643000)
