## <img src="https://i.ibb.co/QNzSJy4/1024-copia.png" data-canonical-src="https://i.ibb.co/QNzSJy4/1024-copia.png" width="25" height="25" /> Description
HUDuino is an implementation of a HUD for bikers. This HUD will allows the biker to visualize the speed at which they are driving, the temperature inside the helmet, as well as the humidity, and they will be able to see if any notifications arrive on the phone while they are driving. 
This project seeks to improve the safety of bikers, avoiding distractions by lowering the head or handling the phone. 
Here you will find all the necessary resources to: 
 
 - View or modify the source code of the Android application
  - View, modify and upload to the development board the code necessary to make it work 
  - Make the electronic connections on the development board. 
  - Build the case for all the hardware with a 3D printer.

This project has three parts:
 - Android application
 - Arduino Sketch
 - Hardware construction


## <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Android_logo_%282014-2019%29.png/600px-Android_logo_%282014-2019%29.png" data-canonical-src="https://upload.wikimedia.org/wikipedia/commons/thumb/f/fc/Android_logo_%282014-2019%29.png/600px-Android_logo_%282014-2019%29.png" width="25" height="25" /> Android application 
The application will obtain several telemetries (described below) and send them via bluetooth to the Arduino device. It will also receive temperature and humidity data from the Arduino device.
The telemetry obtained by the application are:
- GPS Speed (Using Google API)
- Notifications received on the phone (It will only show that you have a notification from a specific application) The applications considered for notifications are the following:
   - WhatsApp
   - Facebook (and Facebook Messenger)
   - Instagram
   - Incoming calls (Considers the official android and samsung call application. Other applications of other brands are not contemplated, below it will be described how to add them)
   - The rest of the notifications will be ignored (Although they can be shown by modifying a bit of code)

### Add new applications to show their notifications

In NotificationService.java:

> We will have to add a new static variable of type string in the ApplicationPackageNames class, in the value of the variable we will put the full name of the application (com.xxxx.appname)
> 
> In the InterceptedNotificationCode class we will create another static variable, type int in which we will introduce a number greater than the existing ones (normally 6)
> 
> In the matchNotificationCode function we will have a case type control structure. In it we will add a new case and introduce the name of the package with a return of the code created previously.


In MainActivity.java file:

> In the MainActivity.java file you will have to modify the receiveNotification function. In this function there is a case control structure, we will have to add one more case by modifying only the application code (1,2,3,4...), the string to be displayed in the corresponding TextView and the sendNotification variable. This variable will have to have a string of 3 characters that are different from those already described in the contemplated applications.

And this is everything. 

### Enable "Other notifications"
You just need to remove the negation symbol from the if structure control inside the onNotificationPosted function in the NotificationService.java file
