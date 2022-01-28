# Recommendations for a DHIS 2 mobile deployment { #capture_app_recommendations }

If you plan to deploy the DHIS2 Android App in the field, we strongly recommend you to read the [Mobile Implementation Guidelines](https://docs.dhis2.org/master/en/dhis2_android_implementation_guideline/about-this-guide.html) published by UiO. It includes chapters on technology requirements, security and configuration aspects, and testing and roll out recommendations. Below you will find some key aspects briefly introduced, we recommend reading the extended document.

## Mobile device specifications { #capture_app_recommendations_mobile_specs }

The DHIS2 Android App is compatible and supported for DHIS 2 versions 2.35 to 2.37. And has no breaking changes with 2.30 to 2.34.

It requires a device that is running Android v4.4 (not recommended but supported until Apr 2022) or higher. The minimum recommended for new devices: Android 7 or higher.

In [the specific section of the Mobile Implementation Guidelines](https://docs.dhis2.org/master/en/dhis2_android_implementation_guideline/mobile-device-specifications.html) you will find recommendations for new mobile device acquisitions for a Dhis2 Android deployment.

## Testing and Piloting { #capture_app_recommendations_testing }

If you plan to deploy the DHIS 2 Android App in the field, you should first do a full round of testing of the app in you own configuration.

The app has been extensively tested with the demo servers, and during Beta testing it was tested against some real configurations as well. We know, however, that every DHIS 2 configuration is special in many senses, and might cause inconsistencies that we have not being able to identify.

It is strongly advised to carry out a comprehensive testing of the app in your own server before piloting it.
