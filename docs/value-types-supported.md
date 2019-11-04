# Value types supported

The following is a comprehensive list of all value types available in DHIS 2, and notes on whether or not these have been implemented in the Android Capture app.

Any issues around using a particular feature with Android are highlighted with an exclamation mark \!.


|||
| :-: | :------ |
| ✓ | Value type implemented |
| &ndash; | Value type not implemented, but will be safely ignored (if not compulsory) |
| ![](resources/images/image3_icon.png) | Work in progress. Feature not completely implemented yet or with unexpected behaviour already reported |


| Value type | Description of value type | Program with registration | Program with registration | Program without registration | Notes on implementation |
| :-- | :---- | :-: | :-: | :-: | :-- |
| | | **Attributes** | **Data Elements** | **Data Elements** |
| Time | Time only | ✓ | ✓ | ✓ | |
| Date & Time | Date plus time | ✓ | ✓ | ✓ | |
| Date | Date only | ✓ | ✓ | ✓ |  |
| Age | Enables entry of either an age in years/months/days or a date-of-birth (both are stored as date-of-birth) | ✓ | ✓ | ✓ | |
| Phone number | A valid phone number | ✓ | ✓ | ✓ | |
| Email | An email address in a valid format | ✓ | ✓ | ✓ | |
| Yes/no | Boolean yes/no (or no response) | ✓ | ✓ | ✓ | |
| Yes only | Yes or no response | ✓ | ✓ | ✓ | |
| Number | Any valid number, including decimals | ✓ | ✓ | ✓ | |
| Integer | Any integer (whole numbers, no decimals) | ✓ | ✓ | ✓ | |
| Positive Integer | Only positive integers (no zero or negative values) | ✓ | ✓ | ✓ | |
| Positive or Zero Integer | Only zero or positive integers (no negative values) | ✓ | ✓ | ✓ | |
| Negative Integer | Only negative integers (no zero or positive values) | ✓ | ✓ | ✓ | |
| Percentage | Any decimal value between 0 and 100 | ✓ | ✓ | ✓ | |
| Unit interval | Any decimal value between 0 and 1 | ✓ | ✓ | ✓ | |
| Text | Text (length of text up to 50,000 characters) | ✓ | ✓ | ✓ | |
| Long text | Text (no constraints on length) | ✓ | ✓ | ✓ | |
| Letter | A single letter | ✓ | ✓ | ✓ | |
| File | Enables upload of files in various formats (requires appropriate storage to be configured) | ![](resources/images/image3_icon.png) | ![](resources/images/image3_icon.png) | ![](resources/images/image3_icon.png) | |
| Organisation unit | Enables selection of a DHIS2 organisation unit as the chosen value | ✓ | ✓ | ✓ | |
| Tracker Associate | Enables selection of an existing Tracker 'tracked entity instance' (e.g. a person) as the value | &ndash; | &ndash; | &ndash; | |
| Username | Enables selection of a valid DHIS2 username as the value | ![](resources/images/image3_icon.png) | ![](resources/images/image3_icon.png) | ![](resources/images/image3_icon.png) | |
| Coordinate | Enables manual entry of geographical coordinates (doesn't enable automatic capture of coordinates) | ✓ | ✓ | ✓ | |
| URL | Enables manual entry of a URL. | ✓ | ✓ | ✓ | |
| Image | Enables upload of images. | ✓ | ✓ | ✓ | |
