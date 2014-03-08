/*
 * Copyright (C) 2014 Bitbase LLC
 *
 * All rights reserved.
 */

package se.bitba.songbase;

import se.bitba.songbase.util.URLUtil;

import java.net.URL;

public final class SongbaseConstants {

    /* API URLs */
    public static final URL ACTIVITIES_URL = URLUtil.makeURL("http://dev3.songza.com/api/1/gallery/tag/activities");
    public static final URL STATIONS_URL = URLUtil.makeURL("http://dev3.songza.com/api/1/station/multi");

    /* Intent keys */
    public static final String ACTIVITY = "se.bitba.songbase.ACTIVITY";
    public static final String STATION = "se.bitba.songbase.STATION";
}
