package com.icen.icenlibs;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.icen.blelibrary.BleManager;
import com.icen.blelibrary.activity.BleBaseActivity;
import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.blelibrary.ui.BleConfigActivity;

import java.util.Arrays;
import java.util.HashMap;

public class BleDemoActivity extends BleBaseActivity
        implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {

    private View mRootView;
    private Switch mSHLESwitch;
    private Button mBtnScan, mBtnConnect, mBtnShowDeviceList;

    private LinearLayout mLLDeviceInfo;
    private TextView mTVDeviceName, mTVDeviceMac, mTVDeviceClass, mTVDeviceRSSI, mTVDeviceContent;

    private String mCurrentDeviceName, mCurrentDeviceMac, mCurrentDeviceClass;
    private String mCurrentDeviceRSSI, mCurrentDeviceContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_demo);
        mRootView = findViewById(R.id.ble_demo_le_root);
        mBtnConnect = ((Button) findViewById(R.id.ble_demo_le_connect_button));
        mBtnConnect.setOnClickListener(this);
        mBtnScan = ((Button) findViewById(R.id.ble_demo_le_scan_button));
        mBtnScan.setOnClickListener(this);
        mBtnShowDeviceList = (Button) findViewById(R.id.ble_demo_le_show_list_button);
        mBtnShowDeviceList.setOnClickListener(this);
        mSHLESwitch = (Switch) findViewById(R.id.ble_demo_le_switch);

        mLLDeviceInfo = (LinearLayout) findViewById(R.id.ble_demo_le_select);
        mLLDeviceInfo.setVisibility(View.GONE);
        mTVDeviceName = (TextView) findViewById(R.id.ble_demo_le_select_name);
        mTVDeviceMac  = (TextView) findViewById(R.id.ble_demo_le_select_mac);
        mTVDeviceClass = (TextView) findViewById(R.id.ble_demo_le_select_class);
        mTVDeviceRSSI = (TextView) findViewById(R.id.ble_demo_le_select_rssi);
        mTVDeviceContent  = (TextView) findViewById(R.id.ble_demo_le_select_content);

        mRootView.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLogUtils.outputActivityLog("BleDemoActivity::onActivityResult::requestCode= " + requestCode +
                " resultCode= " + resultCode + " data= " + data);
        if (requestCode == DeviceListActivity.REQUEST_CODE &&
                resultCode == RESULT_OK){
            mLLDeviceInfo.setVisibility(View.VISIBLE);
            mBtnConnect.setEnabled(true);
            Bundle select_device = data.getExtras();
            mCurrentDeviceName = select_device.getString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME);
            mCurrentDeviceMac = select_device.getString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS);
            mCurrentDeviceClass = select_device.getString(BleLibsConfig.BROADCAST_INFO_DEVICE_CLASS);
            mCurrentDeviceRSSI = String.valueOf(select_device.getLong(BleLibsConfig.BROADCAST_INFO_SIGNAL));
            byte[] broadcast_record = select_device.getByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT);
            mCurrentDeviceContent = (null != broadcast_record && broadcast_record.length <= 0) ?
                    "NONE" : Arrays.toString(broadcast_record);

            mTVDeviceName.setText(mCurrentDeviceName);
            mTVDeviceMac.setText(mCurrentDeviceMac);
            mTVDeviceClass.setText(mCurrentDeviceClass);
            mTVDeviceRSSI.setText(mCurrentDeviceRSSI);
            mTVDeviceContent.setText(mCurrentDeviceContent);
        } else {
            mLLDeviceInfo.setVisibility(View.GONE);
            mBtnConnect.setEnabled(false);
            mCurrentDeviceName = "";
            mCurrentDeviceMac = "";
            mCurrentDeviceClass = "";
            mCurrentDeviceRSSI = "";
            mCurrentDeviceContent = "";
        }
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
                if (!TextUtils.isEmpty(mCurrentDeviceName) && !TextUtils.isEmpty(mCurrentDeviceMac)) {
                    boolean is_success = BleInformationActivity.startMySelf(this, mCurrentDeviceMac, mCurrentDeviceName);
                    if (!is_success) {
                        Toast.makeText(this, "To info activity false", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.ble_demo_le_scan_button:
                mBleManager.startScanDevice();
                break;
            case R.id.ble_demo_le_show_list_button:
                DeviceListActivity.startMySelfWithResult(this);
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
            mBleManager.setManagerCallback(this);
            if (mBleManager.isLeEnabled()) {
                mBtnScan.setEnabled(true);
            } else {
                mBtnScan.setEnabled(false);
            }

            if (!mBleManager.isSupportLE()){
                Toast.makeText(this, "Current device not support le", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(BleDemoActivity.this, "initial manager false", Toast.LENGTH_LONG).show();
            mSHLESwitch.setOnCheckedChangeListener(null);
            mSHLESwitch.setChecked(false);
            mBleManager.setManagerCallback(null);
            mBtnScan.setEnabled(false);
            mBtnConnect.setEnabled(false);
        }
    }

    @Override
    public void onLESwitch(int current_state){
        AppLogUtils.outputActivityLog("BleDemoActivity::onLESwitch::current_state= " +current_state);
        if (BleLibsConfig.BLE_SWITCH_CLOSING == current_state || BleLibsConfig.BLE_SWITCH_OPENING == current_state) {
            Toast.makeText(this, "Bluetooth is operating now", Toast.LENGTH_LONG).show();
            mBtnScan.setEnabled(false);
            mBtnConnect.setEnabled(false);
            mSHLESwitch.setEnabled(false);
        } else if (BleLibsConfig.BLE_SWITCH_ON == current_state) {
            mBtnScan.setEnabled(true);
            mSHLESwitch.setEnabled(true);
        } else if (BleLibsConfig.BLE_SWITCH_OFF == current_state) {
            mBtnScan.setEnabled(false);
            mSHLESwitch.setEnabled(true);
        } else if (BleLibsConfig.BLE_SWITCH_ERROR == current_state){
            mSHLESwitch.setOnCheckedChangeListener(null);
            mSHLESwitch.setChecked(mBleManager.isLeEnabled());
            mSHLESwitch.setOnCheckedChangeListener(this);
            if (mBleManager.isLeEnabled()){
                mBtnScan.setEnabled(true);
            } else {
                mBtnScan.setEnabled(false);
            }
        }
    }

    @Override
    public void onLEScan(int scan_process, String device_name, String device_class, String device_mac,
                         int device_rssi, byte[] broadcast_content) {
        AppLogUtils.outputActivityLog("BleDemoActivity::onLEScan::process= " + scan_process +
                " name= " + device_name + " mac= " + device_mac +
                " class= " + device_class +  " RSSI= " + device_rssi +
                " content= " + Arrays.toString(broadcast_content));

        if (BleLibsConfig.LE_SCAN_PROCESS_BEGIN == scan_process){//指示扫描开始
            Toast.makeText(this, "LE Scan begin", Toast.LENGTH_LONG).show();
            mBtnShowDeviceList.setEnabled(false);
        } else if (BleLibsConfig.LE_SCAN_PROCESS_DOING == scan_process) {//扫描进行中
            //do nothing
        } else if (BleLibsConfig.LE_SCAN_PROCESS_END == scan_process){//扫描结束
            Toast.makeText(this, "Scan finished", Toast.LENGTH_LONG).show();
            mBtnShowDeviceList.setEnabled(true);
        } else {//扫描发生异常
            Toast.makeText(this, "Scan exception. finish this process", Toast.LENGTH_LONG).show();
            mBtnShowDeviceList.setEnabled(false);
        }
    }

    @Override
    public void onConnectDevice(boolean is_success, String device_name, String device_mac) {
        super.onConnectDevice(is_success, device_name, device_mac);
        AppLogUtils.outputActivityLog("BleDemoActivity::onLEScan::is_success= " + is_success +
            " device_name= " + device_name + " device_mac= " + device_mac);
        if (is_success){

        }
    }
}
