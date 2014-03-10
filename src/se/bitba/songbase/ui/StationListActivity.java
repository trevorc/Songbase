/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.loopj.android.image.SmartImageView;
import se.bitba.songbase.R;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.model.SongzaActivity;
import se.bitba.songbase.model.SongzaStation;
import se.bitba.songbase.util.FetchObserver;
import se.bitba.songbase.util.SongzaAPIClient;

import java.util.List;

public class StationListActivity
        extends Activity
        implements AdapterView.OnItemClickListener
{
    private static final String TAG = StationListActivity.class.getName();
    private static final int REQUEST_FAVORITE = 0;

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
                stationAdapter.clear();
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
        Intent intent = new Intent(this, StationDetailActivity.class);
        intent.putExtra(SongbaseConstants.ACTIVITY, songzaActivity);
        intent.putExtra(SongbaseConstants.STATION, stationAdapter.getItem(position));
        startActivityForResult(intent, REQUEST_FAVORITE);
    }

    private class StationAdapter
            extends ArrayAdapter<SongzaStation>
    {
        public StationAdapter() {
            super(StationListActivity.this, android.R.layout.simple_list_item_1);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, String.format("getView(%d, %s, %s)", position, convertView, parent));
            final SongzaStation station = getItem(position);
            final View listItem = convertView == null
                    ? getLayoutInflater().inflate(R.layout.station_list_item, parent, false)
                    : convertView;
            assert listItem != null;

            final SmartImageView imageView = (SmartImageView)listItem.findViewById(R.id.station_item_cover);
            final TextView nameView = (TextView)listItem.findViewById(R.id.station_item_name);
            final TextView descriptionView = (TextView)listItem.findViewById(R.id.station_item_description);
            assert imageView != null && nameView != null && descriptionView != null;

            imageView.setImageUrl(station.getCoverURL());
            nameView.setText(station.getName());
            descriptionView.setText(station.getDescription());
            if (station.isFavorite()) listItem.setBackgroundColor(Color.YELLOW);

            return listItem;
        }
    }
}
