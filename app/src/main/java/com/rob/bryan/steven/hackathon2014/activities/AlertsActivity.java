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
import android.widget.ImageView;
import android.widget.TextView;

import com.rob.bryan.steven.hackathon2014.R;
import com.rob.bryan.steven.hackathon2014.object.Alert;
import com.rob.bryan.steven.hackathon2014.services.SensorDataService;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class AlertsActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorDataService.startActionSubscribe(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<Alert> alerts = new ArrayList<Alert>();

        alerts.add(new Alert("Light", Alert.AlertType.LIGHT, "Main light broken", 0));
        alerts.add(new Alert("Refrigerator", Alert.AlertType.TEMPERATURE, "Too cold", 0));
        alerts.add(new Alert("Room", Alert.AlertType.TEMPERATURE, "Too cold", 0));
        alerts.add(new Alert("Window", Alert.AlertType.PROXIMITY, "Is open", 0));
        alerts.add(new Alert("Sound", Alert.AlertType.SOUND, "Too noisy", 0));

        mAdapter = new AlertsAdapter(alerts);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if (mAdapter.getItemCount() == 0) {
                    mRecyclerView.setVisibility(View.INVISIBLE);
                } else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_alerts;
    }

    class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.ViewHolder> {
        private ArrayList<Alert> alerts;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView card;

            public ViewHolder(CardView card) {
                super(card);
                this.card = card;
            }
        }

        public AlertsAdapter(ArrayList<Alert> alerts) {
            this.alerts = alerts;
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
                }

            });
        }

        @Override
        public int getItemCount() {
            return alerts.size();
        }
    }

    public void onEvent(boolean isUpdate) {
        Log.d("AlertsActivity", "boolean: " + isUpdate);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }
}