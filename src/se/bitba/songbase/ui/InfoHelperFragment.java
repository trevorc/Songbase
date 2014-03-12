package se.bitba.songbase.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class InfoHelperFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String CONTENT_URI = "CONTENT_URI";
    private static final String PROJECTION = "PROJECTION";
    private static final String SELECTION = "SELECTION";
    private static final String SELECTION_ARGS = "SELECTION_ARGS";
    private Callbacks observer;

    public InfoHelperFragment(Uri contentUri, String[] projection,
                              String selection, String[] selectionArgs) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(CONTENT_URI, contentUri);
        arguments.putStringArray(PROJECTION, projection);
        arguments.putString(SELECTION, selection);
        arguments.putStringArray(SELECTION_ARGS, selectionArgs);
        setArguments(arguments);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) throw new ClassCastException(
                String.format("activity must implement %s", Callbacks.class.getSimpleName()));
        observer = (Callbacks)activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), (Uri)args.getParcelable(CONTENT_URI),
                                args.getStringArray(PROJECTION), args.getString(SELECTION),
                                args.getStringArray(SELECTION_ARGS), null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (!cursor.moveToFirst()) return;
        try {
            observer.onInfoAvailable(this, cursor);
        } finally {
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public interface Callbacks
    {
        void onInfoAvailable(InfoHelperFragment fragment, Cursor cursor);
    }
}
