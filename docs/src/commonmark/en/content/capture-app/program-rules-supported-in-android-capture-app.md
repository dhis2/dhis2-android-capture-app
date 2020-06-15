# Program rules supported in Android Capture App

The following is a comprehensive list of all Program rule components (variable types and actions) available in DHIS 2, and notes on whether or not these have been implemented in the Android Capture app.

Any issues around using a particular feature with Android are highlighted with an exclamation mark \!.

|legend|description|
|:--:|:------|
|![](resources/images/../../admin/icon-complete.png)|Value type implemented|
|![](resources/images/../../admin/icon-incomplete.png)|Value type not implemented, but will be safely ignored (if not compulsory) |
|![](resources/images/../../admin/icon-na.png)|Not applicable|
|![](resources/images/../../admin/icon-wip.png)|Work in progress. Feature not completely implemented yet or with unexpected behavior already reported |


## Program rule Variable source types supported
| Variable type| Description of variable type| Program with registration| Program without registration| Notes on implementation|
|-|---|:-:|:-:|---|
|Data element from the newest event for a program stage|This source type works the same way as "Data element from the newest event in the current program", except that it only evaluates values from a specific program stage.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-na.png)||
|Data element from the newest event in the current program (with registration)|This source type is populated with the newest data value collected for the specified data element within the enrolment.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-na.png)||
|Data element from the newest event in the current program (without registration)|This program rule variable will be populated with the newest data value found within the 10 newest events in the same organization unit.|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-complete.png)||
|Data element in current event (with registration)|Variable takes the data element&rsquo;s value from the current event.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-na.png)||
|Data element in current event (without registration)|Contains the data value from the same event that the user currently has open.|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-complete.png)||
|Data element from previous event (with registration)|Program rule variables with this source type will contain the newest value from all previous events for the specified data element. The event currently open is not evaluated.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-na.png)||
|Data element from previous event (without registration)|This program rule variable will be populated with the newest data value found within the 10 events preceding the current event date (i.e. not including the current event).|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-complete.png)||
|Tracked entity attribute|Populates the program rule variable with a specified tracked entity attribute for the current TEI (e.g. current patient).|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-na.png)||
|Calculated value|Calculated value.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||

## Program rule Actions supported (Data element in current event)

| Action| Description of action| Program with registration| Program without registration| Notes on implementation|
|-|---|:-:|:-:|---|
|Hide Field|Hides an individual data element if the rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|! If you change the value after the field is hidden, it will revert the action depending on the value type rule engine default value. We recommend its use combined with the hasvalue function.||
|Hide Section|Hides a whole section and its data elements if the rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Hide Option|Hide a single option for an option set in a given data element/tracked entity attribute. When combined with <b>show option group</b> the <b>hide option</b> takes precedence|![](resources/images/../../admin/icon-wip.png)|![](resources/images/../../admin/icon-wip.png)||
|Hide Option Group|Hide all options in a given option group and data element/tracked entity attribute. When combined with <b>show option group</b> the <b>hide option</b> takes precedence |![](resources/images/../../admin/icon-wip.png)|![](resources/images/../../admin/icon-wip.png)||
|Show option group|Used to show only options from a given option group in a given data element/tracked entity attribute. To show an option group implicitly hides all options that is not part of the group(s) that is shown.|![](resources/images/../../admin/icon-wip.png)|![](resources/images/../../admin/icon-wip.png)||
|Assign Value|Assigns a value to a specified data element or attribute if the rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Show Warning|Shows pop-up warning to the user if rule is true; does not prevent the user from continuing.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Warning on Complete|Shows a pop-up warning to the user if, at the point &lsquo;complete&rsquo; is clicked, a rule is true; this does not prevent the user from continuing.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Show Error|Shows a pop-up error message to the user as soon as a rule is true, and prevents user from continuing until rule is no longer true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|The field will be cleared out so that the user is unable to store a value unless it meets the criteria of the program rule.||
|Error on Complete|Shows a pop-up warning to the user if, when "complete"; is clicked, a rule is true, and prevents user from continuing until rule is no longer true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Make Field Mandatory|Sets a data element as "mandatory"; if rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Display Text (Event Programs)|Used to display information that is not an error or a warning, for example feedback.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)| Independently from the source variable type, text will be displayed in the form as the last element of the last section. Text will be displayed as the messages in the indicators tab.||
|Display Text (Tracker Programs)|Used to display information that is not an error or a warning, for example feedback.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|<b>1. Program rule configured as "Trigger rule only for program stage".</b> Text will be displayed ONLY in the form as the last element of the last section. Text will be displayed as the messages in the indicators tab. </br>-> If the program rule uses any variable type which is not from the current stage, the rule will not be able to evaluate and the message will not be shown.</br><b>2. Program rule NOT configured as "Trigger rule only for program stage".</b> Text will be displayed ONLY in the indicators tab and NOT in the form.</br>--> If the program rule uses any variable of type Current event, the rule will not be able to evaluate and the message will not be shown.||
|Display Key Value/Pair (Event Programs)|Used to display information drawn from a data element.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|<b>Variable Type:</b> </br>* Data element from the newest event in the current program</br>* Data element from previous event</br>* Data element in current event</br>* Built-in variable</br>Key/Value Pair will be displayed in the form ONLY in the specified section.||
|Display Key Value/Pair (Traker Programs)|Used to display information drawn from a data element.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|<b>1. Variable Type:</b></br>* Data element in current event</br>Key/Value Pair will be displayed in the form ONLY in the specified section.</br><b>2. Variable Type:</b></br>* Data element from the newest event in the current program</br>* Data element from previous event</br>* Data element from the newest event for a program stage</br>* Tracked entity attribute</br>* Built-in variable</br>Key/Value Pair will be displayed ONLY in the indicators tab and NOT in the form.||
|Hide Program Stage|Hides a whole program stage from the user if the rule is true.|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|Action rule only supported for <b>Data element from the newest event in the current program type and tracked entity </b> attribute variables.||
|Send Message|Send Message triggers a notification based on provided message template.This action will be taken whenever there is a change in data value. However this behaviour can be controlled by providing event-enrollment status in program rule expression|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|This feature is executed on the server side.||
|Schedule Message|Schedule Message will schedule notification at date provided by Expression in the data field.|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|This feature is executed on the server side.||

## Program rule Actions supported (Other variables)

| Action| Description of Action| Data Element from the Newest Event in the Current Program (with registration)|Data Element from the Newest Event in the Current Program (without registration)| Data Element from Previous Event (with registration) |Data Element from Previous Event (without registration)| Data Element from the Newest Event for a Program Stage (with registration)|Tracked Entity Atribute (with registration) |Notes on implementation|
|-|---|:-:|:-:|:-:|:-:|:-:|:-:|---|
|Hide Field|Hides an individual data element if the rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Hide Section|Hides a whole section and its data elements if the rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Hide Option|Hide a single option for an option set in a given data element/tracked entity attribute. When combined with <b>show option group</b> the <b>hide option</b> takes precedence.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Hide Option Group|Hide all options in a given option group and data element/tracked entity attribute.When combined with show option group the hide option takes precedence.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Assign Value|Assigns a value to a specified data element or attribute if the rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Show Warning|Shows pop-up warning to the user if rule is true; does not prevent the user from continuing.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Warning on Complete|Shows a pop-up warning to the user if, at the point "complete" is clicked, a rule is true; this does not prevent the user from continuing.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-na.png)||
|Show Error|Shows a pop-up error message to the user as soon as a rule is true, and prevents user from continuing until rule is no longer true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|The rule will let the user to finish the enrollment but will prevent from completing the events until rule is no longer true. The field will be cleared out so that the user is unable to store a value unless it meets the criteria of the program rule.||
|Error on Complete|Shows a pop-up warning to the user if, at the point "complete" is clicked, a rule is true; this does not prevent the user from continuing.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-na.png)||
|Make Field Mandatory|Sets a data element as "mandatory" if rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)||
|Display Text (Event Programs)|Used to display information that is not an error or a warning, for example feedback.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|| Independently from the source variable type, text will be displayed in the form as the last element of the last section. Text will be displayed as the messages in the indicators tab.||
|Display Text (Tracker Programs)|Used to display information that is not an error or a warning, for example feedback.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|<b>1. Program rule configured as "Trigger rule only for program stage".</b> Text will be displayed ONLY in the form as the last element of the last section. Text will be displayed as the messages in the indicators tab. </br>-> If the program rule uses any variable type which is not from the current stage, the rule will not be able to evaluate and the message will not be shown.</br><b>2. Program rule NOT configured as "Trigger rule only for program stage".</b> Text will be displayed ONLY in the indicators tab and NOT in the form.</br>--> If the program rule uses any variable of type Current event, the rule will not be able to evaluate and the message will not be shown.||
|Display Key Value/Pair (Event Programs)|Used to display information drawn from a data element.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|<b>Variable Type:</b> </br>* Data element from the newest event in the current program</br>* Data element from previous event</br>* Data element in current event</br>* Built-in variable</br>Key/Value Pair will be displayed in the form ONLY in the specified section.||
|Display Key Value/Pair (Traker Programs)|Used to display information drawn from a data element.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-complete.png)|<b>1. Variable Type:</b></br>* Data element in current event</br>Key/Value Pair will be displayed in the form ONLY in the specified section.</br><b>2. Variable Type:</b></br>* Data element from the newest event in the current program</br>* Data element from previous event</br>* Data element from the newest event for a program stage</br>* Tracked entity attribute</br>* Built-in variable</br>Key/Value Pair will be displayed ONLY in the indicators tab and NOT in the form.||
|Hide Program Stage|Hides a whole program stage from the user if the rule is true.|![](resources/images/../../admin/icon-complete.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-complete.png)|Action rule only supported for <b>Data element from the newest event in the current program variable </b> type. If the event is auto-generated, the rule will not apply.||
|Send Message|Send Message triggers a notification based on provided message template.This action will be taken whenever there is a change in data value. However this behaviour can be controlled by providing event-enrollment status in program rule expression|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|This feature is executed on the server side.||
|Schedule Message|Schedule Message will schedule notification at date provided by Expression in the data field.|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|![](resources/images/../../admin/icon-na.png)|This feature is executed on the server side.||

## Functions to use in program rule expressions

| Function   | Description of function | Status        | Notes on implementation |
| ---- | ----------------------------- | :----: | -- |
| d2:ceil    | Rounds the input argument up to the nearest whole number.   | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:floor   | Rounds the input argument down to the nearest whole number. | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:round   | Rounds the input argument to the nearest whole number.      | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:modulus | Produces the modulus when dividing the first with the second argument.        | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:zing    | Evaluates the argument of type number to zero if the value is negative, otherwise to the value itself.   | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:oizp    | Evaluates the argument of type number to one if the value is zero or positive, otherwise to zero.        | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:concatenate      | Produces a string concatenated string from the input parameters. Supports any number of parameters.      | ![](resources/images/../../admin/icon-complete.png)    |Use d2:concatenate function instead of using "+" as the expression evaluator in the app will be adding numbers if it can.||
| d2:daysBetween      | Produces the number of days between the first and second argument. If the second argument date is before the first argument,  the return value will be the negative number of days between the two dates. The static date format is 'yyyy-MM-dd'.        | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:weeksBetween     | Produces the number of full weeks between the first and second argument. If the second argument date is before the first argument,  the return value will be the negative number of weeks between the two dates. The static date format is 'yyyy-MM-dd'. | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:monthsBetween    | Produces the number of full months between the first and second argument. If the second argument date is before the first argument the return value will be the negative number of months between the two dates. The static date format is 'yyyy-MM-dd'. | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:yearsBetween     | Produces the number of years between the first and second argument. If the second argument date is before the first argument, the return value will be the negative number of years between the two dates. The static date format is 'yyyy-MM-dd'.       | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:addDays | Produces a date based on the first argument date, adding the second argument number of days.    | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:count   | Counts the number of values that is entered for the source field in the argument.      | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:countIfValue     | Counts the number of matching values that is entered for the source field in the first argument. Only occurrences that matches the second argument is counted. | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:countIfZeroPos   | Counts the number of values that is zero or positive entered for the source field in the argument. The source field parameter is the name of one of the defined source fields in the program.      | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:hasValue         | Returns the number of numeric zero and positive values among the given object arguments. Can be provided with any number of arguments.       | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:validatePattern  | Evaluates to true if the input text is an exact match with the supplied regular expression pattern. The regular expression needs to be escaped.       | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:left    | Evaluates to the left part of a text, num-chars from the first character.     | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:right   | Evaluates to the right part of a text, num-chars from the last character.     | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:substring        | Evaluates to the part of a string specified by the start and end character number.     | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:split   | Split the text by delimiter, and keep the nth element (0 is the first).       | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:length  | Find the length of a string.     | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:zpvc    | Returns the number of numeric zero and positive values among the given object arguments. Can be provided any number of arguments.   | ![](resources/images/../../admin/icon-complete.png)    | |
| d2:inOrgUnitGroup\* | Evaluates whether the current organization unit is in the argument group. The argument can be defined with either ID or organization unit group code. | ![](resources/images/../../admin/icon-complete.png) | |
| d2:hasUserRole\** |Returns true if the current user has this role otherwise false.| ![](resources/images/../../admin/icon-complete.png) | |
| d2:zScoreWFA\*** |Function calculates z-score based on data provided by WHO weight-for-age indicator. Its value varies between -3.5 to 3.5 depending upon the value of weight.| ![](resources/images/../../admin/icon-complete.png) | |

> Notes:
>
> \* Available in DHIS 2 v2.30
> 
> \** Available in DHIS 2 v2.31 onwards
> 
> \*** Available in DHIS 2 v2.32

## Standard variables to use in program rule expressions

Available in DHIS2 v2.30

| Variable     | Description of function       | Status | Notes on implementation |
| --- | -------------------------------------------- | :---: | -- |
| V{current_date}       | Contains the current date whenever the rule is executed. | ![](resources/images/../../admin/icon-complete.png)      | |
| V{event_date}         | Contains the event date of the current event execution. Will not have a value at the moment the rule is executed as part of the registration form. | ![](resources/images/../../admin/icon-complete.png)      | |
| V{event_status}         | Contains status of the current event or enrollment. | ![](resources/images/../../admin/icon-complete.png)      | |
| V{due_date} \*        | This variable will contain the current date when the rule is executed. Note: This means that the rule might produce different results at different times, even if nothing else has changed.     | ![](resources/images/../../admin/icon-complete.png)      | |
| V{event_count}        | Contains the total number of events in the enrollment.   | ![](resources/images/../../admin/icon-complete.png)      | |
| V{enrollment_date} \* | Contains the enrollment date of the current enrollment. Will not have a value for single event programs.       | ![](resources/images/../../admin/icon-complete.png)      | |
| V{incident_date} \*   | Contains the incident date of the current enrollment. Will not have a value for single event programs.         | ![](resources/images/../../admin/icon-complete.png)      | |
| V{enrollment_id} \*   | Universal identifier string(UID) of the current enrollment. Will not have a value for single event programs.   | ![](resources/images/../../admin/icon-complete.png)      | |
| V{event_id}  | Universal identifier string(UID) of the current event context. Will not have a value at the moment the rule is executed as part of the registration form.   | ![](resources/images/../../admin/icon-complete.png)      | |
| V{orgunit_code}       | Contains the code of the orgunit that is linked to the current enrollment. For single event programs the code from the current event Org Unit will be used instead.  | ![](resources/images/../../admin/icon-complete.png)      | |
| V{environment}        | Contains a code representing the current runtime environment for the rules. The possible values is "WebClient", "AndroidClient" and "Server". Can be used when a program rule is only supposed to run in one or more of the client types.    | ![](resources/images/../../admin/icon-complete.png)      | |
| V{program_stage_id}   | Contains the ID of the current program stage that triggered the rules. This can be used to run rules in specific program stages, or avoid execution in certain stages. When executing the rules in the context of a TEI registration form the variable will be empty.   | ![](resources/images/../../admin/icon-complete.png)      | |
| V{program_stage_name} | Contains the name of the current program stage that triggered the rules. This can be used to run rules in specific program stages, or avoid execution in certain stages. When executing the rules in the context of a TEI registration form the variable will be empty. | ![](resources/images/../../admin/icon-complete.png)      | |

> Notes:
> 
> \* Only applies to tracker

## Differences between the Program Rules in the web and the Android version

As the web and the Android application are currently using a different *program rule engine* there might be programs rule that work in one system and not in the other. In general terms it can be said that the Android *program rule engine* is more strict and so, some Program Rules that work in the web version of DHIS2 will fail in Android. This subsection describes the main differences and how to adapt the rules in order to have them working in both systems.

### Evaluation of type Boolean

DHIS2 web version considers the type boolean as 0 or 1 (which can be evaluated to true or false), however Android evaluates them only as true or false. While this makes possible the addition of booleans in web, it will fail in Android; in order to fix this an additional *program rule variable* is needed to transform the boolean into an number that can be operated. Check the table below for examples and possible solutions.

For the examples belows consider the following:

* yn_prv1: is a program rule variable that has been configured to get the value of a 'Yes/No' data element
* yn_prv2: is a program rule variable that has been configured to get the value of a 'Yes/No' data element
* prv_boolean_one: is a program rule variable that has been configured to get the value of a 'Yes/No' data element
* prv_boolean_two: is a program rule variable that has been configured to get the value of a 'Yes/No' data element
* prv_boolean_one_to_number: is a program rule variable with calculated value
* prv_boolean_two_to_number: is a program rule variable with calculated value
* sometimes true is used as program rule condition meaning the action is always performed
* The following acronyms are used: 
	* DE (Data Elemetn)
	* PR (Program Rule)
	* PRE (Program Rule Expression)
	* PRC (Program Rule Condition)
	* PRV (Program Rule Variable)
	* PRA (Program Rule Action)


| Program Rule Condition(s) | Program Rule Action(s) | Web version | Android version | Comment |
| ----------- | ----------- | :---: | :---: | ----- |
| d2:hasValue('yn_prv1') \|\| d2:hasValue('yn_prv2') | Assign fixed value to DE | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | |
| #{yn_prv1} \|\| #{yn_prv2} | Assign fixed value to DE | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | |
| d2:hasValue('yn_prv1') \|\| d2:hasValue('yn_prv2') | Assign value to DE: #{yn_prv1} + #{yn_prv2} + 1 | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-negative.png) | Crashes in Android  whenver a boolean is marked as the expression would result in *true*+*false*+1 |
| d2:hasValue('yn_prv1') \|\| d2:hasValue('yn_prv2') | Assign value to DE: #{yn_prv1} + #{yn_prv2} + 1 | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-negative.png) | Crashes in Android  whenver a boolean is marked as the expression would result in *true*+*false*+1 |
| PR1: #{prv_boolean_one} <br /><br />PR2: #{prv_boolean_two} <br /><br />PR3: #{prv_boolean_one} \|\| #{prv_boolean_two} | PRA1. Assign value  "1" to PRV "#{prv_bool_one_to_number}" <br /><br />PRA2. Assign value: "1" to PRV "#{prv_bool_two_to_number}" <br /><br />PRA3. Assign value to DE: "#{prv_bool_one_to_number} + #{prv_bool_two_to_number} + 1"| ![](resources/images/../../admin/icon-negative.png) | ![](resources/images/../../admin/icon-negative.png) | There are 2 variables for boolean, one gets the value via a PRV definition “value form DE” and the other one via a PRA. If a boolean is not marked it is counted as string instead of a number |
| Four PR to assign 1 or 0 to the booleans and an additional for the addition. Priorities go from top to bottom <br /><br />PRC1: !d2:hasValue('prv_boolean_one')  \|\| !#{prv_boolean_one} <br /><br />PRC2: d2:hasValue('prv_boolean_one') && #{prv_boolean_one}<br /><br />PRC3: !d2:hasValue('prv_boolean_two')  \|\| !#{prv_boolean_two} <br /><br />PRC4: d2:hasValue('prv_boolean_two') && #{prv_boolean_two} <br /><br />PRC5: true | PRA1: Assign value: "0" to PRV "#{prv_bool_one_to_number}" <br /><br />PRA2: Assign value: "1" to PRV "#{prv_bool_one_to_number}" <br /><br />PRA3: Assign value: "0" to PRV "#{prv_bool_two_to_number}" <br /><br />PRA4: Assign value: "1" to PRV "#{prv_bool_two_to_number}" <br /><br />PRA5: Assign value: "#{prv_bool_one_to_number} + #{prv_bool_two_to_number} + 1" to DE <br /> | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | There are 2 variables for boolean, one gets the value via a PRV definition “value form DE” and the other one via a PRA.

### Evaluation of numbers

DHIS2 web version evaluate numbers in a more flexible way casting values from integer to floats if required for a division, however, Android take numbers as such (without a casting) which my end up giving unexpected results. Check the table below for examples and possible solutions.


| Program Rule Condition(s) | Program Rule Action(s) | Web version | Android version | Comment |
| ----------- | ----------- | :---: | :---: | ----- |
| true | Assign value to DE: d2:daysBetween('2020-05-13', '2020-05-17') / 3 | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-negative.png) | The user would expect the division to be calculated as 4/3 with a result of 1.3333. However, Android does not cast 4 to a float (4.0 as the web version does) so the result in Android is a pure 1 as the result of the integer division 4/3 |
| true | Assign value to DE: d2:daysBetween('2020-05-13', '2020-05-17') / 3.0 | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | Division results in 1.33333 in both web and Android | 

---
