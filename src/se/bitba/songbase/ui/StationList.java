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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.model.SongzaActivity;
import se.bitba.songbase.model.SongzaStation;
import se.bitba.songbase.util.FetchObserver;
import se.bitba.songbase.util.SongzaAPIClient;

import java.util.List;

public class StationList
        extends Activity
        implements AdapterView.OnItemClickListener
{
    private static final String TAG = StationList.class.getName();
    private SongzaActivity songzaActivity;
    private StationAdapter stationAdapter;
    private SongzaAPIClient apiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, String.format("onCreate() with intent %s", getIntent()));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        songzaActivity = getIntent().getParcelableExtra(SongbaseConstants.ACTIVITY);
        Log.d(TAG, String.format("Got songzaActivity %s", songzaActivity));
        setTitle(songzaActivity.getName());
        apiClient = new SongzaAPIClient();
        stationAdapter = new StationAdapter();

        final ListView listView = new ListView(this);
        listView.setAdapter(stationAdapter);
        listView.setOnItemClickListener(this);
        setContentView(listView);

        fetchStations();
    }

    private void fetchStations() {
        apiClient.fetchStations(songzaActivity.getStations(), new FetchObserver<List<SongzaStation>>() {
            @Override
            public void onSuccess(List<SongzaStation> result) {
                stationAdapter.addAll(result);
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "failed to fetch stations");
            }
        });
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, StationDetail.class);
        intent.putExtra(SongbaseConstants.ACTIVITY, songzaActivity);
        intent.putExtra(SongbaseConstants.STATION, stationAdapter.getItem(position));
        startActivity(intent);
    }

    private class StationAdapter
            extends ArrayAdapter<SongzaStation>
    {
        public StationAdapter() {
            super(StationList.this, android.R.layout.simple_list_item_1);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, String.format("getView(%d, %s, %s)", position, convertView, parent));
            final TextView view = convertView == null
                    ? (TextView)getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false)
                    : (TextView)convertView;
            final SongzaStation station = getItem(position);
            assert view != null;
            view.setText(station.getName());
            return view;
        }
    }
}
