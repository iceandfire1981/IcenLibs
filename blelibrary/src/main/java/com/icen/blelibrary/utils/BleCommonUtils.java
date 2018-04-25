package com.icen.blelibrary.utils;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BleCommonUtils {
    /**
     * 通用低功耗蓝牙服务UUID字典
     */
    private static final HashMap<String, String> BLE_SERVICE_MAP = new HashMap<>();
    static {
        BLE_SERVICE_MAP.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        BLE_SERVICE_MAP.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        BLE_SERVICE_MAP.put("00001800-0000-1000-8000-00805f9b34fb", "GenericAccess");
        BLE_SERVICE_MAP.put("00001801-0000-1000-8000-00805f9b34fb", "GenericAttribute");
        BLE_SERVICE_MAP.put("00001802-0000-1000-8000-00805f9b34fb", "Immediate Alert");
        BLE_SERVICE_MAP.put("00001803-0000-1000-8000-00805f9b34fb", "Link Loss");
        BLE_SERVICE_MAP.put("00001804-0000-1000-8000-00805f9b34fb", "Tx Power");
        BLE_SERVICE_MAP.put("00001805-0000-1000-8000-00805f9b34fb", "Current Time Service");
        BLE_SERVICE_MAP.put("00001806-0000-1000-8000-00805f9b34fb", "Reference Time Update Service");
        BLE_SERVICE_MAP.put("00001807-0000-1000-8000-00805f9b34fb", "Next DST Change Service");
        BLE_SERVICE_MAP.put("00001808-0000-1000-8000-00805f9b34fb", "Glucose");
        BLE_SERVICE_MAP.put("00001809-0000-1000-8000-00805f9b34fb", "Health Thermometer");
        BLE_SERVICE_MAP.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information");
        BLE_SERVICE_MAP.put("0000180b-0000-1000-8000-00805f9b34fb", "Network Availability");
        BLE_SERVICE_MAP.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate");
        BLE_SERVICE_MAP.put("0000180e-0000-1000-8000-00805f9b34fb", "Phone Alert Status Service");
        BLE_SERVICE_MAP.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Service");
        BLE_SERVICE_MAP.put("00001810-0000-1000-8000-00805f9b34fb", "Blood Pressure");
        BLE_SERVICE_MAP.put("00001811-0000-1000-8000-00805f9b34fb", "Alert Notification Service");
        BLE_SERVICE_MAP.put("00001812-0000-1000-8000-00805f9b34fb", "Human Interface Device");
        BLE_SERVICE_MAP.put("00001813-0000-1000-8000-00805f9b34fb", "Scan Parameters");
        BLE_SERVICE_MAP.put("00001814-0000-1000-8000-00805f9b34fb", "Running Speed and Cadence");
        BLE_SERVICE_MAP.put("00001816-0000-1000-8000-00805f9b34fb", "Cycling Speed and Cadence");
        BLE_SERVICE_MAP.put("00001818-0000-1000-8000-00805f9b34fb", "Cycling Power");
        BLE_SERVICE_MAP.put("00001819-0000-1000-8000-00805f9b34fb", "Location and Navigation");
    }

    /**
     * 通用低功耗蓝牙特征字段
     */
    private static final HashMap<String, String> BLE_CHARACTERISTIC_MAP = new HashMap<>();
    static {
        BLE_CHARACTERISTIC_MAP.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        BLE_CHARACTERISTIC_MAP.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        BLE_CHARACTERISTIC_MAP.put("00002a00-0000-1000-8000-00805f9b34fb", "Device Name");
        BLE_CHARACTERISTIC_MAP.put("00002a01-0000-1000-8000-00805f9b34fb", "Appearance");
        BLE_CHARACTERISTIC_MAP.put("00002a02-0000-1000-8000-00805f9b34fb", "Peripheral Privacy Flag");
        BLE_CHARACTERISTIC_MAP.put("00002a03-0000-1000-8000-00805f9b34fb", "Reconnection Address");
        BLE_CHARACTERISTIC_MAP.put("00002a04-0000-1000-8000-00805f9b34fb", "PPCP");
        BLE_CHARACTERISTIC_MAP.put("00002a05-0000-1000-8000-00805f9b34fb", "Service Changed");
        BLE_CHARACTERISTIC_MAP.put("00002a06-0000-1000-8000-00805f9b34fb", "Alert Level");
        BLE_CHARACTERISTIC_MAP.put("00002a07-0000-1000-8000-00805f9b34fb", "Tx Power Level");
        BLE_CHARACTERISTIC_MAP.put("00002a08-0000-1000-8000-00805f9b34fb", "Date Time");
        BLE_CHARACTERISTIC_MAP.put("00002a09-0000-1000-8000-00805f9b34fb", "Day of Week");
        BLE_CHARACTERISTIC_MAP.put("00002a0a-0000-1000-8000-00805f9b34fb", "Day Date Time");
        BLE_CHARACTERISTIC_MAP.put("00002a0c-0000-1000-8000-00805f9b34fb", "Exact Time 256");
        BLE_CHARACTERISTIC_MAP.put("00002a0d-0000-1000-8000-00805f9b34fb", "DST Offset");
        BLE_CHARACTERISTIC_MAP.put("00002a0e-0000-1000-8000-00805f9b34fb", "Time Zone");
        BLE_CHARACTERISTIC_MAP.put("00002a0f-0000-1000-8000-00805f9b34fb", "Local Time Information");
        BLE_CHARACTERISTIC_MAP.put("00002a11-0000-1000-8000-00805f9b34fb", "Time with DST");
        BLE_CHARACTERISTIC_MAP.put("00002a12-0000-1000-8000-00805f9b34fb", "Time Accuracy");
        BLE_CHARACTERISTIC_MAP.put("00002a13-0000-1000-8000-00805f9b34fb", "Time Source");
        BLE_CHARACTERISTIC_MAP.put("00002a14-0000-1000-8000-00805f9b34fb", "Reference Time Information");
        BLE_CHARACTERISTIC_MAP.put("00002a16-0000-1000-8000-00805f9b34fb", "Time Update Control Point");
        BLE_CHARACTERISTIC_MAP.put("00002a17-0000-1000-8000-00805f9b34fb", "Time Update State");
        BLE_CHARACTERISTIC_MAP.put("00002a18-0000-1000-8000-00805f9b34fb", "Glucose Measurement");
        BLE_CHARACTERISTIC_MAP.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level");
        BLE_CHARACTERISTIC_MAP.put("00002a1c-0000-1000-8000-00805f9b34fb", "Temperature Measurement");
        BLE_CHARACTERISTIC_MAP.put("00002a1d-0000-1000-8000-00805f9b34fb", "Temperature Type");
        BLE_CHARACTERISTIC_MAP.put("00002a1e-0000-1000-8000-00805f9b34fb", "Intermediate Temperature");
        BLE_CHARACTERISTIC_MAP.put("00002a21-0000-1000-8000-00805f9b34fb", "Measurement Interval");
        BLE_CHARACTERISTIC_MAP.put("00002a22-0000-1000-8000-00805f9b34fb", "Boot Keyboard Input Report");
        BLE_CHARACTERISTIC_MAP.put("00002a23-0000-1000-8000-00805f9b34fb", "System ID");
        BLE_CHARACTERISTIC_MAP.put("00002a24-0000-1000-8000-00805f9b34fb", "Model Number String");
        BLE_CHARACTERISTIC_MAP.put("00002a25-0000-1000-8000-00805f9b34fb", "Serial Number String");
        BLE_CHARACTERISTIC_MAP.put("00002a26-0000-1000-8000-00805f9b34fb", "Firmware Revision String");
        BLE_CHARACTERISTIC_MAP.put("00002a27-0000-1000-8000-00805f9b34fb", "Hardware Revision String");
        BLE_CHARACTERISTIC_MAP.put("00002a28-0000-1000-8000-00805f9b34fb", "Software Revision String");
        BLE_CHARACTERISTIC_MAP.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        BLE_CHARACTERISTIC_MAP.put("00002a2a-0000-1000-8000-00805f9b34fb", "IEEE 11073-20601 Regulatory Certification Data List");
        BLE_CHARACTERISTIC_MAP.put("00002a2b-0000-1000-8000-00805f9b34fb", "Current Time");
        BLE_CHARACTERISTIC_MAP.put("00002a31-0000-1000-8000-00805f9b34fb", "Scan Refresh");
        BLE_CHARACTERISTIC_MAP.put("00002a32-0000-1000-8000-00805f9b34fb", "Boot Keyboard Output Report");
        BLE_CHARACTERISTIC_MAP.put("00002a33-0000-1000-8000-00805f9b34fb", "Boot Mouse Input Report");
        BLE_CHARACTERISTIC_MAP.put("00002a34-0000-1000-8000-00805f9b34fb", "Glucose Measurement Context");
        BLE_CHARACTERISTIC_MAP.put("00002a35-0000-1000-8000-00805f9b34fb", "Blood Pressure Measurement");
        BLE_CHARACTERISTIC_MAP.put("00002a36-0000-1000-8000-00805f9b34fb", "Intermediate Cuff Pressure");
        BLE_CHARACTERISTIC_MAP.put("00002a37-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        BLE_CHARACTERISTIC_MAP.put("00002a38-0000-1000-8000-00805f9b34fb", "Body Sensor Location");
        BLE_CHARACTERISTIC_MAP.put("00002a39-0000-1000-8000-00805f9b34fb", "Heart Rate Control Point");
        BLE_CHARACTERISTIC_MAP.put("00002a3e-0000-1000-8000-00805f9b34fb", "Network Availability");
        BLE_CHARACTERISTIC_MAP.put("00002a3f-0000-1000-8000-00805f9b34fb", "Alert Status");
        BLE_CHARACTERISTIC_MAP.put("00002a40-0000-1000-8000-00805f9b34fb", "Ringer Control Point");
        BLE_CHARACTERISTIC_MAP.put("00002a41-0000-1000-8000-00805f9b34fb", "Ringer Setting");
        BLE_CHARACTERISTIC_MAP.put("00002a42-0000-1000-8000-00805f9b34fb", "Alert Category ID Bit Mask");
        BLE_CHARACTERISTIC_MAP.put("00002a43-0000-1000-8000-00805f9b34fb", "Alert Category ID");
        BLE_CHARACTERISTIC_MAP.put("00002a44-0000-1000-8000-00805f9b34fb", "Alert Notification Control Point");
        BLE_CHARACTERISTIC_MAP.put("00002a45-0000-1000-8000-00805f9b34fb", "Unread Alert Status");
        BLE_CHARACTERISTIC_MAP.put("00002a46-0000-1000-8000-00805f9b34fb", "New Alert");
        BLE_CHARACTERISTIC_MAP.put("00002a47-0000-1000-8000-00805f9b34fb", "Supported New Alert Category");
        BLE_CHARACTERISTIC_MAP.put("00002a48-0000-1000-8000-00805f9b34fb", "Supported Unread Alert Category");
        BLE_CHARACTERISTIC_MAP.put("00002a49-0000-1000-8000-00805f9b34fb", "Blood Pressure Feature");
        BLE_CHARACTERISTIC_MAP.put("00002a4a-0000-1000-8000-00805f9b34fb", "HID Information");
        BLE_CHARACTERISTIC_MAP.put("00002a4b-0000-1000-8000-00805f9b34fb", "Report Map");
        BLE_CHARACTERISTIC_MAP.put("00002a4c-0000-1000-8000-00805f9b34fb", "HID Control Point");
        BLE_CHARACTERISTIC_MAP.put("00002a4d-0000-1000-8000-00805f9b34fb", "Report");
        BLE_CHARACTERISTIC_MAP.put("00002a4e-0000-1000-8000-00805f9b34fb", "Protocol Mode");
        BLE_CHARACTERISTIC_MAP.put("00002a4f-0000-1000-8000-00805f9b34fb", "Scan Interval Window");
        BLE_CHARACTERISTIC_MAP.put("00002a50-0000-1000-8000-00805f9b34fb", "PnP ID");
        BLE_CHARACTERISTIC_MAP.put("00002a51-0000-1000-8000-00805f9b34fb", "Glucose Feature");
        BLE_CHARACTERISTIC_MAP.put("00002a52-0000-1000-8000-00805f9b34fb", "Record Access Control Point");
        BLE_CHARACTERISTIC_MAP.put("00002a53-0000-1000-8000-00805f9b34fb", "RSC Measurement");
        BLE_CHARACTERISTIC_MAP.put("00002a54-0000-1000-8000-00805f9b34fb", "RSC Feature");
        BLE_CHARACTERISTIC_MAP.put("00002a55-0000-1000-8000-00805f9b34fb", "SC Control Point");
        BLE_CHARACTERISTIC_MAP.put("00002a5b-0000-1000-8000-00805f9b34fb", "CSC Measurement");
        BLE_CHARACTERISTIC_MAP.put("00002a5c-0000-1000-8000-00805f9b34fb", "CSC Feature");
        BLE_CHARACTERISTIC_MAP.put("00002a5d-0000-1000-8000-00805f9b34fb", "Sensor Location");
        BLE_CHARACTERISTIC_MAP.put("00002a63-0000-1000-8000-00805f9b34fb", "Cycling Power Measurement");
        BLE_CHARACTERISTIC_MAP.put("00002a64-0000-1000-8000-00805f9b34fb", "Cycling Power Vector");
        BLE_CHARACTERISTIC_MAP.put("00002a65-0000-1000-8000-00805f9b34fb", "Cycling Power Feature");
        BLE_CHARACTERISTIC_MAP.put("00002a66-0000-1000-8000-00805f9b34fb", "Cycling Power Control Point");
        BLE_CHARACTERISTIC_MAP.put("00002a67-0000-1000-8000-00805f9b34fb", "Location and Speed");
        BLE_CHARACTERISTIC_MAP.put("00002a68-0000-1000-8000-00805f9b34fb", "Navigation");
        BLE_CHARACTERISTIC_MAP.put("00002a69-0000-1000-8000-00805f9b34fb", "Position Quality");
        BLE_CHARACTERISTIC_MAP.put("00002a6a-0000-1000-8000-00805f9b34fb", "LN Feature");
        BLE_CHARACTERISTIC_MAP.put("00002a6b-0000-1000-8000-00805f9b34fb", "LN Control Point");
    }

    /**
     *  Android框架定义的特征权限字典
     */
    private static HashMap<Integer, String> CHAR_PERMISSION_MAP = new HashMap();
    static {
        CHAR_PERMISSION_MAP.put(0, "UNKNOWN");
        CHAR_PERMISSION_MAP.put(BluetoothGattCharacteristic.PERMISSION_READ, "READ");
        CHAR_PERMISSION_MAP.put(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED, "READ_ENCRYPTED");
        CHAR_PERMISSION_MAP.put(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM, "READ_ENCRYPTED_MITM");
        CHAR_PERMISSION_MAP.put(BluetoothGattCharacteristic.PERMISSION_WRITE, "WRITE");
        CHAR_PERMISSION_MAP.put(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED, "WRITE_ENCRYPTED");
        CHAR_PERMISSION_MAP.put(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM, "WRITE_ENCRYPTED_MITM");
        CHAR_PERMISSION_MAP.put(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED, "WRITE_SIGNED");
        CHAR_PERMISSION_MAP.put(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM, "WRITE_SIGNED_MITM");
    }

    /**
     * Android框架定义的特征属性字典
     */
    private static HashMap<Integer, String> CHAR_PROPERTIES_MAP = new HashMap();
    static {

        CHAR_PROPERTIES_MAP.put(BluetoothGattCharacteristic.PROPERTY_BROADCAST, "BROADCAST");
        CHAR_PROPERTIES_MAP.put(BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS, "EXTENDED_PROPS");
        CHAR_PROPERTIES_MAP.put(BluetoothGattCharacteristic.PROPERTY_INDICATE, "INDICATE");
        CHAR_PROPERTIES_MAP.put(BluetoothGattCharacteristic.PROPERTY_NOTIFY, "NOTIFY");
        CHAR_PROPERTIES_MAP.put(BluetoothGattCharacteristic.PROPERTY_READ, "READ");
        CHAR_PROPERTIES_MAP.put(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE, "SIGNED_WRITE");
        CHAR_PROPERTIES_MAP.put(BluetoothGattCharacteristic.PROPERTY_WRITE, "WRITE");
        CHAR_PROPERTIES_MAP.put(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, "WRITE_NO_RESPONSE");
    }

    private static HashMap<Integer, String> DESC_PERMISSION_MAP = new HashMap();
    static {
        DESC_PERMISSION_MAP.put(0, "UNKNOWN");
        DESC_PERMISSION_MAP.put(BluetoothGattDescriptor.PERMISSION_READ, "READ");
        DESC_PERMISSION_MAP.put(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED, "READ_ENCRYPTED");
        DESC_PERMISSION_MAP.put(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM, "READ_ENCRYPTED_MITM");
        DESC_PERMISSION_MAP.put(BluetoothGattDescriptor.PERMISSION_WRITE, "WRITE");
        DESC_PERMISSION_MAP.put(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED, "WRITE_ENCRYPTED");
        DESC_PERMISSION_MAP.put(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM, "WRITE_ENCRYPTED_MITM");
        DESC_PERMISSION_MAP.put(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED, "WRITE_SIGNED");
        DESC_PERMISSION_MAP.put(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM, "WRITE_SIGNED_MITM");
    }

    private static final String DEFAULT_SERVICE_NAME = "Unknown Service";
    private static final String DEFAULT_CHARACTERISTIC_NAME = "UnKnown";
    private static final String DEFAULT_SERVICE_TYPE = "Unknown Service Type";

    private static boolean hasLeFeature(Context ctx) {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static final String lookupService(String service_uuid) {
        String service_name = BLE_SERVICE_MAP.get(service_uuid);
        return (TextUtils.isEmpty(service_name) ? DEFAULT_SERVICE_NAME : service_name);
    }

    public static final String lookupCharacteristic(String ch_uuid) {
        String characteristic_name = BLE_CHARACTERISTIC_MAP.get(ch_uuid);
        return (TextUtils.isEmpty(characteristic_name) ? DEFAULT_CHARACTERISTIC_NAME : characteristic_name);
    }

    private static HashMap<Integer, String> SERVICE_TYPE = new HashMap();
    static {
        SERVICE_TYPE.put(BluetoothGattService.SERVICE_TYPE_PRIMARY, "PRIMARY");
        SERVICE_TYPE.put(BluetoothGattService.SERVICE_TYPE_SECONDARY, "SECONDARY");
    }

    public static String getServiceType(int type){
        String service_type = SERVICE_TYPE.get(type);
        return (TextUtils.isEmpty(service_type) ? DEFAULT_SERVICE_TYPE : service_type);
    }

    public static String getCharPermission(int permission){
        return getHashMapValue(CHAR_PERMISSION_MAP,permission);
    }

    public static String getCharPropertie(int property){
        return getHashMapValue(CHAR_PROPERTIES_MAP,property);
    }

    public static String getDescPermission(int property){
        return getHashMapValue(DESC_PERMISSION_MAP,property);
    }

    private static String getHashMapValue(HashMap<Integer, String> hashMap,int number){
        String result =hashMap.get(number);
        if(TextUtils.isEmpty(result)){
            List<Integer> numbers = getElement(number);
            result="";
            for(int i=0;i<numbers.size();i++){
                result+=hashMap.get(numbers.get(i))+"|";
            }
        }
        return result;
    }

    private static List<Integer> getElement(int number){
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < 32; i++){
            int b = 1 << i;
            if ((number & b) > 0)
                result.add(b);
        }
        return result;
    }

}
