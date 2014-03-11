package se.bitba.songbase.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import se.bitba.songbase.provider.SongbaseContract;

class ActivityAdapter
        extends CursorAdapter
{
    private final Activity activity;

    public ActivityAdapter(Activity activity, Cursor cursor) {
        super(activity, cursor, 0);
        this.activity = activity;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return activity.getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView)view).setText(cursor.getString(ActivitiesQuery.NAME));
    }

    public interface ActivitiesQuery
    {
        String[] PROJECTION = {
                SongbaseContract.Activity._ID,
                SongbaseContract.Activity.ACTIVITY_ID,
                SongbaseContract.Activity.NAME
        };

        int _ID = 0;
        int ACTIVITY_ID = 1;
        int NAME = 2;
    }
}
