package ru.m9sco.flyphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity{
    static String LOG_TAG = "flyphone.global";

    void loadDevices() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottombar);
        Toolbar topToolBarView = findViewById(R.id.toolbar);
        SurfaceView analyzerSurface = new SurfaceView(this);

        ((FrameLayout) findViewById(R.id.fragment_container)).addView(analyzerSurface);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            String item_s = item.toString();
            if (item_s.equals(getResources().getString(R.string.select_device))) onSourceChange();
            else if (item_s.equals(getResources().getString(R.string.start))) onPlayStop();
        return false;
        });


        topToolBarView.setOnMenuItemClickListener(
                item -> {
                    String item_s = item.toString();


                    if (item_s.equals(getResources().getString(R.string.settings))){
                        Intent myIntent = new Intent(MainActivity.this, Settings.class);
                        MainActivity.this.startActivity(myIntent);
                    }

                    return false;
                }

        );

    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Init on Main frame");
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Set content view");
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "Snapped elements to code");

        DeviceManager.Init(this);
        Log.d(LOG_TAG, "Load DeviceManager");
        loadDevices();
        Log.d(LOG_TAG, "Load Devices");

    }

    public void onSourceChange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        String[] devs = DeviceManager.getDeviceStrings();
        builder.setTitle("Select Device");
        builder.setItems(devs, (dialog, select) -> DeviceManager.setDevice(select));
        builder.show();
    }

    public void onPlayStop(){
        if (!FlyPhoneService.isRunning(getApplicationContext())) {
            if (Settings.Apply(this)) {
                int fd = DeviceManager.openDevice();
                if (fd != -1) {
                    Intent serviceIntent = new Intent(MainActivity.this, FlyPhoneService.class);
                    serviceIntent.putExtra("source", DeviceManager.getDeviceCode());
                    serviceIntent.putExtra("USB", fd);
                    serviceIntent.putExtra("CGFWIDE", Settings.getCGFSetting(this));
                    serviceIntent.putExtra("MODELTYPE", Settings.getModelType(this));
                    serviceIntent.putExtra("FPDS", Settings.getFixedPointDownsampling(this) ? 1 : 0);
                    ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
                } else
                    Toast.makeText(MainActivity.this, "Cannot open USB device. Give permission first and try again.", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(MainActivity.this, "Invalid setting", Toast.LENGTH_LONG).show();

        } else {
            FlyPhoneCatcher.forceStop();
        }
    }



}
