package com.rob.bryan.steven.hackathon2014.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rob.bryan.steven.hackathon2014.R;
import com.rob.bryan.steven.hackathon2014.object.Alert;
import com.rob.bryan.steven.hackathon2014.object.StopService;
import com.rob.bryan.steven.hackathon2014.object.UpdateNotification;
import com.rob.bryan.steven.hackathon2014.services.SensorDataService;
import com.rob.bryan.steven.hackathon2014.utils.AlarmManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.event.EventBus;
import io.relayr.RelayrSdk;


public class AlertsActivity extends BaseActivity {

    private MenuItem mLogIn;
    private MenuItem mLogOut;
    private int loginResultCode = 1337;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout mEmptyList;

    private ArrayList<Alert> alerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);

        mEmptyList = (LinearLayout) findViewById(R.id.list_empty);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        login();
    }

    private void login() {
        if (!RelayrSdk.isUserLoggedIn()) {
            startActivityForResult(new Intent(this, LoginActivity.class), loginResultCode);
        }
        else {
            getData();
        }
    }

    private void getData() {
        SensorDataService.startActionSubscribe(this);

        alerts = new ArrayList<Alert>();

        JSONArray alertsArray = AlarmManager.getAlertsJSONArray(getApplicationContext());

        mAdapter = new AlertsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        for (int i = 0; i < alertsArray.length(); i++) {
            try {
                alerts.add(new Alert((JSONObject) alertsArray.get(i)));
                mAdapter.notifyItemInserted(alerts.size());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == requestCode){
            mLogOut.setVisible(true);
            mLogIn.setVisible(false);
            login();
        }
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_alerts;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);

        mLogIn = menu.findItem(R.id.action_log_in);
        mLogOut = menu.findItem(R.id.action_log_out);

        if (RelayrSdk.isUserLoggedIn()) {
            mLogOut.setVisible(true);
            mLogIn.setVisible(false);
        } else {
            mLogOut.setVisible(false);
            mLogIn.setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_log_in:
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.action_log_out:
                logOut();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {

        //call the logOut method on the reayr SDK

        EventBus.getDefault().post(new StopService());
        RelayrSdk.logOut();

        mLogOut.setVisible(false);
        mLogIn.setVisible(true);


        //use the Toast library to display a message to the user
        Toast.makeText(this, R.string.successfully_logged_out, Toast.LENGTH_SHORT).show();
    }

    class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView card;

            public ViewHolder(CardView card) {
                super(card);
                this.card = card;
            }
        }

        @Override
        public AlertsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CardView card = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_alert, parent, false);

            ViewHolder holder = new ViewHolder(card);

            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final Alert alert = alerts.get(position);

            View label = holder.card.findViewById(R.id.label);
            label.setBackgroundResource(alert.getPriorityColor());

            TextView title = (TextView) holder.card.findViewById(R.id.alert_name);
            title.setText(alert.getName());

            TextView type = (TextView) holder.card.findViewById(R.id.alert_type);
            type.setText(alert.getAlertTypeString());

            TextView description = (TextView) holder.card.findViewById(R.id.alert_description);
            description.setText(alert.getDescription());

            TextView time = (TextView) holder.card.findViewById(R.id.alert_time);
            time.setText(DateUtils.getRelativeTimeSpanString(alert.getAlertTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));

            ImageView icon = (ImageView) holder.card.findViewById(R.id.alert_image);
            icon.setImageDrawable(getResources().getDrawable(alert.getIconID()));

            holder.card.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View card) {
                    mAdapter.notifyItemRemoved(alerts.indexOf(alert));
                    alerts.remove(alert);

                    AlarmManager.markAsDone(alert.getAlertType(), getApplicationContext());

                    AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
                    fadeIn.setInterpolator(new AccelerateInterpolator());
                    fadeIn.setDuration(300);
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if (alerts.size() == 0) {
                                mEmptyList.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    if (alerts.size() == 0) {
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        mEmptyList.startAnimation(fadeIn);
                    }

                    EventBus.getDefault().post(new UpdateNotification());
                }

            });
        }

        @Override
        public int getItemCount() {
            return alerts.size();
        }
    }

    public void onEvent(Alert alert) {
        Log.d("AlertsActivity", "boolean: " + alert.getDescription());

        Alert existingAlert = null;

        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            if (alerts.get(i).getAlertType() == alert.getAlertType()) {
                existingAlert = alerts.get(i);
            }
        }

        if (existingAlert != null) {
            alerts.set(alerts.indexOf(existingAlert), alert);
            mAdapter.notifyItemChanged(alerts.indexOf(alert));
            Log.d("Alerts", "Existed, replaced");
        } else {
            alerts.add(alert);
            mAdapter.notifyItemInserted(alerts.indexOf(alert));
            Log.d("Alerts", "New, added");

            AlphaAnimation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(300);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mEmptyList.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            mRecyclerView.setVisibility(View.VISIBLE);

            if (mEmptyList.getVisibility() == View.VISIBLE) {
                mEmptyList.startAnimation(fadeOut);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Alerts", "onResume");
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        Log.d("Alerts", "onPause");
        super.onPause();
    }
}