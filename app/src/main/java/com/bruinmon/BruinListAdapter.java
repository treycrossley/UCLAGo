package com.bruinmon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.*;

public class BruinListAdapter extends ArrayAdapter<Bruinmon> {

    private ArrayList<Bruinmon> data;
    private Context context;
    private int lastPosition = -1;

    private static class ListRowViewHolder {
        ImageView image;
        TextView name;
        TextView desc;
    }

    BruinListAdapter(ArrayList<Bruinmon> data, Context context) {
        super(context, R.layout.list_bruins, data);
        this.data = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the bruinmon for the current position in the list
        Bruinmon bruinmon = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ListRowViewHolder viewHolder;
        final View result;
        if (convertView == null) {
            viewHolder = new ListRowViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_bruins, parent, false);
            viewHolder.image = convertView.findViewById(R.id.image);
            viewHolder.name = convertView.findViewById(R.id.name);
            viewHolder.desc = convertView.findViewById(R.id.desc);
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
        viewHolder.image.setImageDrawable(context.getResources().getDrawable(bruinmon.getImage()));
        viewHolder.name.setText(bruinmon.getName());
        viewHolder.desc.setText(bruinmon.getDescription());

        // Return the completed view to render on screen
        return convertView;
    }
}
