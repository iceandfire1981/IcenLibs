package com.icen.blelibrary.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.icen.blelibrary.IBleOp;
import com.icen.blelibrary.IBleOpCallback;
import com.icen.blelibrary.R;
import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.blelibrary.utils.BleCommonUtils;
import com.icen.blelibrary.utils.BleLogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.icen.blelibrary.config.BleLibsConfig.DEFAULT_RSSI;

/**
 * Created by Alx Slash on 2017/10/29.
 * Author: alxslashtraces@gmail.com
 */

public class BleManagerService extends Service {

    private static final IntentFilter INTENT_FILTER = new IntentFilter();
    static {
        INTENT_FILTER.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        INTENT_FILTER.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
    }

    private BleOpImpl mBleOpImpl;
    private IBleOpCallback mBleOpCallback;

    private BluetoothAdapter mBleAdapter;
    private BluetoothDevice mCurrentDevice;
    private BluetoothGatt mCurrentGATT;

    private HashMap<String, Bundle> mCurrentDeviceMap;

    private String mSourceDeviceName, mSourceDeviceMAC;
    private ArrayList<BluetoothGattService> mAllServices;
    private HashMap<String, List<BluetoothGattCharacteristic>> mAllChMap;
    private boolean mAutoConnect, mIsScanning;
    private long mScanOvertime;

    private Handler mServiceHandler = new Handler();

    /**
     * 用于监听移动端设备蓝牙开关状态变化以及连接状态
     */
    private BroadcastReceiver mBleStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String current_action = (null == intent) ? null : intent.getAction();
            BleLogUtils.outputServiceLog("mBleStateReceiver::onReceive::Action= " +
                    (!TextUtils.isEmpty(current_action) ? current_action : "No Action Here"));
            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(current_action)) {//处理连接状态的ACTION

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(current_action)) {//处理蓝牙开启状态的ACTION
                int current_stats = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                BleLogUtils.outputServiceLog("mBleStateReceiver::onReceive::current_stats= " + current_stats);
                if (BluetoothAdapter.STATE_ON == current_stats) {//最终状态为关闭
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_ON);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothAdapter.STATE_OFF == current_stats) {//最终状态为打开
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_OFF);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothAdapter.STATE_TURNING_ON == current_stats) {//中间状态：正在打开
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_OPENING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothAdapter.STATE_TURNING_OFF == current_stats) {//中间状态：正在关闭
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_CLOSING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothAdapter.ERROR == current_stats){//失败状态
                    if (null != mBleOpCallback){
                        try {
                            mBleOpCallback.onBLESwitch(BleLibsConfig.BLE_SWITCH_ERROR);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    /**
     * 扫描回调接口
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] bytes) {
            String device_mac = bluetoothDevice.getAddress();
            String device_name = (TextUtils.isEmpty(bluetoothDevice.getName())) ?
                        getString(R.string.default_device_name) : bluetoothDevice.getName().trim();
            String device_class = bluetoothDevice.getBluetoothClass().toString();

            BleLogUtils.outputServiceLog("BleManagerService::onLeScan::mac= " + device_mac +
                    " name= " + device_name + " class= " + device_class +
                    " rssi= " + rssi + " content= " + ((null == bytes || bytes.length <= 0) ? "No Record" : Arrays.toString(bytes)));
            Bundle current_device = new Bundle();
            current_device.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME, device_name);
            current_device.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS, device_mac);
            current_device.putString(BleLibsConfig.BROADCAST_INFO_DEVICE_CLASS, device_class);
            current_device.putInt(BleLibsConfig.BROADCAST_INFO_SIGNAL, rssi);
            current_device.putByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT, bytes);
            mCurrentDeviceMap.put(device_mac, current_device);
            if (null != mBleOpCallback) {
                try {
                    mBleOpCallback.onDeviceScan(BleLibsConfig.LE_SCAN_PROCESS_DOING, bluetoothDevice.getName(),
                            bluetoothDevice.getAddress(), bluetoothDevice.getClass().toString(), rssi, bytes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * BLE外设管理接口
     * 包括：连接，读写，查询等回调
     */
    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

         @Override
         public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
             super.onPhyUpdate(gatt, txPhy, rxPhy, status);
         }

         @Override
         public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
             super.onPhyRead(gatt, txPhy, rxPhy, status);
         }

         @Override
         public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
             super.onConnectionStateChange(gatt, status, newState);
             BleLogUtils.outputServiceLog("gatt_callback::onConnectionStateChange::status= " + status +
                     " new_status= " + newState);
             if (BluetoothGatt.GATT_SUCCESS == status) {//操作成功
                if (BluetoothProfile.STATE_CONNECTED == newState) {//外设连接成功
                    mCurrentGATT = gatt;
                    boolean is_op_success = gatt.discoverServices();
                    if (!is_op_success) {
                        try {
                            mBleOpCallback.onConnectToDevice(false, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (BluetoothProfile.STATE_DISCONNECTED == newState) {//断开操作成功
                    mCurrentGATT = null;
                    gatt.close();
                }
             } else if (BluetoothGatt.GATT_FAILURE == status) {//操作失败
                if (null != mBleOpCallback) {
                    try {
                        mBleOpCallback.onConnectToDevice(false, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
             }
         }

         @Override
         public void onServicesDiscovered(BluetoothGatt gatt, int status) {
             super.onServicesDiscovered(gatt, status);
             BleLogUtils.outputServiceLog("gatt_callback::onServicesDiscovered::status= " + status +
                     " name= " + gatt.getDevice().getName() +
                     " address = " + gatt.getDevice().getAddress());
             if (BluetoothGatt.GATT_FAILURE == status) {//操作失败
                 if (null != mBleOpCallback) {
                     try {
                         mBleOpCallback.onConnectToDevice(false, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                     } catch (RemoteException e) {
                         e.printStackTrace();
                     }
                 }
             }  else if (BluetoothGatt.GATT_SUCCESS == status) {//刷新成功
                    List<BluetoothGattService> all_services = gatt.getServices();
                    if (null == all_services || all_services.size() <= 0) {//没有找到服务
                        if (null != mBleOpCallback) {
                            try {
                                mBleOpCallback.onConnectToDevice(false, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {//获取服务成功
                        if (null != mAllServices) {
                            mAllServices = null;
                        }
                        mAllServices = new ArrayList<>();
                        mAllChMap = new HashMap<>();
                        for (int service_index = 0; service_index < all_services.size(); service_index++){
                            BluetoothGattService current_service = all_services.get(service_index);
                            String service_uuid = current_service.getUuid().toString();
                            mAllServices.add(current_service);
                            List<BluetoothGattCharacteristic> all_chs = current_service.getCharacteristics();
                            BleLogUtils.outputServiceLog("gatt_callback::onServicesDiscovered::s_uuid= " + service_uuid + " ch_size= " +
                                    ((null == all_chs || all_chs.size() <= 0) ? " -1 " : String.valueOf(all_chs.size())));
                            if (null != all_chs && all_chs.size() > 0 ) {
                                mAllChMap.put(service_uuid, all_chs);
                            }
                        }
                        if (null != mBleOpCallback) {
                            try {
                                mBleOpCallback.onConnectToDevice(true, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        //保存连接信息
                        BleLibsConfig.saveDeviceInFile(BleManagerService.this,
                                gatt.getDevice().getName(), gatt.getDevice().getAddress());
                    }
             }
         }

         @Override
         public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
             super.onCharacteristicRead(gatt, characteristic, status);
             byte[] return_bytes = characteristic.getValue();

             BleLogUtils.outputServiceLog("BluetoothGattCallback::onCharacteristicRead::uuid= " + characteristic.getUuid().toString() +
                                " status= " + status + " value= " +
                                ((null == return_bytes || return_bytes.length < 0) ?
                                        "No data here" : Arrays.toString(return_bytes)));

             if (null != mBleOpCallback) {
                 try {
                     mBleOpCallback.onReadCharacteristic((status == BluetoothGatt.GATT_SUCCESS),
                             characteristic.getUuid().toString(),
                             characteristic.getValue());
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 }
             }
         }

         @Override
         public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
             super.onCharacteristicWrite(gatt, characteristic, status);
             byte[] return_bytes = characteristic.getValue();

             BleLogUtils.outputServiceLog("BluetoothGattCallback::onCharacteristicWrite::uuid= " +
                     characteristic.getUuid().toString() +
                     " status= " + status + " value= " +
                     ((null == return_bytes || return_bytes.length < 0) ?
                             "No data here" : Arrays.toString(return_bytes)));

             if (null != mBleOpCallback) {
                 try {
                     mBleOpCallback.onWriteCharacteristic((status == BluetoothGatt.GATT_SUCCESS),
                             characteristic.getUuid().toString(),
                             characteristic.getValue());
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 }
             }
         }

         @Override
         public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
             super.onCharacteristicChanged(gatt, characteristic);
             byte[] return_bytes = characteristic.getValue();

             BleLogUtils.outputServiceLog("BluetoothGattCallback::onCharacteristicWrite::uuid= " +
                     characteristic.getUuid().toString() +
                     " value= " +
                     ((null == return_bytes || return_bytes.length < 0) ?
                             "No data here" : Arrays.toString(return_bytes)));
             if (null != mBleOpCallback) {
                 try {
                     mBleOpCallback.onCharacteristicChange(true, characteristic.getUuid().toString(), characteristic.getValue());
                 } catch (RemoteException e) {
                     e.printStackTrace();
                 }
             }
         }

         @Override
         public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
             super.onDescriptorRead(gatt, descriptor, status);
         }

         @Override
         public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
             super.onDescriptorWrite(gatt, descriptor, status);
         }

         @Override
         public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
             super.onReliableWriteCompleted(gatt, status);
         }

         @Override
         public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
             super.onReadRemoteRssi(gatt, rssi, status);
         }

         @Override
         public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
             super.onMtuChanged(gatt, mtu, status);
         }
     };

    @Override
    public void onCreate() {
        super.onCreate();
        mBleAdapter = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter();
        initialSystemConfig();
        //注册广播监听器
        try {
            registerReceiver(mBleStateReceiver, INTENT_FILTER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBleAdapter.cancelDiscovery();
        try {
            unregisterReceiver(mBleStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        BleLogUtils.outputServiceLog("onBind::info= " + intent);
        if (null == mBleOpImpl)
            mBleOpImpl = new BleOpImpl(this, mBluetoothGattCallback);
        return mBleOpImpl;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        BleLogUtils.outputServiceLog("onUnbind::info= " + intent);
        return super.onUnbind(intent);
    }

    private void initialSystemConfig(){
        mSourceDeviceName = BleLibsConfig.getDeviceNameInFile(this);
        mSourceDeviceMAC = BleLibsConfig.getDeviceMacInFile(this);
        mAutoConnect = BleLibsConfig.getAutoConnectInFile(BleManagerService.this);
        mScanOvertime = BleLibsConfig.getScanOvertime(BleManagerService.this);
        mIsScanning = false;
    }

    private class BleOpImpl extends IBleOp.Stub{

        private Context mContext;
        private BluetoothGattCallback mGattCallback;
        public BleOpImpl(Context ctx, BluetoothGattCallback gatt_callback){
            mContext = ctx;
            mGattCallback = gatt_callback;
        }

        @Override
        public boolean isSupportLE(){
            boolean is_support = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
            BleLogUtils.outputServiceLog("isSupportLE::result= " + is_support);
            return is_support;
        }

        @Override
        public boolean leIsEnable() throws RemoteException {
            boolean is_enable = false;
            if (null != mBleAdapter) {
                is_enable = mBleAdapter.isEnabled();
            }
            BleLogUtils.outputServiceLog("BleOpImpl::leIsEnable::result= " + is_enable);
            return is_enable;
        }

        @Override
        public boolean bleSwitcher(boolean enabled) throws RemoteException {
            boolean is_success = false;

            if (null != mBleAdapter) {
                stopDiscoveryDevice();
                if (enabled)
                    is_success = mBleAdapter.enable();
                else {
                    is_success = mBleAdapter.disable();
                }
            }
            BleLogUtils.outputServiceLog("BleOpImpl::bleSwitcher::enabled= " + enabled + " result= " + is_success);
            return is_success;
        }

        @Override
        public Bundle[] getDeviceInfo(){
            int device_size = (null == mCurrentDeviceMap || mCurrentDeviceMap.size() == 0) ?
                    -1 : mCurrentDeviceMap.size();
            BleLogUtils.outputServiceLog("BleOpImpl::getDeviceInfo::device_size= " + device_size);

            if (device_size > 0) {
                Bundle[] device_bundles = new Bundle[mCurrentDeviceMap.size()];
                int device_index = 0;
                Iterator device_iterator = mCurrentDeviceMap.entrySet().iterator();
                while (device_iterator.hasNext()){
                    Map.Entry<String, Bundle> device_entry =
                            (Map.Entry<String, Bundle>) device_iterator.next();
                    device_bundles[device_index] = device_entry.getValue();
                    device_index = device_index + 1;
                }
                return device_bundles;
            } else {
                return null;
            }
        }

        @Override
        public Bundle[]   getDeviceInfoByAddress(String device_mac){
            int device_size = (null == mCurrentDeviceMap || mCurrentDeviceMap.size() == 0) ?
                    -1 : mCurrentDeviceMap.size();
            BleLogUtils.outputServiceLog("BleOpImpl::getDeviceInfoByAddress::mac= " + device_mac + " device_size= " + device_size);

            if (device_size > 0 && !TextUtils.isEmpty(device_mac)) {
                Iterator device_iterator = mCurrentDeviceMap.entrySet().iterator();
                while (device_iterator.hasNext()) {
                    Map.Entry<String, Bundle> entry =
                            (Map.Entry<String, Bundle>) device_iterator.next();
                    String current_mac = entry.getValue().getString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS);
                    if (device_mac.equalsIgnoreCase(current_mac) ) {
                        Bundle[] result_device_list = new Bundle[1];
                        result_device_list[0] = entry.getValue();
                        return result_device_list;
                    }
                }
                return null;
            }
            return null;
        }

        @Override
        public Bundle[] getDeviceInfoByName(String device_name){
            int device_size = (null == mCurrentDeviceMap || mCurrentDeviceMap.size() == 0) ?
                    -1 : mCurrentDeviceMap.size();
            BleLogUtils.outputServiceLog("BleOpImpl::getDeviceInfoByName::name= " + device_name + " device_size= " + device_size);

            ArrayList<Bundle> device_list = new ArrayList();
            if (device_size > 0) {
                Iterator device_iterator = mCurrentDeviceMap.entrySet().iterator();
                if (!TextUtils.isEmpty(device_name)){
                    while (device_iterator.hasNext()) {
                        Map.Entry<String, Bundle> entry =
                                (Map.Entry<String, Bundle>) device_iterator.next();
                        device_list.add(entry.getValue());
                    }
                }
            }

            if (null == device_list || device_list.size() <= 0) {
                return null;
            } else {
                Bundle[] all_devices = new Bundle[device_list.size()];
                for (int index = 0; index < device_list.size(); index ++) {
                    all_devices[index] = device_list.get(index);
                }
                return all_devices;
            }
        }

        @Override
        public Bundle[] getServices() throws RemoteException {
            if (null == mAllServices || mAllServices.size() <= 0) {
                BleLogUtils.outputServiceLog("BleOpImpl::getServices::info::There is no service here");
                return null;
            } else {
                Bundle[] all_services_bundle = new Bundle[mAllServices.size()];
                for (int service_index = 0; service_index < mAllServices.size(); service_index++){
                    Bundle service_bundle = new Bundle();
                    String service_uuid = mAllServices.get(service_index).getUuid().toString();
                    String service_name = BleCommonUtils.lookupService(service_uuid);
                    String service_type = BleCommonUtils.getServiceType(mAllServices.get(service_index).getType());
                    BleLogUtils.outputServiceLog("BleOpImpl::getServices::process::uuid= " + service_uuid +
                            " name= " + service_name + " type= " + service_type);
                    service_bundle.putString(BleLibsConfig.LE_SERVICE_NAME, service_name);
                    service_bundle.putString(BleLibsConfig.LE_SERVICE_UUID, service_uuid);
                    service_bundle.putString(BleLibsConfig.LE_SERVICE_TYPE, service_type);
                    all_services_bundle[service_index] = service_bundle;
                }
                BleLogUtils.outputServiceLog("BleOpImpl::getServices::info::total= " + all_services_bundle.length);
                return all_services_bundle;
            }
        }

        @Override
        public Bundle[] getCharacteristic(String input_ch_uuid) throws RemoteException {
            BleLogUtils.outputServiceLog("BleOpImpl::getCharacteristic::params= " + input_ch_uuid);
            Bundle[] all_ch_bundle = null;
            if (!TextUtils.isEmpty(input_ch_uuid) && null != mAllChMap && mAllChMap.size() > 0){
                for (Map.Entry<String, List<BluetoothGattCharacteristic>> ch_entry : mAllChMap.entrySet()){
                    String current_service_uuid = ch_entry.getKey();
                    List<BluetoothGattCharacteristic> current_ch_list = ch_entry.getValue();
                    BleLogUtils.outputServiceLog("BleOpImpl::getCharacteristic::Service= " + current_service_uuid +
                                                " found ch total= " + ((null != current_ch_list && current_ch_list.size() > 0) ?
                                                                        String.valueOf(current_ch_list.size()) : "-1"));
                    if (input_ch_uuid.equalsIgnoreCase(current_service_uuid) &&
                            null != current_ch_list && current_ch_list.size() > 0) {
                        all_ch_bundle = new Bundle[current_ch_list.size()];
                        for (int ch_index = 0; ch_index < current_ch_list.size(); ch_index++) {
                            BluetoothGattCharacteristic current_ch = current_ch_list.get(ch_index);
                            String ch_uuid = current_ch.getUuid().toString();
                            String ch_name = BleCommonUtils.lookupCharacteristic(ch_uuid);
                            int    ch_permission = current_ch.getPermissions();
                            int    ch_pro = current_ch.getProperties();
                            BleLogUtils.outputServiceLog("BleOpImpl::getCharacteristic::ch= " + ch_uuid +
                                    " name= " + ch_name + " permission= " + ch_permission + " pro= " + ch_pro + " index= " + ch_index);
                            Bundle ch_bundle = new Bundle();
                            ch_bundle.putString(BleLibsConfig.LE_CHARACTERISTIC_UUID, ch_uuid);
                            ch_bundle.putString(BleLibsConfig.LE_CHARACTERISTIC_NAME, ch_name);
                            ch_bundle.putInt(BleLibsConfig.LE_CHARACTERISTIC_PERMISSION, ch_permission);
                            ch_bundle.putInt(BleLibsConfig.LE_CHARACTERISTIC_PROPERTIES, ch_pro);
                            all_ch_bundle[ch_index] = ch_bundle;
                        }
                        break;
                    }
                }
            }
            BleLogUtils.outputServiceLog("BleOpImpl::getCharacteristic::service= " + input_ch_uuid +
                    " found ch total= " + ((null != all_ch_bundle && all_ch_bundle.length > 0) ?
                    String.valueOf(all_ch_bundle.length) : "-1"));
            return all_ch_bundle;
        }

        @Override
        public boolean startDiscoveryDevice() throws RemoteException {
            //扫描开始前先确认参数：是否自动重连，扫描超时时长
            mAutoConnect = BleLibsConfig.getAutoConnectInFile(BleManagerService.this);
            mScanOvertime = BleLibsConfig.getScanOvertime(BleManagerService.this);
            mSourceDeviceMAC = BleLibsConfig.getDeviceMacInFile(BleManagerService.this);

            boolean is_success = false;
            if (null != mBleAdapter) {
                mCurrentDeviceMap = new HashMap<>();
                if (mIsScanning) {//当前如果正在扫描需要先停止
                    stopDiscoveryDevice();
                    mIsScanning = false;
                }
                is_success = mBleAdapter.startLeScan(mLeScanCallback);
            }

            BleLogUtils.outputServiceLog("BleOpImpl::startDiscoveryDevice::a_c= "
                    + mAutoConnect + " s_o= " + mScanOvertime + " s_mac= " + mSourceDeviceMAC + " op_result= "
                    + is_success + " mIsScanning= " + mIsScanning);
            if (is_success){
                if (null != mBleOpCallback) {
                    mBleOpCallback.onDeviceScan(BleLibsConfig.LE_SCAN_PROCESS_BEGIN, null, null,
                            null, DEFAULT_RSSI, null);
                }
                mIsScanning = true;
                //指定时间后停止扫描
                mServiceHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            BleLogUtils.outputServiceLog("BleOpImpl::startDiscoveryDevice::overtime now, auto= " +
                                    mAutoConnect + " connected= " + hasConnectToDevice());
                            stopDiscoveryDevice();//停止扫描
                            //在没有连接任何设备的情况下自动连接上次成功连接并且当前存在的设备
                            if (mAutoConnect && !hasConnectToDevice()){
                                Bundle[] device_list = getDeviceInfoByAddress(mSourceDeviceMAC);
                                if (null != device_list && device_list.length > 0){
                                    connectToDevice(false, mSourceDeviceMAC);
                                }
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }, mScanOvertime);
            } else {
                if (null != mBleOpCallback)
                    mBleOpCallback.onDeviceScan(BleLibsConfig.LE_SCAN_PROCESS_EXCEPTION, null, null,
                            null, DEFAULT_RSSI, null);
                mIsScanning = false;
            }
            return is_success;
        }

        @Override
        public void stopDiscoveryDevice() throws RemoteException {
            BleLogUtils.outputServiceLog("BleOpImpl::stopDiscoveryDevice::mIs= " + mIsScanning);
            if (null != mBleAdapter && mIsScanning) {
                mBleAdapter.stopLeScan(mLeScanCallback);
                mBleAdapter.cancelDiscovery();
                mIsScanning = false;
            }
            if (null != mBleOpCallback)
                mBleOpCallback.onDeviceScan(BleLibsConfig.LE_SCAN_PROCESS_END, null, null,
                        null, DEFAULT_RSSI, null);
        }

        @Override
        public boolean connectToDevice(boolean force_connect, final String remote_address) throws RemoteException {
            boolean is_success;
            BleLogUtils.outputServiceLog("BleOpImpl::connectToDevice::info::address= " + remote_address +
                                        " Scanning= " + mIsScanning + " connect= " + hasConnectToDevice() + " f_c= " + force_connect);

            //连接开始前需要停止扫描
            if (mIsScanning){
                stopDiscoveryDevice();
                mIsScanning = false;
            }

            //如果当前已经连接一个外设则开始刷新外设服务（Service）列表
            if (hasConnectToDevice()) {
                if (force_connect) {
                    disconnect();//断开连接
                    //3秒后重连
                    mServiceHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                connectToDevice(false, remote_address);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 3000);
                    return true;
                } else {
                    is_success = mCurrentGATT.discoverServices();
                }
            } else {
                //容错：防止适配器为NULL
                if (null == mBleAdapter){
                    mBleAdapter = ((BluetoothManager) getSystemService(BLUETOOTH_SERVICE)).getAdapter();
                }
                //获取外设描述实例
                mCurrentDevice = mBleAdapter.getRemoteDevice(remote_address);
                if (null == mCurrentDevice)
                    is_success = false;
                else {
                    BluetoothGatt b_gatt = mCurrentDevice.connectGatt(mContext, false, mGattCallback);
                    if (null == b_gatt) {
                        is_success = false;
                    } else {
                        is_success = true;
                    }
                }
            }
            BleLogUtils.outputServiceLog("BleOpImpl::connectToDevice::op_result= " + is_success);
            return is_success;
        }

        @Override
        public void initialNotification(String notification_uuid) throws RemoteException {
            BleLogUtils.outputServiceLog("BleOpImpl::initialNotification::param= " + notification_uuid +
                    " service_size= " + ((null != mAllServices && mAllServices.size() > 0) ? String.valueOf(mAllServices.size()) : "-1") +
                    " has_connect= " + hasConnectToDevice());
            BluetoothGattCharacteristic target_ch = null;
            //设置之前必须外设处于连接状态，并且外设有一些服务
            if (hasConnectToDevice() && null != mAllServices && mAllServices.size() > 0 &&
                    !TextUtils.isEmpty(notification_uuid)) {

                for (BluetoothGattService current_service : mAllServices) {
                    List<BluetoothGattCharacteristic> ch_list = current_service.getCharacteristics();
                    if (null != ch_list && ch_list.size() > 0 ) {
                        for (BluetoothGattCharacteristic current_ch : ch_list) {
                            String ch_uuid = current_ch.getUuid().toString();
                            if (notification_uuid.equalsIgnoreCase(ch_uuid)) {
                                target_ch = current_ch;
                                break;
                            }
                        }
                    }
                    if (null != target_ch)
                        break;
                }
                if (null != target_ch) {//如果已经找到特征
                    boolean enable_ch = mCurrentGATT.setCharacteristicNotification(target_ch, true);
                    BleLogUtils.outputServiceLog("BleOpImpl::initialNotification::enable= " + enable_ch);
                    List<BluetoothGattDescriptor> all_ch_desc = target_ch.getDescriptors();
                    if (null != all_ch_desc && all_ch_desc.size() > 0) {
                        for (BluetoothGattDescriptor desc : all_ch_desc) {
                            desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mCurrentGATT.writeDescriptor(desc);
                        }
                    }
                    try {
                        mBleOpCallback.onInitialNotification(true, notification_uuid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        mBleOpCallback.onInitialNotification(false, notification_uuid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (null != mBleOpCallback) {
                    try {
                        mBleOpCallback.onInitialNotification(false, notification_uuid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public boolean disconnect() throws RemoteException {
            BleLogUtils.outputServiceLog("BleOpImpl::disconnect::gatt= " + mCurrentGATT);
            if (null != mCurrentGATT) {
                mCurrentGATT.disconnect();
                mCurrentGATT.close();
                mCurrentGATT = null;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean readCharacteristic(String read_uuid) throws RemoteException {
            boolean is_success = false;
            int service_list_count = (null != mAllServices && mAllServices.size() > 0) ? mAllServices.size() : 0;
            if (!TextUtils.isEmpty(read_uuid) && service_list_count > 0) {
                BluetoothGattCharacteristic target_ch = null;
                for (BluetoothGattService current_service : mAllServices) {
                    List<BluetoothGattCharacteristic> ch_in_service = current_service.getCharacteristics();
                    if (null != ch_in_service && ch_in_service.size() > 0) {
                        for (BluetoothGattCharacteristic current_ch : ch_in_service) {
                            String ch_uuid = current_ch.getUuid().toString();
                            if (read_uuid.equalsIgnoreCase(ch_uuid)){
                                target_ch = current_ch;
                                break;
                            }
                        }
                    }
                    if (null != target_ch)
                        break;
                }

                if (null != target_ch) {
                    is_success = mCurrentGATT.readCharacteristic(target_ch);
                }
            }
            BleLogUtils.outputServiceLog("BleOpImpl::readCharacteristic::uuid= " + read_uuid + " result= " + is_success);
            return is_success;
        }

        @Override
        public boolean writeCharacteristic(String write_uuid, byte[] write_content) throws RemoteException {
            boolean is_success = false;
            int service_list_count = (null != mAllServices && mAllServices.size() > 0) ? mAllServices.size() : 0;
            if (!TextUtils.isEmpty(write_uuid) && service_list_count > 0) {
                BluetoothGattCharacteristic target_ch = null;
                for (BluetoothGattService current_service : mAllServices) {
                    List<BluetoothGattCharacteristic> ch_in_service = current_service.getCharacteristics();
                    if (null != ch_in_service && ch_in_service.size() > 0) {
                        for (BluetoothGattCharacteristic current_ch : ch_in_service) {
                            String ch_uuid = current_ch.getUuid().toString();
                            if (write_uuid.equalsIgnoreCase(ch_uuid)){
                                target_ch = current_ch;
                                break;
                            }
                        }
                    }
                    if (null != target_ch)
                        break;
                }

                if (null != target_ch) {
                    target_ch.setValue(write_content);
                    is_success = mCurrentGATT.writeCharacteristic(target_ch);
                }
            }
            BleLogUtils.outputServiceLog("BleOpImpl::writeCharacteristic::uuid= " + write_uuid + " result= "
                    + is_success + " content= " + Arrays.toString(write_content));
            return is_success;
        }

        @Override
        public boolean writeCharacteristicString(String write_uuid, String write_content) throws RemoteException {
            boolean is_success = false;
            int service_list_count = (null != mAllServices && mAllServices.size() > 0) ? mAllServices.size() : 0;
            if (!TextUtils.isEmpty(write_uuid) && service_list_count > 0) {
                BluetoothGattCharacteristic target_ch = null;
                for (BluetoothGattService current_service : mAllServices) {
                    List<BluetoothGattCharacteristic> ch_in_service = current_service.getCharacteristics();
                    if (null != ch_in_service && ch_in_service.size() > 0) {
                        for (BluetoothGattCharacteristic current_ch : ch_in_service) {
                            String ch_uuid = current_ch.getUuid().toString();
                            if (write_uuid.equalsIgnoreCase(ch_uuid)){
                                target_ch = current_ch;
                                break;
                            }
                        }
                    }
                    if (null != target_ch)
                        break;
                }

                if (null != target_ch) {
                    target_ch.setValue(write_content);
                    is_success = mCurrentGATT.writeCharacteristic(target_ch);
                }
            }
            BleLogUtils.outputServiceLog("BleOpImpl::writeCharacteristic1::uuid= " + write_uuid + " result= "
                    + is_success + " content= " + write_content);
            return is_success;
        }

        @Override
        public boolean writeCharacteristicInt(String write_uuid, int write_content, int content_format) throws RemoteException {
            boolean is_success = false;
            int service_list_count = (null != mAllServices && mAllServices.size() > 0) ? mAllServices.size() : 0;
            if (!TextUtils.isEmpty(write_uuid) && service_list_count > 0) {
                BluetoothGattCharacteristic target_ch = null;
                for (BluetoothGattService current_service : mAllServices) {
                    List<BluetoothGattCharacteristic> ch_in_service = current_service.getCharacteristics();
                    if (null != ch_in_service && ch_in_service.size() > 0) {
                        for (BluetoothGattCharacteristic current_ch : ch_in_service) {
                            String ch_uuid = current_ch.getUuid().toString();
                            if (write_uuid.equalsIgnoreCase(ch_uuid)){
                                target_ch = current_ch;
                                break;
                            }
                        }
                    }
                    if (null != target_ch)
                        break;
                }

                if (null != target_ch) {
                    target_ch.setValue(write_content, content_format, 0);
                    is_success = mCurrentGATT.writeCharacteristic(target_ch);
                }
            }
            BleLogUtils.outputServiceLog("BleOpImpl::writeCharacteristic2::uuid= " + write_uuid + " result= "
                    + is_success + " content= " + write_content);
            return is_success;
        }

        @Override
        public void setBleOpCallback(IBleOpCallback op_callback) throws RemoteException {
            BleLogUtils.outputServiceLog("BleOpImpl::setBleOpCallback:info= " + op_callback);
            mBleOpCallback = op_callback;
        }

        @Override
        public boolean hasConnectToDevice() throws RemoteException {
            boolean has_connect;
            if (null != mCurrentGATT && null != mCurrentDevice) {
                has_connect = true;
            }else {
                has_connect = false;
            }
            BleLogUtils.outputServiceLog("hasConnectToDevice::result= " + has_connect);
            return has_connect;
        }
    }
}
