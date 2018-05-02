package com.icen.icenlibs;

import android.content.pm.PackageInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.icen.blelibrary.BleManager;
import com.icen.blelibrary.BleManagerCallBack;
import com.icen.blelibrary.config.BleLibsConfig;

import java.util.Arrays;
import java.util.HashMap;

public class BleDemoActivity extends AppCompatActivity
        implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        BleManagerCallBack{

    private View mRootView;
    private Switch mSHLESwitch;

    private BleManager mBleManager;
    private HashMap<String, Bundle> mDeviceListByName;
    private HashMap<String, Bundle> mDeviceListByAddress;


    private static String[] REQUEST_PERMISSION = new String[]{
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_demo);
        mRootView = findViewById(R.id.ble_demo_le_root);
        ((Button) findViewById(R.id.ble_demo_le_connect_button)).setOnClickListener(this);
        ((Button) findViewById(R.id.ble_demo_le_scan_button)).setOnClickListener(this);
        mSHLESwitch = (Switch) findViewById(R.id.ble_demo_le_switch);

        mRootView.setEnabled(false);
        mBleManager = new BleManager(this);
        mBleManager.setManagerCallback(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, REQUEST_PERMISSION, 1000);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBleManager.startManager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBleManager.destroyManager();
    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        AppLogUtils.outputActivityLog("BleDemoActivity::onCheckedChanged::check= " + isChecked);
        if (null != mBleManager)
            if (isChecked)
                mBleManager.enableBle();
            else
                mBleManager.disableBle();
    }

    /**
     * Called when a view has been clicked.
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ble_demo_le_connect_button:
                break;
            case R.id.ble_demo_le_scan_button:
                mBleManager.startScanDevice();
                break;
        }
    }

    @Override
    public void onInitialManager(boolean is_success) {
        AppLogUtils.outputActivityLog("BleDemoActivity::onInitialManager::result= " + is_success);
        mRootView.setEnabled(is_success);
        if (is_success) {
            mSHLESwitch.setChecked(mBleManager.isLeEnabled());
            Toast.makeText(BleDemoActivity.this, "initial manager success", Toast.LENGTH_LONG).show();
            mSHLESwitch.setOnCheckedChangeListener(this);
        } else {
            Toast.makeText(BleDemoActivity.this, "initial manager false", Toast.LENGTH_LONG).show();
            mSHLESwitch.setOnCheckedChangeListener(null);
            mSHLESwitch.setChecked(false);
        }
    }

    @Override
    public void onLESwitch(boolean op_flag, boolean is_success){
        AppLogUtils.outputActivityLog("BleDemoActivity::onLESwitch::op= " + op_flag + " result= " + is_success);
    }

    @Override
    public void onLEScan(int scan_process, String device_name, String device_class, String device_mac, int device_rssi, byte[] broadcast_content) {
        AppLogUtils.outputActivityLog("BleDemoActivity::onLEScan::process= " + scan_process + " name= " + device_name +
                                " class= " + device_class + " mac= " + device_mac + " content= " + Arrays.toString(broadcast_content));
        if (BleLibsConfig.LE_SCAN_PROCESS_BEGIN == scan_process){
            Toast.makeText(this, "LE Scan begin", Toast.LENGTH_LONG).show();
            mDeviceListByName = null;
            mDeviceListByName = new HashMap<>();

            mDeviceListByAddress = null;
            mDeviceListByAddress = new HashMap<>();

        } else if (BleLibsConfig.LE_SCAN_PROCESS_DOING == scan_process) {
            Toast.makeText(this, "LE Scan doing", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Scan finished", Toast.LENGTH_LONG).show();
            Bundle[] device_list = mBleManager.getAllDevices();
            if (null != device_list && device_list.length > 0) {
                for (int index = 0; index < device_list.length; index ++) {
                    Bundle current_device = device_list[index];
                    String device_name_b = current_device.getString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME);
                    String device_address_b = current_device.getString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS);
                    mDeviceListByAddress.put(device_address_b, device_list[index]);
                    mDeviceListByName.put(device_name_b, device_list[index]);
                }

            }
        }
    }

    @Override
    public void onConnectDevice(boolean is_success, String device_name, String device_mac) {

    }

    @Override
    public void onInitialNotification(boolean is_success, String notification_uuid) {

    }

    @Override
    public void onReadCh(int read_step, boolean is_success, String read_uuid, byte[] respond_data) {

    }

    @Override
    public void onWriteCh(int write_step, boolean is_success, String write_uuid, byte[] respond_data) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean is_all_success = false;
        for (int index = 0; index < grantResults.length; index ++) {
            if (grantResults[index] != PackageInfo.REQUESTED_PERMISSION_GRANTED) {
                is_all_success = false;
            }
            is_all_success = true;
        }

        if (!is_all_success) {
            Toast.makeText(this, "Request permission false", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
