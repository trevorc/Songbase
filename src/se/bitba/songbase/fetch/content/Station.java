package se.bitba.songbase.fetch.content;

import android.content.ContentValues;
import org.json.JSONException;
import org.json.JSONObject;
import se.bitba.songbase.provider.SongbaseContract;

import java.util.List;

public final class Station
{
    private final long stationId;
    private final String name;
    private final String coverUrl;
    private final String description;
    private final List<FeaturedArtist> featuredArtists;

    public Station(JSONObject object)
            throws JSONException
    {
        stationId = object.getLong("id");
        name = object.getString("name");
        coverUrl = object.getString("cover_url");
        description = object.getString("description");
        featuredArtists = FeaturedArtist.parseMany(object.getJSONArray("featured_artists"));
    }

    public List<FeaturedArtist> getFeaturedArtists() {
        return featuredArtists;
    }

    public ContentValues toContent(String activityId) {
        final ContentValues values = new ContentValues();
        values.put(SongbaseContract.Station.STATION_ID, stationId);
        values.put(SongbaseContract.Station.ACTIVITY_ID, activityId);
        values.put(SongbaseContract.Station.NAME, name);
        values.put(SongbaseContract.Station.COVER_URL, coverUrl);
        values.put(SongbaseContract.Station.DESCRIPTION, description);
        return values;
    }
}
