/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import se.bitba.songbase.R;
import se.bitba.songbase.provider.SongbaseContract;

public class StationDetailActivity
        extends Activity
        implements InfoHelperFragment.Callbacks
{
    private static final String TAG = StationDetailActivity.class.getSimpleName();
    private static final String INFO_HELPER = "INFO_HELPER";
    private String mActivityId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        final Uri uri = getIntent().getData();
        final long stationId = Long.parseLong(SongbaseContract.Station.getStationId(uri));
        final FragmentManager manager = getFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();

        if (manager.findFragmentById(R.id.root_container) == null) {
            transaction.add(R.id.root_container, new StationDetailFragment(stationId));
        }
        if (manager.findFragmentByTag(INFO_HELPER) == null) {
            final InfoHelperFragment fragment = new InfoHelperFragment(
                    SongbaseContract.Station.CONTENT_URI, StationQuery.PROJECTION,
                    StationQuery.SELECTION, new String[]{String.valueOf(stationId)});
            transaction.add(fragment, INFO_HELPER);
        }
        transaction.commit();
    }

    @Override
    public Intent getParentActivityIntent() {
        Log.d(TAG, "getParentActivityIntent()");
        if (mActivityId == null) return new Intent(this, ActivityListActivity.class);
        final Uri uri = SongbaseContract.Activity.buildActivityUri(mActivityId);
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    @Override
    public void onInfoAvailable(InfoHelperFragment fragment, Cursor cursor) {
        getActionBar().setTitle(cursor.getString(StationQuery.NAME));
        mActivityId = cursor.getString(StationQuery.ACTIVITY_ID);
    }

    private interface StationQuery
    {
        String[] PROJECTION = {
                SongbaseContract.StationColumns.ACTIVITY_ID,
                SongbaseContract.StationColumns.NAME
        };
        String SELECTION = SongbaseContract.StationColumns.STATION_ID + "=?";
        int ACTIVITY_ID = 0;
        int NAME = 1;
    }
}
