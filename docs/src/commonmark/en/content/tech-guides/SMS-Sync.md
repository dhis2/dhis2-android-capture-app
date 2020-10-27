
# Android Compressed SMS Sync

![quick overvier of the architecture](resources/images/sms-sync/compressed-sms.png)

The android app compresses the SMS using a specific module from the SDK. After the SMS is compressed it will be sent to a gateway that was configured in the settings screen of the app. When the gateway receives the SMS, a custom webhook will redirect the SMS to dhis2 server using /sms/inbound API. Finally if everything works fine, dhis2 will send a confirmation SMS using the gateway API to the originator device.

Now, you have a slighter idea of how it works. Each of the modules will be explained in detail starting from top to bottom. 

## Server side - Core (2.33 > versions)

It Receives the SMS and tries to handle it. If the SMS can’t be handled due to compression error, command not found, bad encoding etc.. it will be stored in the server with **“Unhandled”** state. You can check all the SMS received in the *mobile configuration* app.

Core has a consumer SMS thread that works with a queue, everytime an SMS is received, it checks which listener can accept the incoming SMS. There are many listeners in the core system, but in this case we are only interested in the “CompressionSMSListener”.

*CompressionSMSListener* verifies that the SMS is in base64, after this it will try to uncompress it using the user metadata. If there are no errors it will continue the flow checking the SubmissionType and then delegating to other specific listeners (aggregate, enrollment, register etc..).

Note: To find out more about the compression/decompression library you can check out these links:

* [https://github.com/dhis2/dhis2-android-capture-app/blob/master/docs/src/commonmark/en/content/tech-guides/SMS-compression.md](https://github.com/dhis2/dhis2-android-capture-app/blob/master/docs/src/commonmark/en/content/tech-guides/SMS-compression.md)
* [https://github.com/dhis2/sms-compression](https://github.com/dhis2/sms-compression)

### Sending an SMS to DHIS2 Server: Inbound API

The API is described in the official documentation: [https://docs.dhis2.org/master/en/developer/html/webapi_sms.html](https://docs.dhis2.org/master/en/developer/html/webapi_sms.html)

Example of using the API:

```
//endpoint
http://android2.dhis2.org:8080/api/sms/inbound

//body
{
  "text": "compressSMSGoestHere",
  "originator": "612121212", 
  "gatewayid": "your gateway goes here", //optional
  "receiveddate": "2019-10-13",
  "sentdate":"2019-10-13",
  "smsencoding": "1"
}
Basic auth (user/pass)
```

*Note*: When using the inbound API for sending the SMS to the dhis2 server. The user should have a phone number in their profile/settings, otherwise you’ll get thrown an exception telling that the *user is not registered*.

## Gateway
The main purpose of the gateway is to receive the SMS delivering it to a specific dhis2 server and also send SMS from dhis2 outbound API.

For receiving SMS and delivering to dhis2 you need go to your gateway website and configure a reply callback. Later, the json that your gateway returns needs to be mapped to the one that the inbound endpoint supports, in order to do this a custom script is required from your side. This script should :

- Be listening in your server and ready to accepted the responses of your gateway provider that is returned by its webhook/callback 
- Map the provider response to the one supported by /api/inbound dhis2
- Perform a POST using inbound API endpoint to your dhis2 server

An example of this script could be the following one (used for *clickatell*):

```javascript
exports.smsWebhook = functions.https.onRequest(async (req, res) => {
 console.log('SMS received from gateway', res);
 const smsBody = req.body
 try {
   await postToDhis2(smsBody);
   return res.end();
 } catch(error) {
   console.error(error);
   return res.status(500).send('Something went wrong
    while posting the sms to dhis2');
 }
});
 
function postToDhis2(smsBody){
 var today = new Date();
 const date = today.getFullYear()+'-'+
        (today.getMonth()+1) + '-'+today.getDate();
 const auth = "Basic " + new Buffer("user:pass").toString("base64");
 
  
return rp({
   method: 'POST',
   uri: 'https://yourServer/api/sms/inbound',
   headers: {
     "Authorization" : auth
   },
   body: {
     text: unescape(smsBody.text),
     originator: '+'+smsBody.fromNumber,
     gatewayid: 'UNKNOWN',
     receiveddate: date,
     sentdate: date,
     smsenconding: '1'
   },
   json: true,
 });
}
```

If you’re wondering where you could upload a script like this [amazon lambda](https://aws.amazon.com/lambda/), [firebase](https://firebase.google.com/docs/functions), [cloud functions](https://cloud.google.com/functions/?utm_source=google&utm_medium=cpc&utm_campaign=emea-es-all-en-dr-bkws-all-all-trial-e-gcp-1008073&utm_content=text-ad-none-any-DEV_c-CRE_253523329901-ADGP_Hybrid+%7C+AW+SEM+%7C+BKWS+~+EXA_M:1_ES_EN_General_Cloud+Functions_google+cloud+ETL-KWID_43700019211658719-kwd-292268097025-userloc_9061033&utm_term=KW_google%20cloud%20functionality-NET_g-PLAC_&ds_rl=1242853&ds_rl=1245734&ds_rl=1245734&gclid=EAIaIQobChMIqPSVjoXK6AIVkkTTCh3CxgXFEAAYASAAEgKBNfD_BwE) **or your own server are good options**. In the android team we chose firebase since it was really fast and easy to upload it. Firebase or any other provider gives you an url that can be set up on your reply callback configuration.

The gateway is also used for confirmation and sending SMS, i.e the app user wants to know if the sync was performed successfully or not on the server side. When this happens the dhis2 server sends a SMS to the gateway using its API, later the gateway will send this confirmation SMS to the android capture app. For this case no script is needed.

Warning: When sending SMS with length > 160 characters to a gateway, the SMS is split into multiple parts and then aggregated into a single one. Keep in mind that some gateways do not aggregate the different parts. So you will be in charge of doing this in your custom script. Some considerations:

- SMS parts don’t always come in order
- Some kind of persistence is needed to not lose the parts until you receive the last one. (Firebase storage for ej in our case)
- To order the parts you need to read the UDH header [https://en.wikipedia.org/wiki/User_Data_Header](https://en.wikipedia.org/wiki/User_Data_Header)

## Android SDK

Compress the information using the lib mentioned before and encode the SMS in base64, then it will send the SMS to the gateway configured in the android app. You can check the code in the following module on GitHub :
[https://github.com/dhis2/dhis2-android-sdk/tree/develop/core/src/main/java/org/hisp/dhis/android/core/sms](https://github.com/dhis2/dhis2-android-sdk/tree/develop/core/src/main/java/org/hisp/dhis/android/core/sms)

## Android APP
Configure SMS gateway (ej: +34670872412) and enable SMS submission in settings:

![Android SMS Settings](resources/images/sms-sync/android-sms-settings.png)

If you really want to get a confirmation SMS to check if the sync was performed successfully on the server side you should enable *wait for SMS result response*.

When you do not have internet connection press sync, after a while you will be asked to sync using SMS. Then one or multiple SMS will be sent to the gateway if you gave the android APP SMS permissions.


## Testing it

You have multiple ways of testing this feature:
- **Real gateway**: Go to Mobile app >  SMS Service configuration. Then select *bulkSMS*, *clickatell* or a custom one. Both BulkSMS and Clickatell let you define custom webhooks
  - https://www.bulksms.com/developer/json/v1/#tag/Webhooks
  - https://www.clickatell.com/developers/api-documentation/php-library/
  - **Note**: After you have developed your own custom webhook. You need a custom script in your server (as mentioned above) that takes care of the mapping, etc...
  - Configure the android capture app SMS settings and perform a sync.
  - Go to dhis2 server mobile app > received SMS and verify the SMS is received with **PROCESSED** state. Then the information you modified in the app should be reflected in the server.
- **Android gateway test app**:
  - Ideally you want to test it with 2 android phones that have SIM cards.
  - Install the tracker capture app in one android device. 
    - Configure the SMS settings (gateway and enable submission).
  - Install, compile and run the android gateway app in another device
    - https://github.com/dhis2/dhis2-sms-android-gateway
    - Make sure that you are accepting SMS permissions for this app.
    - Configure the user, password and dhis2 server URL (2.33 V) for ex: http://play.dhis2.org/android-current/
    - Click on *Save* button and make sure that the button above this one contains the text *Forward message*
  - From the android capture app sync with SMS
  - Check that an alert shows up in the gateway app telling you that the message was delivered to the dhis2 server.
  - Go to dhis2 server mobile app > received SMS and verify the SMS is received with **PROCESSED** state. Then the information you modified in the app should be reflected in the server.
  - **Important**: Everytime the gateway app redirects a SMS, it will send a confirmation SMS to the originator (capture app). Ideally the confirmation SMS would follow a specific format, but due to limitation you will only see a success/fail SMS message in this confirmation.
    - Success message : 200 http code
    - Error message: client and server errors (4xx / 5xx)
  - **Other considerations**: If you send long SMSs (more than 160 chars) this app does not re-assemble the different parts. 
- **Manual gateway** Using postman
  - POST sms/inbound/
  - https://docs.dhis2.org/master/en/developer/html/webapi_sms.html
  - Be aware that in this step you will have to catch the SMS that the app is sending and pass it to the postman request


