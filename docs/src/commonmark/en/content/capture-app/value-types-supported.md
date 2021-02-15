# Value types supported

<!-- DHIS2-SECTION-ID:valuetypes -->

The following is a comprehensive list of all value types available in DHIS 2, and notes on whether or not these have been implemented in the Android Capture app.

Any issues around using a particular feature with Android are highlighted with an exclamation mark \!.

|||
| :-: | :------ |
| ![](resources/images/../../admin/icon-complete.png) | Value type implemented |
| ![](resources/images/../../admin/icon-incomplete.png) | Value type not implemented, but will be safely ignored (if not compulsory) |
| ![](resources/images/../../admin/icon-wip.png) | Work in progress. Feature not completely implemented yet or with unexpected behaviour already reported |


| Value type | Description of value type | Program with registration | Program with registration | Program without registration | Data Set| Notes on implementation |
| :-- | :---- | :-: | :-: | :-: | :-: | :-- |
| | | **Attributes** | **Data Elements** | **Data Elements** | **Data Elements** |
| Time | Time only | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Date & Time | Date plus time | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| Date | Date only | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | |
| Age | Enables entry of either an age in years/months/days or a date-of-birth (both are stored as date-of-birth) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Phone number | A valid phone number | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Email | An email address in a valid format | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Yes/no | Boolean yes/no (or no response) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| Yes only | Yes or no response | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)|
| Number | Any valid number, including decimals | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| Integer | Any integer (whole numbers, no decimals) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| Positive Integer | Only positive integers (no zero or negative values) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| Positive or Zero Integer | Only zero or positive integers (no negative values) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| Negative Integer | Only negative integers (no zero or positive values) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Percentage | Any decimal value between 0 and 100 | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| Unit interval | Any decimal value between 0 and 1 | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Text | Text (length of text up to 50,000 characters) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Long text | Text (no constraints on length) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Letter | A single letter | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| File | Enables upload of files in various formats (requires appropriate storage to be configured) | ![](resources/images/../../admin/icon-wip.png) | ![](resources/images/../../admin/icon-wip.png) | ![](resources/images/../../admin/icon-wip.png) | ![](resources/images/../../admin/icon-incomplete.png) | |
| Organisation unit | Enables selection of a DHIS2 organisation unit as the chosen value | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |  ![](resources/images/../../admin/icon-incomplete.png)| |
| Tracker Associate | Enables selection of an existing Tracker 'tracked entity instance' (e.g. a person) as the value | ![](resources/images/../../admin/icon-incomplete.png) | ![](resources/images/../../admin/icon-incomplete.png) | ![](resources/images/../../admin/icon-incomplete.png) | ![](resources/images/../../admin/icon-incomplete.png) | |
| Username | Enables selection of a valid DHIS2 username as the value | ![](resources/images/../../admin/icon-wip.png) | ![](resources/images/../../admin/icon-wip.png) | ![](resources/images/../../admin/icon-wip.png) |  ![](resources/images/../../admin/icon-incomplete.png) |
| Coordinate | Enables manual entry of geographical coordinates (doesn't enable automatic capture of coordinates) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png)| |
| URL | Enables manual entry of a URL. | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) |![](resources/images/../../admin/icon-complete.png) | |
| Image | Enables upload of images. | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-complete.png) | ![](resources/images/../../admin/icon-incomplete.png) | |

