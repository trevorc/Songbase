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
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.model.SongzaActivity;
import se.bitba.songbase.model.SongzaStation;

public class StationDetail
        extends Activity
{
    private static final String TAG = StationDetail.class.getName();

    private SongzaActivity songzaActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        getActionBar().setDisplayHomeAsUpEnabled(true);

        songzaActivity = getIntent().getParcelableExtra(SongbaseConstants.ACTIVITY);
        final SongzaStation songzaStation = getIntent().getParcelableExtra(SongbaseConstants.STATION);
        assert songzaActivity != null && songzaStation != null;

        setTitle(songzaStation.getName());
    }

    @Override
    public Intent getParentActivityIntent() {
        Intent parentIntent = super.getParentActivityIntent();
        assert parentIntent != null;
        parentIntent.putExtra(SongbaseConstants.ACTIVITY, songzaActivity);
        return parentIntent;
    }
}
