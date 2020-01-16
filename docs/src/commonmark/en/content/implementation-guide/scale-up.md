# Scale Up

## Acquisitions

Now that you have performed all your testing and your pilot project, you are ready to scale up your deployment, for which you will need to do acquisition of Hardware and necessary services. You will need to make decisions regarding:

- Purchasing of devices vs BYOD (bring your own device)
- Distribution of the app (now and later)
- Telecommunication contracts

**Purchasing of devices vs BYOD (bring your own device)**

Initially you should buy different devices to allow users to evaluate them and provide you with feedback. Once the device that you will be using is decided upon, you should only buy 10 or less units, or whatever is needed for the testing and the pilot phases. Only when the pilot is coming to completion, you should buy equipment for the next 6 months roll-out. Some very large projects will take years for a national roll-out, and your hardware adquisicion plan should expand across years. Recommendations on the technical specs for devices are in the chapter [<span class="underline">‘Mobile devices specifications’</span>](#mobile-device-specifications).

You should consider the feasibility of using a BYOD policy - this format allows users to bring their own devices, as long as they satisfy a minimum technical standard, which you will define for your project. You will normally offer some sort of incentive, likely to be in the form of eCash or airtime. The advantages of this approach are obvious: it avoids the large initial cost for acquisition, as well as it reduces the administration costs and logistics considerations. On the other hand, you will have the challenge of a very heterogeneous hardware environment, meaning different devices and Android OS versions. This mainly affects the debugging process.

**Distribution of the app** (now and later)

DHIS 2 Android app has a new release every couple of weeks. Each new release contains bug fixes and could contain new functionality. It could also contain new bugs. New versions are published in GitHub as well as Google Play store. Github is only a repository: you will download a specific APK and install it on your device. You will need to allow the use of third-party permissions to install an APK. Once an APK downloaded from GitHub or via other method, the installed version will never auto-update. On the other hand, if you install from Google Play, it normally auto-updates to the latest version. It is possible to disable auto-update in gPlay if you need to.

Once you complete your testing and training materials, and start your rollout, you don’t want the application version to change for any of the users, unless you have re-tested the new version. Version changes could include a modified UI, erroneous behaviours, or an incompatibility with your DHIS 2 server version. You want to thoroughly test new versions before pushing them to your users, so you can ensure that the new version doesn’t cause any problems to your configuration, requires retraining, on requires changes to your configuration.

In summary, for any installation that involves a significant number of devices you should avoid the use of Google Play, and instead use a Mobile Device Management (MDM) solution, which we discuss in [<span class="underline">this chapter</span>](#mobile-device-management). If you don’t have access to this option, you could consider using Google Play, but you should disable auto-update for the DHIS 2 android application. The procedure on how to do this changes by Android OS version - please google ‘how to disable android auto update by application in Andrid X.X’.

**Telecommunication contracts**

If your installation plans to include the use of SMS for transmitting selected records via SMS when mobile data is not available, you will need to establish a contract with a local aggregator which can provide you with an incoming number to receive the SMS. You should configure your server to receive & send SMSs - please see [<span class="underline">DHIS 2</span> <span class="underline">documentation</span>](https://docs.dhis2.org/master/en/user/html/mobile_sms_service.html#) on SMS connections. You will need to estimate the number of messages per month to be able to forecast the monthly cost.

The process of selecting and signing a contract with an SMS provider varies by country and it depends on the procurement procedures of your organization.

## Mobile Device Management

Mobile Device Management refers to software used for the administration of mobile devices. You will need an MDM software when you have to support hundreds of devices and it becomes necessary to control the apk file distribution across the devices, provide tech support and enforce institutional policies. Most options are offered as monthly-fee services. Some free apps offer kiosk mode, but charge a monthly fee for basic remote management.

The desirable features of an MDM software can be classified as basic and advanced. Here is a list of the desirable features:

- Basic features:
  - Require a screen lock password
  - Provision of authorized apps
  - Lock devices and wipe information if they’re lost or stolen
  - Control the upgrade of the Android App
  - Enforce backup policies
- Advanced features:
  - Enforce password strength policies
  - Enforce network usage policies
  - Track device location
  - Restrict access to settings and features (example - wifi/network, screen capture)

When deciding which is the best MDM software for your needs you should try to answer the following questions:

- How many devices do I need to manage?
- How often do I have physical access to the device?
- Which features do I really need?
- Which policies do I have to implement
- How hard will it be to install and maintain
- How will it affect the user experience?
- Do we need to allow BYO? (Bring Your Own Device).
- How will it affect the device?

In the next page you can find a list of available MDM software (please keep in mind that prices and conditions will change over time).

- Mobilock Free (unable to update software)
- SOTI (MobiControl) (can be expensive - $2.20/device/month)
- Miradore (no remote support)
- Applock (unable to control software update )
- AcDisplay (unable to control software update )
- F-Droid (unable to limit data consumption)
- APPDroid (unable to limit data consumption)
- Master List (unable to control software update )
- Firebase (unable to limit data consumption)
- Intunes (users need to be part of a MS Office 365 deployment)
- MobileIron (can be expensive - 3.15 USD /device/month \+ 2.368 USD for deployment)
- IBM Maas360 (too expensive - 1.60 USD /device/month \+ 0.50 USD /device/month for remote support, for 3.000 devices)
- AirWatch (unresponsive and can be expensive - 3.80 USD /device/month for 3,000 devices for 3 years)
- XenMobile (Citrix) (can be expensive - 2.03 USD /device/month for 3,000 devices)
- Good for Enterprise (Blackberry) (can be expensive - 2 USD /device/month \+ 2.5K USD for deployment)

## Training

An important step before roll up, is the training of the users and if necessary, the training of the teams providing support to the users. There are many training strategies that you can follow and it will depend on the size of the group that needs to be trained, their skill level, the time frame available, the budget, etc. It is important that you put time and energy into designing your training strategy and allocate enough time to accomplish your training goals. Having your users well trained and informed will reduce user’s anxiety and adoption problems and it will also increase the quality of the data collected.

### Technical Preparations for the Training

When preparing for the training, ensure that all the practical technical requirements have been met. This includes having the tablets/mobile devices ready, with the new DHIS 2 Capture Android Application installed. Depending on the availability of internet connectivity at the area where you will be performing the training, you might have all the tablets pre-synched with the server, so that you have enough data and the right configuration for the training.. Before doing the training, the exercises should be tested to ensure everything is working. Troubleshoot issues detected during testing so they do not arise during training. You may want to do a second round of the test to spot any issues missed in the first round.

If the training is done with pre-synched data and configuration, at the end of the training, make sure to let the trainees experience the App accessing the DHIS 2 remote server. This will give the trainees the possibility to experience real-life sync experience, which may include delays in the network. Without experiencing delays, they may later interpret network delays as faults in their device.

### Training Budget

Following, there are some guidelines on preparing the budget which are taken from the DHIS 2 Community Health Information System Guidelines [<span class="underline">document</span>](https://s3-eu-west-1.amazonaws.com/content.dhis2.org/Publications/CHISGuidelines_version_August29.pdf) published by the University of Oslo:

- Follow organizational policies in using approved budget templates and rates (indirect, DSAs, etc.) for all expenses including:
  - Travel (e.g. fuel, car hire, lodging)
  - Personnel (e.g. per diems, meal costs)
  - Venue (e.g. conference space, tea breaks)
  - Materials (e.g. printing, hardware, projectors)
  - Miscellaneous items
- Build budget based on in-sheet calculations of materials needed, unit cost of that material, and number of units needed. You can also build in additional multipliers to illustrate number of units per attendee. This allows flexibility in updating the budget if unit costs change, or number of participants increases or decreases.
- Budget anticipated expenses in local currency, with a conversion rate built in (that can be updated as needed) to convert to the desired currency of your organization or funder.(2).

### Training Agenda

The [<span class="underline">DHIS 2</span> <span class="underline">Community Health Information System Guidelines</span>](https://s3-eu-west-1.amazonaws.com/content.dhis2.org/Publications/CHISGuidelines_version_August29.pdf) document written by the University of Oslo recommends that you consider:

1. The type of seating you require (round table, individual desks, etc.).
2. Technological requirements (computers for all, Wi-Fi bandwidth, etc.),
3. Finance for conference center allowances, participant food and beverages
4. Trainers need space to walk around to observe and help each participant.

Be aware of the number of attendees you expect at each training, as providing sufficient materials and space will be necessary. Event space should be large enough for the group and also appropriate for the planned activities.

### Training Materials

In the same document we find recommendation for the training materials as well, which we include here. The materials you will need for your trainings will depend on your activities. To ensure you are planning for everything, walk through your training agenda with a partner, and discuss what will be done for each part of the training, taking note of the materials needed.

The agenda for training sessions should be defined well-ahead of the training and included in materials distributed.

User documentation should be packaged in Minimal Manuals. These manuals explain a specific work task (e.g. enter monthly data from village health register or compare health in your village with the neighboring villages). After explaining the work task, the Minimal Manual provides numbered step-by-step instructions with screenshots, so that users recognize what to do. Keep in mind that Minimal Manuals do NOT explain the functionality of the app, one by one, like a typical vendor user manual. Since users prefer doing and not reading, the manuals should be a short as possible while still containing all steps.

