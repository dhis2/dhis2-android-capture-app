# Recommendations for a DHIS 2 mobile deployment

If you plan to deploy the DHIS2 Android App in the field, we strongly recommend you to read the [Mobile Implementation Guidelines](https://s3-eu-west-1.amazonaws.com/content.dhis2.org/Publications/DHIS+2+Mobile+Implementation+Guidelines.pdf) published by UiO. It includes chapters on technology requirements, security and configuration aspects, and testing and roll out recommendations. Below you will find some key aspects briefly introduced, we recommend reading the extended document.

## Mobile device specifications

The Android App is compatible with DHIS 2 versions 2.29, 2.30 and 2.31.

It requires a device that is running Android v4.4 or higher.

In [this link](https://www.google.com/url?q=https://docs.google.com/document/d/1jZjw-hb1W8sszkPU9yPWrPoow91gEkTb0nyZJh3IJQQ/edit%23&sa=D&ust=1557433016128000) you will find recommendations for new mobile device acquisitions for a Dhis2 Android deployment.

## Testing and Piloting

If you plan to deploy the DHIS 2 Android App in the field, you should first do a full round of testing of the app in you own configuration.

The app has been extensively tested with the demo servers, and during Beta testing it was tested against some real configurations as well. We know, however, that every DHIS 2 configuration is special in many senses, and might cause inconsistencies that we have not being able to identify.

It is strongly advised to carry out a comprehensive testing of the app in your own server before piloting it.

## How to migrate to Android Capture App

If you are ready for deploying the new Android App in the field and your users are already using Event Capture or Tracker Capture, you should follow these steps:

1.  Sync data of the current app you are using

    > **Caution**
    >
    > Deleting the app without syncing can cause information loss.
    
2.  Download and install the DHIS 2 Android App
3.  Login using your credentials and all data will be synced.
