/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.model.SongzaActivity;
import se.bitba.songbase.util.FetchObserver;
import se.bitba.songbase.util.SongzaAPIClient;

import java.util.List;

public class ActivityList
        extends Activity
        implements AdapterView.OnItemClickListener
{
    private static final String TAG = ActivityList.class.getName();

    private ActivityAdapter activityAdapter;
    private SongzaAPIClient apiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        apiClient = new SongzaAPIClient();
        activityAdapter = new ActivityAdapter();
        final ListView listView = new ListView(this);
        listView.setAdapter(activityAdapter);
        listView.setOnItemClickListener(this);
        setContentView(listView);

        fetchActivities();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, String.format("onItemClick(%d)", position));
        Intent intent = new Intent(this, StationList.class);
        intent.putExtra(SongbaseConstants.ACTIVITY, activityAdapter.getItem(position));
        startActivity(intent);
    }

    private void fetchActivities() {
        apiClient.fetchActivities(new FetchObserver<List<SongzaActivity>>() {
            @Override
            public void onSuccess(List<SongzaActivity> result) {
                activityAdapter.addAll(result);
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "fetch activities failed");
            }
        });
    }

    private class ActivityAdapter
            extends ArrayAdapter<SongzaActivity>
    {
        public ActivityAdapter() {
            super(ActivityList.this, android.R.layout.simple_list_item_1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, String.format("getView(%d, %s, %s)", position, convertView, parent));
            final TextView view = convertView == null
                    ? (TextView)getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false)
                    : (TextView)convertView;
            final SongzaActivity activity = getItem(position);
            assert view != null;
            view.setText(activity.getName());
            return view;
        }
    }
}
