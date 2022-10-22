package com.weto.huduino;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LocationListener {

    // Services
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private ChangeBroadcastReceiver ChangeBroadcastReceiver;

    // GUI
    private TextView notificationData;
    private TextView sensorTempData;
    private TextView sensorHumiData;
    private TextView deviceInfo;
    private TextView speedData;
    private TextView deviceConectivity;
    private SwitchCompat metricImperial;

    // BT
    private boolean socketOk = false;
    private String deviceItem = String.valueOf(R.string.paired_device);
    private final UUID spp = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static final String sTag = "HUDuino";
    private ConnectedThread mConnectedThread;
    private byte[] bytes;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the GUI objects on activity_main.xml
        notificationData = this.findViewById(R.id.text_notification_data);
        deviceInfo = this.findViewById(R.id.text_device_info);
        sensorTempData = this.findViewById(R.id.text_sensor_temp_data);
        sensorHumiData = this.findViewById(R.id.text_sensor_humi_data);
        metricImperial = this.findViewById(R.id.switch_metric_imperial);
        speedData = this.findViewById(R.id.text_speed_data);
        deviceConectivity = this.findViewById(R.id.text_device_conectivity);
        Button resetNotification = this.findViewById(R.id.button_reset_notification);

        // If the user did not turn the notification listener service on we prompt him to do so
        if (!isNotificationServiceEnabled()) {
            AlertDialog enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        // Finally we register a receiver to tell the MainActivity when a notification has been received
        ChangeBroadcastReceiver = new ChangeBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.weto.huduino");
        registerReceiver(ChangeBroadcastReceiver, intentFilter);

        // BT actions
        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            int REQUEST_ENABLE_BT = 0;
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if (deviceName.equals("HC-06")) {
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    deviceInfo.setText(String.format("%s : %s", deviceName, deviceHardwareAddress));
                    deviceItem = deviceInfo.getText().toString();
                    ConnectThread connect = new ConnectThread(device);
                    new Thread(connect).start();
                }
            }
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            locationState();
            this.updateSpeed(null);
        }

        metricImperial.setOnCheckedChangeListener((compoundButton, b) -> {
            MainActivity.this.updateSpeed(null);
            String speedUnit;
            if(metricImperial.isChecked()){
                metricImperial.setText(R.string.metric_units);
                speedUnit = "MET";
            }
            else{
                metricImperial.setText(R.string.imperial_units);
                speedUnit = "IMP";
            }
            bytes = speedUnit.getBytes(Charset.defaultCharset());
            if(socketOk) {mConnectedThread.write(bytes);}
        });

        resetNotification.setOnClickListener(v -> {
            notificationData.setTextColor(Color.WHITE);
            notificationData.setText(R.string.notification_received);
            bytes = "RSN".getBytes(Charset.defaultCharset());
            if(socketOk) {mConnectedThread.write(bytes);}
            Toast.makeText(MainActivity.this, R.string.notification_deleted, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_about:
                Toast.makeText(this, R.string.app_about_text, Toast.LENGTH_SHORT).show();
                break;
            case R.id.item_help:
                Toast.makeText(this, R.string.app_help_text, Toast.LENGTH_LONG).show();
                break;
            case R.id.item_contact:
                Toast.makeText(this, R.string.app_contact_text, Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(ChangeBroadcastReceiver);
    }

    private void receiveNotification(int notificationCode) {
        notificationData.setTextColor(Color.RED);
        String sendNotification = String.valueOf(R.string.notification_received);
        switch (notificationCode) {
            case NotificationService.InterceptedNotificationCode.FACEBOOK_CODE:
                notificationData.setText(R.string.facebook_notification);
                sendNotification = "FBK";
                break;
            case NotificationService.InterceptedNotificationCode.INSTAGRAM_CODE:
                notificationData.setText(R.string.instagram_notification);
                sendNotification = "IGM";
                break;
            case NotificationService.InterceptedNotificationCode.WHATSAPP_CODE:
                notificationData.setText(R.string.whatsapp_notification);
                sendNotification = "WSP";
                break;
            case NotificationService.InterceptedNotificationCode.DIALER_CODE:
                notificationData.setText(R.string.call_notification);
                sendNotification = "ICL";
                break;
            case NotificationService.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
                notificationData.setText(R.string.other_notification);
                sendNotification = "OTR";
                break;
        }
        bytes = sendNotification.getBytes(Charset.defaultCharset());
        if(socketOk) {mConnectedThread.write(bytes);}
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(), ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public class ChangeBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int receivedNotificationCode = intent.getIntExtra("Notification Code", -1);
            receiveNotification(receivedNotificationCode);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            CLocation currLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(currLocation);
        }
        else {
            Log.e(sTag,"Location is null, check gps and locations permissions");
        }
    }

    private boolean useMetricUnits() {
        return metricImperial.isChecked();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // @Override cant be deleted because MainActivity must be abstract.
    }

    @Override
    public void onProviderEnabled(String s) {
        // @Override cant be deleted because MainActivity must be abstract.
    }

    @Override
    public void onProviderDisabled(String s) {
        // @Override cant be deleted because MainActivity must be abstract.
    }

    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes, (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        alertDialogBuilder.setNegativeButton(R.string.no, (dialog, id) -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            Log.e("error", "The user has denied the notification access");
            System.exit(1);
        });
        return (alertDialogBuilder.create());
    }

    // Bluetooth classes and functions
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        
        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(spp);
            } catch (IOException e) {
                Log.e(sTag, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                socketOk = true;
                runOnUiThread(() ->  deviceConectivity.setText(R.string.device_connected));
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                    socketOk = false;
                    runOnUiThread(() ->  deviceConectivity.setText(R.string.device_not_connected));
                } catch (IOException closeException) {
                    Log.e(sTag, "Could not close the client socket", closeException);
                }
                runOnUiThread(() -> deviceInfo.setText(deviceItem));
                return;
            }
            runOnUiThread(() -> deviceInfo.setText(deviceItem));
            manageMyConnectedSocket(mmSocket);
            if(socketOk) {
                byte[] send = "START".getBytes(Charset.defaultCharset());
                mConnectedThread.write(send);
            }
        }

        private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
            Log.d(sTag, "manageMyConnectedSocket: Starting.");
            mConnectedThread = new ConnectedThread(mmSocket);
            mConnectedThread.start();
        }
    }

    public class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(sTag, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(sTag, "Error occurred when creating output stream", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            // mmBuffer store for the stream
            byte[] mmBuffer = new byte[10];
            int numBytes; // bytes returned from read()
            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
                    final String incomingMessage = new String(mmBuffer, 0, numBytes);
                    List<String> items = Arrays.asList(incomingMessage.split("\\u0020"));
                    runOnUiThread(() -> {
                        sensorTempData.setText(String.format("%sºC", items.get(0)));
                        sensorHumiData.setText(String.format("%s%%", items.get(1)));
                    });
                   // runOnUiThread(() -> sensorHumiData.setText(String.format(items.get(1) + "%")));
                } catch (IOException e) {
                    Log.d(sTag, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(sTag, "Error occurred when sending data", e);
            }
        }
    }

    //update speed functions
    @SuppressLint("MissingPermission")
    private void locationState() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    private void updateSpeed(CLocation location) {
        float nCurrSpeed = 0;
        if (location != null) {
            location.setUseMetricUnits(this.useMetricUnits());
            nCurrSpeed = location.getSpeed();
        }
        String strCurrSpeed = String.valueOf(Math.round(nCurrSpeed));
        if (this.useMetricUnits()) {
            System.out.println(strCurrSpeed);
            speedData.setText(String.format("%s km/h", strCurrSpeed));
            if (nCurrSpeed == 0) {
                speedData.setText(R.string.metric_speed_zeroed);
            }
            bytes = strCurrSpeed.getBytes(Charset.defaultCharset());
            if(socketOk) {
                mConnectedThread.write(bytes);
            }
        } else {
            System.out.println(strCurrSpeed);
            speedData.setText(String.format("%s mph", strCurrSpeed));
            if (nCurrSpeed == 0) {
                speedData.setText(R.string.imperial_speed_zeroed);
            }
            bytes = strCurrSpeed.getBytes(Charset.defaultCharset());
            if(socketOk) {
                mConnectedThread.write(bytes);
            }
        }
    }
}