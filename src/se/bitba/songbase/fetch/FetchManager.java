package se.bitba.songbase.fetch;

import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import se.bitba.songbase.SongbaseConstants;
import se.bitba.songbase.provider.SongbaseContract;
import se.bitba.songbase.fetch.content.Activity;
import se.bitba.songbase.fetch.content.Station;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class FetchManager
{
    private static final String TAG = FetchManager.class.getSimpleName();
    private final AsyncHttpClient httpClient = new AsyncHttpClient();

    private static void addActivities(JSONArray response, ContentValues[] activities,
                                      List<ContentValues> stations)
            throws JSONException {
        for (int i = 0; i < response.length(); ++i) {
            final Activity activity = new Activity(response.getJSONObject(i));
            activities[i] = activity.toContent();
            for (long stationId : activity.getStationIds()) {
                final ContentValues station = new ContentValues();
                station.put(SongbaseContract.StationColumns.ACTIVITY_ID, activity.getActivityId());
                station.put(SongbaseContract.StationColumns.STATION_ID, stationId);
                stations.add(station);
            }
        }
    }

    private static final Uri STATION_BARE_URI = SongbaseContract.Station.CONTENT_URI.buildUpon()
            .appendQueryParameter(SongbaseContract.Station.QUERY_PARAMETER_BARE, "true")
            .build();

    public void fetchActivities(final Context context) {
        Log.d(TAG, String.format("fetching %s", SongbaseConstants.ACTIVITIES_URL));
        httpClient.get(SongbaseConstants.ACTIVITIES_URL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray response) {
                Log.d(TAG, String.format("got JSONArray[%d]", response.length()));
                final ContentValues[] activities = new ContentValues[response.length()];
                final List<ContentValues> stations = new ArrayList<>();

                try {
                    addActivities(response, activities, stations);
                } catch (JSONException e) {
                    Log.e(TAG, "fetchActivities failed", e);
                    this.onFailure(e, response);
                    return;
                }

                final ContentValues[] stationsArray = stations.toArray(
                        new ContentValues[stations.size()]);
                int numActivities = context.getContentResolver().bulkInsert(
                        SongbaseContract.Activity.CONTENT_URI, activities);
                int numStations = context.getContentResolver().bulkInsert(
                        STATION_BARE_URI, stationsArray);
                Log.d(TAG, String.format("inserted %d activities and %d stations",
                                         numActivities, numStations));
            }
        });
    }

    private interface StationIdQuery {
        String[] PROJECTION = {SongbaseContract.StationColumns.STATION_ID};
        String SELECTION = SongbaseContract.StationColumns.ACTIVITY_ID + "=?";
        int STATION_ID = 0;
    }

    private static List<Long> getStationIds(final Context context, final String activityId) {
        Log.d(TAG, String.format("getStationIds(%s)", activityId));
        Cursor cursor = context.getContentResolver().query(
                SongbaseContract.Station.CONTENT_URI,
                StationIdQuery.PROJECTION,
                StationIdQuery.SELECTION,
                new String[] {activityId},
                null);
        assert cursor != null;
        List<Long> stationIds = new LinkedList<>();
        try {
            while (cursor.moveToNext()) stationIds.add(cursor.getLong(StationIdQuery.STATION_ID));
            Log.d(TAG, String.format("found %s stations", cursor.getCount()));
        } finally {
            cursor.close();
        }
        return stationIds;
    }

    public void fetchStations(final Context context, final String activityId) {
        final RequestParams params = new RequestParams();
        for (long id : getStationIds(context, activityId)) params.add("id", Long.toString(id));

        Log.d(TAG, String.format("fetching %s with params %s", SongbaseConstants.STATIONS_URL, params));
        httpClient.get(SongbaseConstants.STATIONS_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray response) {
                Log.d(TAG, String.format("got JSONArray[%d]", response.length()));
                final ContentValues[] stations = new ContentValues[response.length()];
                try {
                    for (int i = 0; i < response.length(); ++i) {
                        stations[i] = new Station(response.getJSONObject(i)).toContent(activityId);
                    }
                    int count = context.getContentResolver().bulkInsert(
                            SongbaseContract.Station.CONTENT_URI, stations);
                    Log.d(TAG, String.format("bulkInsert() created %d rows", count));
                } catch (JSONException e) {
                    Log.e(TAG, "fetchStations failed", e);
                    this.onFailure(e, response);
                }
            }
        });
    }
}
