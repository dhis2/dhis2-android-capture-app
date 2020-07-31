# Using SMS Gateways for reporting in DHIS2

<!--DHIS2-SECTION-ID:sms_report_sending-->

DHIS2 supports accepting data via [SMS](https://docs.dhis2.org/master/en/dhis2_user_manual_en/mobile.html), however, the SMS needs to be composed in a cryptic way to protect the information. The DHIS2 Android App acts as a transparent layer to send the information via SMS where the user does not have to worry about writing the SMS. To send SMSs with the Android App the SMS gateway need to be properly configured. This section explains the different options available and how to achieve that.

## Sending SMS

<!--DHIS2-SECTION-ID:sms_report_sening-->

It is important to clarify firstly, that this section mainly concerns the set up of **receiving SMS** (from mobile devices to the DHIS2 server), which is necessary when considering using the App to send (sync) information recorded in the app to the DHIS2 server via SMS. In the App this can be set-up under the *Settings* > *SMS Settings*

Sending SMS, i.e. from the DHIS2 server to mobile devices, is relatively simple to set up. If all that is required is the sending of notifications to users phones from DHIS2 when certain events occur (messaging, thresholds e.t.c.) only sending SMS is required.

This can all be configured in the SMS Service Configuration page within the [Mobile Configuration section](https://docs.dhis2.org/master/en/user/html/mobile_sms_service.html).

There is out of the box support for common providers such as *Bulk SMS* and *Clickatell*, and both providers support sending of SMS to numbers in most countries.

Note also, it is possible to use a different SMS Gateway for sending and receiving SMS. So even if you set up a solution for receiving SMS below, it is still possible to use one of the aforementioned solutions above for sending SMS.

## Using an Android device as SMS Gateway

<!--DHIS2-SECTION-ID:sms_report_android_gateway-->

The simplest solution by far is to use a dedicated Android device as your SMS Gateway. Any phone or tablet running Android OS (4.4, Kitkat or above) should be fine. It will require a constant internet connection, in order to forward messages to your DHIS2 server and it will also need a SIM card to receive the incoming SMS.

You’ll need to download and install the DHIS2 Android SMS Gateway app on the mobile device. See a list of [releases](https://github.com/dhis2/dhis2-sms-android-gateway/releases) where you can download the latest APK file to install. There are instructions on the app page itself, but essentially you’ll just need to start the app and enter the details of your DHIS2 server (URL, username and password).

Once this is set up and running, you then enter the phone number of this gateway device in the configuration page of any other mobile device using the DHIS2 Capture App. Then, when SMS are sent from these reporting devices, they will be received on the gateway device and automatically forwarded to the DHIS2 server where they will be processed.

**Using this gateway device is perfect when testing the SMS functionality.** It would be fine when piloting projects that require SMS reporting. As long as the device is plugged into a power supply and has a constant internet connection it works well for small scale projects.

However, when considering moving a project to production it would be necessary to investigate one of the more permanent and reliable solutions for gateways below.

### Sending SMS using an Android Device Gateway

This option is currently not supported nor documented.

## Dedicated SMS Gateways

<!--DHIS2-SECTION-ID:sms_report_dedicated_gateway-->

This section discusses the use of more permanent and dedicated SMS gateways and the options available. Each of these options below will involve a provider (or yourself) having an SMPP connection to a phone carrier in country and using this connection to receive incoming SMS and forward them on to your DHIS2 server over the internet using HTTP.

These solutions can either use a **long number** or **short code**. A long number is a standard mobile phone number of the type that most private people use, i.e. +61 400123123. A short code is simply a short number, such as 311. Short codes typically cost more to set up and maintain.

### Ensuring incoming SMS to DHIS2 server are formatted correctly

When sending incoming SMS to a DHIS2 server via the API you use the following URL: *https://<DHIS2_server_url>/api/sms/inbound*

In DHIS2 version 2.34 and below, this endpoint requires the format of inbound SMS to be in a very specific format, i.e. the message itself must be a parameter called text, the phone number of the sender must be a parameter called originator.

When using all of the below SMS gateway options, when you configure them to forward incoming SMS on to another web service, they will each have their own format, which will be different to the one expected by the DHIS2 API. For this reason then, it’s necessary to reformat them before sending them on to the DHIS2 server.

One option is to run your own very simple web service, which simply receives the incoming SMS from the gateway provider, reformats it to the one required for DHIS2 and forwards it on to your DHIS2 API. Such a service would need to be written by a software developer.

In DHIS2 version 2.35, it is planned to support these cases with a templating system for incoming SMS, so you can specify the format of the messages which will be sent from your provider. That way, you can configure the DHIS2 server to accept incoming SMS from any other SMS gateway provider and they can directly send incoming SMS to the DHIS2 API, without the need for such a formatting web service.

### Using RapidPro

[RapidPro](https://rapidpro.io/) is a service run by UNICEF in over 50 countries around the world. It is a collection of software which works with in-country phone carriers to enable organisations to design SMS solutions for their projects, such as SMS reporting or awareness campaigns.

The RapidPro service will involve an SMPP connection to one or more phone carriers in-country, usually via a shortcode, potentially dedicated to Health work for NGOs. It’s then possible to add a webhook so that incoming SMS are forwarded to another web service, such as the formatting web service described above. If the shortcode is used for other purposes as well, it may be necessary to add the phone numbers of your reporting devices to a separate group, so that only the incoming SMS from those devices is forwarded to the webhook.

RapidPro is currently set up and running in roughly half of the countries which are currently using or piloting DHIS2. Before considering one of the solutions below, which can be costly in terms of both finance and time, it is worth getting in contact with Unicef to see if RapidPro is available and if it can be used for health reporting in your country.

### Using commercial SMS gateway providers

Of the commercial SMS gateway providers mentioned in the Sending SMS section above, they will usually have capability to *send* SMS in most countries but can only support *receiving* SMS in a limited amount of countries. The majority of countries they support receiving SMS in are not those using DHIS2. Of the countries that are using DHIS2, most are already covered by having a RapidPro service running in-country.

However, it is worth researching what commercial options are available for your country. In some countries there will be small national companies that provide SMS services, they’ll have existing SMPP connections with the phone providers you can use.

### Using phone carriers directly

If none of the above solutions are available it would be necessary to approach the phone carriers in your country directly. The first question to ask them would be whether they are aware of any companies which are operating SMPP connections with them which you may be able to approach.

If not, as a final option, you would need to consider setting up and maintaining your own SMPP connection with the phone provider. However, not all phone providers might offer such a service.

You would need to run your own server running software such as [Kannel](https://www.kannel.org/), which connects (usually via a VPN) to an SMPP service running in the phone providers network. With this in place, any incoming SMS for the configured long number or shortcode are sent from the phone carrier to your Kannel server and you can then forward on these messages as above.

### Receiving concatenated or multipart SMS

When syncing data via SMS with the DHIS2 Android App, it uses a compressed format to use as little space (characters of text) as possible. Despite this, it will quite often be the case that a message will extend over the 160 character limit of one standard SMS. On most modern mobile devices these messages will still be sent as one concatenated or multipart SMS, and received as one message. When sending between two mobile devices, when an Android device is used as the gateway, this should be handled without issue.

When selecting an SMS gateway then, it is important to confirm that the phone carrier used supports concatenated SMS. Most of them will support this, but it is important to confirm as the SMS functionality will not work if SMS are split. This relies on something called a UDH (User Data Header). When discussing with providers then, ensure you ask if it is supported.
