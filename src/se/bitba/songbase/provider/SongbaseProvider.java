/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

import static se.bitba.songbase.provider.SongbaseContract.*;
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
    private static final int FEATURED_ARTISTS = 40;
    private static final int FEATURED_ARTISTS_ITEM = 41;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = SongbaseContract.AUTHORITY;

        matcher.addURI(authority, "activities", ACTIVITIES);
        matcher.addURI(authority, "activities/*", ACTIVITIES_ITEM);
        matcher.addURI(authority, "stations", STATIONS);
        matcher.addURI(authority, "stations/*", STATIONS_ITEM);
        matcher.addURI(authority, "favorites", FAVORITES);
        matcher.addURI(authority, "favorites/*", FAVORITES_ITEM);
        matcher.addURI(authority, "featured_artists", FEATURED_ARTISTS);
        matcher.addURI(authority, "featured_artists/*", FEATURED_ARTISTS_ITEM);

        return matcher;
    }

    private SongbaseDatabase mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new SongbaseDatabase(getContext());
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
            case FEATURED_ARTISTS:
                return SongbaseContract.FeaturedArtist.CONTENT_TYPE;
            case FEATURED_ARTISTS_ITEM:
                return SongbaseContract.FeaturedArtist.CONTENT_ITEM_TYPE;
        }
        throw new IllegalArgumentException(String.format("unsupported URI: %s", uri));
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
        assert CharMatcher.is('?').countIn(selection) == selectionArgs.length;

        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        assert db != null;

        switch (URI_MATCHER.match(uri)) {
            case ACTIVITIES: {
                builder.setTables(Tables.ACTIVITIES);
                builder.setProjectionMap(ACTIVITY_PROJECTION_MAP);
                return builder.query(db, projection, selection, selectionArgs, null, null, null);
            }
            case STATIONS: {
                builder.setTables(Tables.STATIONS);
                builder.setProjectionMap(STATION_PROJECTION_MAP);
                final String order = sortOrder == null ? SongbaseContract.Station.DEFAULT_SORT : sortOrder;
                return builder.query(db, projection, selection, selectionArgs, null, null, order);
            }
            case FEATURED_ARTISTS: {
                builder.setTables(Tables.FEATURED_ARTISTS);
                final String order = sortOrder == null ? SongbaseContract.FeaturedArtist.DEFAULT_SORT : sortOrder;
                return builder.query(db, projection, selection, selectionArgs, null, null, order);
            }
        }
        throw new IllegalArgumentException(String.format("unsupported URI: %s", uri));
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) { // TODO: boolean isBatch -> disable notifications
        final ContentResolver contentResolver = getContext().getContentResolver();
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
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
                final Long stationId = values.getAsLong(StationColumns.STATION_ID);
                assert stationId != null;
                return SongbaseContract.Station.buildStationUri(stationId);
            }
            case FAVORITES: {
                final Long stationId = values.getAsLong(FavoriteColumns.STATION_ID);
                if (stationId == null) throw new IllegalArgumentException("STATION_ID is missing");
                db.insertOrThrow(Tables.FAVORITES, null, values);
                contentResolver.notifyChange(SongbaseContract.Station.buildStationUri(stationId), null);
                return SongbaseContract.Favorite.buildFavoriteUri(stationId);
            }
            case FEATURED_ARTISTS: {
                final long featuredArtistId = db.insertOrThrow(Tables.FEATURED_ARTISTS, null, values);
                contentResolver.notifyChange(uri, null);
                return SongbaseContract.FeaturedArtist.buildFeaturedArtistsUri(featuredArtistId);
            }
        }
        throw new IllegalArgumentException(String.format("unsupported URI: %s", uri));
    }

    @Override
    public int bulkInsert(Uri uri, @NotNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
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
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        assert contentResolver != null && db != null;

        switch (URI_MATCHER.match(uri)) {
            case FAVORITES: {
                int deleted = db.delete(Tables.FAVORITES, selection, selectionArgs);
                final long stationId = Long.parseLong(selectionArgs[0]);
                final Uri stationUri = SongbaseContract.Station.buildStationUri(stationId);
                contentResolver.notifyChange(stationUri, null);
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
