#include <SoftwareSerial.h>    //For bluetooth communication
#include <Wire.h>              //This library allow the I2C communication
#include <Adafruit_GFX.h>      //Graphics for the screen
#include <Adafruit_SSD1306.h>  //Screen driver
#include <DHT.h>               //DHT-22 Library

#define SCREEN_WIDTH 128  // OLED display width, in pixels
#define SCREEN_HEIGHT 64  // OLED display height, in pixels
#define DHTPIN 12         // GPIO PIN
#define DHTTYPE DHT22     //DHT TYPE (DHT11, DHT22...)

DHT sensor(DHTPIN, DHTTYPE);
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);
SoftwareSerial hc06(13, 15);  //Serial for BT : Rx/Tx pin

//Useful variables
String cmd, data, notification = "";
String speed = "  0";
String speedUnit = " kmh/h";
String temperatureUnit = " C";
int sensor_val = 0;
int humidity, temperature = 0;
int lastRead = 0;
int waitDHT22 = 2000;
//START the application
void setup() {
  Serial.begin(9600);  //Initialize Serial Monitor
  hc06.begin(9600);    //Initialize Bluetooth Serial Port
  sensor.begin();      //Initialize DHT-22 sensor
  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("SSD1306 allocation failed"));
    for (;;)
      ;
  }
  delay(2000);
  display.clearDisplay();                          //Clear everything in the display
  display.setTextSize(1);                          //Font size small
  display.setTextColor(WHITE);                     //Color must be white, this screen have not any other color.
  display.setCursor(0, 10);                        //Positions: x,y
  display.println("Please, connect your phone.");  // print text in the screen
  display.display();                               //Refresh data and show everything
}

void loop() {
  if (lastRead > waitDHT22) {                       //Readings should be made every two seconds, not before
    humidity = round(sensor.readHumidity());        //Obtain temperature from DHT-22 and round to int
    temperature = round(sensor.readTemperature());  //Obtain humidity from DHT-22 and round to int
    if (isnan(temperature) || isnan(humidity)) {    //Check if the DHT-22 is failing.
      Serial.println("Error reading values of DHT22");
      lastRead = 0;
      return;
      temperature = 0;
      humidity = 0;
    }
    lastRead = 0;
  }
  //Read data from HC06 when BT connection is available.
  while (hc06.available() > 0) cmd += (char)hc06.read();

  //Trim the obtained data from BT to 3 characters (BT can be very dirty)
  if (cmd != "") {
    if (cmd.length() > 3) data = cmd.substring(0, 3);
    else if (cmd.length() == 3) data = cmd;
    else if (cmd.length() == 2) data = " " + cmd;
    else if (cmd.length() == 1) data = "  " + cmd;

    //Check if data received is any notification
    if (data == "FBK") notification = "Facebook notification";
    else if (data == "IGM") notification = "Instagram notification";
    else if (data == "WSP") notification = "Whatsapp notification";
    else if (data == "ICL") notification = "In-call notification";
    else if (data == "OTR") notification = "Other notification";
    else if (data == "RSN") notification = "";

    //Check if data received is the SPEED
    if (isDigit(data.charAt(2))) speed = data;

    //Check if data received is the speed units change
    if (data == "0ME") speedUnit = " km/h";
    else if (data == "0IM") speedUnit = " mph";

    display.clearDisplay();  //CLEAR SCREEN
    //HUMIDITY
    display.setTextSize(1);
    display.setCursor(0, 0);
    display.println(String(humidity) + " %");
    //TEMPERATURE
    display.setTextSize(1);
    display.setCursor(100, 0);
    display.println(String(temperature) + temperatureUnit);
    //SPEED
    display.setTextSize(3);
    display.setTextColor(WHITE);
    display.setCursor(25, 20);
    display.println(speed);
    //SPEED UNIT
    display.setTextSize(1);
    display.setTextColor(WHITE);
    display.setCursor(75, 34);
    display.println(speedUnit);
    //NOTIFICATIONS
    display.setTextSize(1);
    display.setCursor(0, 57);
    display.println(notification);

    display.display();  // PRINT SCREEN
    cmd = "";           //reset cmd
    data = "";
  }
  //Write sensor data to HC06
  hc06.print(temperature);
  delay(100);
  lastRead += 100;  //This is for the while
}