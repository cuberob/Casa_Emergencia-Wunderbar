package com.rob.bryan.steven.hackathon2014.activities;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rob.bryan.steven.hackathon2014.R;
import com.rob.bryan.steven.hackathon2014.object.Alert;
import com.rob.bryan.steven.hackathon2014.services.SensorDataService;
import com.rob.bryan.steven.hackathon2014.utils.AlarmManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import de.greenrobot.event.EventBus;


public class AlertsActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LinearLayout mEmptyList;

    private ArrayList<Alert> alerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorDataService.startActionSubscribe(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);

        mEmptyList = (LinearLayout) findViewById(R.id.list_empty);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

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
    protected int getLayoutResource() {
        return R.layout.activity_alerts;
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
                            mEmptyList.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    if (alerts.size() == 0) {
                        mRecyclerView.setVisibility(View.INVISIBLE);
                        mEmptyList.startAnimation(fadeIn);
                    }
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