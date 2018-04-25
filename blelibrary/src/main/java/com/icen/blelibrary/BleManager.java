package com.icen.blelibrary;


import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

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

    private Activity mContext;
    private IBleOp mBleOp;

    private BleManagerCallBack mClientCallback;

    public BleManager(Activity ctx){
        mContext = ctx;
        mClientCallback = null;
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

        BleLogUtils.outputManagerLog("BleManager::startManager::is_success= " + is_success);
        return is_success;
    }

    /**
     * 销毁管理器，包括：
     *  取消返回回调接口；断开BLE到手机的连接；断开管理器和管理服务的连接
     */
    public void destroyManager(){
        BleLogUtils.outputManagerLog("BleManager::destroyManager::op= " + mBleOp);
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
        if (null != mBleOp){
            try {
                return mBleOp.hasConnectToDevice();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取附近的BLE设备
     * @return BLE设备列表
     */
    public Bundle[] getAllDevices(){
        if (null != mBleOp){
            try {
                Bundle[] all_devices = mBleOp.getDeviceInfo();
                return all_devices;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取已经连接的BLE外设包含的服务列表
     * @return 服务列表，是一个BUNDLE数组
     */
    public Bundle[] getDeviceService(){
        if (null != mBleOp){
            try {
                Bundle[] all_services = mBleOp.getServices();
                BleLogUtils.outputManagerLog("getDeviceService::Get service= "  +
                        ((null == all_services || all_services.length <= 0) ?
                                "No service in device" : String.valueOf(all_services.length)));
                return all_services;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("getDeviceService::Get device service false");
        return null;
    }

    /**
     * 获取指定服务中包含的特征列表
     * @param service_uuid 指定的UUID
     * @return 服务中包含的特征列表
     */
    public Bundle[] getAllCharacteristicInService(String service_uuid) {
        if (null != mBleOp) {
            try {
                Bundle[] all_characteristics = mBleOp.getCharacteristic(service_uuid);
                BleLogUtils.outputManagerLog("getAllCharacteristicInService::Get s_uuid= " + service_uuid +
                        " characteristics result= " + ((null == all_characteristics || all_characteristics.length <= 0) ?
                        "No characteristics" : String.valueOf(all_characteristics.length)));
                return all_characteristics;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("getAllCharacteristicInService::Get s_uuid= " + service_uuid +
                " characteristics false");
        return null;
    }

    /**
     * 获取BLE外设包含的所有特征，返回一个hash-map，依据服务的UUID进行分类
     * @return HashMap，包含了所有的特征
     */
    public HashMap<String, ArrayList<Bundle>> getAllCharacteristics() {
        HashMap<String, ArrayList<Bundle>> characteristic_map = new HashMap<>();
        if (null != mBleOp) {
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
        BleLogUtils.outputManagerLog("getAllCharacteristics::Get characteristic finish. Result = " +
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
        if (null != mBleOp){
            try {
                return mBleOp.getDeviceInfoByAddress(device_mac);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 根据输入的设备名称在所有被搜索到的LE外设中查找目标外设。
     * 如果输入的LE外设名称是空，则返回所有LE外设
     * @param device_name 需要查找的LE外设名称
     * @return 外设广播信息列表
     */
    public Bundle[] getDeviceInfoByName(String device_name) {
        if (null != mBleOp){
            try {
                return mBleOp.getDeviceInfoByName(device_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取设备蓝牙状态
     * @return true: 当前蓝牙开关已经开启；false：当前蓝牙开关已经关闭
     */
    public boolean isLeEnabled(){
        boolean is_enabled = false;
        if (null != mBleOp) {
            try {
                is_enabled = mBleOp.leIsEnable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("BleManager::isLeEnabled= " + is_enabled);
        return is_enabled;
    }

    /**
     * 打开蓝牙开关
     * @return TRUE 表示成功；FALSE表示操作失败
     */
    public boolean enableBle(){
        boolean is_success = false;
        if (null != mBleOp) {
            try {
                if (!isLeEnabled())
                    is_success = mBleOp.bleSwitcher(true);
                else
                    is_success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (null != mClientCallback)
            mClientCallback.onLESwitch(true, is_success);
        BleLogUtils.outputManagerLog("BleManager::enableBle= " + is_success);
        return is_success;
    }

    /**
     * 关闭蓝牙开关
     * @return TRUE表示成功
     */
    public boolean disableBle(){
        boolean is_success = false;
        if (null != mBleOp) {
            try {
                if (isLeEnabled())
                    is_success = mBleOp.bleSwitcher(false);
                else
                    is_success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != mClientCallback)
            mClientCallback.onLESwitch(false, is_success);
        BleLogUtils.outputManagerLog("BleManager::disableBle= " + is_success);
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
                BleLogUtils.outputManagerLog("BleManager::startDiscoveryDevice::exception= " + e.getCause());
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("BleManager::startDiscoveryDevice::mCompleteFinish= " + is_success);
        return is_success;
    }

    /**
     * 连接到一个指定的LE外设
     * @param remote_address
     * @return
     */
    public boolean connectToDevice(String remote_address){
        if (null != mBleOp){
            try {
                return mBleOp.connectToDevice(remote_address);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 初始化LE外设广播监听
     * @param notification_uuid 通知的UUID
     */
    public void initialNotification(String notification_uuid){
        if (null != mBleOp){
            try {
                mBleOp.initialNotification(notification_uuid);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 切断所有LE外设的LE连接
     * @return
     */
    public boolean disconnect(){
        if (null != mBleOp) {
            try {
                return mBleOp.disconnect();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 切断与指定外设名称的LE外设的连接
     * 如果参数为NULL，与disconnect效果相同
     * @param device_name LE外设名称，扫描阶段提供
     * @return
     */
    public boolean disconnectByName(String device_name){
        if (null != mBleOp) {
            try {
                return mBleOp.disconnectByName(device_name);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 切断与指定MAC地址的LE外设的连接
     * 如果参数为NULL，与disconnect效果相同
     * @param device_mac LE外设MAC地址，扫描阶段提供
     * @return
     */
    public boolean disconnectByMac(String device_mac){
        if (null != mBleOp) {
            try {
                return mBleOp.disconnectByName(device_mac);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return false;
    }

    //-----------------------------------------implement ServiceConnection interface--------------------------------------//
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        BleLogUtils.outputManagerLog("BleManager::onServiceConnected::cn= " + componentName);
        mBleOp = IBleOp.Stub.asInterface(iBinder);
        if (null != mClientCallback)
            mClientCallback.onInitialManager(true);
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
                             String device_class, byte[] broadcast_content) throws RemoteException {
        BleLogUtils.outputManagerLog("BleManager:onDeviceScan:name= " + device_name + " address= " + device_address +
                                     " class= " + device_class + " content= " + Arrays.toString(broadcast_content) +
                                    " scan_process= " + scan_process);
        if (null != mClientCallback)
            mClientCallback.onLEScan(scan_process, device_name, device_class, device_address, broadcast_content);

    }

    @Override
    public void onConnectToDevice(boolean is_success, String device_address, String device_name) throws RemoteException {

    }

    @Override
    public void onInitialNotification(boolean is_success) throws RemoteException {

    }

    @Override
    public void onReadCharacteristic(boolean is_success, String ch_uuid, byte[] ble_value) throws RemoteException {

    }

    @Override
    public void onCharacteristicChange(boolean is_success, String ch_uuid, byte[] ble_value) throws RemoteException {

    }

    @Override
    public void onWriteCharacteristic(boolean is_success, String ch_uuid, byte[] ble_value) throws RemoteException {

    }

    @Override
    public void onDescriptorWrite(boolean is_success, String ch_uuid) throws RemoteException {

    }
    //-----------------------------------------implement IBleOpCallback interface----------------------------------------//

}
