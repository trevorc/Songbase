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
import android.widget.*;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.fetch.FetchManager;
import se.bitba.songbase.provider.SongbaseContract;

import static se.bitba.songbase.ui.ActivityAdapter.ActivitiesQuery;

public class ActivityListActivity
        extends Activity
        implements AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = ActivityListActivity.class.getSimpleName();

    private final FetchManager fetchManager = new FetchManager(); // TODO: service
    private ListView listView;
    private Cursor cursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");

        getLoaderManager().initLoader(0, null, this);
        fetchManager.fetchActivities(this);

        listView = new ListView(this);
        listView.setOnItemClickListener(this);
        setContentView(listView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (cursor == null) return;
        cursor.moveToPosition(position);

        Intent intent = new Intent(this, StationListActivity.class);
        intent.putExtra(SongbaseConstants.ACTIVITY_ID, cursor.getString(ActivitiesQuery.ACTIVITY_ID));
        intent.putExtra(SongbaseConstants.ACTIVITY_NAME, cursor.getString(ActivitiesQuery.NAME));
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader()");
        return new CursorLoader(this, SongbaseContract.Activity.CONTENT_URI,
                                ActivitiesQuery.PROJECTION,
                                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, String.format("onLoadFinished() row count is %d", data.getCount()));
        cursor = data;
        listView.setAdapter(new ActivityAdapter(this, data));
        data.setNotificationUri(getContentResolver(), SongbaseContract.Activity.CONTENT_URI);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset()");
        cursor = null;
        listView.setAdapter(null);
    }
}
