package se.bitba.songbase.fetch.content;

import android.content.ContentValues;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class FeaturedArtist
{
    private final String name;

    private FeaturedArtist(String name) {
        this.name = name;
    }

    private FeaturedArtist(JSONObject object)
            throws JSONException {
        this(object.getString("name"));
    }

    public static List<FeaturedArtist> parseMany(JSONArray featuredArtists)
            throws JSONException {
        List<FeaturedArtist> artists = new ArrayList<>();
        for (int i = 0; i < featuredArtists.length(); ++i) {
            artists.add(new FeaturedArtist(featuredArtists.getJSONObject(i)));
        }
        return artists;
    }

    public ContentValues toContent() {
        return null;
    }
}
