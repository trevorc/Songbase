/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase.provider;

import android.app.Activity;
import android.net.Uri;
import android.provider.BaseColumns;

public final class SongbaseContract
{
    public static final String AUTHORITY = "se.bitba.songbase";
    public static final Uri URI_BASE = Uri.parse("content://" + AUTHORITY);
    public static final String CONTENT_TYPE_FORMAT = "vnd.android.cursor.dir/vnd.songbase.%s";
    public static final String CONTENT_ITEM_TYPE_FORMAT = "vnd.android.cursor.item/vnd.songbase.%s";

    private SongbaseContract() {}

    public interface ActivityColumns
            extends BaseColumns
    {
        String ACTIVITY_ID = "activity_id";
        String NAME = "name";
        String STATION_COUNT = "station_count";
    }

    public static final class Activity
            implements ActivityColumns
    {
        private Activity() {}

        public static final String PATH = "activities";

        public static final Uri CONTENT_URI = URI_BASE.buildUpon().appendPath(PATH).build();
        public static final String CONTENT_TYPE = String.format(CONTENT_TYPE_FORMAT, "activity");
        public static final String CONTENT_ITEM_TYPE = String.format(CONTENT_ITEM_TYPE_FORMAT, "activity");


        public static Uri buildActivityUri(String activityId) {
            return CONTENT_URI.buildUpon().appendPath(activityId).build();
        }
    }

    public interface StationColumns
            extends BaseColumns
    {
        String STATION_ID = "station_id";
        String ACTIVITY_ID = "activity_id";
        String NAME = "name";
        String COVER_URL = "cover_url";
        String DESCRIPTION = "description";
        String FAVORITE = "is_favorite";
    }

    public static final class Station
            implements StationColumns
    {
        private Station() {}

        public static final String PATH = "stations";

        public static final Uri CONTENT_URI = URI_BASE.buildUpon().appendPath(PATH).build();
        public static final String CONTENT_TYPE = String.format(CONTENT_TYPE_FORMAT, "station");
        public static final String CONTENT_ITEM_TYPE = String.format(CONTENT_ITEM_TYPE_FORMAT, "station");
        public static final String DEFAULT_SORT = "name ASC";

        public static final String QUERY_PARAMETER_BARE = "bare";

        public static Uri buildStationUri(String stationId) {
            return CONTENT_URI.buildUpon().appendPath(stationId).build();
        }
    }

    public interface FavoriteColumns
            extends BaseColumns
    {
        String STATION_ID = "station_id";
    }

    public static final class Favorite
            implements FavoriteColumns
    {
        private Favorite() {}

        public static final String PATH = "favorites";
        public static final Uri CONTENT_URI = URI_BASE.buildUpon().appendPath(PATH).build();
        public static final String CONTENT_TYPE = String.format(CONTENT_TYPE_FORMAT, "favorite");
        public static final String CONTENT_ITEM_TYPE = String.format(CONTENT_ITEM_TYPE_FORMAT, "favorite");


        public static Uri buildFavoriteUri(String stationId) {
            return CONTENT_URI.buildUpon().appendPath(stationId).build();
        }
    }
}
