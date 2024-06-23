package com.example.notifylistenerdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.notifylistenerdemo.ble.BLEManager;
import com.example.notifylistenerdemo.permission.PermissionListener;
import com.example.notifylistenerdemo.permission.PermissionRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    private static final int REQUEST_CODE = 9527;
    private TextView textView;


    private static final String TAG = "BLEMain";

    //bt_patch(mtu).bin
    public static final String SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b";  //蓝牙通讯服务
    public static final String WRITE_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8";

    //动态申请权限
    private String[] requestPermissionArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_PRIVILEGED,
    };
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权限
    private List<String> deniedPermissionList = new ArrayList<>();


    private static final int CONNECT_SUCCESS = 0x01;
    private static final int CONNECT_FAILURE = 0x02;
    private static final int DISCONNECT_SUCCESS = 0x03;
    private static final int SEND_SUCCESS = 0x04;
    private static final int SEND_FAILURE = 0x05;
    private static final int RECEIVE_SUCCESS = 0x06;
    private static final int RECEIVE_FAILURE = 0x07;
    private static final int START_DISCOVERY = 0x08;
    private static final int STOP_DISCOVERY = 0x09;
    private static final int DISCOVERY_DEVICE = 0x0A;
    private static final int DISCOVERY_OUT_TIME = 0x0B;
    private static final int SELECT_DEVICE = 0x0C;
    private static final int BT_OPENED = 0x0D;
    private static final int BT_CLOSED = 0x0E;
    private static final int SET_TEXT = 0x0F;



    private Context mContext;
    private BLEManager bleManager;
    private BluetoothDevice curBluetoothDevice;  //当前连接的设备

    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothDevice bluetoothDevice;
    //当前设备连接状态
    private boolean curConnState = false;

    private String BLEGattServerAddress = "CC:7B:5C:1E:17:3E";

    BluetoothGattCharacteristic characteristic;

    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning;
    private Handler handler = new Handler();

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private TextView tvBLEState;

    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
//                    leDeviceListAdapter.addDevice(result.getDevice());
//                    leDeviceListAdapter.notifyDataSetChanged();
                    // log the device
                    Log.d(TAG, "onScanResult: " + result.getDevice().getName() + " " + result.getDevice().getAddress());
                }
            };


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case START_DISCOVERY:
                    Log.d(TAG, "开始搜索设备...");
                    break;

                case STOP_DISCOVERY:
                    Log.d(TAG, "停止搜索设备...");
                    break;

                case DISCOVERY_DEVICE:  //扫描到设备
                    BLEDevice bleDevice = (BLEDevice) msg.obj;
//                    lvDevicesAdapter.addDevice(bleDevice);

                    break;

                case SELECT_DEVICE:
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) msg.obj;
//                    tvName.setText(bluetoothDevice.getName());
//                    tvAddress.setText(bluetoothDevice.getAddress());
                    curBluetoothDevice = bluetoothDevice;
                    break;

                case CONNECT_FAILURE: //连接失败
                    Log.d(TAG, "连接失败");
//                    tvCurConState.setText("连接失败");
                    curConnState = false;
                    break;

                case CONNECT_SUCCESS:  //连接成功
                    Log.d(TAG, "连接成功");
//                    tvCurConState.setText("连接成功");
                    curConnState = true;
//                    llDataSendReceive.setVisibility(View.VISIBLE);
//                    llDeviceList.setVisibility(View.GONE);
                    break;

                case DISCONNECT_SUCCESS:
                    Log.d(TAG, "断开成功");
//                    tvCurConState.setText("断开成功");
                    curConnState = false;

                    break;

                case SEND_FAILURE: //发送失败
                    byte[] sendBufFail = (byte[]) msg.obj;
//                    String sendFail = TypeConversion.bytes2HexString(sendBufFail,sendBufFail.length);
//                    tvSendResult.setText("发送数据失败，长度" + sendBufFail.length + "--> " + sendFail);
                    break;

                case SEND_SUCCESS:  //发送成功
                    byte[] sendBufSuc = (byte[]) msg.obj;
//                    String sendResult = TypeConversion.bytes2HexString(sendBufSuc,sendBufSuc.length);
//                    tvSendResult.setText("发送数据成功，长度" + sendBufSuc.length + "--> " + sendResult);
                    break;

                case RECEIVE_FAILURE: //接收失败
                    String receiveError = (String) msg.obj;
//                    tvReceive.setText(receiveError);
                    break;

                case RECEIVE_SUCCESS:  //接收成功
                    byte[] recBufSuc = (byte[]) msg.obj;
//                    String receiveResult = TypeConversion.bytes2HexString(recBufSuc,recBufSuc.length);
//                    tvReceive.setText("接收数据成功，长度" + recBufSuc.length + "--> " + receiveResult);
                    break;

                case BT_CLOSED:
                    Log.d(TAG, "系统蓝牙已关闭");
                    break;
                case BT_OPENED:
                    Log.d(TAG, "系统蓝牙已打开");
                    break;
                case SET_TEXT:
                    tvBLEState.setText(msg.obj.toString());
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        // 初始化视图
        final EditText editText = findViewById(R.id.input_password);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, editText.getText(), Toast.LENGTH_SHORT).show();
            }
        });
        initView();

        // 检查蓝牙是否可用
        if (!checkBLE()) {
            Toast.makeText(this, "该设备不支持低功耗蓝牙", Toast.LENGTH_SHORT).show();
        }

        //初始化权限
        initPermissions();
        // 启用蓝牙
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanLeDevice();
        try {
            if (BluetoothAdapter.checkBluetoothAddress(BLEGattServerAddress)) {
                //It is a valid MAC address.
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(BLEGattServerAddress);
//                btSocket = createBluetoothSocket(device);
            } else {

                Toast.makeText(mContext, "Invalid MAC: Address", Toast.LENGTH_LONG).show();
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        bluetoothGatt = bluetoothDevice.connectGatt(this, true, gattCallback);




    }


    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                gatt.discoverServices();
                mHandler.obtainMessage(SET_TEXT, "蓝牙连接成功").sendToTarget();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                mHandler.obtainMessage(SET_TEXT, "蓝牙连接断开").sendToTarget();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // get the service
                characteristic = gatt.getService(UUID.fromString(SERVICE_UUID)).getCharacteristic(UUID.fromString(WRITE_UUID));
            } else {
                mHandler.obtainMessage(SET_TEXT, "服务未发现").sendToTarget();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                tvBLEState.setText("发送成功");
            } else {
//                tvBLEState.setText("发送失败");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                tvBLEState.setText("接收成功");
            } else {
//                tvBLEState.setText("接收失败");
            }
        }
    };


    /**
     * 检查蓝牙是否可用
     * @return
     */
    private boolean checkBLE() {
        // Use this check to determine whether Bluetooth classic is supported on the device.
// Then you can selectively disable BLE-related features.
        boolean bluetoothAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);

// Use this check to determine whether BLE is supported on the device. Then
// you can selectively disable BLE-related features.
        boolean bluetoothLEAvailable = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        if (bluetoothAvailable && bluetoothLEAvailable) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 初始化视图
     */
    private void initView() {
        tvBLEState = findViewById(R.id.tvBLEState);
        textView = findViewById(R.id.textView);
    }

    /**
     * 初始化权限
     */
    private void initPermissions() {
        //Android 6.0以上动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final PermissionRequest permissionRequest = new PermissionRequest();
            permissionRequest.requestRuntimePermission(MainActivity.this, requestPermissionArray, new PermissionListener() {
                @Override
                public void onGranted() {
                    Log.d(TAG, "所有权限已被授予");
                }

                //用户勾选“不再提醒”拒绝权限后，关闭程序再打开程序只进入该方法！
                @Override
                public void onDenied(List<String> deniedPermissions) {
                    deniedPermissionList = deniedPermissions;
                    for (String deniedPermission : deniedPermissionList) {
                        Log.e(TAG, "被拒绝权限：" + deniedPermission);
                    }
                }
            });
        }
    }




}