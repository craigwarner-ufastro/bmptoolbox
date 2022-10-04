// Date and time functions using a PCF8523 RTC connected via I2C and Wire lib
#include "RTClib.h"
// include the SD library:
#include <SPI.h>
#include <SD.h>
#include "PinChangeInterrupt.h"

// set up variables using the SD utility library functions:
//Sd2Card card;
//SdVolume volume;
//SdFile root;

RTC_PCF8523 rtc;

// Adafruit SD shields and modules: pin 10
const int chipSelect = 10;

volatile int count[4];
const int tipperPin[4] = {2, 3, 6, 9};
int prevDay;
volatile int history[31][4];
volatile int month[31];
long b = 115200;

void setup(){
  //start serial connection
  Serial.begin(115200);
  //Serial.begin(9600);
  delay(1000);
  while (!Serial) delay(500);


/*
#ifndef ESP8266
  while (!Serial); // wait for serial port to connect. Needed for native USB
#endif
*/
  // ================================================================== //
  //setup RTC
  if (! rtc.begin()) {
    Serial.println(F("Couldn't find RTC"));
    Serial.flush();
    abort();
  }

  if (! rtc.initialized() || rtc.lostPower()) {
    //Serial.println("RTC is NOT initialized, let's set the time!");
    // When time needs to be set on a new device, or after a power loss, the
    // following line sets the RTC to the date & time this sketch was compiled
    rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
    // This line sets the RTC with an explicit date & time, for example to set
    // January 21, 2014 at 3am you would call:
    // rtc.adjust(DateTime(2014, 1, 21, 3, 0, 0));
    //
    // Note: allow 2 seconds after inserting battery or applying external power
    // without battery before calling adjust(). This gives the PCF8523's
    // crystal oscillator time to stabilize. If you call adjust() very quickly
    // after the RTC is powered, lostPower() may still return true.
  }

  // When time needs to be re-set on a previously configured device, the
  // following line sets the RTC to the date & time this sketch was compiled
  //rtc.adjust(DateTime(F(__DATE__), F(__TIME__)));
  // This line sets the RTC with an explicit date & time, for example to set
  // January 21, 2014 at 3am you would call:
  // rtc.adjust(DateTime(2014, 1, 21, 3, 0, 0));

  // When the RTC was stopped and stays connected to the battery, it has
  // to be restarted by clearing the STOP bit. Let's do this to ensure
  // the RTC is running.
  rtc.start();

  // The PCF8523 can be calibrated for:
  //        - Aging adjustment
  //        - Temperature compensation
  //        - Accuracy tuning
  // The offset mode to use, once every two hours or once every minute.
  // The offset Offset value from -64 to +63. See the Application Note for calculation of offset values.
  // https://www.nxp.com/docs/en/application-note/AN11247.pdf
  // The deviation in parts per million can be calculated over a period of observation. Both the drift (which can be negative)
  // and the observation period must be in seconds. For accuracy the variation should be observed over about 1 week.
  // Note: any previous calibration should cancelled prior to any new observation period.
  // Example - RTC gaining 43 seconds in 1 week
  float drift = 43; // seconds plus or minus over oservation period - set to 0 to cancel previous calibration.
  float period_sec = (7 * 86400);  // total obsevation period in seconds (86400 = seconds in 1 day:  7 days = (7 * 86400) seconds )
  float deviation_ppm = (drift / period_sec * 1000000); //  deviation in parts per million (μs)
  float drift_unit = 4.34; // use with offset mode PCF8523_TwoHours
  // float drift_unit = 4.069; //For corrections every min the drift_unit is 4.069 ppm (use with offset mode PCF8523_OneMinute)
  int offset = round(deviation_ppm / drift_unit);
  // rtc.calibrate(PCF8523_TwoHours, offset); // Un-comment to perform calibration once drift (seconds) and observation period (seconds) are correct
  // rtc.calibrate(PCF8523_TwoHours, 0); // Un-comment to cancel previous calibration
  //Serial.print(F("Offset is ")); Serial.println(offset); // Print to control offset
/*
  DateTime now1 = rtc.now();
  Serial.println("DATE");
  Serial.println(now1.year());
  Serial.println(now1.month());
  Serial.println(now1.day());
  Serial.println(now1.hour());
  Serial.println(now1.minute());
*/
  //set prevDay
  DateTime now = rtc.now();
  // calculate a date which is 1 day, 0 hours 0 min, and 0 seconds into the past 
  DateTime past (now - TimeSpan(1,0,0,0));
  prevDay = past.day();

  // ===================================================================================== //
  //initialize SD card
  //Serial.print(F("Initializing SD card..."));

  // see if the card is present and can be initialized:
  if (!SD.begin(chipSelect)) {
    Serial.println(F("Card failed, or not present"));
    // don't do anything more:
    while (1);
  }
  //Serial.println(F("card initialized."));

  //configure pin as an input and enable the internal pull-up resistor
  pinMode(tipperPin[0], INPUT_PULLUP);
  pinMode(tipperPin[1], INPUT_PULLUP);
  pinMode(tipperPin[2], INPUT_PULLUP);
  pinMode(tipperPin[3], INPUT_PULLUP);
  pinMode(LED_BUILTIN, OUTPUT); 
  //attachInterrupt(digitalPinToInterrupt(tipperPin[0]), incrementCount1, FALLING);
  //attachInterrupt(digitalPinToInterrupt(tipperPin[1]), incrementCount2, FALLING);
  //attachInterrupt(digitalPinToInterrupt(tipperPin[2]), incrementCount3, FALLING);
  //attachInterrupt(digitalPinToInterrupt(tipperPin[3]), incrementCount4, FALLING);
  attachPinChangeInterrupt(digitalPinToPinChangeInterrupt(tipperPin[0]), incrementCount1, FALLING);
  attachPinChangeInterrupt(digitalPinToPinChangeInterrupt(tipperPin[1]), incrementCount2, FALLING);
  attachPinChangeInterrupt(digitalPinToPinChangeInterrupt(tipperPin[2]), incrementCount3, FALLING);
  attachPinChangeInterrupt(digitalPinToPinChangeInterrupt(tipperPin[3]), incrementCount4, FALLING);



  for (int i = 0; i < 4; i++) {
    count[i] = 0;
    for (int j = 0; j < 31; j++) history[j][i] = 0;
  }
  for (int i = 0; i < 31; i++) month[i] = 0;
}

void loop(){
  //READ RTC - if hour == 0 and month != month[prevDay] {
  DateTime now = rtc.now();
  // calculate a date which is 1 day, 0 hours 0 min, and 0 seconds into the past 
  DateTime past (now - TimeSpan(1,0,0,0));
  prevDay = past.day();
  int prevDayMonth = past.month();
  int prevDayYear2d = past.year()-2000;
  int currMonth = now.month();
  //if hour == 0, check if month array has been updated
  //this occurs once at midnight each day - current count is stored
  //in history and written to SD card and count is reset
  if (now.hour() >= 0 && prevDayMonth != month[prevDay]) {
    //Serial.print(F("prevDayMonth = "));
    //Serial.print(prevDayMonth);
    //Serial.print(F("prevDay = "));
    //Serial.println(prevDay);
    month[prevDay] = prevDayMonth;
    for (int i = 0; i < 4; i++) {
      history[prevDay][i] = count[i];
      char filename[] = "c000000.txt";
      sprintf(filename, "c%02d%02d%02d%01d.txt", prevDayYear2d, prevDayMonth, prevDay, i);
      //Serial.print(F("File "));
      //Serial.print(filename);
      if (!SD.exists(filename)) {
        // open the file. note that only one file can be open at a time,
        // so you have to close this one before opening another
        File dataFile = SD.open(filename, FILE_WRITE);
        //write to SD card
        // if the file is available, write to it:
        if (dataFile) {
          dataFile.println(count[i]);
          dataFile.close();
          // print to the serial port too:
          //Serial.print(F("File "));
          //Serial.print(filename);
          //Serial.print(F(" counts = "));
          //Serial.println(count[i]);
        } else {
          Serial.print(F("Error opening "));
          Serial.println(filename);
        }
      }
      count[i] = 0;
    }
  }
  if (Serial.available()) {
    String s = Serial.readString();
    //Serial.println(s);
    if (isCommand(s)) {
      if (getCommand(s).equals("G")) {
        if (getParam(s).equals("C")) {
          Serial.print(F("T"));
          for (int i = 0; i < 4; i++) {
            Serial.print(F("::"));
            //Serial.print(i);
            //Serial.print(F("::CURR::"));
            //Serial.println(count[i]);
            Serial.print(count[i]);
          }
          Serial.println(F("::E"));
          Serial.flush();
          //Serial.println(F("TIP::END"));
        } else if (isValidNumber(getParam(s))) {
          int dom = getDay(s); 
          short thisMonth = getMonth(s);
          //Serial.println(F("DOM "));
          //Serial.println(dom);
          //Serial.println(thisMonth);
          Serial.print(F("H::"));
          Serial.print(dom);
          Serial.print(F("::"));
          Serial.print(thisMonth);
          //GET_TIPPER_VALUES::day_of_month
          for (int i = 0; i < 4; i++) {
            //Serial.print(F("TIP_"));
            //Serial.print(i);
            //Serial.print(F("::HIST::"));
            Serial.print(F("::"));
            if (thisMonth == month[dom]) {
              //Serial.println(history[dom][i]);
              Serial.print(history[dom][i]);
            } else Serial.print(-1);
          }
          //Serial.println(F("TIP::END"));
          Serial.println(F("::E"));
          Serial.flush();        
        }
      } else if (getCommand(s).equals("C")) {
        Serial.println(F("A::OK"));
        Serial.flush();
      } else if (getCommand(s).equals("D")) {
        if (getParam(s).equals("C")) {
          //print current date
          sendDate();
        } else if (getParam(s).startsWith("S")) {
          //set date
          rtc.adjust(DateTime(getParam(s).substring(1).c_str()));
          //print current date
          sendDate();
        }
      }
    } else changeBaud();
  }
  /*
  delay(3000);
  Serial.print(now.hour(), DEC);
  Serial.print(":");
  Serial.print(now.minute(), DEC);
  Serial.print(":");
  Serial.print(now.second(), DEC);
  Serial.print(" = ");
  for (int i = 0; i < 4; i++) {
    Serial.print(count[i], DEC);
    Serial.print(" ");
  }
  Serial.println();
  */
}

void changeBaud() {
  if (b == 115200) {
    b = 9600;
    Serial.begin(b);
  } else {
    b = 115200;
    Serial.begin(b);
  }
  delay(500);
  Serial.println(F("B::OK"));
  Serial.flush();
}

void incrementCount1() {
  count[0]++;
  Serial.print(F(" 1 = "));
  Serial.println(count[0]);
}

void incrementCount2() {
  count[1]++;
  Serial.print(F(" 2 = "));
  Serial.println(count[1]);
}

void incrementCount3() {
  count[2]++;
  Serial.print(F(" 3 = "));
  Serial.println(count[2]);
} 

void incrementCount4() {
  count[3]++;
  Serial.print(F(" 4 = "));
  Serial.println(count[3]);
}

bool isCommand(String command) {
  if (command.indexOf("::") != -1) return true;
  return false;
}

String getCommand(String command) {
  return command.substring(0, command.indexOf("::"));
}

String getParam(String command) {
  String param = command.substring(command.indexOf("::")+2);
  param.trim();
  return param;
  //return command.substring(command.indexOf("::")+2).trim();
}

short getMonth(String command) {
  String param = command.substring(command.indexOf("::")+2, command.indexOf("::")+4);
  param.trim();
  return (short)(param.toInt());
}

int getDay(String command) {
  String param = command.substring(command.indexOf("::")+4);
  param.trim();
  return param.toInt();
}

bool isValidNumber(String str){
  for(byte i=0;i<str.length();i++) {
    if(isDigit(str.charAt(i))) return true;
  }
  return false;
}

void sendd2(int x) {
  if (x < 10) Serial.print("0");
  Serial.print(x);
}

void sendDate() {
  DateTime now1 = rtc.now();
  Serial.print(now1.year());
  Serial.print(F("-"));
  sendd2(now1.month());
  Serial.print(F("-"));
  sendd2(now1.day());
  Serial.print(F(" "));
  sendd2(now1.hour());
  Serial.print(F(":"));
  sendd2(now1.minute());
  Serial.print(F(":"));
  sendd2(now1.second());
  Serial.println(F("::D"));
  Serial.flush();
}
