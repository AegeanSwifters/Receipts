package com.sotirelischristos.receipts.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.sotirelischristos.receipts.R;

public class SwipeListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<Place> venueList;
    private String[] bgColors;

    public SwipeListAdapter(Activity activity, List<Place> venueList) {
        this.activity = activity;
        this.venueList = venueList;
        bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.movie_serial_bg);
    }

    @Override
    public int getCount() {
        return venueList.size();
    }

    @Override
    public Object getItem(int location) {
        return venueList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_row, null);

        TextView serial = (TextView) convertView.findViewById(R.id.serial);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView category = (TextView) convertView.findViewById(R.id.distance);

        serial.setText(String.valueOf(venueList.get(position).distance));
        title.setText(venueList.get(position).title);
        category.setText(venueList.get(position).category);

        String color = bgColors[position % bgColors.length];
        serial.setBackgroundColor(Color.parseColor(color));

        return convertView;
    }

}