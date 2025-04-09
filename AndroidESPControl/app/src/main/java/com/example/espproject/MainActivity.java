package com.example.espproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private Button controlButton;
    private Button scanButton;
    private ListView deviceList;
    private ArrayList<String> devices = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private boolean isScanning = false;
    private String selectedDeviceIp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controlButton = findViewById(R.id.controlButton);
        scanButton = findViewById(R.id.scanButton);
        deviceList = findViewById(R.id.deviceList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices);
        deviceList.setAdapter(adapter);

        scanButton.setOnClickListener(v -> {
            if (!isScanning) {
                startDeviceScan();
            }
        });

        controlButton.setOnClickListener(v -> {
            if (!selectedDeviceIp.isEmpty()) {
                sendControlCommand();
            } else {
                Toast.makeText(this, "Please select a device first", Toast.LENGTH_SHORT).show();
            }
        });

        deviceList.setOnItemClickListener((parent, view, position, id) -> {
            selectedDeviceIp = devices.get(position);
            deviceList.setVisibility(View.GONE);
            Toast.makeText(this, "Selected: " + selectedDeviceIp, Toast.LENGTH_SHORT).show();
        });
    }

    private void startDeviceScan() {
        isScanning = true;
        devices.clear();
        new Thread(() -> {
            // Scan local network (192.168.1.1 - 192.168.1.254)
            for (int i = 1; i < 255; i++) {
                final String ip = "192.168.1." + i;
                HttpClient.sendGetRequest("http://" + ip + "/scan", 
                    new HttpClient.HttpResponseCallback() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null && response.contains("ESP8266")) {
                                runOnUiThread(() -> {
                                    devices.add(ip);
                                    updateDeviceList();
                                });
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            // Ignore connection errors
                        }
                    });
                try {
                    Thread.sleep(20); // Small delay between scans
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                isScanning = false;
                if (devices.isEmpty()) {
                    Toast.makeText(MainActivity.this, 
                        "No ESP devices found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateDeviceList() {
        adapter.notifyDataSetChanged();
        deviceList.setVisibility(devices.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void sendControlCommand() {
        String url = "http://" + selectedDeviceIp + "/control";
        HttpClient.sendGetRequest(url, new HttpClient.HttpResponseCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, 
                    "LED toggled successfully", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, 
                    "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
