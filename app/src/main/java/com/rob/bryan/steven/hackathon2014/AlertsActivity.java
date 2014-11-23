package com.rob.bryan.steven.hackathon2014;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.rob.bryan.steven.hackathon2014.object.Alert;

import java.util.ArrayList;
import java.util.List;


public class AlertsActivity extends Activity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ArrayList<Alert> alerts = new ArrayList<Alert>();

        alerts.add(new Alert("Name", Alert.AlertType.LIGHT));


        mAdapter = new AlertsAdapter(alerts);
        mRecyclerView.setAdapter(mAdapter);
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
        public void onBindViewHolder(ViewHolder holder, int position) {
            TextView title = (TextView) holder.card.findViewById(R.id.alert_name);
            title.setText(alerts.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return alerts.size();
        }
    }

}