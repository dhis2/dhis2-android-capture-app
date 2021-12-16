# Mobile Device Specifications { #implementation_guide_mobile_specs }


If your project plans to do a large acquisition of devices, it is good practice to delay the bulk of the acquisition as much as possible. The idea is to get the best device that you can afford. Technology, and particularly mobile devices, evolves very rapidly. A given model is normally refreshed on an annual cycle, giving consumers access to significant technical improvements year-on-year, but with similar price point. More recommendations on acquisitions can be found in the [<span class="underline">Scale Up</span>](#scale-up) section.

Specifications for mobile devices to use the DHIS 2 Android App deployment are included in the following table. Please, note that these recommendations are very generic as performance of the device will be hihgly impacted by your configuration. For example, having a tracker program with hundres of program rules will require a more powerful device than in an implementation where you are only collecting a small set of aggregate data.

In general terms when having to choose different versions of Android aim for the higher. Also, acquiring devices from well known brands might be an indicator of having better after sales services like repairing and/or updates.

<table>
<thead>
<tr class="header">
<th></th>
<th><b>Mobile phones</b></th>
<th><b>Tablets</b></th>
<th><b>Chromebooks</b></th>
</tr>
</thead>
<tbody>
<tr>
<td><b>Construction</b></td>
<td colspan="3">Probably the most important feature: this device is going to be doing a lot of field work, and it needs to last 2+ years</td>
</tr>
<tr>
<td><b>Brand</b></td>
<td colspan="3">If you are going to be responsible for managing a lot of devices, it is easier to stick to one brand</td>
</tr>
<tr>
<td><b>OS</b></td>
<td colspan="2"> 
Minimum Supported: Android 4.4 (to be deprecated Apr 2022) <br />
Minimum Recommended for new devices: <b>Android 7.X</b> <br />
Recommended for new devices: <b>Android 8.X</b> or superior
</td>
<td>Chrome OS devices are updatable to the latest version of Chrome OS for at least 5 years after release. Check <a href="https://support.google.com/chrome/a/answer/6220366?hl=en"><span class="underline">here</span></a></td>
</tr>
<tr>
<td><b>Processor</b></td>
<td colspan="2">Recommended: 4 cores, 1.2GHz</td>
<td>various</td>
</tr>
<tr>
<td><b>RAM</b></td>
<td>
Minimum: 1Gb <br />
Recommended: 2Gb or more
</td>
<td>
Minimum: 1.5Gb<br />
Recommended: 3Gb or more
</td>
<td>
Minimum: 4Gb<br />
Recommended: 4-8Gb
</td>
</tr>
<tr>
<td><b>Storage</b></td>
<td colspan="2">
Minimum: 8Gb <br />
Recommended: 32Gb <br />
DHIS 2 app do not uses much space. However, storage of personal images & videos uses a lot of space
</td>
<td>
Minimum: 16Gb<br />
Recommended: 32-128Gb
</td>
</tr>
<tr>
<td><b>Screen Size</b></td>
<td>
Minimum: 4" <br />
Recommended: from 5.5"
</td>
<td>Minimum: 7"</td>
<td>11" - 14"</td>
</tr>
<tr>
<td><b>Camera</b></td>
<td colspan="2">
Minimum: 5Mpx, with flash <br />
Recommended: at least 8Mpx, flash
</td>
<td>optional</td>
</tr>
<tr>
<td>
<b>Accessories</b>
*Case, Keyboard, External power*
</td>
<td colspan="2">	
Consider an appropriate external cover and a screen protector. For tablets, consider an external keyboard for desk operation <br />
Consider supplying an external power bank (10,000 mAh - 20,000 mAh)
</td>
<td>
USB 3G/4G modem <br />
Mouse <br />
WebCam
</td>
</tr>
<tr>
<td><b>Connectivity</b></td>
<td colspan="2">
4G (LTE)/ 3G radio, <b>unlocked</b>. If importing devices, check the compatibility of frequency bands with local mobile operators <br />
Bluetooth 4.0 or better. WiFi 2.4 GHz &amp; 5 GHz
</td>
<td>
Bluetooth 4.0 or better. WiFi 2.4 GHz &amp; 5 GHz <br />
External USB 3G/4G dongle or Wifi hotspot <br />
</td>
</tr>
</tbody>
</table>

> **Note**
> 
> Please note that currently the DHIS2 Mobile application relies on some (Google Play Services)[https://developers.google.com/android/guides/overview] and therefore will not work on devices not running this service. This is common in late Huawei phones and AOSP devices. 
