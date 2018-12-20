package com.icen.icenlibs;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.icen.blelibrary.BleManager;
import com.icen.blelibrary.BleManagerCallBack;
import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.icenlibs.adapter.DeviceListAdapter;

import java.util.HashMap;

public class DeviceListActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener, BleManagerCallBack{

    public static final int REQUEST_CODE = 9000;
    public static final String KEY_RESULT_BUNDLE = "my_bundle";
    private static final String INTENT_STRING = "com.icen.icenlibs.BLE_DEVICE_LIST_DEMO";
    public static final boolean startMySelfWithResult(Activity ctx){
        Intent start_intent = new Intent(INTENT_STRING);
        start_intent.addCategory(Intent.CATEGORY_DEFAULT);
        boolean is_success = false;
        try {
            ctx.startActivityForResult(start_intent, REQUEST_CODE);
            is_success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is_success;
    }

    public static final boolean startMySelf(Activity ctx){
        Intent start_intent = new Intent(INTENT_STRING);
        start_intent.addCategory(Intent.CATEGORY_DEFAULT);
        boolean is_success = false;
        try {
            ctx.startActivity(start_intent);
            is_success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is_success;
    }

    private ListView mLVDeviceList;

    private BleManager mBleManager;
    private DeviceListAdapter mDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        mLVDeviceList = (ListView) findViewById(R.id.device_list_activity_list);
        mLVDeviceList.setOnItemClickListener(this);
        mBleManager = new BleManager(this);
        mBleManager.setManagerCallback(this);
        mBleManager.startManager();
    }

    @Override
    public void onBackPressed() {
        AppLogUtils.outputActivityLog("DeviceListActivity::onBackPressed==============");
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void finish() {
        AppLogUtils.outputActivityLog("DeviceListActivity::finish==============");
        if (null != mBleManager & mBleManager.isReady())
            mBleManager.destroyManager();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBleManager.destroyManager();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int selected_index, long l) {
        AppLogUtils.outputActivityLog("DeviceListActivity::index= " + selected_index);
        Bundle select_device = (Bundle) mDeviceListAdapter.getItem(selected_index);
        Intent result_intent = new Intent();
        result_intent.putExtras(select_device);
        setResult(RESULT_OK, result_intent);
        finish();
    }

    @Override
    public void onInitialManager(boolean is_success) {
        AppLogUtils.outputActivityLog("DeviceListActivity::onInitialManager::result= " + is_success);
        if (is_success){
            Bundle[] devices_list = mBleManager.getAllDevices();
            if (null != devices_list && devices_list.length > 0 ) {
                if (null != mDeviceListAdapter) {
                    mDeviceListAdapter = null;
                }
                HashMap<String, Bundle> device_map = new HashMap<>();
                for (int index = 0; index < devices_list.length; index ++) {
                    Bundle device_bundle = devices_list[index];
                    String device_mac = device_bundle.getString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS);
                    device_map.put(device_mac, device_bundle);
                }
                mDeviceListAdapter = new DeviceListAdapter(this, device_map);
                mLVDeviceList.setAdapter(mDeviceListAdapter);
            } else {
                AppLogUtils.outputActivityLog("DeviceListActivity::onInitialManager::There are not device here");
                finish();
            }
        } else {
            finish();
        }

    }

    @Override
    public void onLESwitch(int current_state) {

    }

    @Override
    public void onLEScan(int scan_process, String device_name, String device_class, String device_mac, int device_rssi, byte[] broadcast_content) {

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
    public void onChChange(boolean is_success, String ch_uuid, byte[] ble_value) {

    }
}
