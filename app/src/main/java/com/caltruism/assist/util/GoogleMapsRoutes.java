package com.caltruism.assist.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.caltruism.assist.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GoogleMapsRoutes {

    private final static String TAG = "GoogleMapsRoutes";

    private Context context;
    private CustomCallbackListener.RouteParserCallbackListener callbackListener;

    public GoogleMapsRoutes(Context context, LatLng origin, LatLng dest, CustomCallbackListener.RouteParserCallbackListener callbackListener){
        this.context = context;
        this.callbackListener = callbackListener;

        String url = String.format("https://maps.googleapis.com/maps/api/directions/json?origin=%s,%s&destination=%s,%s&mode=walking&key=AIzaSyA02-vDGwNxALg9kzyrY6f_kVk3j4NAzIo",
                origin.latitude, origin.longitude, dest.latitude, dest.longitude);
        Log.e(TAG, "URL " + url);
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url);

    }

    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urlString) {

            String data = "";
            HttpURLConnection urlConnection = null;

            URL url;

            try {
                url = new URL(urlString[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
            } catch (Exception e) {
                Log.e(TAG, "downloadURL exception", e);
            }

            try (InputStream iStream = urlConnection.getInputStream()) {
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                br.close();
            } catch (Exception e) {
                Log.e(TAG, "downloadURL exception", e);
            } finally {
                assert urlConnection != null;
                urlConnection.disconnect();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> route = null;
            ArrayList<Object> latLngBoundAndDuration;

            try {
                JSONObject jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                route = parser.parseRoute(jObject);

                latLngBoundAndDuration = parser.parseLatLangBoundAndDuration();
                if (latLngBoundAndDuration != null && latLngBoundAndDuration.size() == 3)
                    callbackListener.onLatLngAndDurationCompleted(latLngBoundAndDuration);
            } catch (Exception e) {
                Log.e(TAG, "ParserTask exception", e);
            }
            return route;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            ArrayList<LatLng> points = new ArrayList<>();
            PolylineOptions polylineOptions = new PolylineOptions();;

            for (int i = 0; i < result.size(); i++) {
                HashMap<String, String> point = result.get(i);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            polylineOptions.addAll(points);
            polylineOptions.width(10);
            polylineOptions.color(ContextCompat.getColor(context, R.color.colorAccent));

            callbackListener.onTaskCompleted(polylineOptions);
        }
    }
}
