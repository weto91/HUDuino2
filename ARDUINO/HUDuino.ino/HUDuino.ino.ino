#include <SoftwareSerial.h>
#include <Wire.h>
#include <Adafruit_GFX.h> //Graphics for the screen
#include <Adafruit_SSD1306.h> //Screen driver

#define SCREEN_WIDTH 128 // OLED display width, in pixels
#define SCREEN_HEIGHT 64 // OLED display height, in pixels

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);
SoftwareSerial hc06(13,15); // Serial for BT : Rx/Tx pin

String cmd="";
int sensor_val=0;

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
  //Read data from HC06
  while(hc06.available()>0){
    cmd+=(char)hc06.read();
  }

  //Select function with cmd
  if(cmd!=""){
    Serial.print("Command recieved : "); // Only for debug, must be deleted after production
    Serial.println(cmd); // Only for debug, must be deleted after production
    
    // From this, the code is only for example and testing. Must be changed with the real code.
    if(cmd=="ON"){
          display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0, 10);
  display.println("Connected.");
  display.display(); 
    }else if(cmd=="OFF"){
          display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0, 10);
  display.println("Disconnected.");
  display.display(); 
    }else{
        Serial.println("Function is off by default");
    }
    cmd=""; //reset cmd
  }
  
  // Simulate sensor measurement to check if BT send from arduino to android data too.
  sensor_val=(int)2; // random number between 0 and 255
    
  //Write sensor data to HC06
  hc06.print(sensor_val);
  delay(100);
}
