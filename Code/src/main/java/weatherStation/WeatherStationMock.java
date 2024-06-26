package weatherStation;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static weatherStation.KafkaMsgProducer.rainingTriggers;
import static weatherStation.KafkaMsgProducer.sendMsg;
import org.codehaus.jettison.json.JSONObject;

public class WeatherStationMock {
    private static final Random random = new Random();
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    static OpenMeteo openMeteo = new OpenMeteo();
    ChannelAdapter channelAdapter = new ChannelAdapter();


    public static void main(String[] args) {
        String station_id = System.getenv("STATION_ID");
        executor.scheduleWithFixedDelay(new WeatherStation(station_id), 0, 1, TimeUnit.SECONDS);
    }

    static class WeatherStation implements Runnable {
        private final int stationId;
        private long sNo = 1;
        private String batteryStatus = "medium"; // Initial battery status

        public WeatherStation(String stationId) {
            this.stationId = Integer.parseInt(stationId);
        }

        @Override
        public void run() {
            try {
                String message = generateWeatherStatus();
                if (message != null) {
                    // Kafka Producer must be set here
                    String stationTopic = "weather_topic";
                    sendMsg(stationTopic, message);
                    System.out.println("=============== Message is Sent ====================");
                    rainingTriggers(stationTopic);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String generateWeatherStatus() {
            try {
                // Generate weather status message
                long statusTimestamp = System.currentTimeMillis() / 1000; // Unix timestamp
               JSONObject weatherData = openMeteo.getData(ChannelAdapter.timeStampToDate(statusTimestamp), stationId);
               WeatherDetails weatherDetails = ChannelAdapter.adapt(weatherData);
               int humidity = weatherDetails.getHumidity();
               int temperature = weatherDetails.getTemperature();
               int windSpeed = weatherDetails.getWindSpeed();

                // int humidity = random.nextInt(101); // Random humidity
                // int temperature = random.nextInt(141) - 20; // Random temperature (-20 to 120 Fahrenheit)
                // int windSpeed = random.nextInt(51); // Random wind speed (0 to 50 km/h)

                // Randomly change battery status
                double rand = random.nextDouble();
                if (rand < 0.3) {
                    batteryStatus = "low";
                } else if (rand < 0.7) {
                    batteryStatus = "medium";
                } else {
                    batteryStatus = "high";
                }

                // Randomly drop messages
                if (random.nextDouble() < 0.1) {
                    return null;
                }

                // Construct JSON message
                String json = String.format(
                        "{\"station_id\": %d, \"s_no\": %d, \"battery_status\": \"%s\", \"status_timestamp\": %d, \"weather\": {\"humidity\": %d, \"temperature\": %d, \"wind_speed\": %d}}",
                        stationId, sNo++, batteryStatus, statusTimestamp, humidity, temperature, windSpeed);

                return json;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
