package com.icen.blelibrary.config;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.icen.blelibrary.services.BleManagerService;
import com.icen.blelibrary.utils.BleLogUtils;

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

    private static final boolean DEFAULT_ENABLED_AUTO_CONNECT = true;//是否自动连接（默认为自动连接）
    private static final long    DEFAULT_SCAN_OVERTIME = 5000L;
    public  static final int     DEFAULT_RSSI = -1;//默认信号强度
    private static final String  BLE_DEVICE_SPILT = "|";
    private static final String  BLE_DEVICE_INFO_SPILT = "%";

    public static final int      BLE_SWITCH_ON = 0;
    public static final int      BLE_SWITCH_OPENING = BLE_SWITCH_ON + 1;
    public static final int      BLE_SWITCH_CLOSING = BLE_SWITCH_ON + 2;
    public static final int      BLE_SWITCH_OFF = BLE_SWITCH_ON + 3;
    public static final int      BLE_SWITCH_ERROR = BLE_SWITCH_ON - 1;

    //扫描进度：扫描开始，扫描进行中，扫描结束，扫描异常
    public static final int      LE_SCAN_PROCESS_BEGIN = 0;
    public static final int      LE_SCAN_PROCESS_DOING = LE_SCAN_PROCESS_BEGIN + 1;
    public static final int      LE_SCAN_PROCESS_END = LE_SCAN_PROCESS_BEGIN + 2;
    public static final int      LE_SCAN_PROCESS_EXCEPTION = LE_SCAN_PROCESS_BEGIN - 1;

    //BLE特征操作步骤标志，包括: 读操作结果反馈，因读操作获取数据
    public static final int     LE_RW_GET_RESULT = 0;
    public static final int     LE_RW_GET_DATA = LE_RW_GET_RESULT + 1;

    //BLE服务(SERVICE)配置
    public static final String   LE_SERVICE_NAME = "le_service_name";
    public static final String   LE_SERVICE_UUID = "le_service_uuid";
    public static final String   LE_SERVICE_TYPE = "le_service_type";

    //BLE特征（Characteristic）配置
    public static final String   LE_CHARACTERISTIC_UUID = "le_characteristic_uuid";
    public static final String   LE_CHARACTERISTIC_NAME = "le_characteristic_name";
    public static final String   LE_CHARACTERISTIC_PERMISSION = "le_characteristic_permission";
    public static final String   LE_CHARACTERISTIC_PROPERTIES = "le_characteristic_properties";

    /**
     * BLE管理服务配置文件：文件名为ble_config
     */
    private static final String BLE_CONFIG_FILE_NAME = "ble_config";
    /**
     * BLE管理服务配置文件：是否自动重连已经成功连接的最后一个BLE设备。
     * 默认值：true
     */
    private static final String BLE_CONFIG_AUTO_CONNECT = "ble_config_auto_connect";
    /**
     * BLE管理服务配置文件：设备扫描时间配置
     * 默认值：5秒
     */
    private static final String BLE_CONFIG_SCAN_TIMEOUT = "ble_config_scan_overtime";
    /**
     * 成功连接的设备名称
     */
    private static final String BLE_CONFIG_DEVICE_NAME = "ble_config_device_name";

    /**
     * 成功链接的设备MAC地址
     */
    private static final String BLE_CONFIG_DEVICE_MAC = "ble_config_device_mac";

    /**
     * 广播数据消息配置：设备名称
     */
    public static final String BROADCAST_INFO_DEVICE_NAME = "device_name";
    /**
     * 广播数据消息配置：设备MAC地址
     */
    public static final String BROADCAST_INFO_DEVICE_ADDRESS = "device_mac";
    /**
     * 广播数据消息配置：设备类型
     */
    public static final String BROADCAST_INFO_DEVICE_CLASS = "device_class";
    /**
     * 广播数据消息配置：广播数据实体
     */
    public static final String BROADCAST_INFO_DEVICE_CONTENT = "device_content";

    /**
     * 广播数据消息配置：设备信号强度
     */
    public static final String BROADCAST_INFO_SIGNAL = "device_signal";

    /**
     * 电源信息服务
     */
    public static final String BATTERY_CH_NAME = "Battery";

    /**
     * 启动BLE管理服务
     * @param ctx                上下文
     * @param service_connection 与管理服务连接的类
     * @return  true：启动成功；false：启动失败
     */
    public static final boolean startBleService(Context ctx, ServiceConnection service_connection) {
        Intent start_ble_service = new Intent(ACTION_START_BLE_SERVICE);
        start_ble_service.setComponent(new ComponentName(ctx, BleManagerService.class));
        boolean is_success = ctx.bindService(start_ble_service, service_connection, Context.BIND_AUTO_CREATE);
        BleLogUtils.outputUtilLog("BleLibsConfig::startBleService= " + is_success);
        return false;
    }

    /**
     * 停止BLE管理服务
     * @param ctx                上下文
     * @param service_connection 与管理服务连接的类
     */
    public static final void stopBleService(Context ctx, ServiceConnection service_connection) {
        try {
            ctx.unbindService(service_connection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BleLogUtils.outputUtilLog("BleLibsConfig::stopBleService=============");
    }

    /**
     * 获取系统配置：是否重新连接已经成功连接的设备
     * @param ctx   上下文
     * @return  true：需要自动连接，false：无需自动连接
     */
    public static final boolean getAutoConnectInFile(Context ctx) {
        boolean is_auto_connect = ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE)
                .getBoolean(BLE_CONFIG_AUTO_CONNECT, DEFAULT_ENABLED_AUTO_CONNECT);
        BleLogUtils.outputUtilLog("BleLibsConfig::getAutoConnect= " + is_auto_connect);
        return is_auto_connect;
    }

    /**
     * 保存自动重连配置到配置文件中
     * @param ctx              上下文
     * @param is_auto_connect  重连标记
     */
    public static final  void saveAutoConnectInFile(Context ctx, boolean is_auto_connect) {
        ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(BLE_CONFIG_AUTO_CONNECT, is_auto_connect)
                .commit();
        BleLogUtils.outputUtilLog("BleLibsConfig::saveAutoConnectInFile= " + is_auto_connect);
    }

    /**
     * 获取扫描时间长度
     * @param ctx 上下文
     * @return  超时时长
     */
    public static final long getScanOvertime(Context ctx) {
        long scan_overtime = ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE)
                .getLong(BLE_CONFIG_SCAN_TIMEOUT, DEFAULT_SCAN_OVERTIME);
        BleLogUtils.outputUtilLog("BleLibsConfig::getScanOvertime= " + scan_overtime);
        return scan_overtime;
    }

    /**
     * 保存扫描设备时间长度
     * @param ctx       上下文
     * @param over_time 设置的扫描时长
     */
    public static final void saveScanOvertime(Context ctx, long over_time){
        BleLogUtils.outputUtilLog("BleLibsConfig::saveScanOvertime= " + over_time);
        ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit()
                .putLong(BLE_CONFIG_SCAN_TIMEOUT, over_time)
                .commit();
    }

    /**
     * 保存前次连接的所有设备信息。列表形式
     * @param ctx                 上下文
     * @param current_device_name 设备名称
     * @param current_device_mac  设备MAC地址
     */
    public static final void saveDeviceInFile(Context ctx, String current_device_name, String current_device_mac) {
        String save_device_mac = getDeviceMacInFile(ctx);
        boolean is_same_device;
        if (TextUtils.isEmpty(save_device_mac)) {
            is_same_device = false;
        } else {
            if (save_device_mac.equalsIgnoreCase(current_device_mac)){
                is_same_device = true;
            } else {
                is_same_device = false;
            }
        }

        if (!is_same_device){
            SharedPreferences.Editor editor =  ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(BLE_CONFIG_DEVICE_NAME, current_device_name);
            editor.putString(BLE_CONFIG_DEVICE_MAC, current_device_mac);
            editor.commit();
        }
    }

    public static final String getDeviceNameInFile(Context ctx){
        String device_name = ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).
                getString(BLE_CONFIG_DEVICE_NAME, "");
        return device_name;
    }

    public static final String getDeviceMacInFile(Context ctx) {
        String device_mac = ctx.getSharedPreferences(BLE_CONFIG_FILE_NAME, Context.MODE_PRIVATE).
                getString(BLE_CONFIG_DEVICE_MAC, "");
        return device_mac;
    }
}
