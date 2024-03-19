package com.example.mobilediagnostics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView cpuCardOneText, cpuCardTwoText, cpuCardThreeText;
    private TextView memoryInfoText;
    private ProgressBar memProgress;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroSensor;
    private TextView acceleroText;
    private TextView gyroText;

    private Button vibrationBtn, brightnessBtn, networkBtn, gpsBtn, microphoneBtn, storageBtn;

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

        vibrationBtn = findViewById(R.id.vibrationButton);
        vibrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testVibration(MainActivity.this); // Call your function passing the context
            }
        });

        microphoneBtn = findViewById(R.id.microphoneButton);
        microphoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMicrophoneWorking(MainActivity.this); // Call your function passing the context
            }
        });

        networkBtn = findViewById(R.id.networkConnectivityButton);
        networkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNetworkConnected(MainActivity.this);
            }
        });

        gpsBtn = findViewById(R.id.gpsButton);
        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkGPSAvailability(MainActivity.this);
            }
        });

        brightnessBtn = findViewById(R.id.brightnessButton);
        brightnessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testScreenBrightness(MainActivity.this);
            }
        });

        storageBtn = findViewById(R.id.storageButton);
        storageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAvailableStorage(MainActivity.this);
            }
        });

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

    public void testVibration(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 1000}; // Vibrate for 1 second, pause for 1 second, vibrate for 1 second
            vibrator.vibrate(pattern, -1); // -1 for no repeat
        } else {
            Toast.makeText(this, "Vibration not supported.", Toast.LENGTH_SHORT);
        }
    }

    public void checkBatteryHealth(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);

        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD:
                Toast.makeText(this, "Battery Health: Good", Toast.LENGTH_SHORT);
                break;

            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                Toast.makeText(this, "Battery Health: Overheat", Toast.LENGTH_SHORT);
                break;
            // Add other cases as per requirement
            default:
                Toast.makeText(this, "Battery Health: Unknown", Toast.LENGTH_SHORT);
                break;
        }
    }

    public void isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            Toast.makeText(context, "Network is connected.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Network is not connected.", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkGPSAvailability(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, "GPS is enabled.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "GPS is not enabled.", Toast.LENGTH_SHORT).show();
        }
    }

    public void testScreenBrightness(Context context) {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 1F; // Max brightness
        getWindow().setAttributes(layout);
    }

    public void checkAvailableStorage(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        long megabytesAvailable = bytesAvailable / (1024 * 1024); // Convert bytes to megabytes

        String message = "Available storage: " + megabytesAvailable + " MB";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public void isMicrophoneWorking(Context context) {
        boolean isWorking = false;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Microphone permission not granted.", Toast.LENGTH_SHORT).show();
                return;
            }
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_DEFAULT, 44100);
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                Toast.makeText(context, "Microphone Working", Toast.LENGTH_SHORT).show();
            recorder.release();
            return;

        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(context, "Microphone Not Working", Toast.LENGTH_SHORT).show();
    }

}