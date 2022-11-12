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
- A mirror sticker will also be needed
- And finally, a reflective protector

I will leave links to all the necessary hardware below, in "links"
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
| ESP8266 PIN | Component PIN  |
|--|--|
| D8 | RXD Bluetooth |
| D7 | TXD Bluetooth |
| D6 | Signal DHT22 |
| D2 | SDA Display |
| D1 | SCL/SCK Display |
| 3V3 | VCC Display |
| VIN | VCC DHT22 |
| VIN | VCC Bluetooth |
| VIN | + TC4056A |

 ## <img src="https://w7.pngwing.com/pngs/789/777/png-transparent-computer-icons-tinyurl-hyperlink-symbol-url-shortening-chain-miscellaneous-text-technic.png" data-canonical-src="https://w7.pngwing.com/pngs/789/777/png-transparent-computer-icons-tinyurl-hyperlink-symbol-url-shortening-chain-miscellaneous-text-technic.png" width="25" height="25" /> Links
- [Wemos D1 Mini V3.0 (3,54€)](https://es.aliexpress.com/item/1005001621784437.html?spm=a2g0o.productlist.0.0.7ca3470fL3Tjxw&algo_pvid=67611696-1d3b-4081-b543-5a0c33e4cd6a&algo_exp_id=67611696-1d3b-4081-b543-5a0c33e4cd6a-0&pdp_ext_f=%7B%22sku_id%22:%2212000016846399621%22%7D&pdp_npi=2@dis!EUR!2.39!1.98!!!1.41!!@2100bb4916682736658158617e1742!12000016846399621!sea&curPageLogUid=pXBOqu7UFDyW)
- [0,96 inch OLED SSD1306 (2,37€)](https://es.aliexpress.com/item/4001028369082.html?spm=a2g0o.productlist.0.0.76581d87iZrQez&algo_pvid=ad3f0086-4147-43a5-90a4-a13214d53591&algo_exp_id=ad3f0086-4147-43a5-90a4-a13214d53591-6&pdp_ext_f=%7B%22sku_id%22:%2210000013573591133%22%7D&pdp_npi=2@dis!EUR!2.05!1.62!!!0.75!!@2100bde316682737848228485e1652!10000013573591133!sea&curPageLogUid=7cHKZu6GKCVn)
- [DHT-22 with PCB (2,46€)](https://es.aliexpress.com/item/33037061522.html?spm=a2g0o.productlist.0.0.2cdc8361tW3RSp&algo_pvid=5b1ef29f-1949-4dbf-be44-6ad1cb44c291&algo_exp_id=5b1ef29f-1949-4dbf-be44-6ad1cb44c291-2&pdp_ext_f=%7B%22sku_id%22:%2267335069429%22%7D&pdp_npi=2@dis!EUR!1.87!1.83!!!0.63!!@0b0a119a16682744707767295ec691!67335069429!sea&curPageLogUid=OeRahkAPFQUo)
- [HC-06 compatible (2,65€)](https://es.aliexpress.com/item/1005004472598061.html?spm=a2g0o.productlist.0.0.73b97c9eXn1oZu&algo_pvid=d0a2c52c-a08c-4e6d-a672-78f453b721e0&algo_exp_id=d0a2c52c-a08c-4e6d-a672-78f453b721e0-3&pdp_ext_f=%7B%22sku_id%22:%2212000029285661878%22%7D&pdp_npi=2@dis!EUR!2.59!2.31!!!!!@0b0a182b16682745338526291ee8f7!12000029285661878!sea&curPageLogUid=Cy27QXzzQIzX)
- [TC4056A (1,85€)](https://es.aliexpress.com/item/1005002925698704.html?spm=a2g0o.productlist.0.0.b1ba258fuYD9ns&algo_pvid=998b2c49-4ff3-4040-b3a1-368deb075bb5&algo_exp_id=998b2c49-4ff3-4040-b3a1-368deb075bb5-0&pdp_ext_f=%7B%22sku_id%22:%2212000027422694098%22%7D&pdp_npi=2@dis!EUR!3.89!2.72!!!2.03!!@2100bdf016682746114102885eb72d!12000027422694098!sea&curPageLogUid=dz225q3W1imp)
- [Mirror sticker 10u (1,37€)](https://es.aliexpress.com/item/1005004340986514.html?spm=a2g0o.productlist.0.0.47c216585laSrf&algo_pvid=62572371-607a-4a63-b486-daa148fc7792&algo_exp_id=62572371-607a-4a63-b486-daa148fc7792-11&pdp_ext_f=%7B%22sku_id%22:%2212000028823906125%22%7D&pdp_npi=2@dis!EUR!6.97!4.18!!!!!@2100bdd816682746720324656ef3f0!12000028823906125!sea&curPageLogUid=AUlcvPsPnDZH)
- [reflective sticker (1,83€)](https://es.aliexpress.com/item/1005002937614641.html?spm=a2g0o.productlist.0.0.6e994883wuTLj9&algo_pvid=d99f8360-8d0c-4a7d-a1bf-903c82f83427&algo_exp_id=d99f8360-8d0c-4a7d-a1bf-903c82f83427-0&pdp_ext_f=%7B%22sku_id%22:%2212000022875656100%22%7D&pdp_npi=2@dis!EUR!3.48!2.27!!!!!@2100bdf116682748125408182e2a79!12000022875656100!sea&curPageLogUid=BnKT2eEYIGgk)
- [AWG24 wire 5M (2€)](https://es.aliexpress.com/item/1005003164183920.html?spm=a2g0o.productlist.0.0.36c67d54R4NPIo&algo_pvid=814afe25-46a6-4b76-b57b-6f4c5cf1b04f&algo_exp_id=814afe25-46a6-4b76-b57b-6f4c5cf1b04f-2&pdp_ext_f=%7B%22sku_id%22:%2212000024441742097%22%7D&pdp_npi=2@dis!EUR!2.0!2.0!!!!!@2100bdd716682748775365983e40be!12000024441742097!sea&curPageLogUid=kwE3AlWTJXoE)


**Total cost: 18,07€**
