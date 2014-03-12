/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.util;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public abstract class ContextAsyncTask
        extends AsyncTask<Void, Void, Void>
{
    protected abstract void doInBackground();

    final WeakReference<Context> mContextRef;

    protected ContextAsyncTask(Context context) {
        mContextRef = new WeakReference<>(context);
    }

    public Context getContext() {
        return mContextRef.get();
    }

    @Override
    protected Void doInBackground(Void... params) {
        doInBackground();
        return null;
    }
}
