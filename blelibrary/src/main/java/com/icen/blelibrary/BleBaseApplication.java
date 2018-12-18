package com.icen.blelibrary;

import android.app.Application;
import android.os.Bundle;

import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.Arrays;
import java.util.HashMap;

public class BleBaseApplication extends Application
                                implements BleManagerCallBack{

    private BleManager mBleManager;

    private HashMap<String, Bundle> mAllDevicesMap;

    @Override
    public void onCreate() {
        super.onCreate();
        mBleManager = new BleManager(this);
        mBleManager.setManagerCallback(this);
        mBleManager.startManager();
    }

    public BleManager getManager(){
        return mBleManager;
    }

    @Override
    public void onInitialManager(boolean is_success) {
        BleLogUtils.outputApplicationLog("onInitialManager::result= " + is_success);
    }

    @Override
    public void onLESwitch(int current_state) {
        BleLogUtils.outputApplicationLog("onLESwitch::result= " + current_state);
    }

    @Override
    public void onLEScan(int scan_process, String device_name,
                         String device_class, String device_mac,
                         int device_rssi, byte[] broadcast_content) {
        BleLogUtils.outputApplicationLog("onLEScan::info::process= " + scan_process + " device_name= " + device_name +
                                        " mac= " + device_mac + " class= " + device_class + " rssi= " + device_rssi +
                                        " content= " + ((null == broadcast_content || broadcast_content.length <= 0) ?
                                                            "No content" : Arrays.toString(broadcast_content)));
        switch (scan_process) {
            case BleLibsConfig.LE_SCAN_PROCESS_BEGIN:
                mAllDevicesMap = new HashMap<>();
                break;
            case BleLibsConfig.LE_SCAN_PROCESS_DOING:
                Bundle current_device = new Bundle();
                current_device.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME, device_name);
                current_device.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS, device_mac);
                current_device.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_CLASS, device_class);
                current_device.putInt(BleLibsConfig.BROADCAST_INFO_SIGNAL, device_rssi);
                current_device.putByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT, broadcast_content);
                mAllDevicesMap.put(device_mac, current_device);
                break;
            default:
                    break;
        }
    }

    @Override
    public void onConnectDevice(boolean is_success, String device_name, String device_mac) {
        BleLogUtils.outputApplicationLog("onConnectDevice::result= " + is_success +
                    " name= " + device_name + " mac= " + device_mac);
    }

    @Override
    public void onInitialNotification(boolean is_success, String notification_uuid) {
        BleLogUtils.outputApplicationLog("onInitialNotification::result= " + is_success +
                " _uuid= " + notification_uuid );
    }

    @Override
    public void onReadCh(int read_step, boolean is_success, String read_uuid, byte[] respond_data) {
        BleLogUtils.outputApplicationLog("onReadCh::result= " + is_success +
                " read_step= " + read_step + " uuid= " + read_uuid + " respond_data= " +
                ((null == respond_data || respond_data.length <= 0) ?
                "No content" : Arrays.toString(respond_data)));
    }

    @Override
    public void onWriteCh(int write_step, boolean is_success, String write_uuid, byte[] respond_data) {
        BleLogUtils.outputApplicationLog("onReadCh::result= " + is_success +
                " write_step= " + write_step + " uuid= " + write_uuid + " respond_data= " +
                ((null == respond_data || respond_data.length <= 0) ?
                        "No content" : Arrays.toString(respond_data)));
    }

    @Override
    public void onChChange(boolean is_success, String ch_uuid, byte[] ble_value) {
        BleLogUtils.outputApplicationLog("onReadCh::result= " + is_success +
                " uuid= " + ch_uuid + " ble_value= " +
                ((null == ble_value || ble_value.length <= 0) ?
                        "No content" : Arrays.toString(ble_value)));
    }
}
