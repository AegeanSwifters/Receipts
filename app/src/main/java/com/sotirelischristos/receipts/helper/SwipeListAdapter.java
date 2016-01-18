package com.sotirelischristos.receipts.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sotirelischristos.receipts.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

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

        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView category = (TextView) convertView.findViewById(R.id.distance);

        title.setText(venueList.get(position).title);
        category.setText(venueList.get(position).category);

        final ImageView venueImage = (ImageView) convertView.findViewById(R.id.venueImageView);

        /**
         * SOS
         * Foursquare API recently changed its policy and needs oauth in order
         * to make requests to the venue photos endpoint.
         * Until notice, no venue photos will be downloaded.
         */
        String URL = "https://api.foursquare.com/v2/venues/" + venueList.get(position).id + "/photos&v=20151022&client_id=GBEDG1W53A2XFD5UN1JQSU5QDSA1WIZ0JU04KFULEKACIVBA&client_secret=N4Z23VVUKGDLV2DAP0EGALD1ONPBIGOFN5ETSX3I1JLWMOKV";
        Log.e("Adapter URL", URL);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                URL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.e("Adapter RESPONSE", response.toString());
                try {
                    // Take the "reponse" object from whole API response
                    JSONObject fs_response = response.getJSONObject("response");
                    JSONObject fs_photos = response.getJSONObject("photos");
                    // Take the "venues" array from "response" object
                    JSONArray fs_items = fs_photos.getJSONArray("items");
                    JSONObject img = fs_items.getJSONObject(0);
                    String imgURL = img.getString("prefix") + img.getString("suffix");
                    Picasso.with(activity.getApplicationContext()).load(imgURL).into(venueImage);
                } catch (Exception e) {
                    Log.e("Adapter", "JSON parsing error: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Adapter", "Server error: " + error.getMessage());
            }
        });
        return convertView;
    }

}