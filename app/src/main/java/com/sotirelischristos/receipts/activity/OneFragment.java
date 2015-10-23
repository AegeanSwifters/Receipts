package com.sotirelischristos.receipts.activity;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sotirelischristos.receipts.R;
import com.sotirelischristos.receipts.app.MyApp;
import com.sotirelischristos.receipts.helper.Place;
import com.sotirelischristos.receipts.helper.SwipeListAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OneFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Foursquare API config
    private final String fs_client_id = "GBEDG1W53A2XFD5UN1JQSU5QDSA1WIZ0JU04KFULEKACIVBA";
    private final String fs_client_secret = "N4Z23VVUKGDLV2DAP0EGALD1ONPBIGOFN5ETSX3I1JLWMOKV";
    private String TAG = MainActivity.class.getSimpleName();
    private String fs_limit = "15";
    private String fs_range = "100";
    private String BASE_URL = "https://api.foursquare.com/v2/venues/search?limit=" + fs_limit + "&v=20151022&client_id=" + fs_client_id + "&client_secret=" + fs_client_secret;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private SwipeListAdapter adapter;
    private List<Place> placeList;

    // Location services config
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    public OneFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check availability of GooglePlayServices
        if (checkPlayServices()) {
            buildGoogleAPIClient();
        }
    }

    @Override
    public void onRefresh() {
        // Clear initial list to avoid duplicates
        if (!placeList.isEmpty()) {
            placeList.clear();
        }
        fetchPlaces(getLocation());
    }

    /**
     * Fetch places from Foursquare
     */
    private void fetchPlaces(Location loc) {
        double lat = loc.getLatitude();
        double lon = loc.getLongitude();
        Log.e(TAG, "Latitude: " + lat + " - Longitude: " + lon);
        // Append location coordinates to request URL
        String URL = BASE_URL + "&ll=" + String.valueOf(lat) + "," + String.valueOf(lon);
        // Show refresh animation
        swipeRefreshLayout.setRefreshing(true);
        // Make request to Foursquare API through Volley
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,
                URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e(TAG, response.toString());
                        try {
                            // Take the "reponse" object from whole API response
                            JSONObject fs_response = response.getJSONObject("response");
                            // Take the "venues" array from "response" object
                            JSONArray fs_venues = fs_response.getJSONArray("venues");
                            if (fs_venues.length() > 0) {
                                for (int i = 0; i < fs_venues.length(); i++) {
                                    JSONObject place = fs_venues.getJSONObject(i);
                                    placeList.add(0, new Place(0, place.getString("name")));
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "JSON parsing error: " + e.getMessage());
                        }
                        // Hide refresh animation
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Server error: " + error.getMessage());
                            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
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
        swipeRefreshLayout.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        swipeRefreshLayout.setRefreshing(true);
                                        fetchPlaces(getLocation());
                                    }
                                }
        );
        */
    }

    /**
     * Get the location
     * */
    private Location getLocation() {
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            return mLastLocation;
        }
        return null;
    }

    /**
     * Create Google API client
     * */
    protected synchronized void buildGoogleAPIClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Verify GooglePlayServices on device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Get location once connected to Google API
        fetchPlaces(getLocation());
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

}