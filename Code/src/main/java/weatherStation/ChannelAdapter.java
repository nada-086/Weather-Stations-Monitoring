package weatherStation;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Date;
import CentralStation.*;

public class ChannelAdapter {

     public static Date timeStampToDate(long timestamp){
        return new Date(timestamp * 1000L);
    }

    public static WeatherDetails adapt(JSONObject response) throws JSONException {
        int humidity = response.getInt("relativehumidity_2m");
       int temp = (int) Math.round(response.getDouble("temperature_2m"));
        int wind = (int) Math.round(response.getDouble("windspeed_10m"));
        WeatherDetails weatherData = new WeatherDetails(humidity, temp, wind);
        return weatherData;
    }
    
}
