package com.example.androidtut2;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.androidtut2.MainActivity;
import com.example.androidtut2.R;

import java.util.ArrayList;


public class BluetoothFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "MainActivity";
    @Nullable


    BluetoothAdapter mBluetoothAdapter;
    Switch bluetoothSwitch;
    Button discoverableButton;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView LvNewDevices;


    // Create a BroadcastReceiver for ON/OFF BLUETOOTH
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };


    // Create a BroadcastReceiver for BLUETOOTH DISCOVERABILITY
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, mBluetoothAdapter.ERROR);

                switch(mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2 : Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled, able to receive connection.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability disabled. not able to receive connection.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting..");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected");
                        break;
                }
            }
        }
    };

    // Create a BroadcastReceiver for BLUETOOTH searching
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!mBTDevices.contains(device)){
                    mBTDevices.add(device);
                }

                Log.d(TAG, "onReceive:" + device.getName() + ":" + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                LvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };


    // Create a BroadcastReceiver for BLUETOOTH pairing
    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases
                //case 1 : bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
//                    Toast.makeText(getApplicationContext(), "Connected",Toast.LENGTH_LONG).show();
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver : BOND_BONDING");
//                    Toast.makeText(getApplicationContext(), "Connecting",Toast.LENGTH_LONG).show();
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver : BOND_NONE");
//                    Toast.makeText(getApplicationContext(), "Disconnected",Toast.LENGTH_LONG).show();
                }
            }
        }
    };


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bluetooth);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothSwitch = (Switch) findViewById(R.id.bluetoothSwitch);
        discoverableButton = (Button) findViewById(R.id.discoverableButton);
        LvNewDevices = (ListView) findViewById(R.id.discoverableDevicesList);
        mBTDevices = new ArrayList<>();
        LvNewDevices.setOnItemClickListener(BluetoothFragment.this);


        //Broadcasts when bond state changes (ie : pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);


        //Bluetooth on/off switch method
        bluetoothSwitch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Boolean switchState = bluetoothSwitch.isChecked();
                enableDisableBT(switchState);
            }
        });


        //discoverable button method
        discoverableButton.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view){
                enableDiscoverable();
                btnDiscover();
            }
        });

    }

    //enable bluetooth method
    public void enableDisableBT(Boolean switchState) {
        if(mBluetoothAdapter==null){
            Log.d(TAG,"enableDisableBT : does not have BT capabilities.");
        }

        if(switchState == Boolean.TRUE){
            mBluetoothAdapter.enable();
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            Toast.makeText(getApplicationContext(), "Turned On",Toast.LENGTH_LONG).show();
        }
        else {
            mBluetoothAdapter.disable();
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
            Toast.makeText(getApplicationContext(), "Turned Off",Toast.LENGTH_LONG).show();
        }
    }


    //enable discoverable method
    public void enableDiscoverable(){

        Log.d(TAG, "Making device discoverable for 300 seconds");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);
    }



    //enable searching unpaired device method
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover(){
        Log.d(TAG, "BtnDiscover : Looking for unpair devices.");

        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "BtnDiscover : cancelling discovery.");

            //checkBTPermissions, if it's greater than lollipop
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter((BluetoothDevice.ACTION_FOUND));
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);

        }
        if (!mBluetoothAdapter.isDiscovering()){
            Log.d(TAG, "BtnDiscover : searching.");
            //checkBTPermissions, if it's greater than lollipop
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter((BluetoothDevice.ACTION_FOUND));
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }


    //pairing function
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its memory intensive.
        mBluetoothAdapter.cancelDiscovery();
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick : deviceName = "+ deviceName);
        Log.d(TAG, "onItemClick : deviceAddress = "+ deviceAddress);

        //create the bond
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with" + deviceName);
            mBTDevices.get(i).createBond();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


}





//button OnClick method
//    Button addButton = (Button) findViewById(R.id.addButton);
//        addButton.setOnClickListener(new View.OnClickListener(){
//        @Override
//        public void onClick(View view){
//            EditText firstNumEditText = (EditText) findViewById(R.id.firstNumEditText);
//            EditText secondNumEditText = (EditText) findViewById(R.id.secondNumEditText);
//            TextView resultTextView = (TextView) findViewById(R.id.resultTextView);
//            int num1 = Integer.parseInt(firstNumEditText.getText().toString());
//            int num2 = Integer.parseInt(secondNumEditText.getText().toString());
//            int result= num1+num2;
//            resultTextView.setText(result + "");
//        }
//    });