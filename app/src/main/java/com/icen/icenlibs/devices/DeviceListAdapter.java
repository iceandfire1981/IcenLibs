package com.icen.icenlibs.devices;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.blelibrary.utils.AdvDataUtils;
import com.icen.icenlibs.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class DeviceListAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Bundle> mInputDevices;

    public DeviceListAdapter(Context ctx, HashMap<String, Bundle> devices_map){
        mContext = ctx;

        if (null != mInputDevices)
            mInputDevices = null;

        mInputDevices = new ArrayList<>();
        if (null != devices_map && devices_map.size() > 0) {
            Iterator<Map.Entry<String, Bundle>> devices_iterator =
                    devices_map.entrySet().iterator();
            while (devices_iterator.hasNext()) {
                Map.Entry<String, Bundle> device_entry = devices_iterator.next();
                mInputDevices.add(device_entry.getValue());
            }
        }
    }

    public void resetData(HashMap<String, Bundle> devices_map) {
        mInputDevices = null;
        mInputDevices = new ArrayList<>();

        if (null != devices_map || devices_map.size() > 0) {
            Iterator<Map.Entry<String, Bundle>> devices_iterator =
                    devices_map.entrySet().iterator();
            while (devices_iterator.hasNext()) {
                Map.Entry<String, Bundle> device_entry = devices_iterator.next();
                mInputDevices.add(device_entry.getValue());
            }
        }
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return (null == mInputDevices || mInputDevices.size() <= 0) ? 0 : mInputDevices.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return mInputDevices.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.device_item_layout, null);
        }
        TextView tv_device_name = (TextView) convertView.findViewById(R.id.device_item_name);
        TextView tv_device_mac  = (TextView) convertView.findViewById(R.id.device_item_mac);
        TextView tv_device_power_level  = (TextView) convertView.findViewById(R.id.device_item_power_level);
        TextView tv_device_signal_level  = (TextView) convertView.findViewById(R.id.device_item_signal);

        Bundle current_device_info = mInputDevices.get(position);
        String device_name = current_device_info.getString(BleLibsConfig.BROADCAST_INFO_DEVICE_NAME);
        String device_mac  = current_device_info.getString(BleLibsConfig.BROADCAST_INFO_DEVICE_ADDRESS);
        long   device_rssi = current_device_info.getLong(BleLibsConfig.BROADCAST_INFO_SIGNAL);
        byte[] device_content = current_device_info.getByteArray(BleLibsConfig.BROADCAST_INFO_DEVICE_CONTENT);
        byte[] power_level_byte = AdvDataUtils.adv_report_parse(AdvDataUtils.BLE_GAP_AD_TYPE_TX_POWER_LEVEL, device_content);

        tv_device_name.setText(device_name);
        tv_device_mac.setText(device_mac);
        tv_device_signal_level.setText(String.valueOf(device_rssi));
        tv_device_power_level.setText((null != power_level_byte && power_level_byte.length > 0 ) ?
                Arrays.toString(power_level_byte) : "-1");
        return convertView;
    }
}
