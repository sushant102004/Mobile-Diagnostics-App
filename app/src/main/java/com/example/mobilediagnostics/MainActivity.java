package com.example.mobilediagnostics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView cpuCardOneText, cpuCardTwoText, cpuCardThreeText;
    private TextView memoryInfoText;
    private ProgressBar memProgress;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroSensor;
    private TextView acceleroText;
    private TextView gyroText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        cpuCardOneText = findViewById(R.id.cpuCardOneText);
        cpuCardTwoText = findViewById(R.id.cpuCardTwoText);
        cpuCardThreeText = findViewById(R.id.cpuCardThreeText);

        memoryInfoText = findViewById(R.id.memoryDetailsText);
        memProgress = findViewById(R.id.storageProgressBar);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        acceleroText = findViewById(R.id.acceleroText);
        gyroText = findViewById(R.id.gyroText);


        getCPUDetails();
        getMemoryInfo();
    }

    private String getCPUDetails() {
        StringBuilder cpuCores = new StringBuilder();
        StringBuilder cpuArch = new StringBuilder();
        StringBuilder cpuFreq = new StringBuilder();

        // Get number of cores
        int numCores = Runtime.getRuntime().availableProcessors();
        cpuCores.append("CPU Cores: ").append(numCores);

        cpuCardOneText.setText(cpuCores);

        // Get CPU architecture
        cpuArch.append("Architecture: ").append(Build.SUPPORTED_ABIS[0]);
        cpuCardTwoText.setText(cpuArch);

        // Get CPU max frequency
        cpuFreq.append("Max CPU Frequency: ").append(getMaxCPUFrequency()).append(" MHz");
        cpuCardThreeText.setText(cpuFreq);

        return cpuFreq.toString();
    }

    private int getMaxCPUFrequency() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq");
            Process process = processBuilder.start();
            process.waitFor();
            String result = new java.util.Scanner(process.getInputStream()).next();
            return Integer.parseInt(result) / 1000; // Convert from kHz to MHz
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    private void getMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long totalMemory = memoryInfo.totalMem;
        long freeMemory = memoryInfo.availMem;
        long usedMemory = totalMemory - freeMemory;

        String memInfo = "Total: " + formatFileSize(totalMemory)
                + " Used: " + formatFileSize(usedMemory);

        float progressRatio = (float) usedMemory / totalMemory; // Calculate the ratio of used memory to total memory

        memoryInfoText.setText(memInfo);
        memProgress.setProgress((int) (progressRatio * 100)); // Convert progress ratio to a percentage
    }


    private String formatFileSize(long size) {
        if (size <= 0) return "0";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float xAcceleration = event.values[0];
            float yAcceleration = event.values[1];
            float zAcceleration = event.values[2];

            // Update TextView with accelerometer data
            acceleroText.setText("Accelerometer X: " + xAcceleration + " Y: " + yAcceleration + " Z: " + zAcceleration);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float xRotationRate = event.values[0];
            float yRotationRate = event.values[1];
            float zRotationRate = event.values[2];

            // Update TextView with gyroscope data
            gyroText.setText("Gyroscope Data X: " + xRotationRate + " Y: " + yRotationRate + " Z: " + zRotationRate);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}