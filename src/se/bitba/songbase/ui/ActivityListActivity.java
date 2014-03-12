/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import se.bitba.songbase.R;

public class ActivityListActivity
        extends Activity
{
    private static final String TAG = ActivityListActivity.class.getSimpleName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_activity_list);
    }
}
