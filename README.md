This project is under development. at this point everything is working, but there are some design issues that needs to be fixed. You can see the next steps below.

------------

### Next steps:
 - [ ] Add all necessary documentation and video of operation in README.md.
 - [ ] Redesign the shell to better fit any full-face helmet
 - [ ] Fix the problems that arise in the code.
 - [ ] Modify the wiring diagram to use the development board wemos d1 mini instead of nodemcu ESP12

------------

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

## <img src="https://www.nicepng.com/png/detail/207-2079566_arduino-1-logo-png-transparent-arduino-logo-png.png" data-canonical-src="https://www.nicepng.com/png/detail/207-2079566_arduino-1-logo-png-transparent-arduino-logo-png.png" width="25" height="25" /> Arduino Sketch

The Sketch is prepared to be used with the following components:
- ESP8266, any one will do, but so that it takes up as little space as possible and can fit in the case I have designed, it will have to be a Wemos D1 mini (or a replica)
- 0.96 inch I2C OLED screen. Any will do, but I recommend white or blue for better visibility.
- DHT22 temperature sensor, in this project the version with circuit is used, since it includes the resistance that the sensor needs to work correctly. However, we must eliminate the plastic that covers the sensor, thus saving space.
- HC-06 bluetooth module. The HC-05 model would also work, but to reduce costs and simplify the code, this model has been chosen.
- TC4056A, to charge the battery. I have chosen this model because it allows you to charge the battery while using the product, it also takes up very little space and is easy to use. It has USB-C.
- HAC-006 battery. It is the one used in the Nintendo Switch joycons. I have chosen this one because of its small size, durability and because it is cheap on amazon.
- The cables must be fine. CAT-5 network cables or AWG24 electrical cable can be used.
### How its works:
The sketch repeats its operation every 0.1 seconds. It listens for SoftwareSerial (Bluetooth). It makes a cut of what is received, making sure to get only 3 characters for each reception, according to the received characters, through control structures, it will do different actions:
- Switch between KM/h and mph
- Show GPS speed
- Warn if any notification arrives

In addition, it will continuously read the DHT-22 sensor, display the humidity and temperature values on the screen and send them via SoftwareSerial to the smartphone.

 ## <img src="https://www.freepnglogos.com/uploads/wrench/wrench-logo-png-gear-hard-repair-fix--0.png" data-canonical-src="https://www.freepnglogos.com/uploads/wrench/wrench-logo-png-gear-hard-repair-fix--0.png" width="25" height="25" /> 3D case
 The case has been designed with SolidWorks. The editable SLDPRT files are attached, as well as the STL if you want to print them directly.

They are prepared to house all the components, wiring everything in the tightest way possible so that the whole occupies as little as possible inside the helmet.

Please note that this part is still under development. Soon I will be uploading the updated designs so that it fits better inside any helmet.
 ## <img src="https://w7.pngwing.com/pngs/948/752/png-transparent-power-cord-electrical-cable-electrical-wires-cable-others-miscellaneous-text-electrical-wires-cable-thumbnail.png" data-canonical-src="https://w7.pngwing.com/pngs/948/752/png-transparent-power-cord-electrical-cable-electrical-wires-cable-others-miscellaneous-text-electrical-wires-cable-thumbnail.png" width="25" height="25" /> Wiring
 Use this diagram to wire your own HUDuino:
 
 ![enter image description here](https://raw.githubusercontent.com/weto91/HUDuino2/master/RESOURCES/DOC/ProtoBoard_connections.png)
