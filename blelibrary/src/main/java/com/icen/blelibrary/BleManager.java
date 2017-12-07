package com.icen.blelibrary;


import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.google.protobuf.ByteString;
import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.blelibrary.config.ConnectBleDevice;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.Arrays;

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

    public BleManager(Activity ctx){
        mContext = ctx;
    }

    /**
     *  启动并连接BLE管理服务
     * @return is_success: true：表示启动服务成功，false：表示启动服务失败
     */
    public boolean startManager(){
        boolean is_success = BleLibsConfig.startBleService(mContext, this);
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

    public boolean enableBle(){
        boolean is_success = false;
        if (null != mBleOp) {
            try {
                is_success = mBleOp.bleSwitcher(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BleLogUtils.outputManagerLog("BleManager::enableBle= " + is_success);
        return is_success;
    }

    public boolean disableBle(){
        boolean is_success = false;
        if (null != mBleOp) {
            try {
                is_success = mBleOp.bleSwitcher(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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



    //-----------------------------------------implement ServiceConnection interface--------------------------------------//
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        BleLogUtils.outputManagerLog("BleManager::onServiceConnected::cn= " + componentName);
        mBleOp = IBleOp.Stub.asInterface(iBinder);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        BleLogUtils.outputManagerLog("BleManager::onServiceDisconnected::cn= " + componentName);
        mBleOp = null;
    }
    //-----------------------------------------implement ServiceConnection interface--------------------------------------//

    //-----------------------------------------implement IBleOpCallback interface----------------------------------------//
    @Override
    public void onDeviceScan(int scan_process, String device_name, String device_address,
                             String device_class, byte[] broadcast_content) throws RemoteException {
        BleLogUtils.outputManagerLog("BleManager:onDeviceScan:name= " + device_name + " address= " + device_address +
                                     " class= " + device_class + " content= " + Arrays.toString(broadcast_content) +
                                    " scan_process= " + scan_process);

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
