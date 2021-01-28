# Comparison Chart
The following chart (adapted from [List of Mobile Device Management software](https://en.wikipedia.org/wiki/List_of_Mobile_Device_Management_software)) aims to summarize all the content in this document and can be helpful to have a quick overview. However it is recommended to go through the whole document to understand all the advantages and disadvantages of the MDM proposed here.

All the presented solutions comply with the previously defined as required features:

* Android as supported platform
* Minimum Features:
	* App management
	* Remote wipe
	* Security enforcement (min. password)
* Price is very approximate as it would depend on specific configurations.

|||||||
|--- |--- |------ |------ |------ |-------- |
|**MDM Name** <br />(tested)| **(C)loud / (P)remises**|**Price approx.**|**Strengths**|**Weaknesses**|**Additional Comments**|
|<u>Flyve MDM</u>|C / P| - Free* (if hosted) <br />- 350 $ / month (no limit on devices)| - Open Source <br />-GLPI plugin| - Set of features| - If GLPI is already used this can be a really interesting option <br />-Supported features might be a limitation|
|<u>Headwind MDM</u>|C / P| - Free* (if hosted) <br /> - 1990 USD / 1st year + 500 USD every other year| - Open Source <br />- Java App (like DHIS 2)|Free version might omit some features||
|<u>Entgra EMM</u>|C/P|- Free* (if hosted) <br />- Price for SaaS not discosed|- Open Source<br />- Java App (like DHIS2)|||
|<u>TinyMDM</u>|C|22 $ / device / year.|- Easy to use<br />- Simple but powerful features<br />- Android Zero configuration||- The Android Zero configuration can help while deploying in a large number of devices.<br /> -Customer support offered discount|
|<u>Miradore</u>|C|24 $ / device / year|- NFC and mass provision<br />- Android Zero|Full potential with Samsung devices|When tested the admin console was a bit slow.<br />- Discounts available|
|<u>Scale Fusion</u>|C|24 - 36 $ / device / yea|- Mass provision <br />- Android Zero <br />- Lot of features<br />- Remote cast<br />- Remote support chat & calls||Previously known as MobiLock|
|<u>Manage Engine</u>|C / P|- 10-24 $ / device / year<br />- Free for <25 devices|- Lot of features and options<br /> - Android Zero<br /> - Remote chat & cast||The on-premises version requires Windows, this can be a strength or a weakness considering the current architecture although DHIS2 is only supported in Linux so another server would be mandatory.|
|**MDM Name** <br />(**not** tested)| **(C)loud / (P)remises**|**Price approx.**|**Strengths**|**Weaknesses**|**Additional Comments**|
|Air Watch|C|4 $ / device / month|||Complies with the required and nice to have features|
|BlackBerry UEM 12.12|C / P|?|||Complies with the required and nice to have features|
|AppTech360|C / P|2 $ / device / month|||Complies with the required and nice to have features|
|Hexnode|C / P|1 $ / device / month|||Complies with the required and nice to have features|
|Kaspersky Endpoint Security|C / P|?|||Complies with the required and nice to have features <br />Might be worth exploring if this solution is already in place|
|Ivanti|C|?|||Complies with the required and nice to have features|
|MaaS360|C|4 $ / device / month|||Complies with the required and nice to have features|
|MobileIron|C / P|?|||Complies with the required and nice to have features|
|Cisco Meraki  Systems Manager|C|?|||Complies with the required and nice to have features<br /> Might be worth exploring if this solution is already in place for network devices|
|SureMDM|C / P|4 $ / device / month|||Complies with the required and nice to have features|
|Citrix Endpoint Management|C / P|?|||Complies with the required and nice to have features|
|**MDM Name** <br />(Other account managers)| **(C)loud / (P)remises**|**Price approx.**|**Strengths**|**Weaknesses**|**Additional Comments**|
|Microsoft InTune|C|6 $ / device / month|Robust|Really expensive if only as MDM|This is different from MDM for Office 365 which provides a smaller set of features (not including App Management). <br />Might be the ideal solution if the project is already using Microsoft (E3 or E5) as it is included. Otherwise should probably not be considered.|
|Endpoint Management (Google G Suite)|C|6 - 25$ / device / month| - Easy to deploy <br />- Robust|Really expensive if only as MDM|Might be the ideal solution if the project is already using Google G Suite. Otherwise should probably not be consider.|

>
> **Note**
> 
> <u>Underlined MDMs</u> have been tested with DHIS2 Android App, non-underlined have been included here based on research or usage with other App.
>
> When used the word Free* In the costs it is not considered the running costs of an online / premises server as it is considered that this cost is already part of the DHIS2 implementation. Although it might be recommended to have the services running on different servers.
