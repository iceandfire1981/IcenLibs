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

import java.util.ArrayList;

public class ServicesAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Bundle> mServicesList;

    public ServicesAdapter(Context ctx, @NonNull Bundle[] service_list) {
        mContext = ctx;
        mServicesList = new ArrayList<>();
        for (int service_index = 0; service_index < service_list.length; service_index++) {
            mServicesList.add(service_list[service_index]);
        }
    }

    @Override
    public int getCount() {
        return mServicesList.size();
    }

    @Override
    public Object getItem(int i) {
        return mServicesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View contentView, ViewGroup viewGroup) {
        if (null == contentView) {
            contentView = LayoutInflater.from(mContext).inflate(R.layout.service_item_layout, null);
        }

        Bundle service_info = mServicesList.get(i);
        TextView tv_service_name = (TextView) contentView.findViewById(R.id.s_i_name);
        tv_service_name.setText(service_info.getString(BleLibsConfig.LE_SERVICE_NAME));
        TextView tv_service_uuid = (TextView) contentView.findViewById(R.id.s_i_uuid);
        tv_service_uuid.setText(service_info.getString(BleLibsConfig.LE_SERVICE_UUID));
        TextView tv_service_type = (TextView) contentView.findViewById(R.id.s_i_type);
        tv_service_type.setText(service_info.getString(BleLibsConfig.LE_SERVICE_TYPE));
        return contentView;
    }
}
