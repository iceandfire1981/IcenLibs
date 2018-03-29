package com.icen.blelibrary.model;

import com.icen.blelibrary.utils.BleLogUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * BLE广播数据的数据结构
 * Created by icean on 2018/2/27.
 */

public final class AdvData {

    private byte[] mFlags;
    private byte[] mLocalName;
    private byte[] mAllLocalName;
    private byte[] mPowerLevel;
    private byte[] mDeviceClass;
    private byte[] mSimplePairingHashC;
    private byte[] mSimplePairingRandomizerR;
    private byte[] mSecurityManagerTKValue;
    private byte[] mSecurityManagerOOBFlags;


    private HashMap<String, UUIDData> mUUIDMap;


    public void setFlag(byte[] current_flag) {
        if (null == current_flag || current_flag.length <= 0)
            return;

        ByteBuffer flag_buffer = ByteBuffer.wrap(current_flag);
        if (null != mFlags)
                mFlags = null;
        mFlags = new byte[flag_buffer.remaining()];
        flag_buffer.get(mFlags, 0, mFlags.length);
    }

    public byte[] getFlag(){
        return mFlags;
    }

    public void setLocalName(byte[] current_local_name) {
        if (null == current_local_name || current_local_name.length <= 0)
            return;

        ByteBuffer flag_buffer = ByteBuffer.wrap(current_local_name);
        if (null != mLocalName)
            mLocalName = null;
        mLocalName = new byte[flag_buffer.remaining()];
        flag_buffer.get(mLocalName, 0, mLocalName.length);

    }

    public byte[] getLocalName() {
        return mLocalName;
    }

    public void setAllLocalName(byte[] current_all_local_name) {
        if (null == current_all_local_name || current_all_local_name.length <= 0)
            return;

        ByteBuffer flag_buffer = ByteBuffer.wrap(current_all_local_name);
        if (null != mAllLocalName)
            mAllLocalName = null;
        mAllLocalName = new byte[flag_buffer.remaining()];
        flag_buffer.get(mAllLocalName, 0, mAllLocalName.length);
    }

    public byte[] getAllLocalName(){
        return mAllLocalName;
    }

    public void setPowerLevel(byte[] power_level){
        if (null == power_level || power_level.length <= 0)
            return;

        ByteBuffer flag_buffer = ByteBuffer.wrap(power_level);
        if (null != mPowerLevel)
            mPowerLevel = null;
        mPowerLevel = new byte[flag_buffer.remaining()];
        flag_buffer.get(mPowerLevel, 0, mPowerLevel.length);
    }

    public byte[] getPowerLevel(){
        return mPowerLevel;
    }

    public void setDeviceClass(byte[] device_class){
        if (null == device_class || device_class.length <= 0)
            return;

        ByteBuffer flag_buffer = ByteBuffer.wrap(device_class);
        if (null != mDeviceClass)
            mDeviceClass = null;
        mDeviceClass = new byte[flag_buffer.remaining()];
        flag_buffer.get(mDeviceClass, 0, mDeviceClass.length);
    }

    public byte[] getDeviceClass(){
        return mDeviceClass;
    }

    public void setSimplePairingHashC(byte[] s_p_hash_c){
        if (null == s_p_hash_c || s_p_hash_c.length <= 0)
            return;

        ByteBuffer flag_buffer = ByteBuffer.wrap(s_p_hash_c);
        if (null != mSimplePairingHashC)
            mSimplePairingHashC = null;
        mSimplePairingHashC = new byte[flag_buffer.remaining()];
        flag_buffer.get(mSimplePairingHashC, 0, mSimplePairingHashC.length);
    }

    public byte[] getSimplePairingHashC(){
        return mSimplePairingHashC;
    }

    public void setSimplePairingRandomizerR(byte[] s_p_r_r){
        if (null == s_p_r_r || s_p_r_r.length <= 0)
            return;

        ByteBuffer flag_buffer = ByteBuffer.wrap(s_p_r_r);
        if (null != mSimplePairingRandomizerR)
            mSimplePairingRandomizerR = null;
        mSimplePairingRandomizerR = new byte[flag_buffer.remaining()];
        flag_buffer.get(mSimplePairingRandomizerR, 0, mSimplePairingRandomizerR.length);
    }

    public byte[] getSimplePairingRandomizerR(){
        return mSimplePairingRandomizerR;
    }



    public void addUUID(int uuid_type, byte[] uuid_value){
        if (null == mUUIDMap) {
            mUUIDMap = new HashMap<>();
        }
        if (!mUUIDMap.containsKey(Arrays.toString(uuid_value))) {
            UUIDData uuid_data = new UUIDData();
            uuid_data.setUUIDType(uuid_type);
            uuid_data.setUUIDValue(uuid_value);
        }
    }

    public boolean clearUUIDRecordByUUID(byte[] input_uuid) {
        if (null == input_uuid || input_uuid.length <= 0) {
            return true;
        } else {
            String map_key = Arrays.toString(input_uuid);
            if (mUUIDMap.containsKey(map_key)) {
                mUUIDMap.remove(map_key);
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean clearUUIDRecordByType(int input_type) {
        if (null == mUUIDMap || mUUIDMap.size() <= 0 ) {
            return true;
        } else {
            Iterator map_iter = mUUIDMap.entrySet().iterator();
            int delete_record = -1;
            while (map_iter.hasNext()) {
                Map.Entry<String, UUIDData> uuid_info_record = (Map.Entry<String, UUIDData>) map_iter.next();
                UUIDData current_data = uuid_info_record.getValue();
                BleLogUtils.outputUtilLog("clearUUIDRecordByType::target_info= " + current_data.toString());
                if (input_type == current_data.getUUIDType()) {
                    mUUIDMap.remove(uuid_info_record.getKey());
                    delete_record = delete_record + 1;
                }
            }

            if (delete_record > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    public byte[] getUUIDByType(int input_type) {
        if (null != mUUIDMap && mUUIDMap.size() > 0) {
            Iterator map_iter = mUUIDMap.entrySet().iterator();
            byte[] bytes = null;
            while (map_iter.hasNext()) {
                Map.Entry<String, UUIDData> uuid_info_record = (Map.Entry<String, UUIDData>) map_iter.next();
                UUIDData current_data = uuid_info_record.getValue();
                BleLogUtils.outputUtilLog("getUUIDByType::target_info= " + current_data.toString() + " type= " + input_type);
                if (input_type == current_data.getUUIDType()) {
                    ByteBuffer record_buffer = ByteBuffer.wrap(current_data.getUUIDValue());
                    bytes = new byte[record_buffer.remaining()];
                    record_buffer.get(bytes, 0, bytes.length);
                    break;
                }
            }
            return bytes;
        } else {
            return null;
        }
    }

    /**
     * 一个用于描述广播的UUID的结构，包括UUID的值以及UUID的类型
     * mUUIDValue：保存了UUID的值
     * mUUIDType : 保存UUID的类型
     */
    private class UUIDData{
        private ByteBuffer mUUIDValue;
        private int mUUIDType;

        public UUIDData(){
            mUUIDValue = null;
        }

        public void setUUIDValue(byte[] uuid_value) {
            if (null != uuid_value && uuid_value.length > 0) {
                mUUIDValue = ByteBuffer.wrap(uuid_value);
            }
        }

        public byte[] getUUIDValue(){
            if (null != mUUIDValue) {
                byte[] result_value = new byte[mUUIDValue.remaining()];
                mUUIDValue.get(result_value, 0, result_value.length);
                return result_value;
            } else {
                return null;
            }
        }

        public void setUUIDType(int uuid_type) {
            mUUIDType = uuid_type;
        }

        public int getUUIDType(){
            return mUUIDType;
        }

        @Override
        public String toString() {
            StringBuffer uuid_info_buffer = new StringBuffer();
            uuid_info_buffer.append("uuid_info::type= ");
            uuid_info_buffer.append(mUUIDType);
            uuid_info_buffer.append(" uuid= ");
            if (null == mUUIDValue){
                uuid_info_buffer.append("NA");
            } else {
                byte[] uuid_value = new byte[mUUIDValue.remaining()];
                mUUIDValue.get(uuid_value, 0, uuid_value.length);
                uuid_info_buffer.append(Arrays.toString(uuid_value));
            }
            return uuid_info_buffer.toString();
        }
    }
}
