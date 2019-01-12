// IBleOp.aidl
package com.icen.blelibrary;

import android.os.Bundle;
import com.icen.blelibrary.IBleOpCallback;

interface IBleOp {
    Bundle[] getDeviceInfo();
    Bundle[] getServices();
    Bundle[] getCharacteristic(String service_uuid);
    Bundle[] getDeviceInfoByAddress(String device_mac);
    Bundle[] getDeviceInfoByName(String device_name);

    boolean getBatteryLevel();
    boolean getRssi();

    boolean isSupportLE();
    boolean leIsEnable();
    boolean bleSwitcher(boolean enabled);
    boolean startDiscoveryDevice();
    void stopDiscoveryDevice();
    boolean connectToDevice(boolean force_connect, String remote_address);
    void initialNotification(String notification_uuid);
    boolean disconnect();

    boolean readCharacteristic(String read_uuid);
    boolean writeCharacteristic(String write_uuid, in byte[] write_content);
    boolean writeCharacteristicString(String write_uuid, String write_content);
    boolean writeCharacteristicInt(String write_uuid, int write_content, int content_format);

    void setBleOpCallback(in IBleOpCallback op_callback);
    boolean hasConnectToDevice();
    void saveAutoReconnect(boolean reconnect_flag);
    boolean getAutoReconnect();
    void saveAutoConnect(boolean auto_connect_flag);
    boolean getAutoConnect();
}
