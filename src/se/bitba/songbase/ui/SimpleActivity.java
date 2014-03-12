/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import se.bitba.songbase.R;

public abstract class SimpleActivity
        extends Activity
{
    abstract public Fragment getFragment();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        final FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.findFragmentById(R.id.root_container) == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.root_container, getFragment())
                    .commit();
        }
    }
}
