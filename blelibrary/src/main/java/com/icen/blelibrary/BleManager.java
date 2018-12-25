package com.icen.blelibrary;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * BLE系统管理器，实现功能包括：
 * 1、管理到BLE管理服务（BleManagerService）的连接
 * 2、对Activity提供BLE操作接口
 * 3、查询连接状态
 *
 * Note：BLE管理构造完毕后需要调用startManager方法管理器，界面销毁或者其他适当时候调用destroyManager方法
 *
 * Created by Alx Slash on 2017/10/29.
 * Author: alxslashtraces@gmail.com
 */
public final class BleManager extends IBleOpCallback.Stub implements ServiceConnection{

    private Context mContext;
    private IBleOp mBleOp;
    private BleManagerCallBack mClientCallback;

    public BleManager(Context ctx){
        mContext = ctx;
        mClientCallback = null;
        mBleOp = null;
    }

    /**
     *  启动并连接BLE管理服务
     * @return is_success: true：表示启动服务成功，false：表示启动服务失败
     */
    public boolean startManager(){
        boolean is_success;
        if (isReady()){
            is_success = true;
            if (null != mClientCallback)
                mClientCallback.onInitialManager(is_success);
        } else {
            is_success = BleLibsConfig.startBleService(mContext, this);
        }

        BleLogUtils.outputManagerLog("BleManager::startManager::System Ready= " + isReady() + " result= " + is_success);
        return is_success;
    }

    /**
     * 重新初始化管理器
     * @return
     */
    public boolean reInitialManager(){
        boolean is_success = false;
        if (isReady()) {
            try {
                mBleOp.setBleOpCallback(this);
                is_success = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("reInitialManager::ready= " + isReady() + " result= " + is_success);
        return is_success;
    }

    /**
     * 销毁管理器，包括：
     *  取消返回回调接口；断开BLE到手机的连接；断开管理器和管理服务的连接
     */
    public void destroyManager(){
        BleLogUtils.outputManagerLog("BleManager::destroyManager::ready= " + isReady());
        if (null != mBleOp) {
            try {
                mBleOp.setBleOpCallback(null);
                mBleOp.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mBleOp = null;
        mClientCallback = null;
        BleLibsConfig.stopBleService(mContext, this);
    }

    /**
     * 管理服务是否就绪
     * “就绪”是指：管理器和管理服务是否完成连接
     * @return True 表示已经就绪，反之未就绪
     */
    public boolean isReady(){
        boolean is_ready = false;
        if (null != mBleOp) {
            is_ready = true;
        }
        BleLogUtils.outputManagerLog("BleManager::isReady= " + is_ready);
        return is_ready;
    }

    /**
     * 设置异步回调接口
     * @param call_back
     */
    public void setManagerCallback(BleManagerCallBack call_back){
        mClientCallback = call_back;
    }

    /**
     * 判断当前是否已经与LE外设有连接
     * @return TRUE 表示已经连接上； FALSE：表示没有任何连接
     */
    public boolean hasConnectToDevice(){
        boolean is_success = false;
        if (isReady()){
            try {
                is_success = mBleOp.hasConnectToDevice();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("hasConnectToDevice::System Ready= " + isReady() + " is_success= " + is_success);
        return is_success;
    }

    /**
     * 获取附近的BLE设备
     * @return BLE设备列表
     */
    public Bundle[] getAllDevices(){
        Bundle[] all_devices = null;
        if (isReady()){
            try {
                all_devices = mBleOp.getDeviceInfo();
                return all_devices;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("getAllDevices::System Ready= " + isReady() + " Found= " +
                ((null == all_devices && all_devices.length <= 0) ? "No Device found":("Found device total= " + all_devices.length)));
        return all_devices;
    }

    /**
     * 获取已经连接的BLE外设包含的服务列表
     * @return 服务列表，是一个BUNDLE数组
     */
    public Bundle[] getDeviceService(){
        Bundle[] all_services = null;
        if (isReady()){
            try {
                all_services = mBleOp.getServices();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("getDeviceService::System Ready= " + isReady() + " Found= " +
                ((null == all_services && all_services.length <= 0) ? "No Device found":("Found device total= " + all_services.length)));
        return all_services;
    }

    /**
     * 获取指定服务中包含的特征列表
     * @param service_uuid 指定的UUID
     * @return 服务中包含的特征列表
     */
    public Bundle[] getAllCharacteristicInService(String service_uuid) {
        Bundle[] all_characteristics = null;
        if (isReady()) {
            try {
                all_characteristics = mBleOp.getCharacteristic(service_uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("getAllCharacteristicInService::System Ready= " + isReady() + " Found= " +
                ((null == all_characteristics && all_characteristics.length <= 0) ? "No Device found":("Found device total= " + all_characteristics.length)));
        return all_characteristics;
    }

    /**
     * 获取BLE外设包含的所有特征，返回一个hash-map，依据服务的UUID进行分类
     * @return HashMap，包含了所有的特征
     */
    public HashMap<String, ArrayList<Bundle>> getAllCharacteristics() {
        HashMap<String, ArrayList<Bundle>> characteristic_map = new HashMap<>();
        if (isReady()) {
            try {
                Bundle[] all_services = mBleOp.getServices();
                if (null != all_services && all_services.length > 0) {
                    for (int service_index = 0; service_index < all_services.length; service_index++) {
                        String service_uuid = all_services[service_index].getString(BleLibsConfig.LE_SERVICE_UUID);
                        Bundle[] current_ch_list = mBleOp.getCharacteristic(service_uuid);
                        if (null != current_ch_list && current_ch_list.length > 0) {
                            ArrayList<Bundle> current_ch_array = new ArrayList<>();
                            for (int ch_index = 0; ch_index < current_ch_list.length; ch_index ++) {
                                current_ch_array.add(current_ch_list[ch_index]);
                            }
                            characteristic_map.put(service_uuid, current_ch_array);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("getAllCharacteristics::System Ready= " + isReady() +
                "Get characteristic finish. Result = " +
                ((null == characteristic_map || characteristic_map.size() <= 0) ?
                        "No characteristics" : String.valueOf(characteristic_map.size())) );
        return characteristic_map;
    }

    /**
     * 根据输入的设备MAC地址在所有被搜索到的LE外设中查找目标外设。
     * 如果输入的LE外设MAC地址是空，则返回所有LE外设
     * @param device_mac 需要查找的LE外设名称
     * @return 外设广播信息列表
     */
    public Bundle[] getDeviceInfoByMac(String device_mac) {
        Bundle[] all_devices = null;
        if (isReady()){
            try {
                all_devices =  mBleOp.getDeviceInfoByAddress(device_mac);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("getDeviceInfoByMac::System Ready= " + isReady() +
                "getDeviceInfoByMac::Found= " +
                ((null == all_devices && all_devices.length <= 0) ? "No Device found":("Found device total= " + all_devices.length)));
        return all_devices;
    }

    /**
     * 根据输入的设备名称在所有被搜索到的LE外设中查找目标外设。
     * 如果输入的LE外设名称是空，则返回所有LE外设
     * @param device_name 需要查找的LE外设名称
     * @return 外设广播信息列表
     */
    public Bundle[] getDeviceInfoByName(String device_name) {
        Bundle[] all_devices = null;
        if (isReady()){
            try {
                all_devices = mBleOp.getDeviceInfoByName(device_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("getDeviceInfoByName::System Ready= " + isReady() +
                "getDeviceInfoByMac::Found= " +
                ((null == all_devices && all_devices.length <= 0) ? "No Device found":("Found device total= " + all_devices.length)));
        return all_devices;
    }

    /**
     * 查询当前设备是否支持LE特性
     * @return true 表示支持支持LE特性，反之不支持
     */
    public boolean isSupportLE() {
        boolean is_support = false;
        if (isReady()) {
            try {
                is_support = mBleOp.isSupportLE();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" isSupportLE::System Ready= " + isReady() + " result=" + is_support);
        return is_support;
    }

    /**
     * 获取设备蓝牙状态
     * @return true: 当前蓝牙开关已经开启；false：当前蓝牙开关已经关闭
     */
    public boolean isLeEnabled(){
        boolean is_enabled = false;
        if (isReady()) {
            try {
                is_enabled = mBleOp.leIsEnable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" isLeEnabled::System Ready= " + isReady() + " result=" + is_enabled);
        return is_enabled;
    }

    /**
     * 打开蓝牙开关
     * @return TRUE 表示成功；FALSE表示操作失败
     */
    public boolean enableBle(){
        boolean is_success = false;
        if (isReady()) {
            try {
                if (!isLeEnabled())
                    is_success = mBleOp.bleSwitcher(true);
                else
                    is_success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        BleLogUtils.outputManagerLog(" enableBle::System Ready= " + isReady() + " result=" + is_success);
        return is_success;
    }

    /**
     * 关闭蓝牙开关
     * @return TRUE表示成功
     */
    public boolean disableBle(){
        boolean is_success = false;
        if (isReady()) {
            try {
                if (isLeEnabled())
                    is_success = mBleOp.bleSwitcher(false);
                else
                    is_success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" disableBle::System Ready= " + isReady() + " result=" + is_success);
        return is_success;
    }

    /**
     * 对BLE服务的操作接口：启动BLE设备发现流程
     * 启动成功后，每次扫描到一个BLE设备就会通过@onDeviceScan回掉接口收到通知
     * @return TRUE,启动成功，FALSE，启动失败
     */
    public boolean startScanDevice(){
        boolean is_success = false;
        if (null != mBleOp){
            try {
                is_success = mBleOp.startDiscoveryDevice();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" startScanDevice::System Ready= " + isReady() + " result=" + is_success);
        return is_success;
    }

    /**
     * 连接到一个指定的LE外设
     * @param remote_address
     * @return
     */
    public boolean connectToDevice(boolean force_connect, String remote_address){
        boolean is_success = false;
        if (isReady()){
            try {
                is_success = mBleOp.connectToDevice(force_connect, remote_address);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" connectToDevice::System Ready= " + isReady() + " result=" + is_success);
        return is_success;
    }

    /**
     * 初始化LE外设广播监听
     * @param notification_uuid 通知的UUID
     */
    public void initialNotification(String notification_uuid){
        if (isReady()){
            try {
                mBleOp.initialNotification(notification_uuid);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" initialNotification::System Ready= " + isReady() + " operation finish!!!");
    }

    /**
     * 切断所有LE外设的LE连接
     * @return
     */
    public boolean disconnect(){
        boolean is_success = false;
        if (isReady()) {
            try {
                is_success = mBleOp.disconnect();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" disconnect::System Ready= " + isReady() + " result= " + is_success);
        return is_success;
    }

    /**
     * 向指定的特征发送读指令。
     * 【异步方法】
     * @param read_uuid 读信息的UUID
     * @return 操作是否成功
     */
    public boolean readCharacteristic(String read_uuid) {
        boolean is_success = false;
        if (isReady()){
            try {
                is_success =  mBleOp.readCharacteristic(read_uuid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" readCharacteristic::System Ready= " + isReady() + " result= " + is_success);
        return is_success;
    }

    /**
     * LE外设交互指令（写）：byte交互版本
     * 【异步方法】
     * @param write_uuid 写信息的UUID
     * @param write_content 写的内容
     * @return 操作是否成功
     */
     public boolean writeCharacteristic(String write_uuid, byte[] write_content){
        boolean is_success = false;
        if (isReady()) {
            try {
                is_success = mBleOp.writeCharacteristic(write_uuid, write_content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
         BleLogUtils.outputManagerLog(" writeCharacteristic::System Ready= " + isReady() + " result= " + is_success);
        return is_success;
     }

    /**
     * LE外设交互指令（写）：字符串交互版本
     * 【异步方法】
     * @param write_uuid 写信息的UUID
     * @param write_content 写的内容
     * @return 操作是否成功
     */
    public boolean writeCharacteristic(String write_uuid, String write_content){
        boolean is_success = false;
        if (isReady()) {
            try {
                is_success = mBleOp.writeCharacteristicString(write_uuid, write_content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" writeCharacteristic1::System Ready= " + isReady() + " result= " + is_success);
        return is_success;
    }

    /**
     * LE外设交互指令（写）：整型交互版本
     * 【异步方法】
     * @param write_uuid 写信息的UUID
     * @param write_content 写的内容
     * @param content_format 格式
     * @return 操作是否成功
     */
    public boolean writeCharacteristic(String write_uuid, int write_content, int content_format){
        boolean is_success = false;
        if (null != mBleOp) {
            try {
                is_success = mBleOp.writeCharacteristicInt(write_uuid, write_content, content_format);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog(" writeCharacteristic2::System Ready= " + isReady() + " result= " + is_success);
        return is_success;
    }

    public boolean readBattery(){
        boolean is_success = false;
        if (null != mBleOp) {
            try {
                is_success = mBleOp.getBatteryLevel();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return is_success;
    }

    public boolean readRSSI(){
        boolean is_success = false;
        if (null != mBleOp) {
            try {
                is_success = mBleOp.getRssi();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return is_success;
    }
    //-----------------------------------------implement ServiceConnection interface--------------------------------------//
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        BleLogUtils.outputManagerLog("BleManager::onServiceConnected::cn= " + componentName);
        mBleOp = IBleOp.Stub.asInterface(iBinder);
        if (null != mClientCallback)
            mClientCallback.onInitialManager(true);
        try {
            mBleOp.setBleOpCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        BleLogUtils.outputManagerLog("BleManager::onServiceDisconnected::cn= " + componentName);
        mBleOp = null;
        if (null != mClientCallback)
            mClientCallback.onInitialManager(false);
    }
    //-----------------------------------------implement ServiceConnection interface--------------------------------------//

    //-----------------------------------------implement IBleOpCallback interface----------------------------------------//
    @Override
    public void onDeviceScan(int scan_process, String device_name, String device_address,
                             String device_class, int device_rssi, byte[] broadcast_content) throws RemoteException {
        BleLogUtils.outputManagerLog("BleManager:onDeviceScan:name= " + device_name + " address= " + device_address +
                                     " class= " + device_class + " rssi = " + device_rssi + " content= " + Arrays.toString(broadcast_content) +
                                    " scan_process= " + scan_process);
        if (null != mClientCallback)
            mClientCallback.onLEScan(scan_process, device_name, device_class, device_address, device_rssi, broadcast_content);

    }

    @Override
    public void onBLESwitch(int current_state){
        BleLogUtils.outputManagerLog("BleManager:onBLESwitch:current_state= " + current_state);
        if (null != mClientCallback)
            mClientCallback.onLESwitch(current_state);
    }

    @Override
    public void onConnectToDevice(boolean is_success, String device_address, String device_name) throws RemoteException {
        BleLogUtils.outputManagerLog("onConnectToDevice::success= " + is_success +
                " device_address= " + device_address + " device_name= " + device_name);
        if (null != mClientCallback){
            mClientCallback.onConnectDevice(is_success, device_name, device_address);
        }
    }

    @Override
    public void onInitialNotification(boolean is_success, String notification_uuid) throws RemoteException {
        BleLogUtils.outputManagerLog("onInitialNotification::is_success= " + is_success +
                " notification_uuid= " + notification_uuid);
        if (null != mClientCallback)
            mClientCallback.onInitialNotification(is_success, notification_uuid);
    }

    @Override
    public void onReadCharacteristic(boolean is_success, String ch_uuid, byte[] ble_value) throws RemoteException {
        BleLogUtils.outputManagerLog("onReadCharacteristic::success= " + is_success + " uuid= " + ch_uuid +
                                   " value= " + Arrays.toString(ble_value));
        if (null != mClientCallback) {
            mClientCallback.onReadCh(0, is_success, ch_uuid, ble_value);
        }
    }

    @Override
    public void onCharacteristicChange(boolean is_success, String ch_uuid, byte[] ble_value) throws RemoteException {
        BleLogUtils.outputManagerLog("onCharacteristicChange::success= " + is_success + " uuid= " + ch_uuid +
                " value= " + Arrays.toString(ble_value));
        if (null != mClientCallback) {
            mClientCallback.onChChange(is_success, ch_uuid, ble_value);
        }
    }

    @Override
    public void onWriteCharacteristic(boolean is_success, String ch_uuid, byte[] ble_value) throws RemoteException {
        BleLogUtils.outputManagerLog("onWriteCharacteristic::success= " + is_success + " uuid= " + ch_uuid +
                " value= " + Arrays.toString(ble_value));
        if (null != mClientCallback) {
            mClientCallback.onWriteCh(0, is_success, ch_uuid, ble_value);
        }
    }

    @Override
    public void onDescriptorWrite(boolean is_success, String ch_uuid) throws RemoteException {

    }

    @Override
    public void onReadRSSI(boolean is_success, int current_rssi){
        BleLogUtils.outputManagerLog("onReadRSSI::success= " + is_success +  " value= " + current_rssi);
        if (null != mClientCallback)
            mClientCallback.onReadRSSI(is_success, current_rssi);
    }

    @Override
    public void onReadBattery(boolean is_success,  byte[] ble_value){
        BleLogUtils.outputManagerLog("onReadBattery::success= " + is_success +  " value= " + Arrays.toString(ble_value));
        if (null != mClientCallback)
            mClientCallback.onReadBattery(is_success, ble_value);
    }
    //-----------------------------------------implement IBleOpCallback interface----------------------------------------//

}
