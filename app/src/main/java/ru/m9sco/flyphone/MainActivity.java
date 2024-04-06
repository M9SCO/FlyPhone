package ru.m9sco.flyphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    static String LOG_TAG = "flyphone.global";

    Spinner spinner;
    Button updButton, StartStopButton;

    void snapElements(){
        spinner = findViewById(R.id.deviceListSpinner);
        updButton = findViewById(R.id.UpdButton);
        StartStopButton = findViewById(R.id.StartStopButton);

    }

    void loadDevices(){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                DeviceManager.getDeviceStrings()
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Init on Main frame");
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Set content view");
        setContentView(R.layout.activity_main);
        snapElements();
        Log.d(LOG_TAG, "Snapped elements to code");

        DeviceManager.Init(this);
        Log.d(LOG_TAG, "Load DeviceManager");
        loadDevices();
        Log.d(LOG_TAG, "Load Devices");

    }


    public void upddevices(View view) {
        loadDevices();
    }


    public void clickStartStopButton(View view){

    }
}