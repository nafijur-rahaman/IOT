# IoT Home Automation Fan Speed Control

This project implements a small home automation system where a **NodeMCU ESP8266** reads temperature and humidity from a **DHT11 sensor**, sends the data to **Firebase Realtime Database**, and controls a **servo motor** as a fan-speed analogy. An **Android app** shows live sensor data and allows manual fan-speed control.

## Project Flow

```text
DHT11 Sensor
    ↓
NodeMCU ESP8266
    ↓ writes temperature, humidity, fanSpeed
Firebase Realtime Database
    ↑ reads mode/manualSpeed
Android App
```

## Features

- Read temperature using DHT11.
- Read humidity using DHT11.
- Upload sensor values to Firebase Realtime Database.
- Auto mode: fan speed changes based on temperature.
- Manual mode: Android app controls fan speed using a slider.
- Servo motor represents fan speed from 0 to 180 degrees.
- Android app displays temperature, humidity, mode, and fan speed in real time.

## Hardware Required

| Component | Quantity |
|---|---:|
| NodeMCU ESP8266 | 1 |
| DHT11 Sensor | 1 |
| Servo Motor | 1 |
| Breadboard | 1 |
| Jumper Wires | As needed |
| USB Cable | 1 |
| Android Phone | 1 |

## Wiring

### DHT11 to NodeMCU

| DHT11 Pin | NodeMCU Pin |
|---|---|
| VCC | 3V3 |
| GND | GND |
| DATA | D4 |

### Servo Motor to NodeMCU

| Servo Wire | NodeMCU Pin |
|---|---|
| Red / VCC | VIN / 5V |
| Brown / GND | GND |
| Orange / Signal | D5 |

> If the servo does not move properly, use an external 5V power supply for the servo. Keep NodeMCU GND and external power GND connected together.

## Firebase Realtime Database Structure

Use the `Db` file in this repository as the database structure.

```json
{
  "homeAutomation": {
    "temperature": 0,
    "humidity": 0,
    "mode": "auto",
    "manualSpeed": 90,
    "fanSpeed": 0,
    "lastUpdated": 0,
    "deviceStatus": "offline"
  }
}
```

## Firebase Setup

1. Create a Firebase project.
2. Create a Realtime Database.
3. Use test mode for lab demo.
4. Import the `Db` JSON structure.
5. Copy the **Database URL**.
6. Go to Project Settings and copy the **Web API Key**.
7. Enable **Anonymous Authentication** from Firebase Authentication.
8. Download `google-services.json` and place it inside:

```text
android/app/google-services.json
```

## NodeMCU Setup

1. Open Arduino IDE.
2. Install ESP8266 board support.
3. Select board: `NodeMCU 1.0 (ESP-12E Module)`.
4. Install these libraries:
   - `DHT sensor library`
   - `Adafruit Unified Sensor`
   - `Servo`
   - `Firebase ESP Client` by Mobizt
5. Open:

```text
nodemcu/home_automation_fan.ino
```

6. Replace:

```cpp
#define WIFI_SSID "YOUR_WIFI_NAME"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"
#define API_KEY "YOUR_FIREBASE_WEB_API_KEY"
#define DATABASE_URL "https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com/"
```

7. Upload the code to NodeMCU.

## Android Setup

1. Open the `android` folder in Android Studio.
2. Add your `google-services.json` file inside `android/app/`.
3. Sync Gradle.
4. Run the app on an Android phone.

## Auto Mode Logic

| Temperature | Servo/Fan Speed |
|---:|---:|
| Below 25°C | 0 |
| 25°C - 29.9°C | 60 |
| 30°C - 34.9°C | 120 |
| 35°C or above | 180 |

## Manual Mode Logic

1. User turns off Auto Mode from the Android app.
2. App writes `mode = manual` to Firebase.
3. User moves slider.
4. App writes `manualSpeed` value to Firebase.
5. NodeMCU reads `manualSpeed` and moves servo.

## Final Demo Script

1. Start NodeMCU.
2. Show live temperature and humidity in Serial Monitor.
3. Open Android app.
4. Show live temperature and humidity in the app.
5. Keep Auto Mode enabled and warm the DHT11 sensor slightly.
6. Show servo moving automatically.
7. Turn off Auto Mode.
8. Move the slider from the app.
9. Show servo moving manually.

## Safety Note

This project uses a servo motor as a fan-speed analogy. Do not directly connect a real AC fan to NodeMCU. Real fan control requires relay/MOSFET/triac driver and proper electrical safety.
