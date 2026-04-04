package edu.uiowa.team7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GeoCodeService {

    public static double[] geocode(String address) throws Exception{
        String encoded = URLEncoder.encode(address,"UTF-8");

        String urlStr = "https://nominatim.openstreetmap.org/search" + "?q=" + encoded + "&format=json&limit=1";

        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestProperty("User-Agent", "Team7-ER-Finder (student project)");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while((line = reader.readLine()) != null){
            response.append(line);
        }
        reader.close();

        JsonArray arr = JsonParser.parseString(response.toString()).getAsJsonArray();

        if (arr.isEmpty()){
            throw new Exception("Address Could Not Be GeoCoded");
        }

        JsonObject obj = arr.get(0).getAsJsonObject();

        double lat = obj.get("lat").getAsDouble();
        double lon = obj.get("lon").getAsDouble();

        return new double[]{lat, lon};
    }
}
