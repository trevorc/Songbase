package se.bitba.songbase.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

import static se.bitba.songbase.provider.SongbaseContract.ActivityColumns;
import static se.bitba.songbase.provider.SongbaseContract.StationColumns;
import static se.bitba.songbase.provider.SongbaseDatabase.Tables;

public class SongbaseProvider
        extends ContentProvider
{
    private static final String TAG = SongbaseProvider.class.getSimpleName();
    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private static final int ACTIVITIES = 10;
    private static final int ACTIVITIES_ITEM = 11;
    private static final int STATIONS = 20;
    private static final int STATIONS_ITEM = 22;
    private static final int FAVORITES = 30;
    private static final int FAVORITES_ITEM = 31;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SongbaseContract.AUTHORITY;

        matcher.addURI(authority, "activities", ACTIVITIES);
        matcher.addURI(authority, "activities/*", ACTIVITIES_ITEM);
        matcher.addURI(authority, "stations", STATIONS);
        matcher.addURI(authority, "stations/*", STATIONS_ITEM);
        matcher.addURI(authority, "favorites", FAVORITES);
        matcher.addURI(authority, "favorites/*", FAVORITES_ITEM);

        return matcher;
    }

    private SongbaseDatabase openHelper;

    @Override
    public boolean onCreate() {
        openHelper = new SongbaseDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ACTIVITIES:
                return SongbaseContract.Activity.CONTENT_TYPE;
            case ACTIVITIES_ITEM:
                return SongbaseContract.Activity.CONTENT_ITEM_TYPE;
            case STATIONS:
                return SongbaseContract.Station.CONTENT_TYPE;
            case STATIONS_ITEM:
                return SongbaseContract.Station.CONTENT_ITEM_TYPE;
            case FAVORITES:
                return SongbaseContract.Favorite.CONTENT_TYPE;
            case FAVORITES_ITEM:
                return SongbaseContract.Favorite.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(String.format("unsupported URI: %s", uri));
        }
    }

    private static final Map<String, String> ACTIVITY_PROJECTION_MAP
            = ImmutableMap.<String, String>builder()
            .put(BaseColumns._ID, BaseColumns._ID)
            .put(BaseColumns._COUNT, "COUNT(*)")
            .put(ActivityColumns.ACTIVITY_ID, ActivityColumns.ACTIVITY_ID)
            .put(ActivityColumns.NAME, ActivityColumns.NAME)
            .put(ActivityColumns.STATION_COUNT, SongbaseDatabase.Subqueries.STATION_COUNT)
            .build();

    private static final Map<String, String> STATION_PROJECTION_MAP
            = ImmutableMap.<String, String>builder()
            .put(BaseColumns._ID, BaseColumns._ID)
            .put(BaseColumns._COUNT, "COUNT(*)")
            .put(StationColumns.STATION_ID, StationColumns.STATION_ID)
            .put(StationColumns.ACTIVITY_ID, StationColumns.ACTIVITY_ID)
            .put(StationColumns.NAME, StationColumns.NAME)
            .put(StationColumns.COVER_URL, StationColumns.COVER_URL)
            .put(StationColumns.DESCRIPTION, StationColumns.DESCRIPTION)
            .put(StationColumns.FAVORITE, SongbaseDatabase.Subqueries.IS_FAVORITE)
            .build();

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, String.format("query(%s, %s)", uri, Arrays.toString(projection)));

        SQLiteDatabase db = openHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        assert db != null;

        switch (URI_MATCHER.match(uri)) {
            case ACTIVITIES:
                builder.setTables(Tables.ACTIVITIES);
                builder.setProjectionMap(ACTIVITY_PROJECTION_MAP);
                return builder.query(db, projection, selection, selectionArgs, null, null, null);
            case STATIONS:
                builder.setTables(Tables.STATIONS);
                builder.setProjectionMap(STATION_PROJECTION_MAP);
                final String order = sortOrder == null ? SongbaseContract.Station.DEFAULT_SORT : sortOrder;
                return builder.query(db, projection, selection, selectionArgs, null, null, order);
            default:
                throw new IllegalArgumentException(String.format("unsupported URI: %s", uri));
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final ContentResolver contentResolver = getContext().getContentResolver();
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        assert contentResolver != null && db != null;

        switch (URI_MATCHER.match(uri)) {
            case ACTIVITIES: {
                db.insertOrThrow(Tables.ACTIVITIES, null, values);
                contentResolver.notifyChange(uri, null);
                return SongbaseContract.Activity.buildActivityUri(values.getAsString(ActivityColumns.ACTIVITY_ID));
            }
            case STATIONS: {
                final boolean isBare = uri.getBooleanQueryParameter(
                        SongbaseContract.Station.QUERY_PARAMETER_BARE, false);
                if (!isBare && !(values.containsKey(StationColumns.NAME) &&
                        values.containsKey(StationColumns.COVER_URL) &&
                        values.containsKey(StationColumns.DESCRIPTION))) {
                    throw new IllegalArgumentException("NAME, COVER_URL or DESCRIPTION missing");
                }
                db.insertWithOnConflict(Tables.STATIONS, null, values, isBare ?
                        SQLiteDatabase.CONFLICT_IGNORE :
                        SQLiteDatabase.CONFLICT_REPLACE);
                if (!isBare) contentResolver.notifyChange(uri, null);
                return SongbaseContract.Station.buildStationUri(values.getAsString(StationColumns.STATION_ID));
            }
            case FAVORITES: {
                db.insertOrThrow(Tables.FAVORITES, null, values);
                contentResolver.notifyChange(uri, null);
                return SongbaseContract.Favorite.buildFavoriteUri(
                        values.getAsString(SongbaseContract.FavoriteColumns.STATION_ID));
            }
        }
        throw new IllegalArgumentException(String.format("unsupported URI: %s", uri));
    }

    @Override
    public int bulkInsert(Uri uri, @NotNull ContentValues[] values) {
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        final int numValues = values.length;
        assert db != null;

        db.beginTransaction();
        try {
            super.bulkInsert(uri, values);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return numValues;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, String.format("delete(%s, %s, %s)", uri, selection, Arrays.toString(selectionArgs)));

        final ContentResolver contentResolver = getContext().getContentResolver();
        final SQLiteDatabase db = openHelper.getWritableDatabase();
        assert contentResolver != null && db != null;

        switch (URI_MATCHER.match(uri)) {
            case FAVORITES: {
                int deleted = db.delete(Tables.FAVORITES, selection, selectionArgs);
                contentResolver.notifyChange(uri, null);
                return deleted;
            }
        }
        throw new IllegalArgumentException(String.format("unsupported URI: %s", uri));
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("delete not supported");
    }
}
