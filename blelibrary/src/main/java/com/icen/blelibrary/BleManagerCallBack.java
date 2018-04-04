package com.icen.blelibrary;

public interface BleManagerCallBack {
    void onInitialManager(boolean is_success);
    void onLESwitch(boolean op_flag, boolean is_success);
    void onLEScan(int scan_process, String device_name, String device_class, String device_mac, byte[] broadcast_content);
}
