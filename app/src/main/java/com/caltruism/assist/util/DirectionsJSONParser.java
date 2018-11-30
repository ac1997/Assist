package com.caltruism.assist.util;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.caltruism.assist.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonObject;

public class DirectionsJSONParser {

    private static final String TAG = "DirectionsJSONParser";
    private JSONArray jRoutes;

    public List<HashMap<String,String>> parseRoute(JSONObject jObject){

        List<HashMap<String, String>> route = new ArrayList<HashMap<String,String>>() ;

        try {
            jRoutes = jObject.getJSONArray("routes");

            if (jRoutes.length() > 0) {
                JSONArray jLegs = ( (JSONObject)jRoutes.get(0)).getJSONArray("legs");

                for (int i = 0; i < jLegs.length(); i++) {
                    JSONArray jSteps = ((JSONObject) jLegs.get(i)).getJSONArray("steps");

                    for (int j = 0; j < jSteps.length(); j++) {
                        String polyline = (String) ((JSONObject)((JSONObject)jSteps.get(j)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        for (int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString(list.get(l).latitude) );
                            hm.put("lng", Double.toString(list.get(l).longitude) );
                            route.add(hm);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return route;
    }


    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    public ArrayList<Object> parseLatLangBoundAndDuration() {
        ArrayList<Object> data = new ArrayList<>();
        try {
            if (jRoutes.length() > 0) {
                JSONObject jBounds = ((JSONObject) jRoutes.get(0)).getJSONObject("bounds");
                JSONObject jLatLng = jBounds.getJSONObject("northeast");
                data.add(new LatLng(jLatLng.getDouble("lat"), jLatLng.getDouble("lng")));
                jLatLng = jBounds.getJSONObject("southwest");
                data.add(new LatLng(jLatLng.getDouble("lat"), jLatLng.getDouble("lng")));

                JSONArray jLegs = ((JSONObject) jRoutes.get(0)).getJSONArray("legs");
                if (jLegs.length() > 0) {
                    data.add(((JSONObject) jLegs.get(0)).getJSONObject("duration").getInt("value") / 60);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
