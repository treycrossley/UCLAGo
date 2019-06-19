package com.bruinmon;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private ArrayList<BluetoothDevice> data;
    private Context context;
    private int lastPosition = -1;

    private static class ListRowViewHolder {
        TextView name;
        TextView mac;
    }

    DeviceListAdapter(ArrayList<BluetoothDevice> data, Context context) {
        super(context, R.layout.list_device, data);
        this.data = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the device for the current position in the list
        BluetoothDevice device = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ListRowViewHolder viewHolder;
        final View result;
        if (convertView == null) {
            viewHolder = new ListRowViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_device, parent, false);
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.mac = convertView.findViewById(R.id.mac);
            result = convertView;
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ListRowViewHolder)convertView.getTag();
            result = convertView;
        }

        // Animate the scrolling list
        Animation anim = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(anim);
        lastPosition = position;

        // Set the rows to correct values as we move up/down the list
        viewHolder.name.setText(device.getName());
        viewHolder.mac.setText(device.getAddress());

        // Return the completed view to render on screen
        return convertView;
    }
}
