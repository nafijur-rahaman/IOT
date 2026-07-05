package com.example.iothomeautomation;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView temperatureText;
    private TextView humidityText;
    private TextView fanSpeedText;
    private TextView modeText;
    private TextView manualSpeedLabel;
    private TextView statusText;
    private Switch autoModeSwitch;
    private SeekBar manualSpeedSeekBar;

    private DatabaseReference homeAutomationRef;
    private boolean isUpdatingFromFirebase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        setupFirebase();
        setupControls();
        listenForRealtimeUpdates();
    }

    private void bindViews() {
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        fanSpeedText = findViewById(R.id.fanSpeedText);
        modeText = findViewById(R.id.modeText);
        manualSpeedLabel = findViewById(R.id.manualSpeedLabel);
        statusText = findViewById(R.id.statusText);
        autoModeSwitch = findViewById(R.id.autoModeSwitch);
        manualSpeedSeekBar = findViewById(R.id.manualSpeedSeekBar);
    }

    private void setupFirebase() {
        homeAutomationRef = FirebaseDatabase
                .getInstance()
                .getReference("homeAutomation");
    }

    private void setupControls() {
        autoModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingFromFirebase) {
                return;
            }

            String mode = isChecked ? "auto" : "manual";
            homeAutomationRef.child("mode").setValue(mode)
                    .addOnSuccessListener(unused -> showToast("Mode changed to " + mode))
                    .addOnFailureListener(error -> showToast("Failed to update mode"));
        });

        manualSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                manualSpeedLabel.setText("Manual Speed: " + progress);

                if (fromUser) {
                    homeAutomationRef.child("manualSpeed").setValue(progress)
                            .addOnFailureListener(error -> showToast("Failed to update speed"));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int speed = seekBar.getProgress();
                homeAutomationRef.child("manualSpeed").setValue(speed)
                        .addOnSuccessListener(unused -> showToast("Manual speed: " + speed))
                        .addOnFailureListener(error -> showToast("Failed to update speed"));
            }
        });
    }

    private void listenForRealtimeUpdates() {
        homeAutomationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double temperature = snapshot.child("temperature").getValue(Double.class);
                Double humidity = snapshot.child("humidity").getValue(Double.class);
                Integer fanSpeed = snapshot.child("fanSpeed").getValue(Integer.class);
                Integer manualSpeed = snapshot.child("manualSpeed").getValue(Integer.class);
                String mode = snapshot.child("mode").getValue(String.class);
                String deviceStatus = snapshot.child("deviceStatus").getValue(String.class);

                updateUi(temperature, humidity, fanSpeed, manualSpeed, mode, deviceStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                statusText.setText("Status: Firebase error - " + error.getMessage());
                showToast("Firebase error: " + error.getMessage());
            }
        });
    }

    private void updateUi(
            Double temperature,
            Double humidity,
            Integer fanSpeed,
            Integer manualSpeed,
            String mode,
            String deviceStatus
    ) {
        temperatureText.setText("Temperature: " + formatDouble(temperature) + " °C");
        humidityText.setText("Humidity: " + formatDouble(humidity) + " %");
        fanSpeedText.setText("Fan Speed: " + safeInt(fanSpeed));

        String safeMode = mode == null ? "auto" : mode;
        modeText.setText("Mode: " + safeMode);

        isUpdatingFromFirebase = true;
        autoModeSwitch.setChecked("auto".equalsIgnoreCase(safeMode));
        isUpdatingFromFirebase = false;

        if (manualSpeed != null) {
            manualSpeedSeekBar.setProgress(manualSpeed);
            manualSpeedLabel.setText("Manual Speed: " + manualSpeed);
        }

        String safeDeviceStatus = deviceStatus == null ? "unknown" : deviceStatus;
        statusText.setText("Status: Firebase connected | Device: " + safeDeviceStatus);
    }

    private String formatDouble(Double value) {
        if (value == null) {
            return "--";
        }

        return String.format("%.1f", value);
    }

    private String safeInt(Integer value) {
        if (value == null) {
            return "--";
        }

        return String.valueOf(value);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
