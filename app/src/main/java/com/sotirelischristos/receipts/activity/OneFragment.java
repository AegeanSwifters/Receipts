package com.sotirelischristos.receipts.activity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sotirelischristos.receipts.R;
import com.sotirelischristos.receipts.app.MyApp;
import com.sotirelischristos.receipts.helper.Place;
import com.sotirelischristos.receipts.helper.SwipeListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OneFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String TAG = MainActivity.class.getSimpleName();

    private final String fs_client_id = "GBEDG1W53A2XFD5UN1JQSU5QDSA1WIZ0JU04KFULEKACIVBA";
    private final String fs_client_secret = "N4Z23VVUKGDLV2DAP0EGALD1ONPBIGOFN5ETSX3I1JLWMOKV";
    private String URL = "https://api.foursquare.com/v2/venues/search?ll=40.7,-74&limit=1&v=20151022&client_id=" + fs_client_id + "&client_secret=" + fs_client_secret;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private SwipeListAdapter adapter;
    private List<Place> placeList;

    // initially offset will be 0, later will be updated while parsing the json
    private int offSet = 0;

    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {
        if (!placeList.isEmpty()) {
            placeList.clear();
        }
        fetchPlaces();
    }

    /**
     * Fetching movies json by making http call
     */
    private void fetchPlaces() {

        // showing refresh animation before making http call
        swipeRefreshLayout.setRefreshing(true);

        // Volley's json array request object
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject fs_response = response.getJSONObject("response");
                            JSONArray fs_venues = fs_response.getJSONArray("venues");
                            if (fs_venues.length() > 0) {

                                // looping through json and adding to movies list
                                for (int i = 0; i < fs_venues.length(); i++) {
                                    try {
                                        JSONObject place = fs_venues.getJSONObject(i);
                                        placeList.add(0, new Place(0, place.getString("name")));

                                    } catch (JSONException e) {
                                        Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                                    }
                                }

                                adapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                        }


                        // stopping swipe refresh
                        swipeRefreshLayout.setRefreshing(false);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Server Error: " + error.getMessage());

                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();

                // stopping swipe refresh
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Adding request to request queue
        MyApp.getInstance().addToRequestQueue(req);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        placeList = new ArrayList<>();
        adapter = new SwipeListAdapter(getActivity(), placeList);
        return inflater.inflate(R.layout.fragment_one, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        listView = (ListView) view.findViewById(R.id.listView);
        listView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        swipeRefreshLayout.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);

                                        fetchPlaces();
                                    }
                                }
        );
    }

}