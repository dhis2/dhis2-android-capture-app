# DHIS 2 Server Requirements

<!--DHIS2-SECTION-ID:server_requirements-->

The new DHIS 2 Capture Android App requires a DHIS 2 2.29 or greater instance running in a web server. The DHIS 2 instance can reside on-premise server, a virtual machine or it can be purchased as software-as-a-service (managed hosting). For more information about thifferent DHIS 2 hosting options please visit [https://www.DHIS2.org/hosting](https://www.dhis2.org/hosting).

This section provides basic guidelines on how to configure the DHIS 2 server, which you will need to do in the first two scenarios (on-premis and virtual machine). In the third scenario of managed hosting, you should let your provider know that you will be deploying the Android App and have an open discussion on best ways to configure the server. You should start by sharing these guidelines with your managed hosting provider.

The DHIS 2 Server must be designed and configured keeping in mind: data collection flow, expected data analysis and expected visual UI. At a minimum three servers will be needed for a DHIS 2 deployment: *Testing*, *Production* and *Training*.

The *Testing* Server will be the server where you can change the server configurations and test the results of such configurations. Once you are happy with the configuration, training of users should occur in an environment different to *Production*. A dedicated *Training* server is the ideal environment in which you will train your users. You will create DHIS 2 users for all the trainees and make sure everyone understands and feels comfortable with the changes. The last step once you have tested the configurations and trained the users will be to deploy the configuration to the *Production* environment. You should never make configuration changes or train your users directly into the *Production* environment.

DHIS 2 is licensed under [BSD](http://www.linfo.org/bsdlicense.html), an open source license and is free for everyone to install and use.  However, managing a DHIS 2 instance involves more than setting up a powerful web server. Deploying a reliable and scalable system includet least these aspects:

- Human resources with skills in relevant technologies such as web servers and database systems.
- Reliable backup of your system including safe storage at a remote server.
- Use of SSL (HTTPS / encryption) to keep private information like passwords secure.
- Monitoring of server resources and application performance.
- Stable and high-speed Internet connectivity.
- Stable power supply including a backup power solution.
- Secure server environment to avoid unauthorized access, theft and fire.
- Powerful hardware with potential for scaling together with increased system usage.

The DHIS 2 Capture Android App runs in mobile devices, including smartphones, tablets and Chromebooks. It is important to keep an eye on the number of programs, number of data elements and number of program rules that are made available to a user on those mobile devices. You should also budget sufficient time for creating the necessary translations for your metadata configuration. For the app dialogues, menus and other prompts, if the app is not translated to the language that you need, please send us a message in the [DHIS 2 community](https://community.dhis2.org) and we will let you know how to contribute to the app translations.
