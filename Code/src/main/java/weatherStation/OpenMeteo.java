package weatherStation;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


public class OpenMeteo {
    String[] fields = new String[]{"relativehumidity_2m", "temperature_2m", "windspeed_10m"};
    private int getHour(Date date){
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        String hour = hourFormat.format(date);
        return Integer.parseInt(hour);
    }

    private String getDateFromTimeStamp(Date date){
        // Extract date (yyyy-mm-dd) from the timestamp
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(date);
    }
    @SuppressWarnings("deprecation")
    public JSONObject getData(Date date, long stationId) {

        try{
            String formattedDate = getDateFromTimeStamp(date);
            int hour = getHour(date);
            String apiUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + (stationId+2)  +"&longitude=13.41" +
                "&start_date=" + formattedDate + "&end_date=" + formattedDate +
                "&hourly=temperature_2m,relativehumidity_2m,windspeed_10m";

            System.out.println(apiUrl);

        // Make API request and read response
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(url.openStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            JSONObject currentWeather = new JSONObject(response.toString()).getJSONObject("hourly");

            JSONObject weatherData = new JSONObject();
            for(String field : fields){
                weatherData.put(field , currentWeather.getJSONArray(field).get(hour));
            }

            return weatherData;
        } else {
            System.out.println("Error: " + responseCode);
        }
    } catch (IOException | JSONException e) {
        e.printStackTrace();
    }
        return null;
    }
}
