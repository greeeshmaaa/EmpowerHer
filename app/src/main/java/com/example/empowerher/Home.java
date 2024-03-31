package com.example.empowerher;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class Home extends AppCompatActivity {
    SessionManager sessionManager;
    private TextView signalTextView;
    private TextView bluetoothStatusText; // TextView for displaying Bluetooth connection status
    private BluetoothAdapter bluetoothAdapter;
    private final UUID hc05UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Standard SerialPortService ID
    private BluetoothDevice hc05Device = null;
    private Handler handler = new Handler(Looper.getMainLooper());
    private BluetoothSocket bluetoothSocket = null;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSIONS_REQUEST_CODE = 101;



    // BroadcastReceiver for Bluetooth connection state changes
    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        bluetoothStatusText.setText("Bluetooth Status: Disabled");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bluetoothStatusText.setText("Bluetooth Status: Enabled");
                        connectToHC05();
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        // Check if user is logged in (session cookie is present)
        if (sessionManager.getSessionCookie().isEmpty()) {
            // No session cookie means not logged in
            Intent intent = new Intent(Home.this, LogIn.class);
            startActivity(intent);
            finish(); // Prevents going back to this activity with the back button
            return; // Stop further execution of this method
        }

        String userName = sessionManager.getUserName();
        TextView textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewWelcome.setText(userName != null ? "Welcome, " + userName + "!" : "Welcome!");

        bluetoothStatusText = findViewById(R.id.bluetoothStatusText); // Initialize TextView for Bluetooth status

        Button emergencyContactButton = findViewById(R.id.emergencycontactButton);
        emergencyContactButton.setOnClickListener(v -> startActivity(new Intent(Home.this, Emergency_contact.class)));

        Button alertsButton = findViewById(R.id.alertsButton);
        alertsButton.setOnClickListener(v -> startActivity(new Intent(Home.this, ViewAlerts.class)));

        Button logoutButton = findViewById(R.id.logout);
        logoutButton.setOnClickListener(v -> logoutUser());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            bluetoothStatusText.setText("Bluetooth not supported on this device");
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, PERMISSIONS_REQUEST_CODE);
            } else {
                checkBluetoothEnabledAndConnect();
            }
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);
        signalTextView = findViewById(R.id.signalTextView);
    }

    private void initializeSessionManager() {
        sessionManager = new SessionManager(this);
        if (sessionManager.getSessionCookie().isEmpty()) {
            redirectToLogin();
        }
    }
    private void checkBluetoothEnabledAndConnect() {
        // Check if permissions are granted before calling isEnabled()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSIONS_REQUEST_CODE);
        } else {
            // Permissions are granted, check if Bluetooth is enabled
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                connectToHC05();
            }
        }
    }

    private void connectToHC05() {
        // First, check if we have the necessary permissions for BLUETOOTH_CONNECT
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            bluetoothStatusText.setText("Bluetooth Status: Connection Permission Denied");
            // Request for permission if needed
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    PERMISSIONS_REQUEST_CODE);
            return;
        }

        // Now that we have permissions, proceed with finding and connecting to the HC-05 device
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if (device.getName().equals("HC-05")) {
                hc05Device = device;
                try {
                    // Try to create and connect the socket here
                    BluetoothSocket hc05Socket = hc05Device.createRfcommSocketToServiceRecord(hc05UUID);
                    hc05Socket.connect();
                    bluetoothSocket = hc05Socket; // Assign the connected socket to the global variable
                    bluetoothStatusText.setText("Bluetooth Status: Connected to HC-05");
                    startListeningForData(); // Start listening for data immediately after connection
                } catch (IOException e) {
                    bluetoothStatusText.setText("Bluetooth Status: Connection Failed");
                } catch (SecurityException e) {
                    bluetoothStatusText.setText("Bluetooth Status: Security Exception - Permission Denied");
                }
                return;
            }
        }
        bluetoothStatusText.setText("Bluetooth Status: HC-05 Not Found");
    }


    private void startListeningForData() {
        new Thread(() -> {
            try {
                InputStream inputStream = bluetoothSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                while (true) {
                    String receivedMessage = reader.readLine();
                    if (receivedMessage != null) {
                        Log.d("BluetoothData", "Received: " + receivedMessage);
                        final String message = receivedMessage; // Use final variable to use inside lambda

                        // Update the UI with the received message
                        handler.post(() -> {
                            // Display the received signal/message
                            signalTextView.setText("Signal: " + message);

                            // If the received message matches the new signal, act on it
                            if (message.equals("alert_sent")) {
                                onButtonPressed();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void processReceivedMessage(String message) {
        if ("alert_sent".equals(message)) {
            // Assuming you have a method to handle the action required when the message is received
            onButtonPressed();
        }
    }

    private void onButtonPressed() {
        // Fetch user details or any other info you need to send
        sendMessageToServer("Distress signal from " + sessionManager.getUserName());
    }
    private void sendMessageToServer(String message) {
        new Thread(() -> {
            try {
                URL url = new URL("http://ec2-13-126-93-228.ap-south-1.compute.amazonaws.com:3000/api/distress-signal");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                // Use getUserName() to fetch the username from SessionManager
                String userName = sessionManager.getUserName();
                jsonParam.put("userName", userName); // Ensure the server expects "userName" as a key
                jsonParam.put("message", "needs your help"); // Adjust based on your server's expected payload

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Log.i("ServerResponse", response.toString());
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the BroadcastReceiver
        unregisterReceiver(bluetoothStateReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth has been enabled, attempt to connect to HC-05
                connectToHC05();
            } else {
                // User denied to enable Bluetooth, update UI accordingly
                bluetoothStatusText.setText("Bluetooth Status: Disabled, enable to connect HC-05");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // Permissions have been granted, proceed with checking Bluetooth status and connecting
                checkBluetoothEnabledAndConnect();
            } else {
                // Permissions denied, update UI to reflect lack of permissions for Bluetooth connectivity
                bluetoothStatusText.setText("Bluetooth Status: Permission Denied");
            }
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(Home.this, LogIn.class);
        startActivity(intent);
        finish();
    }

    // Additional methods...

    private void logoutUser() {
        new Thread(() -> {
            try {
                URL url = new URL("http://ec2-13-126-93-228.ap-south-1.compute.amazonaws.com:3000/api/logout");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                String cookie = sessionManager.getSessionCookie();
                if (!cookie.isEmpty()) {
                    conn.setRequestProperty("Cookie", cookie);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    sessionManager.logoutUser();
                    handler.post(this::redirectToLogin);
                } else {
                    Log.e("LogoutError", "Failed to logout, server responded with code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("LogoutException", "Error occurred during logout", e);
            }
        }).start();
    }


}

