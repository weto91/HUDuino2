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
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements LocationListener {

    // Services
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private ChangeBroadcastReceiver ChangeBroadcastReceiver;

    // GUI
    private TextView interceptedNotificationTextView;
    private TextView sensorTextView;
    private TextView deviceTextView;
    private TextView speedTextView;
    private SwitchCompat metric_imperial_switch;

    // BT
    private boolean socketOk = false;
    private String deviceItem = String.valueOf(R.string.paired_device);
    private String deviceOk = " - Not connected";
    private final UUID spp = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static final String TAG = "HUDuino";
    private ConnectedThread mConnectedThread;
    private byte[] bytes;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the GUI objects on activity_main.xml
        interceptedNotificationTextView = this.findViewById(R.id.notification_data);
        deviceTextView = this.findViewById(R.id.device_info);
        sensorTextView = this.findViewById(R.id.sensor_data);
        metric_imperial_switch = this.findViewById(R.id.metric_imperial_switch);
        speedTextView = this.findViewById(R.id.speed_data);

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
                    deviceTextView.setText(String.format("%s : %s", deviceName, deviceHardwareAddress));
                    deviceItem = deviceTextView.getText().toString();
                    ConnectThread connect = new ConnectThread(device);
                    new Thread(connect).start();
                }
            }
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            lState();
            this.updateSpeed(null);
        }

        metric_imperial_switch.setOnCheckedChangeListener((compoundButton, b) -> {
            MainActivity.this.updateSpeed(null);
            if(metric_imperial_switch.isChecked()){
                metric_imperial_switch.setText(R.string.metric_units);
            }
            else{
                metric_imperial_switch.setText(R.string.imperial_units);
            }
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
            case R.id.about_item:
                Toast.makeText(this, R.string.app_about_text, Toast.LENGTH_SHORT).show();
                break;
            case R.id.help_item:
                Toast.makeText(this, R.string.app_help_text, Toast.LENGTH_LONG).show();
                break;
            case R.id.contact_item:
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

    private void NotificationInterceptAndView(int notificationCode) {
        switch (notificationCode) {
            case NotificationService.InterceptedNotificationCode.FACEBOOK_CODE:
                interceptedNotificationTextView.setTextColor(Color.RED);
                interceptedNotificationTextView.setText(R.string.facebook_notification);
                bytes = interceptedNotificationTextView.getText().toString().getBytes(Charset.defaultCharset());
                if(socketOk) {mConnectedThread.write(bytes);}
                break;
            case NotificationService.InterceptedNotificationCode.INSTAGRAM_CODE:
                interceptedNotificationTextView.setTextColor(Color.RED);
                interceptedNotificationTextView.setText(R.string.instagram_notification);
                bytes = interceptedNotificationTextView.getText().toString().getBytes(Charset.defaultCharset());
                if(socketOk) {mConnectedThread.write(bytes);}
                break;
            case NotificationService.InterceptedNotificationCode.WHATSAPP_CODE:
                interceptedNotificationTextView.setTextColor(Color.RED);
                interceptedNotificationTextView.setText(R.string.whatsapp_notification);
                bytes = interceptedNotificationTextView.getText().toString().getBytes(Charset.defaultCharset());
                if(socketOk) {mConnectedThread.write(bytes);}
                break;
            case NotificationService.InterceptedNotificationCode.DIALER_CODE:
                interceptedNotificationTextView.setTextColor(Color.RED);
                interceptedNotificationTextView.setText(R.string.call_notification);
                bytes = interceptedNotificationTextView.getText().toString().getBytes(Charset.defaultCharset());
                if(socketOk) {mConnectedThread.write(bytes);}
                break;
            case NotificationService.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
                interceptedNotificationTextView.setTextColor(Color.RED);
                interceptedNotificationTextView.setText(R.string.other_notification);
                bytes = interceptedNotificationTextView.getText().toString().getBytes(Charset.defaultCharset());
                if(socketOk) {mConnectedThread.write(bytes);}
                break;
        }
    }

    private boolean isNotificationServiceEnabled() {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
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
            NotificationInterceptAndView(receivedNotificationCode);
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            CLocation currLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(currLocation);
        }
        else {
            Log.e(TAG,"Location is null, check gps and locations permissions");
        }
    }

    private boolean useMetricUnits() {
        return metric_imperial_switch.isChecked();
    }

    // Three next @Overrides cant be deleted because Mainactivity must be abstract.
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private AlertDialog buildNotificationServiceAlertDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.notification_listener_service);
        alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation);
        alertDialogBuilder.setPositiveButton(R.string.yes,
                (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        alertDialogBuilder.setNegativeButton(R.string.no,
                (dialog, id) -> {
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
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                socketOk = true;
                deviceOk = " - Connected";
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                    socketOk = false;
                    deviceOk = " - Not connected";
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                runOnUiThread(() -> deviceTextView.setText(String.format("%s%s", deviceItem, deviceOk)));
                return;
            }
            runOnUiThread(() -> deviceTextView.setText(String.format("%s%s", deviceItem, deviceOk)));
            manageMyConnectedSocket(mmSocket);
        }

        private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
            Log.d(TAG, "manageMyConnectedSocket: Starting.");
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
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            // mmBuffer store for the stream
            byte[] mmBuffer = new byte[2];
            int numBytes; // bytes returned from read()
            while (true) {
                try {
                    numBytes = mmInStream.read(mmBuffer);
                    final String incomingMessage = new String(mmBuffer, 0, numBytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    runOnUiThread(() -> sensorTextView.setText(String.format("%s ºC", incomingMessage)));
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                //writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }
    }

    //update speed functions
    @SuppressLint("MissingPermission")
    private void lState() {
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
            String kmh = strCurrSpeed + " km/h";
            System.out.println(kmh);
            speedTextView.setText(kmh);
            if (nCurrSpeed == 0) {
                speedTextView.setText(R.string.metric_speed_zeroed);
            }
            bytes = kmh.getBytes(Charset.defaultCharset());
            if(socketOk) {mConnectedThread.write(bytes);}

        } else {
            String mph = strCurrSpeed + " mph";
            System.out.println(mph);
            if (nCurrSpeed == 0) {
                speedTextView.setText(R.string.imperial_speed_zeroed);
            }
            speedTextView.setText(mph);

            bytes = mph.getBytes(Charset.defaultCharset());
            if(socketOk) {mConnectedThread.write(bytes);}
        }
    }
}