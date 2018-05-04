package com.icen.icenlibs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.icen.blelibrary.BleManager;
import com.icen.blelibrary.BleManagerCallBack;
import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.icenlibs.devices.DeviceListAdapter;

import java.util.HashMap;

public class DeviceListActivity extends AppCompatActivity implements
        AdapterView.OnItemClickListener, BleManagerCallBack{

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
    protected void onDestroy() {
        super.onDestroy();
        mBleManager.destroyManager();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

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
}
