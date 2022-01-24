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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.androidtut2.MainActivity;
import com.example.androidtut2.R;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;


public class BluetoothFragment extends Fragment implements AdapterView.OnItemClickListener {

    //debugging
    private static final String TAG = "BluetoothFragment";

    private final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    //list of devices that can be paired
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;

    //attributes
    BluetoothAdapter mBluetoothAdapter;
    Switch bluetoothSwitch;
    Button discoverableButton;
    ListView LvNewDevices;

    BluetoothConnectionService mBluetoothConnection; //send text attributes
    Button connectButton;
    Button sendButton;

    //the bluetooth device that is connected.
    BluetoothDevice mBTDevice;

    EditText sendText;
    TextView receivedString;
    StringBuilder messages;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        requireActivity().registerReceiver(mBroadcastReceiver4, filter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        bluetoothSwitch = (Switch) view.findViewById(R.id.bluetoothSwitch);
        discoverableButton = (Button) view.findViewById(R.id.discoverableButton);
        LvNewDevices = (ListView) view.findViewById(R.id.discoverableDevicesList);
        mBTDevices = new ArrayList<>();
        LvNewDevices.setOnItemClickListener(BluetoothFragment.this);
        sendButton = (Button) view.findViewById(R.id.sendTextButton);
        connectButton = (Button) view.findViewById(R.id.connectButton);
        sendText = (EditText) view.findViewById(R.id.sendText);
        receivedString = (TextView) view.findViewById(R.id.receivedString);
        messages = new StringBuilder();
    }


    @Override
    public void onStart(){
        super.onStart();

        //device do not have bluetooth capabilities
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT : does not have BT capabilities.");
            return;
        }

        //If bluetooth is not on, turn it on
        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
            requireActivity().registerReceiver(mBroadcastReceiver1, BTIntent);
            Toast.makeText(getContext(), "Bluetooth turned on", Toast.LENGTH_LONG).show();
        }

        setup();

    }

    private void setup(){


        //discoverable button method
        discoverableButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                enableDiscoverable();
                btnDiscover();
            }
        });

        //startConnection button method
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "start connection, connectButton pressed");
                startConnection();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = sendText.getText().toString().getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
                sendText.setText("");

            }
        });

        //receiving incoming message
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

    }

    //startBluetoothConnection
    public void startConnection() {
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(device, uuid);
    }

    // Create a BroadcastReceiver for ON/OFF BLUETOOTH
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch (state) {
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

                switch (mode) {
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

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!mBTDevices.contains(device) && device.getName() != null) {
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
                    //set global device as the device that is connected.
                    mBTDevice = mDevice;
                    Toast.makeText(getContext(), "Paired", Toast.LENGTH_LONG).show();
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver : BOND_BONDING");
                    Toast.makeText(getContext(), "Pairing", Toast.LENGTH_LONG).show();
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver : BOND_NONE");
                    Toast.makeText(getContext(), "Unpaired", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            messages.append(text + "\n");
            receivedString.setText(messages);
        }
    };


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();

    }





    //enable discoverable method
    public void enableDiscoverable() {

        Log.d(TAG, "Making device discoverable for 300 seconds");
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        requireActivity().registerReceiver(mBroadcastReceiver2, intentFilter);
    }


    //enable searching unpaired device method
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnDiscover() {
        Log.d(TAG, "BtnDiscover : Looking for unpair devices.");

        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "BtnDiscover : cancelling discovery.");

            //checkBTPermissions, if it's greater than lollipop
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDevicesIntent = new IntentFilter((BluetoothDevice.ACTION_FOUND));
            requireActivity().registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);

        }
        if (!mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "BtnDiscover : searching.");
            //checkBTPermissions, if it's greater than lollipop
            checkBTPermissions();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter((BluetoothDevice.ACTION_FOUND));
            requireActivity().registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }


    //pairing function
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its memory intensive.
        mBluetoothAdapter.cancelDiscovery();
        String deviceName = mBTDevices.get(i).getName();

        Log.d(TAG, "onItemClick : deviceName = " + deviceName);

        //create the bond
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "Trying to pair with" + deviceName);
            mBTDevices.get(i).createBond();
            mBTDevice = mBTDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(getContext());

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), "Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += ContextCompat.checkSelfPermission(getActivity(), "Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }


}


//bluetooth switch stuff

//enable bluetooth method
//    public void enableDisableBT(Boolean switchState) {
//        if (mBluetoothAdapter == null) {
//            Log.d(TAG, "enableDisableBT : does not have BT capabilities.");
//        }
//
//        if (switchState == Boolean.TRUE) {
//            mBluetoothAdapter.enable();
//            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivity(enableBTIntent);
//            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
//            requireActivity().registerReceiver(mBroadcastReceiver1, BTIntent);
//            Toast.makeText(getContext(), "Turned On", Toast.LENGTH_LONG).show();
//        } else {
//            mBluetoothAdapter.disable();
//            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
//            requireActivity().registerReceiver(mBroadcastReceiver1, BTIntent);
//            Toast.makeText(getContext(), "Turned Off", Toast.LENGTH_LONG).show();
//        }
//    }



//Bluetooth on/off switch method
//        bluetoothSwitch.setOnClickListener(new View.OnClickListener() {
//@Override
//public void onClick(View view) {
//        Boolean switchState = bluetoothSwitch.isChecked();
////                enableDisableBT(switchState);
//        }
//        });


