/**
 * Crafted with
 * <3
 * by Christos Sotirelis
 * and Theodore Palios
 *
 * Not for personal or commercial use!
 * Not for educational or academic purposes, by any means!
 *
 * All rights and lefts reserved!
 *
 * Use with caution, cause NSA approves! ;)
 */

package com.sotirelischristos.receipts.activity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.sotirelischristos.receipts.R;

import java.io.IOException;

public class VenueActivity extends AppCompatActivity {

    private String venue_id, venue_name;
    private String unique;
    private String TAG = VenueActivity.class.getSimpleName();
    private Button yes, no;
    private TextView receipt, number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // First, get the device unique id
        // Then, check if user has already rated this venue
        getUserUniqueId();

        // Retrieve the id & name of the venue that triggered this activity
        Bundle extras = getIntent().getExtras();
        venue_id = extras.getString("venue_id");
        venue_name = extras.getString("venue_name");

        getSupportActionBar().setTitle(venue_name);

        TextView nameTextView = (TextView) findViewById(R.id.nameTextView);
        nameTextView.setText(venue_name);

        receipt = (TextView) findViewById(R.id.receiptTextView);
        number = (TextView) findViewById(R.id.numberTextView);

        yes = (Button) findViewById(R.id.yesButton);
        no = (Button) findViewById(R.id.noButton);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Rate");
        query.whereEqualTo("foursquareVenueId", venue_id);
        query.whereEqualTo("takenReceipt", false);
        query.countInBackground(new CountCallback() {

            public void done(int count, ParseException e) {
                if (e == null) {
                    number.append(String.valueOf(count));
                }
            }
        });
    }

    protected void checkIfExists() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Rate");
        query.whereEqualTo("userUniqueId", unique);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Rate");
                    query.whereEqualTo("foursquareVenueId", venue_id);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {

                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            // User has already rated this venue
                            if (e == null) {
                                boolean rating = parseObject.getBoolean("takenReceipt");
                                if (rating) {
                                    Toast.makeText(VenueActivity.this, "You have received receipt from this venue!", Toast.LENGTH_LONG).show();
                                    // User has taken receipt so disable the "Yes" button
                                    disableButton(yes);
                                    // receipt.setText("You have received receipt from this venue!");
                                } else if (!rating) {
                                    Toast.makeText(VenueActivity.this, "You have not received receipt from this venue!", Toast.LENGTH_LONG).show();
                                    // User has not taken receipt so disable the "No" button
                                    disableButton(no);
                                    // receipt.setText("You have not received receipt from this venue!");
                                }
                            } else {
                                Log.e(TAG, "foursquareVenueId>" + e.getMessage());
                            }
                        }

                    });
                } else {
                    Log.e(TAG, "userUniqueId: " + unique + " - " + e.getMessage());
                }
            }

        });

        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Add new record or update existing
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Rate");
                query.whereEqualTo("userUniqueId", unique);
                query.whereEqualTo("foursquareVenueId", venue_id);
                query.getFirstInBackground(new GetCallback<ParseObject>() {

                    @Override
                    public void done(ParseObject parseObject, ParseException e) {

                        // Update takenReceipt to true
                        if (e == null) {
                            parseObject.put("takenReceipt", true);
                            Log.e(TAG, "Updated to: True");
                            try {
                                parseObject.save();
                                Toast.makeText(VenueActivity.this, "Received receipt!", Toast.LENGTH_LONG).show();
                                disableButton(yes);
                                enableButton(no, "no");
                                // receipt.setText("You have received receipt from this venue!");
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            // Create new record with takenReceipt: true
                        } else {
                            if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                ParseObject y = new ParseObject("Rate");
                                y.put("userUniqueId", unique);
                                y.put("takenReceipt", true);
                                y.put("foursquareVenueId", venue_id);
                                y.saveInBackground(new SaveCallback() {

                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Log.e(TAG, "Created new rating with: True");
                                            Toast.makeText(VenueActivity.this, "Received receipt!", Toast.LENGTH_LONG).show();
                                            disableButton(yes);
                                            enableButton(no, "no");
                                            // receipt.setText("You have received receipt from this venue!");
                                        } else {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }

        });

        no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Add new record or update existing
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Rate");
                query.whereEqualTo("userUniqueId", unique);
                query.whereEqualTo("foursquareVenueId", venue_id);
                query.getFirstInBackground(new GetCallback<ParseObject>() {

                    @Override
                    public void done(ParseObject parseObject, ParseException e) {

                        if (e == null) {
                            parseObject.put("takenReceipt", false);
                            try {
                                parseObject.save();
                                disableButton(no);
                                Toast.makeText(VenueActivity.this, "Did not receive receipt!", Toast.LENGTH_LONG).show();
                                enableButton(yes, "yes");
                                // receipt.setText("You have not received receipt from this venue!");
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                        } else {
                            if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                ParseObject n = new ParseObject("Rate");
                                n.put("userUniqueId", unique);
                                n.put("takenReceipt", false);
                                n.put("foursquareVenueId", venue_id);
                                n.saveInBackground(new SaveCallback() {

                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            disableButton(no);
                                            Toast.makeText(VenueActivity.this, "Did not receive receipt!", Toast.LENGTH_LONG).show();
                                            enableButton(yes, "yes");
                                            // receipt.setText("You have not received receipt from this venue!");
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }

        });
    }

    protected void disableButton(Button btn) {
        btn.setClickable(false);
        btn.setBackgroundColor(Color.parseColor("#808080"));
    }

    protected void enableButton(Button btn, String type) {
        btn.setClickable(true);
        if (type.equals("yes")) {
            btn.setBackgroundColor(Color.parseColor("#09BB07"));
        } else {
            btn.setBackgroundColor(Color.parseColor("#F43530"));
        }
    }

    protected void registerUser() {
        // Check if user's unique id is already registered
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
        query.whereEqualTo("userUniqueId", unique);
        query.getFirstInBackground(new GetCallback<ParseObject>() {

            @Override
            public void done(ParseObject parseObject, ParseException e) {
                // User is already registered
                if (e == null) {
                    return;
                }
                else {
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        // Register user
                        ParseObject user = new ParseObject("Users");
                        user.put("userUniqueId", unique);
                        user.saveInBackground();
                    }
                }
            }

        });
    }

    // Get a unique device ID for this user (runs in background async thread)
    protected void getUserUniqueId() {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait...");
        progress.show();
        progress.setCancelable(false);

        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String advertId = null;
                try {
                    advertId = idInfo.getId();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return advertId;
            }

            @Override
            protected void onPostExecute(String advertId) {
                unique = advertId;
                Log.e(TAG, unique);
                // Register user in database if doesn't exist
                registerUser();
                checkIfExists();
                progress.dismiss();
            }

        };
        task.execute();
    }

}
