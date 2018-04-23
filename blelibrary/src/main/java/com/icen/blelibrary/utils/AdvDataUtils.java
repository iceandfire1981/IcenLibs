package com.icen.blelibrary.utils;

import com.icen.blelibrary.model.AdvData;

import java.util.Arrays;


/**
 * Created by icean on 2018/2/8.
 */

public final class AdvDataUtils {

    public static final int SERVICE_UUID_TYPE_16_MORE   = 0;
    public static final int SERVICE_UUID_TYPE_16_ALL    = 1;
    public static final int SERVICE_UUID_TYPE_32_MORE   = 2;
    public static final int SERVICE_UUID_TYPE_32_ALL    = 3;
    public static final int SERVICE_UUID_TYPE_128_MORE  = 4;
    public static final int SERVICE_UUID_TYPE_128_ALL   = 5;
    public static final int SERVICE_DATA_UUID_TYPE_32   = 6;
    public static final int SERVICE_DATA_UUID_TYPE_128  = 7;
    public static final int SOLICITED_SERVICE_UUIDS_16  = 8;
    public static final int SOLICITED_SERVICE_UUIDS_128 = 9;


    public static final short BLE_GAP_AD_TYPE_FLAGS                               = 0x01; /**< Flags for discoverability. */
    public static final short BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE   = 0x02; /**< Partial list of 16 bit service UUIDs. */
    public static final short BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_COMPLETE         = 0x03; /**< Complete list of 16 bit service UUIDs. */
    public static final short BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_MORE_AVAILABLE   = 0x04; /**< Partial list of 32 bit service UUIDs. */
    public static final short BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_COMPLETE         = 0x05; /**< Complete list of 32 bit service UUIDs. */
    public static final short BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE  = 0x06; /**< Partial list of 128 bit service UUIDs. */
    public static final short BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_COMPLETE        = 0x07; /**< Complete list of 128 bit service UUIDs. */
    public static final short BLE_GAP_AD_TYPE_SHORT_LOCAL_NAME                    = 0x08; /**< Short local device name. */
    public static final short BLE_GAP_AD_TYPE_COMPLETE_LOCAL_NAME                 = 0x09; /**< Complete local device name. */
    public static final short BLE_GAP_AD_TYPE_TX_POWER_LEVEL                      = 0x0A; /**< Transmit power level. */
    public static final short BLE_GAP_AD_TYPE_CLASS_OF_DEVICE                     = 0x0D; /**< Class of device. */
    public static final short BLE_GAP_AD_TYPE_SIMPLE_PAIRING_HASH_C               = 0x0E; /**< Simple Pairing Hash C. */
    public static final short BLE_GAP_AD_TYPE_SIMPLE_PAIRING_RANDOMIZER_R         = 0x0F; /**< Simple Pairing Randomizer R. */
    public static final short BLE_GAP_AD_TYPE_SECURITY_MANAGER_TK_VALUE           = 0x10; /**< Security Manager TK Value. */
    public static final short BLE_GAP_AD_TYPE_SECURITY_MANAGER_OOB_FLAGS          = 0x11; /**< Security Manager Out Of Band Flags. */
    public static final short BLE_GAP_AD_TYPE_SLAVE_CONNECTION_INTERVAL_RANGE     = 0x12; /**< Slave Connection Interval Range. */
    public static final short BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_16BIT       = 0x14; /**< List of 16-bit Service Solicitation UUIDs. */
    public static final short BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_128BIT      = 0x15; /**< List of 128-bit Service Solicitation UUIDs. */
    public static final short BLE_GAP_AD_TYPE_SERVICE_DATA                        = 0x16; /**< Service Data - 16-bit UUID. */
    public static final short BLE_GAP_AD_TYPE_PUBLIC_TARGET_ADDRESS               = 0x17; /**< Public Target Address. */
    public static final short BLE_GAP_AD_TYPE_RANDOM_TARGET_ADDRESS               = 0x18; /**< Random Target Address. */
    public static final short BLE_GAP_AD_TYPE_APPEARANCE                          = 0x19; /**< Appearance. */
    public static final short BLE_GAP_AD_TYPE_ADVERTISING_INTERVAL                = 0x1A; /**< Advertising Interval. */
    public static final short BLE_GAP_AD_TYPE_LE_BLUETOOTH_DEVICE_ADDRESS         = 0x1B; /**< LE Bluetooth Device Address. */
    public static final short BLE_GAP_AD_TYPE_LE_ROLE                             = 0x1C; /**< LE Role. */
    public static final short BLE_GAP_AD_TYPE_SIMPLE_PAIRING_HASH_C256            = 0x1D; /**< Simple Pairing Hash C-256. */
    public static final short BLE_GAP_AD_TYPE_SIMPLE_PAIRING_RANDOMIZER_R256      = 0x1E; /**< Simple Pairing Randomizer R-256. */
    public static final short BLE_GAP_AD_TYPE_SERVICE_DATA_32BIT_UUID             = 0x20; /**< Service Data - 32-bit UUID. */
    public static final short BLE_GAP_AD_TYPE_SERVICE_DATA_128BIT_UUID            = 0x21; /**< Service Data - 128-bit UUID. */
    public static final short BLE_GAP_AD_TYPE_3D_INFORMATION_DATA                 = 0x3D; /**< 3D Information Data. */
    public static final short BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA          = 0xFF; /**< Manufacturer Specific Data. */

    /**
     * 解析广播数据，生成广播数据结构
     * @param adv_data  BLE广播数据
     * @return  广播数据实例
     */
    public static final AdvData advDataParser(byte[] adv_data) {
        BleLogUtils.outputUtilLog("advDataParser::info= " + Arrays.toString(adv_data));
        AdvData current_data = new AdvData();
        for (int loop_index = 0; loop_index < adv_data.length; loop_index ++) {
            int adv_data_length = adv_data[loop_index];
            short adv_type = adv_data[loop_index + 1];
            byte[] current_adv_data = new byte[adv_data_length - 1];
            for (int data_index = 0; data_index < adv_data_length - 1; data_index ++) {
                current_adv_data[data_index] = adv_data[loop_index + 2 + data_index];
            }
            BleLogUtils.outputUtilLog("advDataParser::field_info::index= " + loop_index +
                            " type = " + adv_type + " data= " + Arrays.toString(current_adv_data));
            switch (adv_type) {
                case BLE_GAP_AD_TYPE_FLAGS:
                    byte[] current_flag = adv_report_parse(BLE_GAP_AD_TYPE_FLAGS, adv_data);
                    current_data.setFlag(current_flag);
                    break;
                case BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE:
                    current_data.addUUID(SERVICE_UUID_TYPE_16_MORE,
                            adv_report_parse(BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_MORE_AVAILABLE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_COMPLETE:
                    current_data.addUUID(SERVICE_UUID_TYPE_16_ALL,
                            adv_report_parse(BLE_GAP_AD_TYPE_16BIT_SERVICE_UUID_COMPLETE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_MORE_AVAILABLE:
                    current_data.addUUID(SERVICE_UUID_TYPE_32_MORE,
                            adv_report_parse(BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_MORE_AVAILABLE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_COMPLETE:
                    current_data.addUUID(SERVICE_UUID_TYPE_32_ALL,
                            adv_report_parse(BLE_GAP_AD_TYPE_32BIT_SERVICE_UUID_COMPLETE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE:
                    current_data.addUUID(SERVICE_UUID_TYPE_128_MORE,
                            adv_report_parse(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_MORE_AVAILABLE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_COMPLETE:
                    current_data.addUUID(SERVICE_UUID_TYPE_128_ALL,
                            adv_report_parse(BLE_GAP_AD_TYPE_128BIT_SERVICE_UUID_COMPLETE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SHORT_LOCAL_NAME:
                    current_data.setLocalName(adv_report_parse(BLE_GAP_AD_TYPE_SHORT_LOCAL_NAME, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_COMPLETE_LOCAL_NAME:
                    current_data.setAllLocalName(adv_report_parse(BLE_GAP_AD_TYPE_COMPLETE_LOCAL_NAME, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_TX_POWER_LEVEL:
                    current_data.setPowerLevel(adv_report_parse(BLE_GAP_AD_TYPE_TX_POWER_LEVEL, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_CLASS_OF_DEVICE:
                    current_data.setDeviceClass(adv_report_parse(BLE_GAP_AD_TYPE_CLASS_OF_DEVICE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SIMPLE_PAIRING_HASH_C:
                    current_data.setSimplePairingHashC(adv_report_parse(BLE_GAP_AD_TYPE_SIMPLE_PAIRING_HASH_C, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SIMPLE_PAIRING_RANDOMIZER_R:
                    current_data.setSimplePairingRandomizerR(adv_report_parse(BLE_GAP_AD_TYPE_SIMPLE_PAIRING_RANDOMIZER_R, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SECURITY_MANAGER_TK_VALUE:
                    current_data.setSecurityManagerTKValue(adv_report_parse(BLE_GAP_AD_TYPE_SECURITY_MANAGER_TK_VALUE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SECURITY_MANAGER_OOB_FLAGS:
                    current_data.setSecurityManagerOOBFlags(adv_report_parse(BLE_GAP_AD_TYPE_SECURITY_MANAGER_OOB_FLAGS, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SLAVE_CONNECTION_INTERVAL_RANGE:
                    current_data.setSlaveConnectionInternalRange(adv_report_parse(BLE_GAP_AD_TYPE_SLAVE_CONNECTION_INTERVAL_RANGE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_16BIT:
                    current_data.addUUID(SOLICITED_SERVICE_UUIDS_16,
                            adv_report_parse(BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_16BIT, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_128BIT:
                    current_data.addUUID(SOLICITED_SERVICE_UUIDS_128,
                            adv_report_parse(BLE_GAP_AD_TYPE_SOLICITED_SERVICE_UUIDS_128BIT, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SERVICE_DATA:
                    current_data.setServiceData(adv_report_parse(BLE_GAP_AD_TYPE_SERVICE_DATA, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_PUBLIC_TARGET_ADDRESS:
                    current_data.setPublicTargetAddress(adv_report_parse(BLE_GAP_AD_TYPE_PUBLIC_TARGET_ADDRESS, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_RANDOM_TARGET_ADDRESS:
                    current_data.setRandomTargetAddress(adv_report_parse(BLE_GAP_AD_TYPE_RANDOM_TARGET_ADDRESS, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_APPEARANCE:
                    current_data.setAppearance(adv_report_parse(BLE_GAP_AD_TYPE_APPEARANCE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_ADVERTISING_INTERVAL:
                    current_data.setAdvertisintInternal(adv_report_parse(BLE_GAP_AD_TYPE_ADVERTISING_INTERVAL, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_LE_BLUETOOTH_DEVICE_ADDRESS:
                    current_data.setLEBluetoothDeviceAddress(adv_report_parse(BLE_GAP_AD_TYPE_LE_BLUETOOTH_DEVICE_ADDRESS, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_LE_ROLE:
                    current_data.setLERole(adv_report_parse(BLE_GAP_AD_TYPE_LE_ROLE, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SIMPLE_PAIRING_HASH_C256:
                    current_data.setSimplePairingHashC256(adv_report_parse(BLE_GAP_AD_TYPE_SIMPLE_PAIRING_HASH_C256, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SIMPLE_PAIRING_RANDOMIZER_R256:
                    current_data.setSimplePairingRANDOMIZERC256(adv_report_parse(BLE_GAP_AD_TYPE_SIMPLE_PAIRING_RANDOMIZER_R256, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SERVICE_DATA_32BIT_UUID:
                    current_data.addUUID(SERVICE_DATA_UUID_TYPE_32,
                            adv_report_parse(BLE_GAP_AD_TYPE_SERVICE_DATA_32BIT_UUID, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_SERVICE_DATA_128BIT_UUID:
                    current_data.addUUID(SERVICE_DATA_UUID_TYPE_128,
                            adv_report_parse(BLE_GAP_AD_TYPE_SERVICE_DATA_128BIT_UUID, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_3D_INFORMATION_DATA:
                    current_data.setServiceData(adv_report_parse(BLE_GAP_AD_TYPE_3D_INFORMATION_DATA, adv_data));
                    break;
                case BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA:
                    current_data.setManufacturerSpecificData(adv_report_parse(BLE_GAP_AD_TYPE_MANUFACTURER_SPECIFIC_DATA, adv_data));
                    break;
            }
            loop_index = loop_index + adv_data_length + 1;
        }
        return current_data;
    }

    /**
     * 从BLE的ADV数据中获取指定类型的值
     * @param type      指定类型
     * @param adv_data  被解析数据
     * @return          指定类型的二进制值
     */
    public static final byte[] adv_report_parse(short type, byte[] adv_data) {
        int index = 0;
        int length;

        byte[] data;

        byte field_type = 0;
        byte field_length = 0;

        length = adv_data.length;
        while (index < length) {
            try {
                field_length = adv_data[index];
                field_type = adv_data[index + 1];
            } catch (Exception e) {
                return null;
            }

            if (field_type == (byte) type) {
                data = new byte[field_length - 1];

                byte i;
                for (i = 0; i < field_length - 1; i++) {
                    data[i] = adv_data[index + 2 + i];
                }
                return data;
            }
            index += field_length + 1;
            if (index >= adv_data.length) {
                return null;
            }
        }
        return null;
    }
}
