/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import static se.bitba.songbase.provider.SongbaseContract.*;

public class SongbaseDatabase
            extends SQLiteOpenHelper
{
    private static final String TAG = SongbaseDatabase.class.getSimpleName();
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "songbase.sqlite";

    interface Tables {
        String ACTIVITIES = "activities";
        String STATIONS = "stations";
        String ACTIVITIES_JOIN_STATIONS = ACTIVITIES + " " +
                "LEFT JOIN " + STATIONS + " " +
                "ON " + ACTIVITIES + "." + ActivityColumns.ACTIVITY_ID + "=" +
                STATIONS + "." + StationColumns.ACTIVITY_ID;
        String FAVORITES = "favorites";
        String FEATURED_ARTISTS = "featured_artists";
    }

    interface Subqueries {
        String STATION_COUNT = "(SELECT " +
                "COUNT(station_count." + BaseColumns._ID + ") " +
                "FROM " + Tables.STATIONS + " station_count " +
                "WHERE station_count." + StationColumns.ACTIVITY_ID + "=" +
                Tables.ACTIVITIES + "." + ActivityColumns.ACTIVITY_ID + ")";
        String IS_FAVORITE = "EXISTS (SELECT 1 " +
                "FROM " + Tables.FAVORITES + " station_favorite " +
                "WHERE station_favorite." + FavoriteColumns.STATION_ID + "=" +
                Tables.STATIONS + "." + StationColumns.STATION_ID + ")";
    }

    public SongbaseDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.ACTIVITIES + " (" +
                           BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           ActivityColumns.ACTIVITY_ID + " TEXT NOT NULL, " +
                           ActivityColumns.NAME + " TEXT NOT NULL, " +
                           "UNIQUE (" + ActivityColumns.ACTIVITY_ID + ") ON CONFLICT REPLACE)");
        db.execSQL("CREATE TABLE " + Tables.STATIONS + " (" +
                           BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           StationColumns.STATION_ID + " INTEGER NOT NULL," +
                           StationColumns.ACTIVITY_ID + " TEXT, " +
                           StationColumns.NAME + " TEXT, " +
                           StationColumns.COVER_URL + " TEXT, " +
                           StationColumns.DESCRIPTION + " TEXT, " +
                           "UNIQUE (" + StationColumns.STATION_ID + ") ON CONFLICT REPLACE)");
        db.execSQL("CREATE TABLE " + Tables.FAVORITES + " (" +
                           BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           FavoriteColumns.STATION_ID + " INTEGER NOT NULL, " +
                           "UNIQUE (" + FavoriteColumns.STATION_ID + ") ON CONFLICT REPLACE)");
        db.execSQL("CREATE TABLE " + Tables.FEATURED_ARTISTS + " (" +
                           BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           FeaturedArtistColumns.STATION_ID + " INTEGER NOT NULL, " +
                           FeaturedArtistColumns.NAME + " TEXT NOT NULL, " +
                           "UNIQUE (" + FeaturedArtistColumns.STATION_ID + ", " +
                           FeaturedArtistColumns.NAME + ") ON CONFLICT REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // first version
    }
}
