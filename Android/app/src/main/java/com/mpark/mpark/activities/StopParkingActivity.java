package com.mpark.mpark.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mpark.mpark.fragments.AdvertiseServiceFragment;
import com.mpark.mpark.R;
import com.mpark.mpark.fragments.ServiceFragment;
import com.mpark.mpark.utilities.AppGlobalData;
import com.mpark.mpark.utilities.TOTP;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class StopParkingActivity extends Activity implements ServiceFragment.ServiceFragmentDelegate {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = StartParkingActivity.class.getCanonicalName();
    private static final String CURRENT_FRAGMENT_TAG = "CURRENT_FRAGMENT";

    private static final UUID CHARACTERISTIC_USER_DESCRIPTION_UUID = UUID
            .fromString("00002901-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    //private EditText edttxtDeviceName;
    Button btnStop;
    TextView waiting;
    SpinKitView spinKitView;
    FirebaseDatabase database = AppGlobalData.getDatabase();
    FirebaseAuth mAuth;
    DatabaseReference users = database.getReference("users");
    DatabaseReference parkings;
    String keyofCurrentParking;
    //private TextView mConnectionStatus;
    private ServiceFragment mCurrentServiceFragment;
    private BluetoothGattService mBluetoothGattService;
    private HashSet<BluetoothDevice> mBluetoothDevices;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private AdvertiseData mAdvData;
    private AdvertiseData mAdvScanResponse;
    private AdvertiseSettings mAdvSettings;
    private BluetoothLeAdvertiser mAdvertiser;
    private final AdvertiseCallback mAdvCallback = new AdvertiseCallback() {
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "Not broadcasting: " + errorCode);
            int statusText;
            switch (errorCode) {
                case ADVERTISE_FAILED_ALREADY_STARTED:
                    statusText = R.string.status_advertising;
                    Log.w(TAG, "App was already advertising");
                    break;
                case ADVERTISE_FAILED_DATA_TOO_LARGE:
                    statusText = R.string.status_advDataTooLarge;
                    break;
                case ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    statusText = R.string.status_advFeatureUnsupported;
                    break;
                case ADVERTISE_FAILED_INTERNAL_ERROR:
                    statusText = R.string.status_advInternalError;
                    break;
                case ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    statusText = R.string.status_advTooManyAdvertisers;
                    break;
                default:
                    statusText = R.string.status_notAdvertising;
                    Log.wtf(TAG, "Unhandled error: " + errorCode);
            }
            Toast.makeText(StopParkingActivity.this,statusText,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            Log.v(TAG, "Broadcasting");
            setLoadingScreen();
            //Toast.makeText(StopParkingActivity.this,R.string.status_advertising,Toast.LENGTH_SHORT).show();
            BluetoothGattCharacteristic mWriteCharacteristic = mBluetoothGattService.getCharacteristic(UUID.fromString("34369a6a-e0e9-11e7-ba4b-6f6e6c696e65"));
            BluetoothGattCharacteristic phoneNumberCharacteristic = new BluetoothGattCharacteristic(UUID.fromString("cd7735fa-a5b3-4e13-a5f4-02cf2f3ab446"),BluetoothGattCharacteristic.PROPERTY_BROADCAST,BluetoothGattCharacteristic.PERMISSION_READ);
            phoneNumberCharacteristic.setValue("918975959200");
            mBluetoothGattService.addCharacteristic(phoneNumberCharacteristic);
            mWriteCharacteristic.setValue(0,android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8,0);
            btnStop.setText("Broadcasting,\n Waiting for Gate");
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stop_parking);
        btnStop = (Button)findViewById(R.id.btnStopParking);
        waiting = findViewById(R.id.tVWaiting);
        spinKitView = findViewById(R.id.spinKit);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBluetoothDevices = new HashSet<>();
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //mBluetoothAdapter.setName(edttxtDeviceName.getText().toString());
        mAuth = FirebaseAuth.getInstance();

        btnStop.setOnClickListener(stopParkingClick);
        //parkings.addValueEventListener(dataListener);

        // If we are not being restored from a previous state then create and add the fragment.
        if (savedInstanceState == null) {
            int peripheralIndex = 0;
            if (peripheralIndex == 0) {

                mCurrentServiceFragment = new AdvertiseServiceFragment();
            } else {
                Log.wtf(TAG, "Service doesn't exist");
            }

        } else {
            mCurrentServiceFragment = (ServiceFragment) getFragmentManager()
                    .findFragmentByTag(CURRENT_FRAGMENT_TAG);
        }
        mBluetoothGattService = mCurrentServiceFragment.getBluetoothGattService();

        mAdvSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();



    }

    private void setLoadingScreen()
    {
        waiting.setVisibility(View.VISIBLE);
        spinKitView.setVisibility(View.VISIBLE);
    }

    private void removeLoadingScreen()
    {
        waiting.setVisibility(View.INVISIBLE);
        spinKitView.setVisibility(View.INVISIBLE);
    }

    private View.OnClickListener stopParkingClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startParking();
        }
    };


    private BluetoothGattServer mGattServer;
    private final BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {
        @Override
        public void onConnectionStateChange(BluetoothDevice device, final int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    mBluetoothDevices.add(device);
                    updateConnectedDevicesStatus();
                    Log.v(TAG, "Connected to device: " + device.getAddress());
                    //tVStatus.setText("Success! Opening Gate");
                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    parkings.child(keyofCurrentParking).child("status").setValue("Inactive");
                    parkings.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Intent i = new Intent(StopParkingActivity.this,Ticket.class);
                            startActivity(i);
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    mBluetoothDevices.remove(device);
                    updateConnectedDevicesStatus();
                    Log.v(TAG, "Disconnected from device");
                }
            } else {
                mBluetoothDevices.remove(device);
                updateConnectedDevicesStatus();
                // There are too many gatt errors (some of them not even in the documentation) so we just
                // show the error to the user.
                final String errorMessage = getString(R.string.status_errorWhenConnecting) + ": " + status;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StopParkingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
                Log.e(TAG, "Error when connecting: " + status);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            Log.d(TAG, "Device tried to read characteristic: " + characteristic.getUuid());
            Log.d(TAG, "Value: " + Arrays.toString(characteristic.getValue()));
            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
            /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                    offset, characteristic.getValue());
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.v(TAG, "Notification sent. Status: " + status);
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);
            Log.v(TAG, "Characteristic Write request: " + Arrays.toString(value));
            int status = mCurrentServiceFragment.writeCharacteristic(characteristic, offset, value);
            if (responseNeeded) {
                mGattServer.sendResponse(device, requestId, status,
            /* No need to respond with an offset */ 0,
            /* No need to respond with a value */ null);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId,
                                            int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            Log.d(TAG, "Device tried to read descriptor: " + descriptor.getUuid());
            Log.d(TAG, "Value: " + Arrays.toString(descriptor.getValue()));
            if (offset != 0) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_INVALID_OFFSET, offset,
            /* value (optional) */ null);
                return;
            }
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    descriptor.getValue());
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                             int offset,
                                             byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
                    offset, value);
            Log.v(TAG, "Descriptor Write Request " + descriptor.getUuid() + " " + Arrays.toString(value));
            int status = BluetoothGatt.GATT_SUCCESS;
            if (descriptor.getUuid() == CLIENT_CHARACTERISTIC_CONFIGURATION_UUID) {
                BluetoothGattCharacteristic characteristic = descriptor.getCharacteristic();
                boolean supportsNotifications = (characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
                boolean supportsIndications = (characteristic.getProperties() &
                        BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;

                if (!(supportsNotifications || supportsIndications)) {
                    status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                } else if (value.length != 2) {
                    status = BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH;
                } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    mCurrentServiceFragment.notificationsDisabled(characteristic);
                    descriptor.setValue(value);
                } else if (supportsNotifications &&
                        Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    mCurrentServiceFragment.notificationsEnabled(characteristic, false /* indicate */);
                    descriptor.setValue(value);
                } else if (supportsIndications &&
                        Arrays.equals(value, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                    status = BluetoothGatt.GATT_SUCCESS;
                    mCurrentServiceFragment.notificationsEnabled(characteristic, true /* indicate */);
                    descriptor.setValue(value);
                } else {
                    status = BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED;
                }
            } else {
                status = BluetoothGatt.GATT_SUCCESS;
                descriptor.setValue(value);
            }
            if (responseNeeded) {
                mGattServer.sendResponse(device, requestId, status,
            /* No need to respond with offset */ 0,
            /* No need to respond with a value */ null);
            }
        }
    };

    /////////////////////////////////
    ////// Lifecycle Callbacks //////
    /////////////////////////////////

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                    Toast.makeText(this, R.string.bluetoothAdvertisingNotSupported, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Advertising not supported");
                }
                btnStop.setText("Stop Parking");
                startParking();
            } else {

                Toast.makeText(this, R.string.bluetoothNotEnabled, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Bluetooth not enabled");
                finish();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        removeLoadingScreen();
        FirebaseUser user = mAuth.getCurrentUser();
        //database.setPersistenceEnabled(true);
        DatabaseReference currentUser = users.child(user.getUid());
        parkings = currentUser.child("parkings");
        parkings.addValueEventListener(dataListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGattServer != null) {
            mGattServer.close();
        }
        if (mBluetoothAdapter.isEnabled() && mAdvertiser != null) {
            // If stopAdvertising() gets called before close() a null
            // pointer exception is raised.
            mAdvertiser.stopAdvertising(mAdvCallback);
            btnStop.setText("Stop Parking");
        }
        resetStatusViews();
        parkings.removeEventListener(dataListener);
    }

    @Override
    public void sendNotificationToDevices(BluetoothGattCharacteristic characteristic) {
        boolean indicate = (characteristic.getProperties()
                & BluetoothGattCharacteristic.PROPERTY_INDICATE)
                == BluetoothGattCharacteristic.PROPERTY_INDICATE;
        for (BluetoothDevice device : mBluetoothDevices) {
            // true for indication (acknowledge) and false for notification (unacknowledge).
            mGattServer.notifyCharacteristicChanged(device, characteristic, indicate);
        }
    }

    private void resetStatusViews() {
        //mAdvStatus.setText(R.string.status_notAdvertising);
        //Toast.makeText(StartParkingActivity.this,R.string.status_notAdvertising,Toast.LENGTH_SHORT).show();
        updateConnectedDevicesStatus();
    }

    private void updateConnectedDevicesStatus() {
        final String message = getString(R.string.status_devicesConnected) + " "
                + mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT).size();

        if(mBluetoothManager.getConnectedDevices(BluetoothGattServer.GATT).size()>0){
        }

    }

    ///////////////////////
    ////// Bluetooth //////
    ///////////////////////
    public static BluetoothGattDescriptor getClientCharacteristicConfigurationDescriptor() {
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
                CLIENT_CHARACTERISTIC_CONFIGURATION_UUID,
                (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        descriptor.setValue(new byte[]{0, 0});
        return descriptor;
    }

    public static BluetoothGattDescriptor getCharacteristicUserDescriptionDescriptor(String defaultValue) {
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(
                CHARACTERISTIC_USER_DESCRIPTION_UUID,
                (BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE));
        try {
            descriptor.setValue(defaultValue.getBytes("UTF-8"));
        } finally {
            return descriptor;
        }
    }

    private void ensureBleFeaturesAvailable() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetoothNotSupported, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Bluetooth not supported");
            finish();
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Make sure bluetooth is enabled.
            btnStop.setText("Bluetooth not enabled");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    private void disconnectFromDevices() {
        Log.d(TAG, "Disconnecting devices...");
        for (BluetoothDevice device : mBluetoothManager.getConnectedDevices(
                BluetoothGattServer.GATT)) {
            Log.d(TAG, "Devices: " + device.getAddress() + " " + device.getName());
            mGattServer.cancelConnection(device);
        }
    }



    private void startParking()
    {
        resetStatusViews();
        int flag = 2;
        String s = "01A"+flag;
        String ph = "8975959200";


        /*get the read characteristic from the service*/

        UUID keepsChanging_uuid = getRSAUUID();

        mAdvData = new AdvertiseData.Builder()
                .addServiceData(shortUUID(s),toByteArray(String.valueOf(ph)))
                .addServiceUuid(new ParcelUuid(keepsChanging_uuid))
                .build();

        mAdvScanResponse = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceData(shortUUID(s),toByteArray(String.valueOf(ph)))
                .build();
        // If the user disabled Bluetooth when the app was in the background,
        // openGattServer() will return null.
        mGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        if (mGattServer == null) {
            ensureBleFeaturesAvailable();
            return;
        }
        // Add a service for a total of three services (Generic Attribute and Generic Access
        // are present by default).
        mGattServer.addService(mBluetoothGattService);

        if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            mAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            mAdvertiser.startAdvertising(mAdvSettings, mAdvData, mAdvScanResponse, mAdvCallback);
        } else {
            //mAdvStatus.setText(R.string.status_noLeAdv);
            Toast.makeText(StopParkingActivity.this,R.string.status_noLeAdv,Toast.LENGTH_SHORT).show();
        }

    }


    private UUID getRSAUUID()
    {
        long time = System.currentTimeMillis();
        String secretKey = "6D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F776D656F77";
        String OTP = TOTP.generateTOTP512(secretKey,String.valueOf(time),"6");
        System.out.println("TOTP ---> "+OTP);
        System.out.println("Current time millis ---> "+time);
        String timePartUID = String.valueOf(time);

        String timePartUUID = timePartUID.substring(0,4)+"-"+timePartUID.substring(4,8)+"-"+timePartUID.substring(8,12)+"-"+timePartUID.substring(12,13);


        int flag = 2;
        String phone_number="8975959200";
        try{
            phone_number = mAuth.getCurrentUser().getDisplayName().split(",")[1];
        }catch (Exception e){
            e.printStackTrace();
        }




        UUID uuid = UUID
                .fromString(OTP+flag+"a-"+timePartUUID+"f"+phone_number);

        System.out.println("UUID ---> "+OTP+flag+"a-"+timePartUUID+"f"+phone_number);
        return uuid;

    }

    public ParcelUuid shortUUID(String flag) {
        //Log.d("UUID : ",ParcelUuid.fromString("0000" + s + "-0000-1000-8000-00805F9B34FB").toString());
        return ParcelUuid.fromString("0000" + flag + "-0000-1000-8000-00805F9B34FB");

        //01A = Unique KEY
        //
    }

    public byte[] toByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2 + len % 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + ((i+1)>=len ? 0 : Character.digit(s.charAt(i+1), 16)));
        }
        return data;
    }


    private ValueEventListener dataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            DataSnapshot postSnapShot=null;
            for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                postSnapShot = postSnapshot;
            }
            keyofCurrentParking = postSnapShot.getKey();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


}
