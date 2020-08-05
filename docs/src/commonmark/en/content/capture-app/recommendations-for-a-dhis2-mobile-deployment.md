# Recommendations for a DHIS 2 mobile deployment

<!-- DHIS2-SECTION-ID:recommendations -->

If you plan to deploy the DHIS2 Android App in the field, we strongly recommend you to read the [Mobile Implementation Guidelines](https://docs.dhis2.org/master/en/dhis2_android_implementation_guideline/about-this-guide.html) published by UiO. It includes chapters on technology requirements, security and configuration aspects, and testing and roll out recommendations. Below you will find some key aspects briefly introduced, we recommend reading the extended document.

## Mobile device specifications

<!-- DHIS2-SECTION-ID:recommendations_mdm -->

The Android App is compatible and supported for DHIS 2 versions 2.30, 2.31, 2.32, and 2.33. And has no breaking changes with 2.29.

It requires a device that is running Android v4.4 or higher.

In [the specific section of the Mobile Implementation Guidelines](https://docs.dhis2.org/master/en/dhis2_android_implementation_guideline/mobile-device-specifications.html) you will find recommendations for new mobile device acquisitions for a Dhis2 Android deployment.

## Testing and Piloting

<!-- DHIS2-SECTION-ID:recommendations_testing -->

If you plan to deploy the DHIS 2 Android App in the field, you should first do a full round of testing of the app in you own configuration.

The app has been extensively tested with the demo servers, and during Beta testing it was tested against some real configurations as well. We know, however, that every DHIS 2 configuration is special in many senses, and might cause inconsistencies that we have not being able to identify.

It is strongly advised to carry out a comprehensive testing of the app in your own server before piloting it.

## How to migrate to Android Capture App

<!-- DHIS2-SECTION-ID:recommendations_migrate -->

If you are ready for deploying the new Android App in the field and your users are already using Event Capture or Tracker Capture, you should follow these steps:

1.  Sync data of the current app you are using

    > **Warning**
    >
    > Deleting the app without syncing can cause information loss.
    
2.  Download and install the DHIS 2 Android App
3.  Login using your credentials and all data will be synced.
