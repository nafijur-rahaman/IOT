# Testing Guide

Follow this testing order. Do not test the full project first. Test small parts one by one.

## 1. Firebase Database Test

Go to Firebase Realtime Database and manually update:

```json
{
  "homeAutomation": {
    "mode": "manual",
    "manualSpeed": 120
  }
}
```

Expected result:

- `mode` changes to `manual`.
- `manualSpeed` changes to `120`.
- Android app should show the updated value.
- NodeMCU should move the servo to 120 degrees.

## 2. DHT11 Sensor Test

Upload the NodeMCU code and open Serial Monitor at `9600` baud.

Expected output:

```text
Temperature: 30.00 °C
Humidity: 70.00 %
Mode: auto
Fan Speed / Servo Angle: 120
```

If you see this message:

```text
Failed to read from DHT11 sensor
```

Check:

- DHT11 VCC is connected to 3V3.
- DHT11 GND is connected to GND.
- DHT11 DATA is connected to D4.
- The selected sensor type is DHT11.

## 3. Servo Motor Test

Set Firebase values manually:

```json
{
  "homeAutomation": {
    "mode": "manual",
    "manualSpeed": 0
  }
}
```

Then change `manualSpeed` to:

```text
60
120
180
```

Expected result:

- Servo moves to 0 degrees.
- Servo moves to 60 degrees.
- Servo moves to 120 degrees.
- Servo moves to 180 degrees.

If servo shakes or resets NodeMCU, use external 5V power for servo.

## 4. Auto Mode Test

Set:

```json
{
  "homeAutomation": {
    "mode": "auto"
  }
}
```

Expected speed table:

| Temperature | Expected Servo Angle |
|---:|---:|
| Below 25°C | 0 |
| 25°C - 29.9°C | 60 |
| 30°C - 34.9°C | 120 |
| 35°C or above | 180 |

Warm the DHT11 sensor gently using your hand. Do not use flame or hot air directly.

## 5. Android App Test

Open the Android app.

Expected:

- Temperature appears on screen.
- Humidity appears on screen.
- Fan speed appears on screen.
- Auto switch controls Firebase `mode`.
- Slider controls Firebase `manualSpeed` when manual mode is active.

## 6. Full System Test

### Auto Mode Demo

1. Turn Auto Mode on from Android app.
2. Show Firebase `mode = auto`.
3. Warm the DHT11 sensor slightly.
4. Show temperature increasing in the app.
5. Show servo moving automatically.

### Manual Mode Demo

1. Turn Auto Mode off from Android app.
2. Show Firebase `mode = manual`.
3. Move the slider.
4. Show `manualSpeed` changing in Firebase.
5. Show servo moving according to the slider.

## Common Problems

### Firebase sign-up failed

Solution:

- Enable Firebase Authentication.
- Enable Anonymous sign-in method.
- Check `API_KEY`.
- Check `DATABASE_URL`.

### Permission denied

For lab demo, use temporary Firebase Realtime Database rules:

```json
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

Do not use open rules in production.

### Android app cannot connect to Firebase

Check:

- `google-services.json` is inside `android/app/`.
- App package name matches Firebase Android app package name.
- Internet permission exists in `AndroidManifest.xml`.

### NodeMCU cannot connect to WiFi

Check:

- WiFi name is correct.
- WiFi password is correct.
- ESP8266 usually needs 2.4GHz WiFi, not 5GHz.
