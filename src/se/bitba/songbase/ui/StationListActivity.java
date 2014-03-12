/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import se.bitba.songbase.R;
import se.bitba.songbase.provider.SongbaseContract;

public class StationListActivity
        extends Activity
        implements InfoHelperFragment.Callbacks
{
    private static final String INFO_HELPER = "INFO_HELPER";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        final String activityId = SongbaseContract.Activity.getActivityId(getIntent().getData());
        final FragmentManager manager = getFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();

        if (manager.findFragmentById(R.id.root_container) == null) {
            transaction.add(R.id.root_container, new StationListFragment(activityId));
        }
        if (manager.findFragmentByTag(INFO_HELPER) == null) {
            final InfoHelperFragment fragment = new InfoHelperFragment(
                    SongbaseContract.Activity.CONTENT_URI, ActivityQuery.PROJECTION,
                    ActivityQuery.SELECTION, new String[] {activityId}, null);
            transaction.add(fragment, INFO_HELPER);
        }
        transaction.commit();
    }

    @Override
    public void onInfoAvailable(InfoHelperFragment fragment, Cursor cursor) {
        getActionBar().setTitle(cursor.getString(ActivityQuery.NAME));
    }

    private interface ActivityQuery
    {
        String[] PROJECTION = {
                SongbaseContract.ActivityColumns.NAME
        };
        String SELECTION = SongbaseContract.ActivityColumns.ACTIVITY_ID + "=?";
        int NAME = 0;
    }
}
