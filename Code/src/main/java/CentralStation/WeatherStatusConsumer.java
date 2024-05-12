package CentralStation;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class WeatherStatusConsumer {
    private static final String TOPIC_NAME = "weather_topic";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final Bitcask bitcask = new Bitcask("/home/toka/Documents/weather_station_data");
    private static final ParquetStatusWriter parquetWriter;
    static {
        parquetWriter = new ParquetStatusWriter();
    }


    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer_group");
        System.out.println("startingggggg");
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TOPIC_NAME));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                System.out.println("number of recordsssssssssss: "+ records.count());
                for (ConsumerRecord<String, String> record : records) {
                    processWeatherStatus(record.value());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processWeatherStatus(String message) throws JsonMappingException, JsonProcessingException {
        Station stationStatus = extracStationDetails(message);
         long stationID = stationStatus.getStationId();
         bitcask.put("station_" + stationID, message);
         //store in parquet files here
         try {
            parquetWriter.archiveWeatherStatus(stationStatus);
            System.out.println("outttttttttttttttttttttttttttttttt");
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    private static Station extracStationDetails(String jsonMessage) throws JsonMappingException, JsonProcessingException{
       
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonMessage);

            long stationId = jsonNode.get("station_id").asLong();
            long sNo = jsonNode.get("s_no").asLong();
            String batteryStatus = jsonNode.get("battery_status").asText();
            long statusTimestamp = jsonNode.get("status_timestamp").asLong();

            JsonNode weatherNode = jsonNode.get("weather");
            int humidity = weatherNode.get("humidity").asInt();
            int temperature = weatherNode.get("temperature").asInt();
            int windSpeed = weatherNode.get("wind_speed").asInt();

            WeatherDetails weather = new WeatherDetails(humidity, temperature, windSpeed);
           
        return new Station(stationId, sNo, batteryStatus, statusTimestamp, weather);
    }

   
}
