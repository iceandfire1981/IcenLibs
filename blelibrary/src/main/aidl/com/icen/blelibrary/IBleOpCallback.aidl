package com.icen.blelibrary;

interface IBleOpCallback {
    void onDeviceScan(int scan_process, String device_name, String device_address, String device_class,
                        int device_rssi, in byte[] broadcast_content);
    void onBLESwitch(int current_state);
    void onConnectToDevice(boolean is_success, String device_address, String device_name);
    void onInitialNotification(boolean is_success, String notification_uuid);
    void onReadCharacteristic(boolean is_success, String ch_uuid, int read_step, in byte[] ble_value);
    void onCharacteristicChange(boolean is_success, String ch_uuid, in byte[] ble_value);
    void onWriteCharacteristic(boolean is_success, String ch_uuid, int write_step, in byte[] ble_value);
    void onDescriptorWrite(boolean is_success, String ch_uuid);
}
