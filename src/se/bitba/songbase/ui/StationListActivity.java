/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import se.bitba.songbase.R;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.fetch.FetchManager;
import se.bitba.songbase.provider.SongbaseContract;

import static se.bitba.songbase.ui.StationAdapter.StationsQuery;

public class StationListActivity
        extends Activity
        implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = StationListActivity.class.getName();

    private final FetchManager fetchManager = new FetchManager(); // TODO: service

    private String activityId;
    private ListView listView;
    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, String.format("onCreate() with intent %s", getIntent()));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        activityId = getIntent().getStringExtra(SongbaseConstants.ACTIVITY_ID);
        final String activityName = getIntent().getStringExtra(SongbaseConstants.ACTIVITY_NAME);
        assert activityId != null && activityName != null;

        Log.d(TAG, String.format("Got songzaActivity %s", activityId));
        setTitle(activityName);

        getLoaderManager().initLoader(0, null, this);
        fetchManager.fetchStations(this, activityId);

        listView = new ListView(this);
        listView.setOnItemClickListener(this);
        setContentView(listView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (cursor == null) return;
        cursor.moveToPosition(position);

        Intent intent = new Intent(this, StationDetailActivity.class);
        intent.putExtra(SongbaseConstants.STATION_ID, cursor.getLong(StationsQuery.STATION_ID));
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        return new CursorLoader(this, SongbaseContract.Station.CONTENT_URI,
                                StationsQuery.PROJECTION, StationsQuery.SELECTION,
                                new String[] {activityId}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, String.format("onLoadFinished() row count is %d", data.getCount()));
        cursor = data;
        listView.setAdapter(new StationAdapter(this, data));
        data.setNotificationUri(getContentResolver(), SongbaseContract.Station.CONTENT_URI);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        cursor = null;
        listView.setAdapter(null);
    }
}
