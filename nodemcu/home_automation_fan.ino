#include <Arduino.h>
#include <ESP8266WiFi.h>
#include <DHT.h>
#include <Servo.h>
#include <Firebase_ESP_Client.h>

#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"

// =========================
// WiFi Configuration
// =========================
#define WIFI_SSID "YOUR_WIFI_NAME"
#define WIFI_PASSWORD "YOUR_WIFI_PASSWORD"

// =========================
// Firebase Configuration
// =========================
// Firebase Console -> Project Settings -> General -> Web API Key
#define API_KEY "YOUR_FIREBASE_WEB_API_KEY"

// Firebase Console -> Realtime Database -> Database URL
// Example: https://your-project-id-default-rtdb.firebaseio.com/
#define DATABASE_URL "https://YOUR_PROJECT_ID-default-rtdb.firebaseio.com/"

// =========================
// Pin Configuration
// =========================
#define DHT_PIN D4
#define DHT_TYPE DHT11
#define SERVO_PIN D5

DHT dht(DHT_PIN, DHT_TYPE);
Servo fanServo;

FirebaseData firebaseData;
FirebaseAuth auth;
FirebaseConfig config;

const String BASE_PATH = "/homeAutomation";

unsigned long lastSensorReadTime = 0;
const unsigned long SENSOR_INTERVAL_MS = 2000;

bool firebaseReady = false;

void connectToWiFi() {
  Serial.print("Connecting to WiFi");
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println();
  Serial.print("WiFi connected. IP: ");
  Serial.println(WiFi.localIP());
}

void connectToFirebase() {
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;

  // Anonymous sign-up requires Firebase Authentication -> Sign-in method -> Anonymous enabled.
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("Firebase anonymous sign-up successful");
    firebaseReady = true;
  } else {
    Serial.print("Firebase sign-up failed: ");
    Serial.println(config.signer.signupError.message.c_str());
    firebaseReady = false;
  }

  config.token_status_callback = tokenStatusCallback;
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
}

int getAutoFanSpeed(float temperature) {
  if (temperature < 25.0) {
    return 0;
  }

  if (temperature >= 25.0 && temperature < 30.0) {
    return 60;
  }

  if (temperature >= 30.0 && temperature < 35.0) {
    return 120;
  }

  return 180;
}

String readModeFromFirebase() {
  if (Firebase.RTDB.getString(&firebaseData, BASE_PATH + "/mode")) {
    String mode = firebaseData.stringData();
    mode.toLowerCase();

    if (mode == "manual" || mode == "auto") {
      return mode;
    }
  } else {
    Serial.print("Failed to read mode: ");
    Serial.println(firebaseData.errorReason());
  }

  return "auto";
}

int readManualSpeedFromFirebase() {
  if (Firebase.RTDB.getInt(&firebaseData, BASE_PATH + "/manualSpeed")) {
    return firebaseData.intData();
  }

  Serial.print("Failed to read manualSpeed: ");
  Serial.println(firebaseData.errorReason());
  return 0;
}

void writeSensorData(float temperature, float humidity, int fanSpeed) {
  Firebase.RTDB.setFloat(&firebaseData, BASE_PATH + "/temperature", temperature);
  Firebase.RTDB.setFloat(&firebaseData, BASE_PATH + "/humidity", humidity);
  Firebase.RTDB.setInt(&firebaseData, BASE_PATH + "/fanSpeed", fanSpeed);
  Firebase.RTDB.setInt(&firebaseData, BASE_PATH + "/lastUpdated", millis());
  Firebase.RTDB.setString(&firebaseData, BASE_PATH + "/deviceStatus", "online");
}

void setup() {
  Serial.begin(9600);
  delay(1000);

  dht.begin();
  fanServo.attach(SERVO_PIN);
  fanServo.write(0);

  connectToWiFi();
  connectToFirebase();
}

void loop() {
  if (!Firebase.ready() || !firebaseReady) {
    Serial.println("Firebase is not ready yet");
    delay(1000);
    return;
  }

  unsigned long currentTime = millis();

  if (currentTime - lastSensorReadTime < SENSOR_INTERVAL_MS) {
    return;
  }

  lastSensorReadTime = currentTime;

  float temperature = dht.readTemperature();
  float humidity = dht.readHumidity();

  if (isnan(temperature) || isnan(humidity)) {
    Serial.println("Failed to read from DHT11 sensor");
    return;
  }

  String mode = readModeFromFirebase();
  int fanSpeed = 0;

  if (mode == "manual") {
    fanSpeed = readManualSpeedFromFirebase();
  } else {
    fanSpeed = getAutoFanSpeed(temperature);
  }

  fanSpeed = constrain(fanSpeed, 0, 180);
  fanServo.write(fanSpeed);

  writeSensorData(temperature, humidity, fanSpeed);

  Serial.println("=========================");
  Serial.print("Temperature: ");
  Serial.print(temperature);
  Serial.println(" °C");

  Serial.print("Humidity: ");
  Serial.print(humidity);
  Serial.println(" %");

  Serial.print("Mode: ");
  Serial.println(mode);

  Serial.print("Fan Speed / Servo Angle: ");
  Serial.println(fanSpeed);
}
