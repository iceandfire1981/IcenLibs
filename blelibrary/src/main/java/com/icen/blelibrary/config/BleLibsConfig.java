package com.icen.blelibrary.config;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.icen.blelibrary.services.BleManagerService;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * BLE库的配置, 包括：
 * 1、是否自动连接
 * 2、上一次成功连接的设备名称
 * 3、上一次成功连接的设备MAC
 * 4、上一次设置的用于通知的特征UUID
 *
 * Created by icean on 2017/10/29.
 */

public final class BleLibsConfig {

    private static final String ACTION_START_BLE_SERVICE = "com.icen.blelibrary.START_BLE_SERVICE";

    private static final boolean ENABLED_AUTO_CONNECT = true;//是否自动连接（默认为自动连接）
    private static final long DEFAULT_SCAN_OVERTIME = 5000L;

    private static final String BLE_CONFIG_FILE_NAME = "ble_config";
    private static final String BLE_CONFIG_AUTO_CONNECT = "ble_config_a_connect";
    private static final String BLE_CONFIG_SCAN_TIMEOUT = "ble_config_scan_overtime";
    private static final String BLE_CONFIG_DEVICE_LIST = "ble_config_device_list";
    private static final String BLE_CONFIG_NOTIFICATION_UUID = "ble_config_n_uuid";

    private static final String BLE_DEVICE_SPILT = "|";
    private static final String BLE_DEVICE_INFO_SPILT = "%";

    public static final int SCAN_PROCESS_BEGIN = 1000;
    public static final int SCAN_PROCESS_SCANNING = SCAN_PROCESS_BEGIN + 1;
    public static final int SCAN_PROCESS_SCAN_END = SCAN_PROCESS_BEGIN + 2;

    public static final boolean startBleService(Context ctx, ServiceConnection service_connection) {
        Intent start_ble_service = new Intent(ACTION_START_BLE_SERVICE);
        start_ble_service.setComponent(new ComponentName(ctx, BleManagerService.class));
        boolean is_success = ctx.bindService(start_ble_service, service_connection, Context.BIND_AUTO_CREATE);
        BleLogUtils.outputUtilLog("BleLibsConfig::startBleService= " + is_success);
        return false;
    }

    public static final void stopBleService(Context ctx, ServiceConnection service_connection) {
        ctx.unbindService(service_connection);
        BleLogUtils.outputUtilLog("BleLibsConfig::stopBleService=============");
    }

    public static final boolean getAutoConnectInFile(Context ctx) {
        boolean is_auto_connect = ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE)
                .getBoolean(BLE_CONFIG_AUTO_CONNECT, ENABLED_AUTO_CONNECT);
        BleLogUtils.outputUtilLog("BleLibsConfig::getAutoConnect= " + is_auto_connect);
        return is_auto_connect;
    }

    public static final  void saveAutoConnectInFile(Context ctx, boolean is_auto_connect) {
        ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(BLE_CONFIG_AUTO_CONNECT, is_auto_connect)
                .commit();
        BleLogUtils.outputUtilLog("BleLibsConfig::saveAutoConnectInFile= " + is_auto_connect);
    }

    public static final String getNotificationUUIDInFile(Context ctx) {
        String notification_uuid = ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE)
                .getString(BLE_CONFIG_NOTIFICATION_UUID, "");
        BleLogUtils.outputUtilLog("BleLibsConfig::getNotificationUUIDInFile= " + notification_uuid);
        return notification_uuid;
    }

    public static final void setNotificationUUIDInFile(Context ctx, String current_uuid) {
        if (!TextUtils.isEmpty(current_uuid)) {
            ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit()
                    .putString(BLE_CONFIG_NOTIFICATION_UUID, current_uuid)
                    .commit();
        }
        BleLogUtils.outputUtilLog("BleLibsConfig::setNotificationUUIDInFile= " + current_uuid);
    }

    public static final long getScanOvertime(Context ctx) {
        long scan_overtime = ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE)
                .getLong(BLE_CONFIG_SCAN_TIMEOUT, DEFAULT_SCAN_OVERTIME);
        BleLogUtils.outputUtilLog("BleLibsConfig::getScanOvertime= " + scan_overtime);
        return scan_overtime;
    }

    public static final void saveScanOvertime(Context ctx, long over_time){
        BleLogUtils.outputUtilLog("BleLibsConfig::saveScanOvertime= " + over_time);
        ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit()
                .putLong(BLE_CONFIG_SCAN_TIMEOUT, over_time)
                .commit();
    }

    public static final void saveDeviceInFile(Context ctx, String current_device_name, String current_device_mac) {
        if (!TextUtils.isEmpty(current_device_name) && !TextUtils.isEmpty(current_device_mac)) {
            HashMap<String, String> device_map = getDeviceListInFile(ctx);
            current_device_mac = current_device_mac.toLowerCase(Locale.getDefault());
            if (null != device_map && device_map.size() > 0) {
                device_map.put(current_device_mac, current_device_name);
            } else {
                device_map = new HashMap<>();
                device_map.put(current_device_mac, current_device_name);
            }
            mapToDeviceRecord(ctx, device_map);
        }
        BleLogUtils.outputUtilLog("BleLibsConfig::getDeviceNameInFile::name " + current_device_name
                + " mac= " + current_device_mac);
    }

    public static final void deleteDeviceByNameInfile(Context ctx, String current_device_name) {
        if (!TextUtils.isEmpty(current_device_name)) {
            HashMap<String, String> device_map = getDeviceListInFile(ctx);
            if (null != device_map && device_map.size() > 0) {
                Iterator<Map.Entry<String, String>> device_iterator = device_map.entrySet().iterator();
                while (device_iterator.hasNext()) {
                    Map.Entry<String, String> device_entry = device_iterator.next();
                    String device_name = device_entry.getValue();
                    BleLogUtils.outputUtilLog("BleLibsConfig::deleteDeviceByNameInfile::name " + current_device_name +
                                            " source= " + device_name);
                    if (current_device_name.equals(device_name)){
                        device_map.remove(device_entry.getValue());
                        break;
                    }
                }
            }
            mapToDeviceRecord(ctx, device_map);
        }
        BleLogUtils.outputUtilLog("BleLibsConfig::deleteDeviceByNameInfile::name " + current_device_name);
    }

    public static final void deleteDeviceByMacInfile(Context ctx, String current_device_mac) {
        if (!TextUtils.isEmpty(current_device_mac)) {
            HashMap<String, String> device_map = getDeviceListInFile(ctx);
            if (null != device_map && device_map.size() > 0) {
                Iterator<Map.Entry<String, String>> device_iterator = device_map.entrySet().iterator();
                while (device_iterator.hasNext()) {
                    Map.Entry<String, String> device_entry = device_iterator.next();
                    String device_mac = device_entry.getKey();
                    BleLogUtils.outputUtilLog("BleLibsConfig::deleteDeviceByMacInfile::mac " + current_device_mac +
                            " source= " + device_mac);
                    if (current_device_mac.equalsIgnoreCase(device_mac)){
                        device_map.remove(device_mac);
                        break;
                    }
                }
            }
            mapToDeviceRecord(ctx, device_map);
        }
        BleLogUtils.outputUtilLog("BleLibsConfig::deleteDeviceByMacInfile::name " + current_device_mac);
    }


    /**
     * 生成设备列表
     * 记录格式：device1%xxxx:xxxxx:xxxx:xxxx\device2%xxxx:xxxxx:xxxx:xxxx
     * @param ctx
     * @return 设备列表MAC-NAME Map
     */
    public static final HashMap<String, String> getDeviceListInFile(Context ctx) {
        String device_list_str = ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE)
                .getString(BLE_CONFIG_DEVICE_LIST, "");
        BleLogUtils.outputUtilLog("BleLibsConfig::getDeviceListByNameInFile= " + device_list_str);
        if (TextUtils.isEmpty(device_list_str)){
            return null;
        } else {
            String[] device_list = device_list_str.split(BLE_DEVICE_SPILT);
            BleLogUtils.outputUtilLog("BleLibsConfig::getDeviceListByNameInFile::device_count= " + device_list.length);
            HashMap<String, String> device_map = new HashMap<>();
            for (String one_device_info : device_list) {
                String[] device_info = one_device_info.split(BLE_DEVICE_INFO_SPILT);
                BleLogUtils.outputUtilLog("BleLibsConfig::getDeviceListByNameInFile::name= " + device_info[0] +
                        " mac= " + device_info[1]);
                device_map.put(device_info[1], device_info[0]);
            }
            return device_map;
        }
    }

    private static final void mapToDeviceRecord(Context ctx, @NonNull  HashMap<String, String> device_map){
        int device_size = ((null != device_map) ? device_map.size() : -1);
        BleLogUtils.outputUtilLog("BleLibsConfig::mapToDeviceRecord= " + device_size);
        if (device_size <= 0) {
            Iterator<Map.Entry<String, String>> device_iterator = device_map.entrySet().iterator();
            StringBuffer device_list_buffer = new StringBuffer();
            while (device_iterator.hasNext()) {
                Map.Entry<String, String> device_entry = device_iterator.next();
                String device_mac = device_entry.getKey();
                String device_name = device_entry.getValue();
                String device_info_str = device_mac + BLE_DEVICE_INFO_SPILT + device_name;
                device_list_buffer.append(device_info_str + BLE_DEVICE_SPILT);
            }
            BleLogUtils.outputUtilLog("BleLibsConfig::mapToDeviceRecord::Result= " + device_list_buffer.toString());
            ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit()
                    .putString(BLE_CONFIG_DEVICE_LIST, device_list_buffer.toString())
                    .commit();
        }
    }
}
