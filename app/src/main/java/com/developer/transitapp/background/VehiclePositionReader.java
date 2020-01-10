package com.developer.transitapp.background;

import android.os.AsyncTask;
import com.google.transit.realtime.GtfsRealtime;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class VehiclePositionReader extends AsyncTask<Void,Void, List<GtfsRealtime.FeedEntity>> {

    @Override
    protected List<GtfsRealtime.FeedEntity> doInBackground(Void... voids) {

        URL url = null;
        try {
            url = new URL("http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        List<GtfsRealtime.FeedEntity>  feed = null;
        try {
            feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream()).getEntityList();
            return feed;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<GtfsRealtime.FeedEntity> feedEntities) {
        super.onPostExecute(feedEntities);
    }
}

