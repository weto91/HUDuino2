#include <SoftwareSerial.h>
#include <Wire.h>
#include <Adafruit_GFX.h> //Graphics for the screen
#include <Adafruit_SSD1306.h> //Screen driver

#define SCREEN_WIDTH 128 // OLED display width, in pixels
#define SCREEN_HEIGHT 64 // OLED display height, in pixels

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);
SoftwareSerial hc06(13,15); // Serial for BT : Rx/Tx pin

String cmd="";
String data="";
int sensor_val=0;
String notification="";
String speed="0";
String speedUnit=" kmh/h";
String temperatureUnit=" C";

void setup(){
  Serial.begin(9600); //Initialize Serial Monitor
  hc06.begin(9600); //Initialize Bluetooth Serial Port
  Serial.print("Comenzamos "); // Only for debug, must be deleted after production

  if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) { // Address 0x3D for 128x64
    Serial.println(F("SSD1306 allocation failed"));
    for(;;);
  }

  delay(2000);
  display.clearDisplay(); // Clear everything in the display
  display.setTextSize(1); // Font size small
  display.setTextColor(WHITE); // Color must be white, this screen have not any other color.
  display.setCursor(0, 10); // positions: x,y
  display.println("Ready."); // print text in the screen
  display.display(); // refresh data and show everything
}

void loop(){
   // Simulate sensor measurement to check if BT send from arduino to android data too.
  sensor_val=random(23,25); // random number between 0 and 255
  //Read data from HC06
  while(hc06.available()>0){
    cmd+=(char)hc06.read();
  }

  if(cmd.length() > 3){
    data=cmd.substring(0, 3);
  }
  else if(cmd.length() == 3){
    data=cmd;
  }
  else if (cmd.length() == 2){
    data=" " + cmd;
  }
  else if(cmd.length() == 1){
    data="  " + cmd;
  }

  //Select function with cmd
  if(cmd!=""){
    //Serial.println("Command recieved : "); // Only for debug, must be deleted after production
    Serial.println("CMD: " + cmd + " || DATA: " + data); // Only for debug, must be deleted after production

    // Check if data received is any notification
    if(data == "FBK"){
      notification="Facebook notification";
    }else if(data == "IGM"){
      notification="Instagram notification";
    }else if(data == "WSP"){
      notification="Whatsapp notification";  
    }else if(data == "ICL"){
      notification="In-call notification";
    }else if(data == "OTR"){
      notification="Other notification";
    }else if(data == "RSN"){
      notification="";
    }

    // Check if data received is the SPEED
    if (isDigit(data.charAt(2))){
      speed=data;
    }

    // Check if data received is the speed units change
    if(data == "0ME"){
      speedUnit=" km/h";
    }
    else if(data == "0IM"){
      speedUnit=" mph";
    }

    display.clearDisplay(); //CLEAR SCREEN
    // TEMPERATURE
    display.setTextSize(1);
    display.setCursor(100, 0);
    display.println(String(sensor_val) + temperatureUnit);
    // SPEED NUMBER
    display.setTextSize(3);
    display.setTextColor(WHITE);
    display.setCursor(25, 20);
    display.println(speed);
    //SPEED UNIT
    display.setTextSize(1);
    display.setTextColor(WHITE);
    display.setCursor(75, 34);
    display.println(speedUnit);
    // NOTIFICATIONS
    display.setTextSize(1);
    display.setCursor(0, 57);
    display.println(notification);

    display.display(); // PRINT SCREEN
    cmd=""; //reset cmd
    data="";
  }
  
 
    
  //Write sensor data to HC06
  hc06.print(sensor_val);
  delay(100);
}
