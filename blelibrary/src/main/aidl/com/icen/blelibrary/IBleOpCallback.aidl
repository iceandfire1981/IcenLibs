package com.icen.blelibrary;

interface IBleOpCallback {
    void onDeviceScan(int scan_process, String device_name, String device_address, String device_class, in byte[] broadcast_content);
    void onConnectToDevice(boolean is_success, String device_address, String device_name);
    void onInitialNotification(boolean is_success);
    void onReadCharacteristic(boolean is_success, String ch_uuid, in byte[] ble_value);
    void onCharacteristicChange(boolean is_success, String ch_uuid, in byte[] ble_value);
    void onWriteCharacteristic(boolean is_success, String ch_uuid, in byte[] ble_value);
    void onDescriptorWrite(boolean is_success, String ch_uuid);
}
