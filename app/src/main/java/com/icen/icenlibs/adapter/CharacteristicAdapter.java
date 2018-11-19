package com.icen.icenlibs.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.icen.blelibrary.config.BleLibsConfig;
import com.icen.icenlibs.R;

public class CharacteristicAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLi;
    private Bundle[] mAllChs;

    public CharacteristicAdapter(Context ctx, @NonNull Bundle[] all_ch)  {
        mContext = ctx;
        mAllChs = all_ch;
        mLi = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return (null == mAllChs || mAllChs.length <= 0) ? 0 : mAllChs.length;
    }

    @Override
    public Object getItem(int position) {
        return mAllChs[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView){
            convertView = mLi.inflate(R.layout.ch_item_layout, null);
        }
        TextView tv_name = convertView.findViewById(R.id.c_i_name);
        TextView tv_uuid = convertView.findViewById(R.id.c_i_uuid);
        TextView tv_property = convertView.findViewById(R.id.c_i_property);
        TextView tv_permission = convertView.findViewById(R.id.c_i_permission);

        Bundle current_bundle = mAllChs[position];
        tv_name.setText(current_bundle.getString(BleLibsConfig.LE_CHARACTERISTIC_NAME));
        tv_uuid.setText(current_bundle.getString(BleLibsConfig.LE_CHARACTERISTIC_UUID));
        tv_property.setText(current_bundle.getString(BleLibsConfig.LE_CHARACTERISTIC_PROPERTIES));
        tv_permission.setText(current_bundle.getString(BleLibsConfig.LE_CHARACTERISTIC_PERMISSION));
        return convertView;
    }
}
