/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase;

public final class SongbaseConstants {

    /* API URLs */
    public static final String API_BASE_URL = "http://dev3.songza.com/api/1";
    public static final String ACTIVITIES_URL = API_BASE_URL  + "/gallery/tag/activities";
    public static final String STATIONS_URL = API_BASE_URL + "/station/multi";

    /* Intent keys */
    public static final String ACTIVITY_ID = "se.bitba.songbase.ACTIVITY_ID";
    public static final String ACTIVITY_NAME = "se.bitba.songbase.ACTIVITY_NAME";
    public static final String ACTIVITY_STATIONS = "se.bitba.songbase.ACTIVITY_STATIONS";
    public static final String STATION_ID = "se.bitba.songbase.STATION";
}
