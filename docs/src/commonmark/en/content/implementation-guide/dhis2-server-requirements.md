# DHIS 2 Server Requirements

The new DHIS 2 Capture Android App requires a DHIS 2 2.29 or greatenstance running in a web server. The DHIS 2 instance can reside on-premise server, a virtual machine or it can be purchased aoftware-as-a-service (managed hosting). For more information about thifferent DHIS 2 hosting options please visit [https://www.DHIS2.org/hosting](https://www.dhis2.org/hosting).

This section provides basic guidelines on how to configure the DHIS erver, which you will need to do in the first two scenarios (on-premisnd virtual machine). In the third scenario of managed hosting, yohould let your provider know that you will be deploying the Android Apnd have an open discussion on best ways to configure the server. Yoould start by sharing these guidelines with your managed hostinrovider.

The DHIS 2 Server must be designed and configured keeping in mind: datollection flow, expected data analysis and expected visual UI. At inimum three servers will be needed for a DHIS 2 deployment: Testing,
Production and Training.

The Testing Server will be the server where you can change the serveonfigurations and test the results of such configurations. Once you arappy with the configuration, training of users should occur in anvironment different to Production. A dedicated Training Server is thdeal environment in which you will train your users. You will creatHIS 2 users for all the trainees and make sure everyone understands aneels comfortable with the changes. The last step once you have testehe configurations and trained the users will be to deploy thonfiguration to the Production environment. You should never makonfiguration changes or train your users directly into the Productionvironment.

DHIS 2 is licensed under [BSD](http://www.linfo.org/bsdlicense.html), an open source license and is free for everyone to install and use.  However, managing a DHIS 2 instance involves more than setting up owerful web server. Deploying a reliable and scalable system includet least these aspects:

- Human resources with skills in relevant technologies such as web servers and database systems.
- Reliable backup of your system including safe storage at a remote server.
- Use of SSL (HTTPS / encryption) to keep private information like passwords secure.
- Monitoring of server resources and application performance.
- Stable and high-speed Internet connectivity.
- Stable power supply including a backup power solution.
- Secure server environment to avoid unauthorized access, theft and fire.
- Powerful hardware with potential for scaling together with increased system usage.

The DHIS 2 Capture Android App runs in mobile devices, includinmartphones, tablets and chromebooks. It is important to keep an eye ohe number of programs, number of data elements and number of prograules that are made available to a user on those mobile devices. Yohould also budget sufficient time for creating the necessarranslations for your metadata configuration. For the app dialogues, menus and other prompts, if the app is not translated to the languaghat you need, please send us a message in the [DHIS 2 community](https://community.dhis2.org) and we will let you know how to contribute to the app translations.

