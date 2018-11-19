package com.icen.blelibrary;

public interface BleManagerCallBack {
    void onInitialManager(boolean is_success);
    void onLESwitch(int current_state);
    void onLEScan(int scan_process, String device_name, String device_class, String device_mac, int device_rssi, byte[] broadcast_content);
    void onConnectDevice(boolean is_success, String device_name, String device_mac);
    void onInitialNotification(boolean is_success, String notification_uuid);
    void onReadCh(int read_step, boolean is_success, String read_uuid, byte[] respond_data);
    void onWriteCh(int write_step, boolean is_success, String write_uuid, byte[] respond_data);
    void onChChange(boolean is_success, String ch_uuid, byte[] ble_value);
}
