# Choosing an MDM/EMM

When deciding which MDM solution to choose it is important to define which set of features will be considered as required and which ones as nice to have. This can vary a lot between implementations; however, we have identified some features as mandatory in the list below due to the nature of DHIS2. While this can be reviewed depending on the implementation, these should be considered as our recommendation.

See Annex A - Mobile Device Management for further details.

Required features and their reason:

* Android as supported platform:

    This might seem obvious but some MDM solutions are aimed at other types of devices like iOS or Windows. At the moment the DHIS2 Android App is only compatible with Android devices, and it supports from version 4.4 (not recommended) and above (we recommend from version 7).

* Application(s) distribution management: 

    DHIS2 implementations need to test and train users before releasing a new App version. As most of the implementations install the DHIS2 Android App from the Google Play store, when an update is published there the devices could be updated without the project being ready if there is no other mechanism in place to manage updates.

* Device information:

    DHIS2 implementations need to maintain an inventory of their devices in order to troubleshoot issues or to update their devices. All the MDM solutions considered include this a basic feature but it is listed here just in case a solution exists that may not include this.

* Password enforcement:

    In most (if not all) DHIS2 implementations, sensitive information is stored in the application. Therefore enforcing a password policy on the device may prevent undesired access to this data.


    Note that despite the DHIS2 Android Application allowing the possibility to set a password for access control, because the information in the device is not yet encrypted (Feb 2020) it could still be extracted by an attacker.

* Remote wipe:

    In most (if not all) DHIS2 implementations sensitive information is stored in the application. If for instance a device is lost or stolen, ensuring that it can be remotely wiped can help prevent the leakage of sensitive data.


Nice to have features and their reason:

* Kiosk mode (AKA single app mode)

    Some DHIS2 implementations might require the devices to be locked down to a single application (DHIS2 Android Capture App) without allowing the user to access any other application or settings. A kiosk policy would achieve this.

* Phone management

    In some DHIS2 implementations it might be required to use devices with SIM cards in order to provide data connection over a mobile network (2G-5G). This might require the devices to use specific calling services in order to recharge data bundles or to limit call support, etc.

* Application/Settings restrictions

    Some DHIS2 implementations might require the users to be able to use not only one but several applications (i.e. a device that needs to be used for DHIS2 and picture capturing). 

* Network management

    Some DHIS2 implementations might require the devices to not use the data network, or limit to specific domains (firewalling) or to always use only specific wireless networks or setting up dynamically the wireless networks, etc. 

* User management

    Some DHIS2 implementations might require the devices to be used by several users (even two DHIS2 users). User management functionality can increase the level of security in this scenario, as each user could have different access codes, allowing multi user accounts for DHIS2 Android Capture App (currently not natively supported), several application policies per user, etc.

## Initial Price and Running Costs

One of the critical factors that projects will face while deciding if they want to implement an MDM is the initial price and the running costs. An MDM can bring unexpected costs so it is recommended to evaluate the need and include its costs as early as possible in the definition of the project and budget.

Most of the MDM solutions presented in the following sections include a monthly or annual running cost which might tremendously increase the cost of the project depending on the number of devices. Thus it is advise to consider some of the following tips:

1. If the project has the capacity to host the MDM solution on their servers it will generally present a better option than choosing a solution including hosting.
2. Some donors might impose choosing a specific MDM solution, if that is the case make sure that budget is allocated for future stages on the project or that the MDM can be used for free (or cheaper) with a limited set of options.
3. Most of the solutions offer different packages as a pricing model, if the solution will mainly (or only) be used to manage the DHIS2 application (installation and update) the usage will be minimal so choosing the cheapest alternative will most probably suffice.
4. Due to the nature of most of the projects (health in developing countries, NGO, education, etc) many MDM providers will probably be able to offer a discount. Negotiating before choosing a solution is highly recommended as while writing this document many providers showed interest and already offered better deals than the announced on their sites.

## BYOD / Corporate device

Another key factor while deciding which MDM/EMM to use is to consider if the deployment will include a BYOD (Bring Your Own Device) policy or will only work with corporate devices. This might be a critical factor as most of the MDM will differentiate in policies that can be applied to these two types of devices. Many DHIS2 implementations are based on corporate-only devices, but in some implementations a mixed BYOD-Corporate or even a full BYOD device policy could be possible.


A BYOD setup implies having an MDM that allows a minimum set of policies complying with the mandatory features listed above. Depending on the MDM this might require a work profile where the DHIS2 Application should be installed. In these implementations training might be even more important in order to explain the differences between the profiles. For example, a user with the DHIS2 Application installed in their personal profile (as well as on the work profile) would add additional data security risks, because any data stored in their personal profile could not be remotely wiped if the device was lost or stolen.


A Corporate device set-up will imply having all the devices under a stricter set of policies (can vary between devices/users/locations). This is the ideal situation from the IT management perspective but will impact flexibility and costs.
